package io.github.xrickastley.originsgenshin.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Lists;

import com.llamalad7.mixinextras.sugar.Local;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.data.CompatibilityDataTypes;
import io.github.apace100.origins.data.OriginsDataTypes;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;

import io.github.xrickastley.originsgenshin.interfaces.IActiveCooldownPower;
import io.github.xrickastley.originsgenshin.interfaces.IOrigin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

@Pseudo
@Mixin(Origin.class)
public class OriginMixin implements IOrigin {
	@Shadow(remap = false)	
	public static final SerializableData DATA = new SerializableData()
        .add("powers", SerializableDataTypes.IDENTIFIERS, Lists.newArrayList())
        .add("icon", CompatibilityDataTypes.ITEM_OR_ITEM_STACK, new ItemStack(Items.AIR))
        .add("unchoosable", SerializableDataTypes.BOOLEAN, false)
        .add("order", SerializableDataTypes.INT, Integer.MAX_VALUE)
        .add("impact", OriginsDataTypes.IMPACT, Impact.NONE)
        .add("loading_priority", SerializableDataTypes.INT, 0)
        .add("upgrades", OriginsDataTypes.UPGRADES, null)
        .add("name", SerializableDataTypes.TEXT, null)
        .add("description", SerializableDataTypes.TEXT, null)
		.add("elemental_skill", SerializableDataTypes.IDENTIFIER, null)
		.add("elemental_burst", SerializableDataTypes.IDENTIFIER, null);

	protected Identifier elementalBurstPower = null;
	protected Identifier elementalSkillPower = null;

	public boolean hasElementalBurstPower(PlayerEntity player) {
		return this.getElementalBurstPower(player) != null;
	}

	public @Nullable ActiveCooldownPower getElementalBurstPower(PlayerEntity player) {
		if (elementalBurstPower == null) return null;

		try {
			ActiveCooldownPower power = null;

			for (Power power2 : PowerHolderComponent.KEY.get(player).getPowers()) {
				if (power2.getType().getIdentifier().equals(elementalBurstPower)) power = (ActiveCooldownPower) power2;
			}
	
			if (power == null) return null;
	
			return ((IActiveCooldownPower)(Object) power).hasElementalBurst()
				? power
				: null;
		} catch (Exception e) {
			return null;
		}
	}

	public boolean hasElementalSkillPower(PlayerEntity player) {
		return this.getElementalSkillPower(player) != null;
	}

	public @Nullable ActiveCooldownPower getElementalSkillPower(PlayerEntity player) {
		if (elementalSkillPower == null) return null;

		try {
			ActiveCooldownPower power = null;

			for (Power power2 : PowerHolderComponent.KEY.get(player).getPowers()) {
				if (power2.getType().getIdentifier().equals(elementalSkillPower)) power = (ActiveCooldownPower) power2;
			}
	
			if (power == null) return null;
	
			return ((IActiveCooldownPower)(Object) power).hasElementalSkill()
				? power
				: null;
		} catch (Exception e) {
			return null;
		}
	}

	@Inject(
		method = "toData()Lio/github/apace100/calio/data/SerializableData$Instance;",
		at = @At(value = "TAIL", shift = At.Shift.BEFORE),
		remap = false
	)
	private void addToDataInstance(CallbackInfoReturnable<SerializableData.Instance> ci, @Local SerializableData.Instance data) {
		data.set("elemental_burst", elementalBurstPower);
		data.set("elemental_skill", elementalSkillPower);
	}

	@Inject(
		method = "createFromData",
		at = @At(value = "TAIL", shift = At.Shift.BEFORE)
	)
	private static void addOriginData(Identifier id, SerializableData.Instance data, CallbackInfoReturnable<Origin> ci, @Local Origin origin) {
		if (data.isPresent("elemental_burst")) ((OriginMixin)(Object) origin).elementalBurstPower = data.getId("elemental_burst");
		if (data.isPresent("elemental_skill")) ((OriginMixin)(Object) origin).elementalSkillPower = data.getId("elemental_skill");
	}
}
