package io.github.xrickastley.originsgenshin.element.reaction;

import java.util.List;
import java.util.function.Predicate;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.component.ElementComponent;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.events.ReactionTriggered;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinSoundEvents;
import io.github.xrickastley.originsgenshin.networking.ShowElectroChargeS2CPacket;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinDamageTypes;
import io.github.xrickastley.originsgenshin.util.TextHelper;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;

import javax.annotation.Nullable;

public class ElectroChargedElementalReaction extends ElementalReaction {
	ElectroChargedElementalReaction() {
		super(
			new ElementalReactionSettings("Electro-Charged", OriginsGenshin.identifier("electro-charged"), TextHelper.reaction("origins-genshin.element.electro-charged", "#d691fc"))
				.setReactionCoefficient(0)
				.setAuraElement(Element.ELECTRO, 5)
				.setTriggeringElement(Element.HYDRO, 6)
				.applyResultAsAura(true)
				.reversable(true)
		);
	}

	@Override
	public boolean isTriggerable(LivingEntity entity) {
		final ElementComponent component = ElementComponent.KEY.get(entity);

		final ElementalApplication applicationAE = component.getElementalApplication(auraElement.getLeft());
		final ElementalApplication applicationTE = component.getElementalApplication(triggeringElement.getLeft());

		// We need both Elements to exist for Electro-Charged.
		return applicationAE != null && !applicationAE.isEmpty()
			&& applicationTE != null && !applicationTE.isEmpty()
			&& !component.isElectroChargedOnCD()
			&& !component.hasElementalApplication(Element.FREEZE);
	}

	@Override
	public boolean trigger(LivingEntity entity, @Nullable LivingEntity origin) {
		if (!isTriggerable(entity) || entity.getWorld().isClient) return false;

		final ElementComponent component = ElementComponent.KEY.get(entity);
		final ElementalApplication auraElement = component.getElementalApplication(this.auraElement.getLeft());
		final ElementalApplication triggeringElement = component.getElementalApplication(this.triggeringElement.getLeft());

		final double reducedGauge = auraElement.reduceGauge(0.4);
		triggeringElement.reduceGauge(reducedGauge);

		this.onReaction(entity, auraElement, triggeringElement, reducedGauge, origin);
		this.displayReaction(entity);

		ReactionTriggered.EVENT
			.invoker()
			.onReactionTriggered(this, reducedGauge, entity, origin);

		entity
			.getWorld()
			.playSound(null, entity.getBlockPos(), OriginsGenshinSoundEvents.REACTION, SoundCategory.PLAYERS, 1.0f, 1.0f);

		return true;
	}

	@Override
	protected void onReaction(LivingEntity entity, ElementalApplication auraElement, ElementalApplication triggeringElement, double reducedGauge, @Nullable LivingEntity origin) {
		final ElementComponent entityComponent = ElementComponent.KEY.get(entity);

		entityComponent.setOrRetainElectroChargedOrigin(origin);

		final Predicate<LivingEntity> predicate = e -> {
			final ElementComponent c = ElementComponent.KEY.get(e);

			return (e == entity || c.hasElementalApplication(Element.HYDRO)) && !c.isElectroChargedOnCD();
		};

		final List<LivingEntity> targets = ElementalReaction.getEntitiesInAoE(entity, 2.5, predicate);

		for (final LivingEntity target : targets) {
			final float damage = ElementalReaction.getReactionDamage(entity, 2.0);
			final ElementalDamageSource source = new ElementalDamageSource(
				entity
					.getDamageSources()
					.create(OriginsGenshinDamageTypes.ELECTRO_CHARGED, entity, origin),
				ElementalApplications.gaugeUnits(target, Element.ELECTRO, 0),
				InternalCooldownContext.ofNone(origin)
			).shouldApplyDMGBonus(false);

			target.damage(source, damage);

			ElementComponent.KEY
				.get(target)
				.resetElectroChargedCD();
		}

		this.sendDisplayPacket(entity, targets);
	}

	private void sendDisplayPacket(LivingEntity mainTarget, List<LivingEntity> otherTargets) {
		if (otherTargets.isEmpty()) return;

		final ShowElectroChargeS2CPacket packet = new ShowElectroChargeS2CPacket(mainTarget, otherTargets);

		if (mainTarget instanceof final ServerPlayerEntity serverPlayer) ServerPlayNetworking.send(serverPlayer, packet);

		for (final ServerPlayerEntity otherPlayer : PlayerLookup.tracking(mainTarget)) {
			if (otherPlayer.getId() == mainTarget.getId()) continue;

			ServerPlayNetworking.send(otherPlayer, packet);
		}
	}

	// These "mixins" are injected pieces of code (likening @Inject) that allow Electro-Charged to work properly, and allow others to easily see the way it was hardcoded.
	public static void mixin$tick(LivingEntity entity) {
		if (!ElementalReactions.ELECTRO_CHARGED.isTriggerable(entity) || entity.getWorld().isClient || entity.isDead()) return;

		ElementalReactions.ELECTRO_CHARGED.trigger(entity);

		ElementComponent.sync(entity);
	}
}
