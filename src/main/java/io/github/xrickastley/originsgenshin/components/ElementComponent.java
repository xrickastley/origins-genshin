package io.github.xrickastley.originsgenshin.components;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.elements.Element;
import io.github.xrickastley.originsgenshin.elements.ElementalApplication;
import io.github.xrickastley.originsgenshin.elements.ElementalDamageSource;
import io.github.xrickastley.originsgenshin.elements.reactions.ElementalReaction;

import net.minecraft.entity.Entity;

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

	public @Nullable ElementalReaction addElementalApplication(ElementalApplication application, String sourceTag);

	public @Nullable ElementalReaction addElementalApplication(Element element, String sourceTag, double gaugeUnits);
	
	public @Nullable ElementalReaction addElementalApplication(Element element, String sourceTag, double gaugeUnits, double duration);
	
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
	public @Nullable ElementalReaction applyFromDamageSource(final ElementalDamageSource source);

	public static void sync(Entity entity) {

		KEY.sync(entity);
	}

	// TODO: Fix elemental Reactions not triggering.
	// TODO: Sync client component with server
}
