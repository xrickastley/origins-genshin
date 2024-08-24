package io.github.botcoder69.originsgenshin.interfaces;

import io.github.botcoder69.originsgenshin.data.ElementalBurst;
import io.github.botcoder69.originsgenshin.data.ElementalSkill;

public interface IActiveCooldownPower {
	public boolean hasElementalBurst();
	public ElementalBurst getElementalBurst();
	public boolean hasElementalSkill();
	public ElementalSkill getElementalSkill();
}
