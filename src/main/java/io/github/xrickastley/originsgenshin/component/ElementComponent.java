package io.github.xrickastley.originsgenshin.component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalApplication;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.element.reaction.ElementalReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public interface ElementComponent extends AutoSyncedComponent, CommonTickingComponent {
	public static final ComponentKey<ElementComponent> KEY = ComponentRegistry.getOrCreate(OriginsGenshin.identifier("elements"), ElementComponent.class);

	/**
	 * Checks if the element can be applied.
	 * @param element The element to test.
	 * @param sourceTag The source of this element. This is the skill that dealt the damage.
	 */
	public boolean canApplyElement(Element element, String sourceTag);

	/**
	 * Checks if the element can be applied.
	 * @param element The element to test.
	 * @param sourceTag The source of this element. This is the skill that dealt the damage.
	 * @param handleICD Whether or not ICD should be handled. Only set this to {@code true} if {@link ElementComponent#canApplyElement}
	 */
	public boolean canApplyElement(Element element, String sourceTag, boolean handleICD);

	public ArrayList<ElementalReaction> addElementalApplication(ElementalApplication application, String sourceTag, @Nullable LivingEntity origin);

	public ArrayList<ElementalReaction> addElementalApplication(Element element, String sourceTag, double gaugeUnits, @Nullable LivingEntity origin);
	
	public ArrayList<ElementalReaction> addElementalApplication(Element element, String sourceTag, double gaugeUnits, double duration, @Nullable LivingEntity origin);
	
	/**
	 * Checks if this entity has a specified Elemental Application with the provided {@code element}.
	 * @param element The element to check.
	 * @return Whether or not the entity has the specified element applied.
	 */
	public boolean hasElementalApplication(Element element);

	/**
	 * Reduces the amount of gauge units in a specified element, then returns the eventual amount of gauge units reduced.
	 * @param element The element to reduce the gauge units of.
	 * @param gaugeUnits The amount of gauge units to reduce.
	 * @return The eventual amount of gauge units reduced. If this value is lower than {@code gaugeUnits}, the current 
	 * element had a current gauge value lesser than {@code gaugeUnits}. However, if this value is {@code -1.0}, the provided
	 * {@code element} was not found or did not exist.
	 * 
	 * @see {@link ElementalApplication#reduceGauge}
	 */
	public double reduceElementalApplication(Element element, double gaugeUnits);

	/**
	 * Gets an Elemental Application with the specified {@code element}.
	 * @param element The {@code Element} to get an Elemental Application from.
	 * @return The {@code ElementalApplication}, if one exists for {@code element}. 
	 */
	public ElementalApplication getElementalApplication(Element element);

	/**
	 * Gets all currently applied elements as a {@link Stream}.
	 */
	public Stream<ElementalApplication> getAppliedElements();

	/**
	 * Applies an {@link ElementalDamageSource} to this entity, <i>possibly</i> triggering an {@link ElementalReaction}. If no reaction is triggered, {@code null} is returned instead.
	 * @param source The {@code ElementalDamageSource} to apply to this entity.
	 * @return A triggered {@link ElementalReaction}, or {@code null} if no reaction was triggered.
	 */
	public ArrayList<ElementalReaction> applyFromDamageSource(final ElementalDamageSource source);

	/**
	 * Gets the lowest {@code priority} value from the currently applied Elements
	 * as an {@link Optional}. <br> <br>
	 * 
	 * If the {@code Optional} has no value, this means that there are no Elements
	 * currently applied.
	 */
	public Optional<Integer> getCurrentElementPriority();

	/**
	 * Gets all currently prioritized applied elements as a {@link Stream}. <br> <br>
	 * 
	 * If there are applied Elements with multiple priority values, the most
	 * prioritized one has to be consumed first before the others can be consumed. <br> <br>
	 * 
	 * Say that Element A has a priority of {@code 1}, while Element B has a priority
	 * of {@code 2}. Element A's application must be consumed entirely before Element B
	 * could be reacted with or reapplied.
	 */
	public Stream<ElementalApplication> getPrioritizedElements();

	public static void sync(Entity entity) {
		KEY.sync(entity);
	}
}
