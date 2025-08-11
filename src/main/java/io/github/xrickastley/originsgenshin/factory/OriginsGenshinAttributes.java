package io.github.xrickastley.originsgenshin.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.element.Element;
import io.github.xrickastley.originsgenshin.element.ElementalDamageSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class OriginsGenshinAttributes {
	private static final List<EntityAttribute> ADDED_ATTRIBUTES = new ArrayList<>();
	private static final Map<Element, ConcurrentHashMap<ModifierType, EntityAttribute>> LINKS = new ConcurrentHashMap<>();
	private static boolean registered = false;

	public static final EntityAttribute PHYSICAL_DMG_BONUS = createAttribute("Physical DMG Bonus%", 0, 0, 400);
	public static final EntityAttribute PYRO_DMG_BONUS = createAttribute("Pyro DMG Bonus%", 0, 0, 400);
	public static final EntityAttribute HYDRO_DMG_BONUS = createAttribute("Hydro DMG Bonus%", 0, 0, 400);
	public static final EntityAttribute ANEMO_DMG_BONUS = createAttribute("Anemo DMG Bonus%", 0, 0, 400);
	public static final EntityAttribute ELECTRO_DMG_BONUS = createAttribute("Electro DMG Bonus%", 0, 0, 400);
	public static final EntityAttribute DENDRO_DMG_BONUS = createAttribute("Dendro DMG Bonus%", 0, 0, 400);
	public static final EntityAttribute CRYO_DMG_BONUS = createAttribute("Cryo DMG Bonus%", 0, 0, 400);
	public static final EntityAttribute GEO_DMG_BONUS = createAttribute("Geo DMG Bonus%", 0, 0, 400);

	public static final EntityAttribute PHYSICAL_RES = createAttribute("Physical RES%", 0, -200, 100);
	public static final EntityAttribute PYRO_RES = createAttribute("Pyro RES%", 0, -200, 100);
	public static final EntityAttribute HYDRO_RES = createAttribute("Hydro RES%", 0, -200, 100);
	public static final EntityAttribute ANEMO_RES = createAttribute("Anemo RES%", 0, -200, 100);
	public static final EntityAttribute ELECTRO_RES = createAttribute("Electro RES%", 0, -200, 100);
	public static final EntityAttribute DENDRO_RES = createAttribute("Dendro RES%", 0, -200, 100);
	public static final EntityAttribute CRYO_RES = createAttribute("Cryo RES%", 0, -200, 100);
	public static final EntityAttribute GEO_RES = createAttribute("Geo RES%", 0, -200, 100);

	public static void register() {
		if (registered) return;

		registerAndLink("generic.physical_dmg_bonus", PHYSICAL_DMG_BONUS, Element.PHYSICAL, ModifierType.DMG_BONUS);
		registerAndLink("generic.pyro_dmg_bonus", PYRO_DMG_BONUS, Element.PYRO, ModifierType.DMG_BONUS);
		registerAndLink("generic.hydro_dmg_bonus", HYDRO_DMG_BONUS, Element.HYDRO, ModifierType.DMG_BONUS);
		registerAndLink("generic.anemo_dmg_bonus", ANEMO_DMG_BONUS, Element.ANEMO, ModifierType.DMG_BONUS);
		registerAndLink("generic.electro_dmg_bonus", ELECTRO_DMG_BONUS, Element.ELECTRO, ModifierType.DMG_BONUS);
		registerAndLink("generic.dendro_dmg_bonus", DENDRO_DMG_BONUS, Element.DENDRO, ModifierType.DMG_BONUS);
		registerAndLink("generic.cryo_dmg_bonus", CRYO_DMG_BONUS, Element.CRYO, ModifierType.DMG_BONUS);
		registerAndLink("generic.geo_dmg_bonus", GEO_DMG_BONUS, Element.GEO, ModifierType.DMG_BONUS);

		registerAndLink("generic.physical_res", PHYSICAL_RES, Element.PHYSICAL, ModifierType.RES);
		registerAndLink("generic.pyro_res", PYRO_RES, Element.PYRO, ModifierType.RES);
		registerAndLink("generic.hydro_res", HYDRO_RES, Element.HYDRO, ModifierType.RES);
		registerAndLink("generic.anemo_res", ANEMO_RES, Element.ANEMO, ModifierType.RES);
		registerAndLink("generic.electro_res", ELECTRO_RES, Element.ELECTRO, ModifierType.RES);
		registerAndLink("generic.dendro_res", DENDRO_RES, Element.DENDRO, ModifierType.RES);
		registerAndLink("generic.cryo_res", CRYO_RES, Element.CRYO, ModifierType.RES);
		registerAndLink("generic.geo_res", GEO_RES, Element.GEO, ModifierType.RES);

		registered = true;
	}

	/**
	 * Modifies the provided damage, applying Resistances and DMG Bonus to it.
	 * @param target The target that will receive the DMG.
	 * @param source The {@link ElementalDamageSource} to use in modifying the damage.
	 * @param amount The current amount of DMG being dealt.
	 * @return The modified amount of DMG that should be dealt.
	 */
	public static float modifyDamage(LivingEntity target, ElementalDamageSource source, float amount) {
		final Element element = source.getElementalApplication().getElement();
		final ConcurrentHashMap<ModifierType, EntityAttribute> modifierMap = OriginsGenshinAttributes.LINKS.getOrDefault(element, new ConcurrentHashMap<>());

		final EntityAttribute dmgBonusAttribute = modifierMap.get(ModifierType.DMG_BONUS);
		final EntityAttribute resAttribute = modifierMap.get(ModifierType.RES);

		final float dmgBonusMultiplier = 1 + (target.getAttributes().hasAttribute(dmgBonusAttribute)
			? (float) (target.getAttributes().getValue(dmgBonusAttribute) / 100)
			: 1);

		final float resMultiplier = target.getAttributes().hasAttribute(resAttribute)
			? (float) getRESMultiplier(target, resAttribute)
			: 1;

		return amount * dmgBonusMultiplier * resMultiplier;
	}

	public static DefaultAttributeContainer.Builder apply(DefaultAttributeContainer.Builder builder) {
		// do this since Mod Load (registering) is delayed.
		OriginsGenshinAttributes.register();
		OriginsGenshinAttributes.ADDED_ATTRIBUTES.forEach(builder::add);

		return builder;
	}

	private static double getRESMultiplier(LivingEntity target, EntityAttribute resAttribute) {
		final double elementalRes = target.getAttributes().getValue(resAttribute) / 100;

		return elementalRes < 0
			? 1 - (elementalRes / 2)
			: 0 <= elementalRes && elementalRes < 0.75
				? 1 - elementalRes
				: 1 / ((4 * elementalRes) + 1);
	}

	private static EntityAttribute registerAndLink(String name, EntityAttribute attribute, Element element, ModifierType modifierType) {
		final ConcurrentHashMap<ModifierType, EntityAttribute> modifierMap = OriginsGenshinAttributes.LINKS.getOrDefault(element, new ConcurrentHashMap<>());

		modifierMap.put(modifierType, attribute);

		OriginsGenshinAttributes.LINKS.put(element, modifierMap);

		return register(name, attribute);
	}

	private static EntityAttribute register(String name, EntityAttribute attribute) {
		OriginsGenshinAttributes.ADDED_ATTRIBUTES.add(attribute);

		return Registry.register(Registries.ATTRIBUTE, OriginsGenshin.identifier(name), attribute);
	}

	private static EntityAttribute createAttribute(final String name, double base, double min, double max) {
		return new ClampedEntityAttribute(name, base, min, max)
			.setTracked(true);
	}

	private static enum ModifierType {
		DMG_BONUS, RES;
	}
}
