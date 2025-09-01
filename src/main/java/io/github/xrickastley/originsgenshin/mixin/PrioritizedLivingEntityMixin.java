package io.github.xrickastley.originsgenshin.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.component.ElementComponentImpl;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.reaction.AdditiveElementalReaction;
import io.github.xrickastley.originsgenshin.element.reaction.AmplifyingElementalReaction;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReactions;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinAttributes;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinStatusEffects;
import io.github.xrickastley.originsgenshin.interfaces.ILivingEntity;
import io.github.xrickastley.originsgenshin.util.ClassInstanceUtil;
import io.github.xrickastley.originsgenshin.util.FilteredIterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

@Mixin(value = LivingEntity.class, priority = 0)
public abstract class PrioritizedLivingEntityMixin
	extends Entity
	implements ILivingEntity
{
	@Unique
	private List<ElementalReaction> originsgenshin$reactions = new ArrayList<>();
	@Unique
	private @Nullable Entity originsgenshin$plannedAttacker;

	public PrioritizedLivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final World world) {
		super(entityType, world);

		throw new AssertionError();
	}

	@Unique
	@Override
	public @Nullable Entity originsgenshin$getPlannedAttacker() {
		return this.originsgenshin$plannedAttacker;
	}

	@Inject(
		method = "removeStatusEffectInternal",
		at = @At("HEAD"),
		cancellable = true
	)
	private void preventRemovingCryoEffectIfCryoElement(@Nullable StatusEffect type, CallbackInfoReturnable<StatusEffectInstance> cir) {
		if (type == OriginsGenshinStatusEffects.CRYO
			&& ElementComponent.KEY.get(this).hasElementalApplication(Element.CRYO)) cir.setReturnValue(null);
	}

	@ModifyVariable(
		method = "clearStatusEffects",
		at = @At("STORE"),
		ordinal = 0
	)
	private Iterator<StatusEffectInstance> replaceIterator(Iterator<StatusEffectInstance> value) {
		if (!ElementComponent.KEY.get(this).hasElementalApplication(Element.CRYO)) return value;

		return FilteredIterator.of(value, v -> v.getEffectType() == OriginsGenshinStatusEffects.CRYO);
	}

	@Inject(
		method = "damage",
		at = @At("HEAD"),
		order = Integer.MIN_VALUE
	)
	private void setPlannedAttacker(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		this.originsgenshin$plannedAttacker = source.getAttacker();
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
		order = Integer.MIN_VALUE // Additive DMG Bonus is a Base DMG multiplier, should be applied ASAP.
	)
	private float applyDMGModifiers(float amount, @Local(argsOnly = true) DamageSource source) {
		final ElementalDamageSource eds = source instanceof final ElementalDamageSource eds2
			? eds2
			: new ElementalDamageSource(source, ElementalApplications.gaugeUnits((LivingEntity)(Entity) this, Element.PHYSICAL, 0.01), InternalCooldownContext.ofNone(source.getAttacker()));

		final ElementComponent component = ElementComponent.KEY.get(this);
		this.originsgenshin$reactions = new ArrayList<>(component.applyFromDamageSource(eds));

		final @Nullable ElementalReaction lastReaction = this.originsgenshin$reactions.isEmpty()
			? null
			: this.originsgenshin$reactions.get(this.originsgenshin$reactions.size() - 1);

		final boolean doShatter = !this.originsgenshin$reactions.contains(ElementalReactions.GEO_SHATTER)
			&& !this.originsgenshin$reactions.contains(ElementalReactions.SHATTER)
			&& ElementalReactions.SHATTER.isTriggerable(this)
			&& (lastReaction == null || !lastReaction.preventsReaction(ElementalReactions.SHATTER));

		if (doShatter) {
			this.originsgenshin$reactions.add(ElementalReactions.SHATTER);
			((ElementComponentImpl) component).setLastReaction(new Pair<>(ElementalReactions.SHATTER, this.getWorld().getTime()));

			ElementalReactions.SHATTER.trigger((LivingEntity)(Entity) this, ClassInstanceUtil.castOrNull(source.getAttacker(), LivingEntity.class));
		}

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
			.sublogger("PrioritizedLivingEntityMixin")
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
			.sublogger("PrioritizedLivingEntityMixin")
			.debug("Damage Phase: AMPLIFY - Damage: {}, Multiplier: {}, Final DMG: {}", amount, amplifier, amount * amplifier);

		return amount * (float) amplifier;
	}
}
