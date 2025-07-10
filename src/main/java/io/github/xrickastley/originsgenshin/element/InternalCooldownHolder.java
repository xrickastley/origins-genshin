package io.github.xrickastley.originsgenshin.element;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.LivingEntity;

public final class InternalCooldownHolder {
	private final LivingEntity owner;
	private final Map<String, InternalCooldown> cooldowns = new HashMap<>();

	protected InternalCooldownHolder(final LivingEntity owner) {
		this.owner = owner;
	}
	
	public InternalCooldown getInternalCooldown(String tag, InternalCooldownType type) {
		return this.getInternalCooldown(InternalCooldownTag.of(tag), type);
	}

	public InternalCooldown getInternalCooldown(InternalCooldownTag tag, InternalCooldownType type) {
		final String id = InternalCooldown.getIcdIdentifier(tag, type);

		return cooldowns.computeIfAbsent(id, _key -> new InternalCooldown(this, tag, type));
	}

	protected LivingEntity getOwner() {
		return owner;
	}
}
