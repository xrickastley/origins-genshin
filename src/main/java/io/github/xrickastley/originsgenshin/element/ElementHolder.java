package io.github.xrickastley.originsgenshin.element;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import io.github.xrickastley.originsgenshin.events.ElementApplied;
import io.github.xrickastley.originsgenshin.events.ElementRefreshed;
import io.github.xrickastley.originsgenshin.events.ElementRemoved;
import io.github.xrickastley.originsgenshin.factory.OriginsGenshinGameRules;
import net.minecraft.entity.LivingEntity;

public final class ElementHolder {
	// The entity holding the element.
	private final LivingEntity owner;
	protected final Map<UUID, InternalCooldownHolder> internalCooldowns = new ConcurrentHashMap<>();
	private final Element element;
	private @Nullable ElementalApplication application;

	public static ElementHolder of(final LivingEntity owner, final Element element) {
		return new ElementHolder(owner, element);
	}

	public static ElementHolder ofApplication(final LivingEntity owner, final Element element, ElementalApplication application) {
		if (application.getElement() != element) throw new IllegalArgumentException("The provided ElementalApplication's element must match that of the provided element argument!");

		return new ElementHolder(owner, element, application);
	}

	private ElementHolder(final LivingEntity owner, final Element element) {
		this(owner, element, null);
	}

	private ElementHolder(final LivingEntity owner, final Element element, ElementalApplication application) {
		this.owner = owner;
		this.element = element;
		this.application = this.shouldDoElements() ? application : null;
	}

	public boolean hasElementalApplication() {
		return application != null && !application.isEmpty();
	}

	public Element getElement() {
		return this.element;
	}

	public @Nullable ElementalApplication getElementalApplication() {
		return this.application;
	}

	public ElementalApplication getOrCreateElementalApplication(double gaugeUnits, boolean aura) {
		if (!this.shouldDoElements()) throw new IllegalStateException("The Game Rule \"doElements\" is false! Check if you can apply elements through ElementHolder#shouldDoElements before calling this method!");

		if (this.application == null) this.setElementalApplication(ElementalApplications.gaugeUnits(owner, element, gaugeUnits, aura));

		return this.application;
	}
	
	public ElementalApplication getOrCreateElementalApplication(double duration, double gaugeUnits) {
		if (!this.shouldDoElements()) throw new IllegalStateException("The Game Rule \"doElements\" is false! Check if you can apply elements through ElementHolder#shouldDoElements before calling this method!");
		
		if (this.application == null) this.setElementalApplication(ElementalApplications.duration(owner, element, gaugeUnits, duration));

		return this.application;
	}

	public void setElementalApplication(@Nullable ElementalApplication application) {
		if (!this.shouldDoElements()) return;

		final @Nullable ElementalApplication prev = this.application;

		this.application = application;

		if (prev != null && application == null) {
			ElementRemoved.EVENT.invoker().onElementRemoved(prev);
		} else if (this.application != null) {
			ElementApplied.EVENT.invoker().onElementApplied(application);
		} else {
			ElementRefreshed.EVENT.invoker().onElementRefreshed(application, prev);
		}
	}

	public boolean shouldDoElements() {
		return owner.getWorld().getGameRules().getBoolean(OriginsGenshinGameRules.DO_ELEMENTS);
	}

	/**
	 * Checks if the element can be applied.
	 * @param element The element to test.
	 * @param icdContext The {@code InternalCooldownContext} of this {@code ElementalDamageSource}.
	 * This controls the Internal Cooldown of specific attacks, as Internal Cooldowns are different
	 * between contexts.
	 */
	public boolean canApplyElement(Element element, InternalCooldownContext icdContext) {
		return this.canApplyElement(element, icdContext, false);
	}
	
	/**
	 * Checks if the element can be applied.
	 * @param element The element to test.
	 * @param icdContext The {@code InternalCooldownContext} of this {@code ElementalDamageSource}.
	 * This controls the Internal Cooldown of specific attacks, as Internal Cooldowns are different
	 * between contexts.
	 * @param handleICD Whether the ICD should be handled.
	 */
	public boolean canApplyElement(Element element, InternalCooldownContext icdContext, boolean handleICD) {
		if (element.bypassesInternalCooldown()) return true;

		final InternalCooldown icdData = icdContext.getInternalCooldown(this);
		
		final boolean inICD = handleICD 
			? icdData.handleInternalCooldown()
			: icdData.isInInternalCooldown();

		return inICD;
	}

	public LivingEntity getOwner() {
		return this.owner;
	}
}
