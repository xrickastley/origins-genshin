package io.github.xrickastley.originsgenshin.interfaces;

import io.github.xrickastley.originsgenshin.data.ElementalBurst;
import io.github.xrickastley.originsgenshin.data.ElementalSkill;

public interface IActiveCooldownPower {
	public boolean hasElementalBurst();
	public ElementalBurst getElementalBurst();
	public boolean hasElementalSkill();
	public ElementalSkill getElementalSkill();
}
