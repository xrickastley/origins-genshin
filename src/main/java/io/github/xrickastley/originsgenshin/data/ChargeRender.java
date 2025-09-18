package io.github.xrickastley.originsgenshin.data;

import java.util.List;

import io.github.apace100.apoli.power.factory.condition.ConditionFactory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Pair;

public class ChargeRender {
	public static final ChargeRender DEFAULT = new ChargeRender(Method.SPLIT, null);

	protected ElementalSkillIcon skillIcon;
	protected final Method method;
	protected final List<Pair<Integer, ConditionFactory<Entity>.Instance>> conditions;

	public ChargeRender(Method method, List<Pair<Integer, ConditionFactory<Entity>.Instance>> conditions) {
		this.method = method;
		this.conditions = conditions;
	}

	protected ChargeRender setElementalSkillIcon(ElementalSkillIcon skillIcon) {
		this.skillIcon = skillIcon;

		return this;
	}

	public Method getMethod() {
		return method;
	}

	public List<Pair<Integer, ConditionFactory<Entity>.Instance>> getConditions() {
		return conditions;
	}

	public int getCurrentCharges(PlayerEntity player) {
		if (this.method == Method.SPLIT) {
			Pair<Integer, Integer> pair = this.skillIcon.resolveCooldownResource(player);

			int cur = pair.getLeft();
			int max = pair.getRight();

			return cur / (max / this.skillIcon.getCharges());
		}

		if (conditions == null) return 1;

		for (Pair<Integer, ConditionFactory<Entity>.Instance> chargeCondition : conditions) {
			if (chargeCondition.getRight().test(player)) return chargeCondition.getLeft();
		}

		return 0;
	}

	public static enum Method {
		SPLIT, CONDITIONAL;
	}
}
