package io.github.xrickastley.originsgenshin.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.entity.DendroCoreEntity;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinSoundEvents;
import io.github.xrickastley.originsgenshin.interfaces.IPlayerEntity;
import io.github.xrickastley.originsgenshin.networking.ShowElementalDamageS2CPacket;
import io.github.xrickastley.originsgenshin.util.BoxUtil;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin
	extends LivingEntity
	implements IPlayerEntity
{
	public PlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(EntityType.PLAYER, world);
		throw new AssertionError();
	}

	@Unique
	private float originsgenshin$subdamage;

	@Unique
	private List<DamageSource> originsgenshin$critDamageSources = new ArrayList<>();

	@Unique
	@Override
	public boolean originsgenshin$isCrit(DamageSource source) {
		return this.originsgenshin$critDamageSources != null && this.originsgenshin$critDamageSources.contains(source);
	}

	@ModifyVariable(
		method = "applyDamage",
		at = @At(
			value = "INVOKE_ASSIGN",
			target = "Lnet/minecraft/entity/player/PlayerEntity;modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"
		),
		ordinal = 0,
		argsOnly = true
	)
	private float applyCrystallizeShield(float amount, @Local(argsOnly = true) DamageSource source) {
		final ElementComponent component = ElementComponent.KEY.get(this);
		final float finalAmount = amount - component.reduceCrystallizeShield(source, amount);

		if (finalAmount < amount) {
			this.getWorld()
				.playSound(null, this.getBlockPos(), OriginsGenshinSoundEvents.CRYSTALLIZE_SHIELD_HIT, SoundCategory.PLAYERS, 1.0f, 1.0f);
		}

		if (finalAmount <= 0) this.originsgenshin$setBlockedByCrystallizeShield(true);

		return finalAmount;
	}

	// why are there two seperate knockbacks :sob:
	@Definition(id = "i", local = @Local(type = int.class, ordinal = 0))
	@Expression("i > 0")
	@ModifyExpressionValue(
		method = "attack",
		at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private boolean preventKnockbackIfCrystallize(boolean original, @Local(argsOnly = true) Entity entity) {
		if (!(entity instanceof final LivingEntity livingEntity)) return original;

		final ElementComponent component = ElementComponent.KEY.get(livingEntity);

		return original && !component.reducedCrystallizeShield();
	}

	@ModifyArg(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
		),
		index = 0
	)
	private DamageSource checkForCritMain(DamageSource source, @Local(ordinal = 2) boolean crit) {
		if (originsgenshin$critDamageSources == null) originsgenshin$critDamageSources = new ArrayList<>();

		if (crit) originsgenshin$critDamageSources.add(source);

		return source;
	}

	@ModifyArg(
		method = "attack",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
		),
		index = 0
	)
	private DamageSource checkForCritSweep(DamageSource source, @Local(ordinal = 2) boolean crit) {
		if (originsgenshin$critDamageSources == null) originsgenshin$critDamageSources = new ArrayList<>();

		if (crit) originsgenshin$critDamageSources.add(source);

		return source;
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	private void removeCritDS(CallbackInfo ci) {
		if (originsgenshin$critDamageSources != null)
			originsgenshin$critDamageSources.clear();
		else
			originsgenshin$critDamageSources = new ArrayList<>();
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
