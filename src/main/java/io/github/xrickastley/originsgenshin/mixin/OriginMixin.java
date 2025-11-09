package io.github.xrickastley.originsgenshin.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActiveCooldownPower;
import io.github.apace100.apoli.power.Power;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.origins.origin.Origin;
import io.github.xrickastley.originsgenshin.interfaces.IActiveCooldownPower;
import io.github.xrickastley.originsgenshin.interfaces.IOrigin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

@Pseudo
@Mixin(value = Origin.class, remap = false)
public class OriginMixin implements IOrigin {
	@Mutable
	@Final
	@Shadow
	public static SerializableData DATA;

	protected Identifier originsgenshin$elementalBurstPower = null;
	protected Identifier originsgenshin$elementalSkillPower = null;

	public boolean originsgenshin$hasElementalBurstPower(PlayerEntity player) {
		return this.originsgenshin$getElementalBurstPower(player) != null;
	}

	public @Nullable ActiveCooldownPower originsgenshin$getElementalBurstPower(PlayerEntity player) {
		if (originsgenshin$elementalBurstPower == null) return null;

		try {
			ActiveCooldownPower power = null;

			for (Power power2 : PowerHolderComponent.KEY.get(player).getPowers()) {
				if (power2.getType().getIdentifier().equals(originsgenshin$elementalBurstPower)) power = (ActiveCooldownPower) power2;
			}

			if (power == null) return null;

			return ((IActiveCooldownPower)(Object) power).originsgenshin$hasElementalBurst()
				? power
				: null;
		} catch (Exception e) {
			return null;
		}
	}

	public boolean originsgenshin$hasElementalSkillPower(PlayerEntity player) {
		return this.originsgenshin$getElementalSkillPower(player) != null;
	}

	public @Nullable ActiveCooldownPower originsgenshin$getElementalSkillPower(PlayerEntity player) {
		if (originsgenshin$elementalSkillPower == null) return null;

		try {
			ActiveCooldownPower power = null;

			for (Power power2 : PowerHolderComponent.KEY.get(player).getPowers()) {
				if (power2.getType().getIdentifier().equals(originsgenshin$elementalSkillPower)) power = (ActiveCooldownPower) power2;
			}

			if (power == null) return null;

			return ((IActiveCooldownPower)(Object) power).originsgenshin$hasElementalSkill()
				? power
				: null;
		} catch (Exception e) {
			return null;
		}
	}

	@Inject(
		method = "write",
		at = @At(
			value = "INVOKE",
			target = "Lio/github/apace100/calio/data/SerializableData;write(Lnet/minecraft/network/PacketByteBuf;Lio/github/apace100/calio/data/SerializableData$Instance;)V",
			shift = At.Shift.BEFORE
		),
		remap = false
	)
	private void addToDataInstance(PacketByteBuf buf, CallbackInfo ci, @Local SerializableData.Instance data) {
		data.set("elemental_burst", originsgenshin$elementalBurstPower);
		data.set("elemental_skill", originsgenshin$elementalSkillPower);
	}

	@Inject(
		method = "createFromData",
		at = @At(value = "TAIL", shift = At.Shift.BEFORE)
	)
	private static void addOriginData(Identifier id, SerializableData.Instance data, CallbackInfoReturnable<Origin> ci, @Local Origin origin) {
		if (data.isPresent("elemental_burst")) ((OriginMixin)(Object) origin).originsgenshin$elementalBurstPower = data.getId("elemental_burst");
		if (data.isPresent("elemental_skill")) ((OriginMixin)(Object) origin).originsgenshin$elementalSkillPower = data.getId("elemental_skill");
	}

	static {
		DATA.add("elemental_skill", SerializableDataTypes.IDENTIFIER, null)
			.add("elemental_burst", SerializableDataTypes.IDENTIFIER, null);
	}
}
