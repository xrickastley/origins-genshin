package io.github.xrickastley.originsgenshin.mixin;

import java.util.ArrayList;
import java.util.List;

import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.reaction.AdditiveElementalReaction;
import io.github.xrickastley.originsgenshin.element.reaction.AmplifyingElementalReaction;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = Integer.MIN_VALUE)
public abstract class PrioritizedLivingEntityMixin extends Entity {
	@Unique
	private List<ElementalReaction> originsgenshin$reactions = new ArrayList<>();

	public PrioritizedLivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final World world) {
		super(entityType, world);
		throw new AssertionError();
	}

	@Inject(
		method = "damage",
		at = @At("HEAD"),
		cancellable = true,
		order = Integer.MIN_VALUE
	)
	private void preventDamageWhenFrozen(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (source.getAttacker() instanceof final LivingEntity entity && entity.hasStatusEffect(OriginsGenshinStatusEffects.FROZEN))
			cir.setReturnValue(false);
	}


	@ModifyVariable(
		method = "damage",
		at = @At("HEAD"),
		argsOnly = true,
		order = Integer.MIN_VALUE // Infusions need to be considered before other DMG effects.
	)
	private DamageSource applyElementalInfusions(DamageSource source) {
		return ElementComponent.applyElementalInfusions(source, (LivingEntity)(Entity) this);
	}

	@ModifyVariable(
		method = "damage",
		at = @At("HEAD"),
		argsOnly = true,
		order = Integer.MIN_VALUE // Additive DMG Bonus is a Base DMG multiplier.
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
					.reduce(0.0f, (acc, reaction) -> acc + (float) reaction.getDamageBonus(this.getWorld()), Float::sum),
				0.0f
			)
			: 0.0f;

		OriginsGenshin
			.sublogger("LivingEntityMixin")
			.debug("Damage Phase: ADDITIVE - Damage: {}, Additive: {}, Final Base DMG: {}", amount, additive, amount + additive);

		return OriginsGenshinAttributes.modifyDamage((LivingEntity)(Entity) this, eds, amount + additive);
	}

	@ModifyVariable(
		method = "modifyAppliedDamage",
		at = @At(
			value = "TAIL",
			shift = At.Shift.BEFORE
		),
		argsOnly = true,
		order = Integer.MAX_VALUE
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
			.debug("Damage Phase: AMPLIFY - Damage: {}, Multiplier: {}, Final DMG: {}", amount, amplifier, amount * amplifier);

		return amount * (float) amplifier;
	}
}
