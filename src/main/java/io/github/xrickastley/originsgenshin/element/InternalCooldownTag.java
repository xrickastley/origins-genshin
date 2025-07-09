package io.github.xrickastley.originsgenshin.element;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

public final class InternalCooldownTag {
	private static final Map<String, InternalCooldownTag> INSTANCES = new ConcurrentHashMap<>();
	public static final InternalCooldownTag NONE = new InternalCooldownTag(null);

	private final @Nullable String tag;

	private InternalCooldownTag(final @Nullable String tag) {
		this.tag = tag;

		if (tag != null) InternalCooldownTag.INSTANCES.put(tag, this);
	}

	public static InternalCooldownTag of(final @Nullable String tag) {
		return tag == null
			? InternalCooldownTag.NONE
			: InternalCooldownTag.tag(tag);
	}

	public static InternalCooldownTag none() {
		return InternalCooldownTag.NONE;
	}

	public static InternalCooldownTag tag(final String tag) {
		return INSTANCES.containsKey(tag)
			? INSTANCES.get(tag)
			: new InternalCooldownTag(tag);
	}

	public @Nullable String getTag() {
		return this.tag;
	}
}
