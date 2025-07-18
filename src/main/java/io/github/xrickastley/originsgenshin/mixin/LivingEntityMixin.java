package io.github.xrickastley.originsgenshin.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.element.reaction.AdditiveElementalReaction;
import io.github.xrickastley.originsgenshin.element.reaction.AmplifyingElementalReaction;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinAttributes;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinGameRules;
import io.github.xrickastley.originsgenshin.networking.ShowElementalDamageS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@Debug(export = true)
public abstract class LivingEntityMixin extends Entity {
	@Unique
	private List<ElementalReaction> originsgenshin$reactions = new ArrayList<>();

	public LivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final World world) {
		super(entityType, world);
		throw new AssertionError();
	}

	@Final
	@ModifyVariable(
		method = "damage",
		at = @At("HEAD"),
		argsOnly = true
	)
	private float applyDMGModifiers(float amount, @Local(argsOnly = true) DamageSource source) {
		if (!(source instanceof final ElementalDamageSource eds)) return OriginsGenshinAttributes.modifyDamage((LivingEntity)(Entity) this, new ElementalDamageSource(source, ElementalApplications.gaugeUnits((LivingEntity)(Entity) this, Element.PHYSICAL, 0), InternalCooldownContext.ofNone(source.getAttacker())), amount);

		final ElementComponent component = ElementComponent.KEY.get(this);
		this.originsgenshin$reactions = component.applyFromDamageSource(eds);

		float additive = this.originsgenshin$reactions != null && !this.originsgenshin$reactions.isEmpty()
			? Math.max(
				this.originsgenshin$reactions
					.stream()
					.filter(reaction -> reaction instanceof AdditiveElementalReaction)
					.map(reaction -> ((AdditiveElementalReaction) reaction))
					.reduce(0.0f, (acc, reaction) -> acc + reaction.applyAmplifier(this.getWorld(), amount), Float::sum),
				0.0f
			)
			: 0.0f;

		OriginsGenshin
			.sublogger("LivingEntityMixin")
			.info("Phase: ADDITIVE - Damage: {}, Additive: {}, Final Base DMG: {}", amount, additive, amount + additive);

		return OriginsGenshinAttributes.modifyDamage((LivingEntity)(Entity) this, eds, amount + additive);
	}
	
	@ModifyVariable(
		method = "modifyAppliedDamage",
		at = @At(
			value = "TAIL",
			shift = At.Shift.BEFORE
		),	
		argsOnly = true
	)
	private float applyReactionAmplifiers(float amount, @Local(argsOnly = true) DamageSource source) {
		double amplifier = this.originsgenshin$reactions != null && !this.originsgenshin$reactions.isEmpty()
			? Math.max(
				this.originsgenshin$reactions
					.stream()
					.filter(reaction -> reaction instanceof AmplifyingElementalReaction)
					.map(reaction -> ((AmplifyingElementalReaction) reaction))
					.reduce(0.0, (acc, reaction) -> acc + reaction.getAmplifier(), Double::sum),
				1.0
			)
			: 1.0;

		OriginsGenshin
			.sublogger("LivingEntityMixin")
			.info("Phase: AMPLIFY - Damage: {}, Multiplier: {}, Final DMG: {}", amount, amplifier, amount * amplifier);

		return amount * (float) amplifier;
	}

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

	@Inject(
		method = "applyDamage",
		at = @At("TAIL")
	)
	private void damageHandlers_elements(final DamageSource source, float amount, CallbackInfo ci) {
		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits((LivingEntity)(Entity) this, Element.PHYSICAL, 0), InternalCooldownContext.ofNone(source.getAttacker()));

		final World world = this.getWorld();

		// OriginsGenshin.LOGGER.info("world.isClient: {}\ndamageSourceMixin.hasElement(): {}\ndamageSourceMixin.getElement(): {}\nworld instanceof {}", world.isClient, damageSourceMixin.hasElement(), damageSourceMixin.getElement(), world.getClass().getSimpleName());
		
		final Element element = eds.getElementalApplication().getElement();
		
		if (world.isClient || !(world instanceof ServerWorld)) return;

		OriginsGenshin.sublogger(LivingEntityMixin.class).info("Spawning damage particles!");

		ShowElementalDamageS2CPacket showElementalDMGPacket = new ShowElementalDamageS2CPacket(this.getId(), element, amount);

		for (ServerPlayerEntity otherPlayer : PlayerLookup.tracking(this)) {
			if (otherPlayer.getId() == this.getId()) return;

			ServerPlayNetworking.send(otherPlayer, showElementalDMGPacket);
		}

		// ((ServerWorld) world).spawnParticles(OriginsGenshinParticleFactory.DAMAGE_TEXT, this.getX(), this.getY() + (this.getHeight() * (1 - Math.min(Math.random(), 0.5))), this.getZ(), 0, amount, color.asARGB(), 1, 1);
	}
}
