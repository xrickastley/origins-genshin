package io.github.xrickastley.originsgenshin.mixin.integration.sevenelements;

import com.llamalad7.mixinextras.sugar.Local;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.xrickastley.originsgenshin.power.ElementalInfusionPower;
import io.github.xrickastley.sevenelements.component.ElementComponent;
import io.github.xrickastley.sevenelements.element.ElementalDamageSource;
import io.github.xrickastley.sevenelements.util.Functions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

@Pseudo
@Mixin(value = ElementComponent.class, remap = false)
public interface ElementComponentMixin {
	@Shadow
	private static <T> @Nullable T get(Optional<T> optional) {
		throw new AssertionError();
	}

	@ModifyArg(
		method = "applyElementalInfusions",
		at = @At(
			value = "INVOKE",
			target = "Lio/github/xrickastley/sevenelements/util/JavaScriptUtil;nullishCoalesingFn([Ljava/util/function/Supplier;)Ljava/lang/Object;"
		),
		index = 0
	)
	private static Supplier<ElementalDamageSource>[] addInfusionPowers(Supplier<ElementalDamageSource>[] suppliers, @Local(argsOnly = true) DamageSource source, @Local(argsOnly = true) LivingEntity target) {
		return ArrayUtils.insert(
			0,
			suppliers,
			Functions.map(Functions.supplier(ElementComponentMixin::attemptInfusionPowerInfusions, source, target), ElementComponentMixin::get)
		);
	}

	@Unique
	private static Optional<ElementalDamageSource> attemptInfusionPowerInfusions(DamageSource source, LivingEntity target) {
		return PowerHolderComponent
			.getPowers(source.getAttacker(), ElementalInfusionPower.class)
			.stream()
			.filter(ElementalInfusionPower::isActive)
			.sorted()
			.findFirst()
			.map(Functions.withArgument(ElementalInfusionPower::infuse, source, target));
	}
}
