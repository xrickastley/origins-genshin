package io.github.xrickastley.originsgenshin.mixins;

import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.components.ElementComponent;
import io.github.xrickastley.originsgenshin.elements.Element;
import io.github.xrickastley.originsgenshin.elements.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.elements.reactions.AmplifyingElementalReaction;
import io.github.xrickastley.originsgenshin.elements.reactions.ElementalReaction;
import io.github.xrickastley.originsgenshin.elements.reactions.ElementalReactions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;

@Mixin(LivingEntity.class)
@Debug(export = true)
public abstract class LivingEntityMixin extends Entity {
	public LivingEntityMixin(final EntityType<? extends LivingEntity> entityType, final World world) {
		super(entityType, world);
		throw new AssertionError();
	}

	@Unique
	protected int electroChargedCD = -1;

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
			if (element.equals(Element.DENDRO)) addedBaseDMG = (1.25f * OriginsGenshin.getLevelMultiplier(this));
			else if (element.equals(Element.ELECTRO)) addedBaseDMG = (1.15f * OriginsGenshin.getLevelMultiplier(this));
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

		float multiplier = 1.0f;

		final ElementalReaction reaction = component.applyFromDamageSource(elementalSource);

		if (reaction != null && reaction instanceof final AmplifyingElementalReaction amplifyingReaction) {
			multiplier = (float) amplifyingReaction.getAmplifier();
		}

		System.out.printf("amount: %.2f; multiplier: %.2f; result: %.2f\n", amount, multiplier, amount * multiplier);

		return amount * multiplier;
	}

	@Inject(
		method = "tick",
		at = @At("HEAD")
	)
	public void tick() {
		if (!(ElementalReactions.ELECTRO_CHARGED.isTriggerable(this) && electroChargedCD <= this.age)) return;

		this.electroChargedCD = this.age + 20;

		ElementalReactions.ELECTRO_CHARGED.trigger(((LivingEntity)(Entity) this));
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
