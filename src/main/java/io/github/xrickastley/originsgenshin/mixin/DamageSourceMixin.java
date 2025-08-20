package io.github.xrickastley.originsgenshin.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import io.github.xrickastley.originsgenshin.interfaces.IDamageSource;
import net.minecraft.entity.damage.DamageSource;

@Mixin(DamageSource.class)
public class DamageSourceMixin implements IDamageSource {
	@Unique
	private boolean originsgenshin$displayDamage = true;

	@Override
	public void originsgenshin$shouldDisplayDamage(boolean display) {
		this.originsgenshin$displayDamage = display;
	}

	@Override
	public boolean originsgenshin$displayDamage() {
		return originsgenshin$displayDamage;
	}
}
