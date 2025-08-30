package io.github.xrickastley.originsgenshin.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinAttributes;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinGameRules;
import io.github.xrickastley.originsgenshin.interfaces.IPlayerEntity;
import io.github.xrickastley.originsgenshin.networking.ShowElementalDamageS2CPacket;
import io.github.xrickastley.originsgenshin.util.BoxUtil;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	public LivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final World world) {
		super(entityType, world);
		throw new AssertionError();
	}

	@Unique
	private float originsgenshin$subdamage;

	@ModifyReturnValue(
		method = "createLivingAttributes",
		at = @At("RETURN")
	)
	private static DefaultAttributeContainer.Builder addToLivingAttributes(DefaultAttributeContainer.Builder builder) {
		return OriginsGenshinAttributes.apply(builder);
	}

	@Inject(
		method = "onStatusEffectRemoved",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/effect/StatusEffect;onRemoved(Lnet/minecraft/entity/attribute/AttributeContainer;)V",
			shift = At.Shift.AFTER
		)
	)
	private void triggerEntityOnRemoved(StatusEffectInstance effect, CallbackInfo ci) {
		effect.getEffectType().onRemoved((LivingEntity)(Entity) this, effect.getAmplifier());
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void applyNaturalElements(CallbackInfo ci) {
		if (this.isWet() && this.getWorld().getGameRules().getBoolean(OriginsGenshinGameRules.HYDRO_FROM_WATER)) {
			final ElementComponent component = ElementComponent.KEY.get(this);

			component.addElementalApplication(
				Element.HYDRO,
				InternalCooldownContext.ofType(this, "origins-genshin:natural_environment", InternalCooldownType.INTERVAL_ONLY),
				1.0
			);
		} else if ((this.isOnFire() || this.getBlockStateAtPos().getBlock() == Blocks.FIRE) && this.getWorld().getGameRules().getBoolean(OriginsGenshinGameRules.PYRO_FROM_FIRE)) {
			final ElementComponent component = ElementComponent.KEY.get(this);

			component.addElementalApplication(
				Element.PYRO,
				InternalCooldownContext.ofType(this, "origins-genshin:natural_environment", InternalCooldownType.INTERVAL_ONLY),
				1.0
			);
		}
	}

	@ModifyVariable(
		method = "applyDamage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;setAbsorptionAmount(F)V",
			ordinal = 0,
			shift = At.Shift.AFTER
		),
		ordinal = 0,
		argsOnly = true
	)
	private float applyCrystallizeShield(float amount, @Local(argsOnly = true) DamageSource source) {
		final ElementComponent component = ElementComponent.KEY.get(this);

		return amount - component.reduceCrystallizeShield(source, amount);
	}

	@ModifyExpressionValue(
		method = "damage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/damage/DamageSource;isIn(Lnet/minecraft/registry/tag/TagKey;)Z",
			ordinal = 7
		)
	)
	private boolean preventKnockbackIfCrystallize(boolean original, @Local(argsOnly = true) DamageSource source, @Share("originsgenshin$hasCrystallizeShield") LocalBooleanRef hasCrystallizeShield) {
		final ElementComponent component = ElementComponent.KEY.get(this);

		return original || component.reducedCrystallizeShield();
	}

	@Inject(
		method = "applyDamage",
		at = @At("TAIL")
	)
	private void damageHandlers_elements(final DamageSource source, float amount, CallbackInfo ci) {
		this.originsgenshin$triggerDendroCoreReactions(source);

		if (!source.originsgenshin$displayDamage()) return;

		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits((LivingEntity)(Entity) this, Element.PHYSICAL, 0), InternalCooldownContext.ofNone(source.getAttacker()));

		originsgenshin$subdamage += amount;

		if (originsgenshin$subdamage < 1) return;

		final float extra = originsgenshin$subdamage - (float) Math.floor(originsgenshin$subdamage);

		originsgenshin$subdamage = (float) Math.floor(originsgenshin$subdamage);

		final World world = this.getWorld();

		if (world.isClient || !(world instanceof ServerWorld)) return;

		final Box boundingBox = this.getBoundingBox();

		final double x = this.getX() + (boundingBox.getLengthX() * 1.25 * Math.random());
		final double y = this.getY() + (boundingBox.getLengthY() * 0.50 * Math.random()) + 0.50;
		final double z = this.getZ() + (boundingBox.getLengthZ() * 1.25 * Math.random());
		final Vec3d pos = new Vec3d(x, y, z);
		final boolean isCrit = eds.getOriginalSource() != null && source.getAttacker() instanceof final PlayerEntity player
			? ((IPlayerEntity) player).originsgenshin$isCrit(eds.getOriginalSource())
			: false;

		final Element element = eds.getElementalApplication().getElement();
		final ShowElementalDamageS2CPacket showElementalDMGPacket = new ShowElementalDamageS2CPacket(pos, element, originsgenshin$subdamage, isCrit);

		originsgenshin$subdamage = extra;

		for (final ServerPlayerEntity player : PlayerLookup.tracking(this)) {
			if (player.getId() == this.getId()) return;

			ServerPlayNetworking.send(player, showElementalDMGPacket);
		}
	}

	@ModifyConstant(
		method = "damage",
		constant = @Constant(intValue = 20, ordinal = 0)
	)
	private int changeTimeUntilRegen(int original, @Local(argsOnly = true) DamageSource source) {
		return source.isIn(TagKey.of(RegistryKeys.DAMAGE_TYPE, OriginsGenshin.identifier("prevents_cooldown_trigger")))
			? 10
			: original;
	}

	@Unique
	private void originsgenshin$triggerDendroCoreReactions(final DamageSource source) {
		if (!(source instanceof final ElementalDamageSource eds)) return;

		final Element element = eds.getElementalApplication().getElement();

		if (element != Element.PYRO && element != Element.ELECTRO) return;

		this.getWorld()
			.getEntitiesByClass(DendroCoreEntity.class, BoxUtil.multiply(this.getBoundingBox(), 2), dc -> true)
			.forEach(dc -> dc.damage(source, 1));
	}
}
