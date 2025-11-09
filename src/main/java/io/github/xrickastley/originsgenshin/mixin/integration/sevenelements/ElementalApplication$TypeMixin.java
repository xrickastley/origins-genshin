package io.github.xrickastley.originsgenshin.mixin.integration.sevenelements;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import io.github.xrickastley.originsgenshin.util.EnumUtil;
import io.github.xrickastley.sevenelements.element.ElementalApplication;

@Mixin(value = ElementalApplication.Type.class, remap = false)
public class ElementalApplication$TypeMixin {
	@ModifyArg(
		method = "<clinit>",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/serialization/Codec;xmap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
		),
		index = 0
	)
	private static Function<String, ElementalApplication.Type> useCaseInsensitive(Function<String, ElementalApplication.Type> _to) {
		return EnumUtil.valueOf(ElementalApplication.Type.class);
	}
}
