package io.github.xrickastley.originsgenshin.data;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;

import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public class ElementalSkillIcon extends RenderableIcon {
	protected final int charges; 
	protected final ChargeRender chargeRender;

	public ElementalSkillIcon(Identifier icon, PowerType<?> cooldown, boolean reverse, int charges, ChargeRender chargeRender, ConditionFactory<Entity>.Instance condition, ConditionFactory<Entity>.Instance disableCondition) {
		super(icon, cooldown, reverse, condition, disableCondition);

		this.charges = Math.min(Math.max(1, charges), 3);
		this.chargeRender = chargeRender;
		this.chargeRender.setElementalSkillIcon(this);
	}
	
	public int getCharges() {
		return Math.min(3, Math.max(charges, 1));
	}

	public ChargeRender getChargeRender() {
		return chargeRender;
	}

	@Override
	public ElementalSkill getSkill() {
		return ((ElementalSkill) super.getSkill());
	}
}
