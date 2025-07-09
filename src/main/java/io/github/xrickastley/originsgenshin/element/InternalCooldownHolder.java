package io.github.xrickastley.originsgenshin.element;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public final class InternalCooldownHolder {
	private final LivingEntity owner;
	private final Map<Pair<String, Identifier>, InternalCooldown> cooldowns = new HashMap<>();

	protected InternalCooldownHolder(final LivingEntity owner) {
		this.owner = owner;
	}
	
	public InternalCooldown getInternalCooldown(String tag, InternalCooldownType type) {
		return this.getInternalCooldown(InternalCooldownTag.of(tag), type);
	}

	public InternalCooldown getInternalCooldown(InternalCooldownTag tag, InternalCooldownType type) {
		final Pair<String, Identifier> id = InternalCooldown.getIcdIdentifier(tag, type);

		return cooldowns.computeIfAbsent(id, _key -> new InternalCooldown(this, tag, type));
	}

	protected LivingEntity getOwner() {
		return owner;
	}
}
