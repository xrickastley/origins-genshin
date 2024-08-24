package io.github.botcoder69.originsgenshin.data;

import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.condition.ConditionFactory;
import io.github.apace100.calio.ClassUtil;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;

import io.github.botcoder69.originsgenshin.util.Color;

import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;

public class OriginsGenshinDataTypes {
	public static final SerializableDataType<Color> COLOR
		= SerializableDataType.compound(
			Color.class,
			new SerializableData()
				.add("red", SerializableDataTypes.INT, 255)
				.add("green", SerializableDataTypes.INT, 255)
				.add("blue", SerializableDataTypes.INT, 255)
				.add("alpha", SerializableDataTypes.FLOAT, 0F)
				.add("hex", SerializableDataTypes.STRING, "#ffffff"),
			dataInst -> dataInst.isPresent("hex")
				? Color.fromRGBAHex(dataInst.getString("hex"))
				: new Color(
					dataInst.getInt("red"),
					dataInst.getInt("green"),
					dataInst.getInt("blue"),
					dataInst.getInt("alpha")
				),
			(data, inst) -> {
				SerializableData.Instance dataInst = data.new Instance();
				dataInst.set("red", inst.getRed());
				dataInst.set("green", inst.getGreen());
				dataInst.set("blue", inst.getBlue());
				dataInst.set("alpha", inst.getAlpha());
				dataInst.set("hex", inst.asHex());
				return dataInst;
			}
		);

	public static final SerializableDataType<ChargeRender> CHARGE_RENDER
		= SerializableDataType.compound(
			ChargeRender.class,
			new SerializableData()
				.add("type", SerializableDataType.enumValue(ChargeRender.Method.class), ChargeRender.Method.SPLIT)
				.add("conditions", SerializableDataType.list(
					SerializableDataType.compound(
						ClassUtil.castClass(Pair.class),
						new SerializableData()
							.add("charge", SerializableDataTypes.INT)
							.add("condition", ApoliDataTypes.ENTITY_CONDITION),
						dataInst -> new Pair<Integer, ConditionFactory<Entity>.Instance>(
							dataInst.getInt("charge"),
							dataInst.get("condition")
						),
						(data, pair) -> {
							SerializableData.Instance inst = data.new Instance();
							inst.set("charge", pair.getLeft());
							inst.set("condition", pair.getRight());
							return inst;
						}
					)
				), null),
			dataInst -> new ChargeRender(
				dataInst.get("type"),
				dataInst.get("conditions")
			),
			(data, inst) -> {
				SerializableData.Instance dataInst = data.new Instance();
				dataInst.set("type", inst.getMethod());
				dataInst.set("conditions", inst.getConditions());
				return dataInst;
			}
		);

	public static final SerializableDataType<ElementalSkillIcon> ELEMENTAL_SKILL_ICON
		= SerializableDataType.compound(
			ElementalSkillIcon.class,
			new SerializableData()
				.add("icon", SerializableDataTypes.IDENTIFIER) // The icon to render, if condition is true.
				.add("cooldown", ApoliDataTypes.POWER_TYPE, null) // The cooldown value to use. If not supplied, uses the cooldown of the Power this Elemental Skill is attached to. 
				.add("reverse", SerializableDataTypes.BOOLEAN, false)
				.add("charges", SerializableDataTypes.INT, 1)
				.add("charge_render", OriginsGenshinDataTypes.CHARGE_RENDER, ChargeRender.DEFAULT)
				.add("condition", ApoliDataTypes.ENTITY_CONDITION, null)
				.add("disable_condition", ApoliDataTypes.ENTITY_CONDITION, null),
			dataInst -> new ElementalSkillIcon(
				dataInst.getId("icon"),
				dataInst.get("cooldown"),
				dataInst.getBoolean("reverse"),
				Math.min(Math.max(1, dataInst.getInt("charges")), 3),
				dataInst.get("charge_render"),
				dataInst.get("condition"),
				dataInst.get("disable_condition")
			),
			(data, inst) -> {
				SerializableData.Instance dataInst = data.new Instance();
				dataInst.set("icon", inst.getIcon());
				dataInst.set("cooldown", inst.getCooldown());
				dataInst.set("reverse", inst.shouldReverseCooldown());
				dataInst.set("charges", inst.getCharges());
				dataInst.set("charge_render", inst.getChargeRender());
				dataInst.set("condition", inst.getCondition());
				dataInst.set("disable_condition", inst.getDisableCondition());
				return dataInst;
			}
		);

	public static final SerializableDataType<ElementalBurstIcon> ELEMENTAL_BURST_ICON
		= SerializableDataType.compound(
			ElementalBurstIcon.class,
			new SerializableData()
				.add("icon", SerializableDataTypes.IDENTIFIER)
				.add("cooldown", ApoliDataTypes.POWER_TYPE, null)
				.add("reverse", SerializableDataTypes.BOOLEAN, false)
				.add("energy_resource", ApoliDataTypes.POWER_TYPE, null)
				.add("color", OriginsGenshinDataTypes.COLOR, null)
				.add("outline_color", OriginsGenshinDataTypes.COLOR, null)
				.add("new_max", SerializableDataTypes.INT, -1)
				.add("condition", ApoliDataTypes.ENTITY_CONDITION, null)
				.add("disable_condition", ApoliDataTypes.ENTITY_CONDITION, null),
			dataInst -> new ElementalBurstIcon(
				dataInst.getId("icon"),
				dataInst.get("cooldown"),
				dataInst.getBoolean("reverse"),
				dataInst.get("energy_resource"),
				dataInst.get("color"),
				dataInst.get("outline_color"),
				dataInst.getInt("new_max"),
				dataInst.get("condition"),
				dataInst.get("disable_condition")
			),
			(data, inst) -> {
				SerializableData.Instance dataInst = data.new Instance();
				dataInst.set("icon", inst.getIcon());
				dataInst.set("cooldown", inst.getCooldown());
				dataInst.set("reverse", inst.shouldReverseCooldown());
				dataInst.set("energy_resource", inst.getResource());
				dataInst.set("color", inst.getColor());
				dataInst.set("outline_color", inst.getOutlineColor());
				dataInst.set("new_max", inst.getNewMax());
				dataInst.set("condition", inst.getCondition());
				dataInst.set("disable_condition", inst.getDisableCondition());
				return dataInst;
			}
		);

	public static final SerializableDataType<ElementalSkill> ELEMENTAL_SKILL
		= SerializableDataType.compound(
			ElementalSkill.class,
			new SerializableData()
				.add("show_cooldown", SerializableDataTypes.BOOLEAN, false)
				.add("should_render", SerializableDataTypes.BOOLEAN, false)
				.add("disable_condition", ApoliDataTypes.ENTITY_CONDITION, null)
				.add("icon_conditions", SerializableDataType.list(OriginsGenshinDataTypes.ELEMENTAL_SKILL_ICON)),
			(dataInst) -> new ElementalSkill(
				dataInst.getBoolean("show_cooldown"),
				dataInst.getBoolean("should_render"),
				dataInst.get("disable_condition"),
				dataInst.get("icon_conditions")
			),
			(data, inst) -> {
				SerializableData.Instance dataInst = data.new Instance();
				dataInst.set("show_cooldown", inst.shouldShowCooldown());
				dataInst.set("should_render", inst.shouldRender());
				dataInst.set("disable_condition", inst.getDisableCondition());
				dataInst.set("icon_conditions", inst.getIcons());
				return dataInst;
			}
		);

	public static final SerializableDataType<ElementalBurst> ELEMENTAL_BURST
		= SerializableDataType.compound(
			ElementalBurst.class,
			new SerializableData()
				.add("show_cooldown", SerializableDataTypes.BOOLEAN, false)
				.add("should_render", SerializableDataTypes.BOOLEAN, false)
				.add("disable_condition", ApoliDataTypes.ENTITY_CONDITION, null)
				.add("icon_conditions", SerializableDataType.list(OriginsGenshinDataTypes.ELEMENTAL_BURST_ICON), null),
			(dataInst) -> {
				return new ElementalBurst(
					dataInst.getBoolean("show_cooldown"),
					dataInst.getBoolean("should_render"),
					dataInst.get("disable_condition"),
					dataInst.get("icon_conditions")
				);
			},
			(data, inst) -> {
				SerializableData.Instance dataInst = data.new Instance();
				dataInst.set("show_cooldown", inst.shouldShowCooldown());
				dataInst.set("should_render", inst.shouldRender());
				dataInst.set("disable_condition", inst.getDisableCondition());
				dataInst.set("icon_conditions", inst.getIcons());
				return dataInst;
			}
		);
}
