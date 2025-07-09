package io.github.xrickastley.originsgenshin.element;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import blue.endless.jankson.annotation.Nullable;
import net.minecraft.entity.LivingEntity;

public final class ElementHolder {
	// The one holding the element.
	private final LivingEntity owner;
	protected final Map<LivingEntity, InternalCooldownHolder> internalCooldowns = new ConcurrentHashMap<>();
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
		this.application = application;
	}

	public boolean hasElementalApplication() {
		return application != null;
	}

	public Element getElement() {
		return this.element;
	}

	public @Nullable ElementalApplication getElementalApplication() {
		return this.application;
	}

	public ElementalApplication getOrCreateElementalApplication(double gaugeUnits, boolean aura) {
		this.application = ElementalApplication.gaugeUnits(owner, element, gaugeUnits, aura);

		return this.application;
	}
	
	public ElementalApplication getOrCreateElementalApplication(double duration, double gaugeUnits) {
		this.application = ElementalApplication.duration(owner, element, gaugeUnits, duration);

		return this.application;
	}

	public void setElementalApplication(@Nullable ElementalApplication application) {
		this.application = application;
	}

	/**
	 * Checks if the element can be applied.
	 * @param element The element to test.
	 * @param sourceTag The source of this element. This is the skill that dealt the damage.
	 */
	public boolean canApplyElement(Element element, InternalCooldownContext icdContext) {
		return this.canApplyElement(element, icdContext, false);
	}
	
	/**
	 * Checks if the element can be applied.
	 * @param element The element to test.
	 * @param sourceTag The source of this element. This is the skill that dealt the damage.
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
