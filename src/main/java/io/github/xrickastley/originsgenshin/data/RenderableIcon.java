package io.github.xrickastley.originsgenshin.data;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.VariableIntPower;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public abstract class RenderableIcon {
	protected RenderableSkill skill;
	protected final Identifier icon;
	protected final PowerType<?> cooldown;
	protected final boolean reverse;
	protected final ConditionFactory<Entity>.Instance disableCondition;
	protected final ConditionFactory<Entity>.Instance condition;

	public RenderableIcon(Identifier icon, PowerType<?> cooldown, boolean reverse, ConditionFactory<Entity>.Instance condition, ConditionFactory<Entity>.Instance disableCondition) {
		this.icon = icon;
		this.cooldown = cooldown;
		this.reverse = reverse;
		this.condition = condition;
		this.disableCondition = disableCondition;
	}

	public void setSkill(RenderableSkill skill) {
		this.skill = skill;
	}

	public RenderableSkill getSkill() {
		return skill;
	}

	public Identifier getIcon() {
		return icon;
	}

	public PowerType<?> getCooldown() {
		return cooldown;
	}

	public boolean shouldReverseCooldown() {
		return reverse;
	}

	public ConditionFactory<Entity>.Instance getCondition() {
		return condition;
	}

	public ConditionFactory<Entity>.Instance getDisableCondition() {
		return disableCondition;
	}

	public boolean shouldRender(PlayerEntity entity) {
		return this.condition == null
			? true
			: this.condition.test(entity);
	}

	public boolean renderAsDisabled(PlayerEntity entity) {
		return this.disableCondition == null
			? false
			: this.disableCondition.test(entity);
	}

	/**
	 * Resolves the cooldown resource. The output will always be a {@code Pair<Integer, Integer>} with the {@code left} rising until the {@code right}.
	 * @param player The player to resolvle the cooldown resource for.
	 * @return A resolved {@code Pair<Integer, Integer>} with the {@code left} rising until the {@code right}.
	 */
	public Pair<Integer, Integer> resolveCooldownResource(PlayerEntity player) {
		final ActiveCooldownPower power = this.skill.getPower();

		if (this.getCooldown() == null) return new Pair<Integer,Integer>(power.cooldownDuration - power.getRemainingTicks(), power.cooldownDuration);

		Pair<Integer, Integer> pair = RenderableIcon.resolveResource(this.cooldown, player);

		return pair == null
			? new Pair<Integer,Integer>(power.cooldownDuration - power.getRemainingTicks(), power.cooldownDuration)
			: this.shouldReverseCooldown()
				? new Pair<Integer,Integer>(pair.getRight() - pair.getLeft(), pair.getRight())
				: pair;
	}

	private static Pair<Integer, Integer> resolveResource(PowerType<?> resource, PlayerEntity player) {
		return PowerHolderComponent.KEY.maybeGet(player)
			.map(component -> {
				final Power power = component.getPower(resource);

				Pair<Integer, Integer> pair = null;

				if (power instanceof VariableIntPower vip) pair = new Pair<Integer, Integer>(vip.getValue(), vip.getMax());
				else if (power instanceof CooldownPower cp) pair = new Pair<Integer, Integer>(cp.getRemainingTicks(), cp.cooldownDuration);

				return pair;
			})
			.orElse(null);
	}
}
