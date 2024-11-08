package io.github.xrickastley.originsgenshin.elements.reactions;

import io.github.xrickastley.originsgenshin.registries.OriginsGenshinRegistries;

import net.minecraft.registry.Registry;

public class ElementalReactions {
	// Also known as Reverse Melt, amplifies Cryo DMG by 1.5x
	public static final ElementalReaction CRYO_MELT = new CryoMeltElementalReaction();
	// Also known as Forward Vaporize, amplifies Hydro DMG by 2x
	public static final ElementalReaction HYDRO_VAPORIZE = new HydroVaporizeElementalReaction();
	// Also known as Forward Melt, amplifies Pyro DMG by 2x
	public static final ElementalReaction PYRO_MELT = new PyroMeltElementalReaction();
	// Also known as Reverse Vaporize, amplifies Pyro DMG by 1.5x
	public static final ElementalReaction PYRO_VAPORIZE = new PyroVaporizeElementalReaction();
	// Keeps the entity in place, preventing movement, attacks and actions.
	public static final ElementalReaction FROZEN = new FrozenElementalReaction();
	// Keeps the entity in place, preventing movement, attacks and actions.
	public static final ElementalReaction ELECTRO_CHARGED = new ElectroChargedElementalReaction();

	public static void register() {
		register(ElementalReactions.CRYO_MELT);
		register(ElementalReactions.HYDRO_VAPORIZE);
		register(ElementalReactions.PYRO_MELT);
		register(ElementalReactions.PYRO_VAPORIZE);
		register(ElementalReactions.FROZEN);
		register(ElementalReactions.ELECTRO_CHARGED);
	}

	private static ElementalReaction register(ElementalReaction reaction) {
		return Registry.register(OriginsGenshinRegistries.ELEMENTAL_REACTION, reaction.getId(), reaction);
	}
}
