package io.github.xrickastley.originsgenshin.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import io.github.apace100.apoli.power.PowerTypes;
import io.github.xrickastley.originsgenshin.registry.OriginsGenshinReloadListener;

import net.minecraft.util.Identifier;

@Pseudo
@Mixin(value = PowerTypes.class, remap = false)
public class PowerTypesMixin {
	@ModifyReturnValue(
		method = "getFabricDependencies",
		at = @At("RETURN")
	)
	private Collection<Identifier> addOriginsGenshinDependency(Collection<Identifier> original) {
		final List<Identifier> extended = new ArrayList<>(original);

		extended.add(OriginsGenshinReloadListener.INSTANCE.getFabricId());

		return Collections.unmodifiableList(extended);
	}
}
