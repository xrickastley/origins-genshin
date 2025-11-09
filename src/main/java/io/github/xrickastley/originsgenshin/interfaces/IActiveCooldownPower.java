package io.github.xrickastley.originsgenshin.interfaces;

import io.github.xrickastley.originsgenshin.data.ElementalBurst;
import io.github.xrickastley.originsgenshin.data.ElementalSkill;

public interface IActiveCooldownPower {
	public boolean originsgenshin$hasElementalBurst();
	public ElementalBurst originsgenshin$getElementalBurst();
	public boolean originsgenshin$hasElementalSkill();
	public ElementalSkill originsgenshin$getElementalSkill();
}
