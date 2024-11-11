package io.github.xrickastley.originsgenshin.element.reaction;

import io.github.xrickastley.originsgenshin.registry.OriginsGenshinRegistries;
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
	// Deals DoT Electro DMG to the target and nearby entities.
	public static final ElementalReaction ELECTRO_CHARGED = new ElectroChargedElementalReaction();
	// Creates an Explosion at the source, damaging all nearby "enemy" entities.
	public static final ElementalReaction OVERLOADED = new OverloadedElementalReaction();
	// Decreases Physical RES% by -40%.
	public static final ElementalReaction SUPERCONDUCT = new SuperconductElementalReaction();
	public static final ElementalReaction FROZEN_SUPERCONDUCT = new FrozenSuperconductElementalReaction();
	// Applies the Quicken aura to the target.
	public static final ElementalReaction QUICKEN = new QuickenElementalReaction();
	// Creates a Dendro Core at the source.
	public static final ElementalReaction DENDRO_BLOOM = new DendroBloomElementalReaction();
	public static final ElementalReaction HYDRO_BLOOM = new HydroBloomElementalReaction();
	public static final ElementalReaction QUICKEN_BLOOM = new QuickenBloomElementalReaction();
	// Affects the applied swirlable element to all other opponents.
	public static final ElementalReaction PYRO_SWIRL = new PyroSwirlElementalReaction();
	public static final ElementalReaction HYDRO_SWIRL = new HydroSwirlElementalReaction();
	public static final ElementalReaction ELECTRO_SWIRL = new ElectroSwirlElementalReaction();
	public static final ElementalReaction CRYO_SWIRL = new CryoSwirlElementalReaction();
	// TODO: Reaction: Burning
	// TODO: Reaction: Crystallize (+ Crystallize Shards)
	// TODO: Reaction: Spread + Aggravate (indicators, reactions already handled by LivingEntityMixin)
	// TODO: Reaction display.

	public static void register() {
		register(ElementalReactions.CRYO_MELT);
		register(ElementalReactions.HYDRO_VAPORIZE);
		register(ElementalReactions.PYRO_MELT);
		register(ElementalReactions.PYRO_VAPORIZE);
		register(ElementalReactions.FROZEN);
		register(ElementalReactions.ELECTRO_CHARGED);
		register(ElementalReactions.OVERLOADED);
		register(ElementalReactions.SUPERCONDUCT);
		register(ElementalReactions.FROZEN_SUPERCONDUCT);
		register(ElementalReactions.QUICKEN);
		register(ElementalReactions.DENDRO_BLOOM);
		register(ElementalReactions.HYDRO_BLOOM);
		register(ElementalReactions.QUICKEN_BLOOM);
		register(ElementalReactions.PYRO_SWIRL);
		register(ElementalReactions.HYDRO_SWIRL);
		register(ElementalReactions.ELECTRO_SWIRL);
		register(ElementalReactions.CRYO_SWIRL);
	}

	private static ElementalReaction register(ElementalReaction reaction) {
		return Registry.register(OriginsGenshinRegistries.ELEMENTAL_REACTION, reaction.getId(), reaction);
	}
}