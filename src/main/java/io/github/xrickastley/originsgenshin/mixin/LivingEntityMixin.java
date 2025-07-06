package io.github.xrickastley.originsgenshin.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;

import java.util.ArrayList;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.reaction.AmplifyingElementalReaction;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReactions;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinAttributes;
import io.github.xrickastley.originsgenshin.interfaces.ILivingEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@Debug(export = true)
public abstract class LivingEntityMixin 
	extends Entity 
	implements ILivingEntity
{
	@Shadow public abstract ItemStack eatFood(World world, ItemStack stack);

	public LivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final World world) {
		super(entityType, world);
		throw new AssertionError();
	}

	@Unique
	protected int originsgenshin$electroChargedCD = -1;

	@Override
	public void resetElectroChargedCD() {
		this.originsgenshin$electroChargedCD = this.age + 20;
	}

	public boolean isElectroChargedOnCD() {
		return this.age < this.originsgenshin$electroChargedCD;
	}

	@ModifyVariable(
		method = "damage",
		at = @At("HEAD"),
		argsOnly = true
	)
	private float applyDMGModifiers(float amount, @Local(argsOnly = true) DamageSource source) {
		return source instanceof final ElementalDamageSource eds
			? OriginsGenshinAttributes.modifyDamage((LivingEntity)(Entity) this, eds, amount)
			: OriginsGenshinAttributes.modifyDamage((LivingEntity)(Entity) this, new ElementalDamageSource(source, ElementalApplication.gaugeUnits((LivingEntity)(Entity) this, Element.PHYSICAL, 0), "dmg"), amount);
	}

	@ModifyVariable(
		method = "damage",
		at = @At("HEAD"),
		argsOnly = true
	)
	private float includeBaseDMGReactionMultipliers(float amount, @Local(argsOnly = true) DamageSource source) {
		if (!(source instanceof final ElementalDamageSource elementalSource)) return amount;

		float addedBaseDMG = 0f;

		if (ElementComponent.KEY.get(this).hasElementalApplication(Element.QUICKEN)) {
			final Element element = elementalSource.getElementalApplication().getElement();

			// The "Level Multiplier" can't really exist here, so just modify the DMG Bonus by a factor and then multiply directly. 
			if (element.equals(Element.DENDRO)) addedBaseDMG = (0.25f * OriginsGenshin.getLevelMultiplier(this));
			else if (element.equals(Element.ELECTRO)) addedBaseDMG = (0.15f * OriginsGenshin.getLevelMultiplier(this));
		}

		return amount + addedBaseDMG;
	}
	
	@ModifyVariable(
		method = "modifyAppliedDamage",
		at = @At(
			value = "TAIL",
			shift = At.Shift.BEFORE
		),	
		argsOnly = true
	)
	private float includeAmplifyingReactionDMGMultipliers(float amount, @Local(argsOnly = true) DamageSource source) {
		if (!(source instanceof final ElementalDamageSource elementalSource)) return amount;

		final ElementComponent component = ElementComponent.KEY.get(this);
		final ArrayList<ElementalReaction> reactions = component.applyFromDamageSource(elementalSource);

		double amplifier = reactions.size() > 0
			? Math.max(
				reactions
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

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	public void electroChargedTick(CallbackInfo ci) {
		if (!ElementalReactions.ELECTRO_CHARGED.isTriggerable(this) || this.getWorld().isClient) return;

		OriginsGenshin
			.sublogger("LivingEntityMixin")
			.info("Electro-Charged - isTriggerable: {} | Hydro: {} | Electro: {}", ElementalReactions.ELECTRO_CHARGED.isTriggerable(this), ElementComponent.KEY.get(this).getElementalApplication(Element.HYDRO), ElementComponent.KEY.get(this).getElementalApplication(Element.ELECTRO));

		ElementalReactions.ELECTRO_CHARGED.trigger(((LivingEntity)(Entity) this));
		ElementComponent.sync(this);
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

	/*
	@Inject(
		method = "applyDamage",
		at = @At("TAIL")
	)
	private void damageHandlers_elements(final DamageSource source, float amount, CallbackInfo ci) {
		IDamageSource damageSourceMixin = ((IDamageSource) source);

		if (elementHandler == null) elementHandler = new ElementHandler((LivingEntity)(Entity) this);
			
		World world = this.getWorld();

		// OriginsGenshin.LOGGER.info("world.isClient: {}\ndamageSourceMixin.hasElement(): {}\ndamageSourceMixin.getElement(): {}\nworld instanceof {}", world.isClient, damageSourceMixin.hasElement(), damageSourceMixin.getElement(), world.getClass().getSimpleName());
		
		final Element element = damageSourceMixin.getElement();
		
		if (element != null) this.elementHandler.applyElement(
			damageSourceMixin.getElement(),
			damageSourceMixin.getElementalGaugeUnits(),
			damageSourceMixin.getInternalCooldownData()
		);
		
		if (world.isClient || !(world instanceof ServerWorld)) return;

		OriginsGenshin.sublogger(LivingEntityMixin.class).info("Spawning damage particles!");

		ShowElementalDamageS2CPacket showElementalDMGPacket = new ShowElementalDamageS2CPacket(this.getId(), element, amount);

		for (ServerPlayerEntity otherPlayer : PlayerLookup.tracking(this)) {
			if (otherPlayer.getId() == this.getId()) return;

			ServerPlayNetworking.send(otherPlayer, showElementalDMGPacket);
		}

		// ((ServerWorld) world).spawnParticles(OriginsGenshinParticleFactory.DAMAGE_TEXT, this.getX(), this.getY() + (this.getHeight() * (1 - Math.min(Math.random(), 0.5))), this.getZ(), 0, amount, color.asARGB(), 1, 1);
	}
	*/
}
