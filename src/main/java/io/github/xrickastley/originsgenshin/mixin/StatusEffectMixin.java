package io.github.xrickastley.originsgenshin.mixin;

import org.spongepowered.asm.mixin.Mixin;

import io.github.xrickastley.originsgenshin.interfaces.EntityAwareEffect;
import net.minecraft.entity.effect.StatusEffect;

@Mixin(StatusEffect.class)
public class StatusEffectMixin implements EntityAwareEffect {

}
