package io.github.xrickastley.originsgenshin.mixins;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.util.HudRender;
import io.github.apace100.calio.data.SerializableData;

import io.github.xrickastley.originsgenshin.data.OriginsGenshinDataTypes;
import io.github.xrickastley.originsgenshin.interfaces.IActiveCooldownPower;
import io.github.xrickastley.originsgenshin.data.ElementalBurst;
import io.github.xrickastley.originsgenshin.data.ElementalSkill;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@Pseudo
@Mixin(ActiveCooldownPower.class)
public abstract class ActiveCooldownPowerMixin
	extends CooldownPower 
	implements IActiveCooldownPower
{
	public ActiveCooldownPowerMixin(PowerType<?> type, LivingEntity entity, int cooldownDuration, HudRender hudRender, Consumer<Entity> activeFunction) {
		super(type, entity, cooldownDuration, hudRender);
		throw new AssertionError();
	}

	protected ElementalBurst elementalBurst;
	protected ElementalSkill elementalSkill;

	protected void setElementalBurst(ElementalBurst elementalBurst) {
		this.elementalBurst = elementalBurst;

		if (this.elementalBurst != null) this.elementalBurst.setPower(((ActiveCooldownPower)(Object) this));
	}

	public boolean hasElementalBurst() {
		return this.elementalBurst != null;
	}

	public ElementalBurst getElementalBurst() {
		return this.elementalBurst;
	}

	protected void setElementalSkill(ElementalSkill elementalSkill) {
		this.elementalSkill = elementalSkill;

		if (this.elementalSkill != null) this.elementalSkill.setPower(((ActiveCooldownPower)(Object) this));
	}

	public boolean hasElementalSkill() {
		return this.elementalSkill != null;
	}

	public ElementalSkill getElementalSkill() {
		return this.elementalSkill;
	}

	@ModifyArg(
		method = "createActiveSelfFactory",
		at = @At(value = "INVOKE", target = "Lio/github/apace100/apoli/power/factory/PowerFactory;<init>(Lnet/minecraft/util/Identifier;Lio/github/apace100/calio/data/SerializableData;Ljava/util/function/Function;)V"),
		index = 1
	)
	private static SerializableData injectElementalBurst(SerializableData data) {
		return data
			.add("elemental_burst", OriginsGenshinDataTypes.ELEMENTAL_BURST, null)
			.add("elemental_skill", OriginsGenshinDataTypes.ELEMENTAL_SKILL, null);
	}

	@ModifyArg(
		method = "createActiveSelfFactory",
		at = @At(value = "INVOKE", target = "Lio/github/apace100/apoli/power/factory/PowerFactory;<init>(Lnet/minecraft/util/Identifier;Lio/github/apace100/calio/data/SerializableData;Ljava/util/function/Function;)V"),
		index = 2
	)
	private static Function<SerializableData.Instance, BiFunction<PowerType<Power>, LivingEntity, Power>> overwriteData(Function<SerializableData.Instance, BiFunction<PowerType<Power>, LivingEntity, Power>> fn) {
		return data ->
			(type, player) -> {
				ActiveCooldownPower power = new ActiveCooldownPower(
					type, 
					player, 
					data.getInt("cooldown"),
					data.get("hud_render"),
					data.get("entity_action")
				);

				power.setKey(data.get("key"));
				if (data.isPresent("elemental_burst")) ((ActiveCooldownPowerMixin)(Object) power).setElementalBurst(data.get("elemental_burst"));
				else if (data.isPresent("elemental_skill")) ((ActiveCooldownPowerMixin)(Object) power).setElementalSkill(data.get("elemental_skill"));

				return power;
			};
	}
}
