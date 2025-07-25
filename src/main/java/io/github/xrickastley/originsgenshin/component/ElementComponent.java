package io.github.xrickastley.originsgenshin.component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementHolder;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalApplications;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.InternalCooldownContext;
import io.github.xrickastley.originsgenshin.element.InternalCooldownType;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import io.github.xrickastley.originsgenshin.power.ElementalInfusionPower;
import io.github.xrickastley.originsgenshin.util.Array;
import io.github.xrickastley.originsgenshin.util.ClassInstanceUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.Pair;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;

public interface ElementComponent extends AutoSyncedComponent, CommonTickingComponent {
	public static final ComponentKey<ElementComponent> KEY = ComponentRegistry.getOrCreate(OriginsGenshin.identifier("elements"), ElementComponent.class);

	/**
	 * Denies Elemental Applications for the specific entity class. <br> <br>
	 *
	 * The provided entity class, and all it's subclasses, cannot have elements applied to them,
	 * either by force or naturally.
	 *
	 * @param entityClass The entity to deny Elemental Applications for.
	 */
	public static <T extends LivingEntity> void denyElementsFor(Class<T> entityClass) {
		ElementComponentImpl.DENIED_ENTITIES.add(ClassInstanceUtil.castInstance(entityClass));
	}

	public static DamageSource applyElementalInfusions(DamageSource source, LivingEntity entity) {
		if (source instanceof ElementalDamageSource) return source;

		if (source.isIn(DamageTypeTags.IS_LIGHTNING)) {
			return new ElementalDamageSource(source, ElementalApplications.gaugeUnits(entity, Element.ELECTRO, 0, true), InternalCooldownContext.ofType(source.getAttacker(), "origins-genshin:natural_environment", InternalCooldownType.INTERVAL_ONLY));
		} else if (source.isIn(DamageTypeTags.IS_FIRE)) {
			return new ElementalDamageSource(source, ElementalApplications.gaugeUnits(entity, Element.PYRO, 0, true), InternalCooldownContext.ofType(source.getAttacker(), "origins-genshin:natural_environment", InternalCooldownType.INTERVAL_ONLY));
		}

		if (source.getAttacker() == null || (source instanceof final ElementalDamageSource eds && eds.getElementalApplication().getElement() != Element.PHYSICAL)) return source;

		final @Nullable ElementalInfusionPower power = PowerHolderComponent
			.getPowers(source.getAttacker(), ElementalInfusionPower.class)
			.stream()
			.filter(ElementalInfusionPower::isActive)
			.sorted()
			.findFirst()
			.orElse(null);

		return power == null
			? source
			: new ElementalDamageSource(source, power.getApplication(entity), power.getIcdContext());
	}

	public LivingEntity getOwner();

	public ElementHolder getElementHolder(Element element);

	public Pair<ElementalReaction, Long> getLastReaction();

	default boolean hasLastReaction() {
		return this.getLastReaction().getLeft() != null;
	}

	default boolean hasValidLastReaction() {
		return this.hasLastReaction() && this.getLastReaction().getRight() + 10 >= this.getOwner().getWorld().getTime();
	}

	public boolean isElectroChargedOnCD();

	public boolean isBurningOnCD();

	public void resetElectroChargedCD();

	public void resetBurningCD();

	public void setElectroChargedOrigin(@Nullable LivingEntity origin);

	public void setBurningOrigin(@Nullable LivingEntity origin);

	public @Nullable LivingEntity getElectroChargedOrigin();

	public @Nullable LivingEntity getBurningOrigin();

	default void setOrRetainElectroChargedOrigin(@Nullable LivingEntity origin) {
		this.setElectroChargedOrigin(origin != null ? origin : this.getElectroChargedOrigin());
	}

	default void setOrRetainBurningOrigin(@Nullable LivingEntity origin) {
		this.setBurningOrigin(origin != null ? origin : this.getBurningOrigin());
	}

	/**
	 * Checks if the element can be applied.
	 * @param element The element to check.
	 * @param icdContext The {@link InternalCooldownContext} of the Element to be applied.
	 */
	default boolean canApplyElement(Element element, InternalCooldownContext icdContext) {
		return this.canApplyElement(element, icdContext, false);
	}

	/**
	 * Checks if the element can be applied.
	 * @param element The element to check.
	 * @param icdContext The {@link InternalCooldownContext} of the Element to be applied.
	 * @param handleICD Whether the ICD should be handled. This will register a "hit" to the gauge sequence.
	 */
	public boolean canApplyElement(Element element, InternalCooldownContext icdContext, boolean handleICD);

	default List<ElementalReaction> addElementalApplication(Element element, InternalCooldownContext icdContext, double gaugeUnits) {
		final boolean isAura = this.getAppliedElements().isEmpty();

		return this.addElementalApplication(ElementalApplications.gaugeUnits(this.getOwner(), element, gaugeUnits, isAura), icdContext);
	}

	default List<ElementalReaction> addElementalApplication(Element element, InternalCooldownContext icdContext, double gaugeUnits, double duration) {
		return this.addElementalApplication(ElementalApplications.duration(this.getOwner(), element, gaugeUnits, duration), icdContext);
	}

	public List<ElementalReaction> addElementalApplication(ElementalApplication application, InternalCooldownContext icdContext);

	/**
	 * Checks if this entity has a specified Elemental Application with the provided {@code element}.
	 * @param element The element to check.
	 * @return Whether the entity has the specified element applied.
	 */
	default boolean hasElementalApplication(Element element) {
		return this
			.getElementHolder(element)
			.hasElementalApplication();
	}

	/**
	 * Reduces the amount of gauge units in a specified element, then returns the eventual amount of gauge units reduced.
	 * @param element The element to reduce the gauge units of.
	 * @param gaugeUnits The amount of gauge units to reduce.
	 * @return The eventual amount of gauge units reduced. If this value is lower than {@code gaugeUnits}, the current
	 * element had a current gauge value lesser than {@code gaugeUnits}. However, if this value is {@code -1.0}, the provided
	 * {@code element} was not found or did not exist.
	 *
	 * @see ElementalApplication#reduceGauge
	 */
	default double reduceElementalApplication(Element element, double gaugeUnits) {
		return Optional.ofNullable(this.getElementalApplication(element))
			.map(application -> application.reduceGauge(gaugeUnits))
			.orElse(-1.0);
	}

	/**
	 * Gets an Elemental Application with the specified {@code element}.
	 * @param element The {@code Element} to get an Elemental Application from.
	 * @return The {@code ElementalApplication}, if one exists for {@code element}.
	 */
	default ElementalApplication getElementalApplication(Element element) {
		return this
			.getElementHolder(element)
			.getElementalApplication();
	}

	/**
	 * Gets all currently applied elements as a {@link Stream}.
	 */
	public Array<ElementalApplication> getAppliedElements();

	/**
	 * Applies an {@link ElementalDamageSource} to this entity, <i>possibly</i> triggering
	 * multiple {@link ElementalReaction}s. If no reactions were triggered, the list will be empty.
	 *
	 * @param source The {@code ElementalDamageSource} to apply to this entity.
	 * @return The triggered {@link ElementalReaction}s.
	 */
	public List<ElementalReaction> applyFromDamageSource(final ElementalDamageSource source);

	/**
	 * Gets the lowest {@code priority} value from the currently applied Elements
	 * as an {@link Optional}. <br> <br>
	 *
	 * If the {@code Optional} has no value, this means that there are no Elements
	 * currently applied.
	 */
	public Optional<Integer> getHighestElementPriority();

	/**
	 * Gets all currently prioritized applied elements as an {@link Array}. <br> <br>
	 *
	 * If there are applied Elements with multiple priority values, the most
	 * prioritized one has to be consumed first before the others can be consumed. <br> <br>
	 *
	 * Say that Element A has a priority of {@code 1}, while Element B has a priority
	 * of {@code 2}. Element A's application must be consumed entirely before Element B
	 * could be reacted with or reapplied.
	 */
	public Array<ElementalApplication> getPrioritizedElements();

	public static void sync(Entity entity) {
		if (entity.getWorld().isClient) return;

		ElementComponent.KEY.sync(entity);
	}
}
