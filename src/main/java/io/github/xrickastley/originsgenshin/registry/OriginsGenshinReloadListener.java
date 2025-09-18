package io.github.xrickastley.originsgenshin.registry;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import io.github.apace100.calio.data.SerializableDataType;
import io.github.xrickastley.originsgenshin.OriginsGenshin;
import io.github.xrickastley.originsgenshin.interfaces.SimpleRegistryAccess;
import io.github.xrickastley.originsgenshin.util.ClassInstanceUtil;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class OriginsGenshinReloadListener implements SimpleSynchronousResourceReloadListener {
	public static final OriginsGenshinReloadListener INSTANCE = new OriginsGenshinReloadListener();

	private static final Logger LOGGER = OriginsGenshin.sublogger(OriginsGenshinReloadListener.class);
	private static final List<ReloadableRegistry<?, ?>> REGISTRIES = new ArrayList<>();
	private static final Map<Registry<?>, List<Consumer<Registry<?>>>> BEFORE_LOAD_LISTENERS = new HashMap<>();
	private static final Map<Registry<?>, List<Consumer<Registry<?>>>> AFTER_LOAD_LISTENERS = new HashMap<>();

	private OriginsGenshinReloadListener() {}

	public static <D, T> void addReloadableRegistry(ReloadableRegistry<D, T> reloadableRegistry) {
		REGISTRIES.add(reloadableRegistry);
	}

	public static boolean hasReloadableRegistry(Registry<?> registry) {
		return REGISTRIES.stream().anyMatch(r -> r.getRegistry().equals(registry));
	}

	public static <T> void addBeforeLoadListener(Registry<T> registry, Consumer<Registry<T>> listener) {
		final List<Consumer<Registry<T>>> listeners = ClassInstanceUtil.cast(BEFORE_LOAD_LISTENERS.getOrDefault(registry, new ArrayList<>()));

		listeners.add(listener);

		BEFORE_LOAD_LISTENERS.put(registry, ClassInstanceUtil.cast(listeners));
	}

	public static <T> void addAfterLoadListener(Registry<T> registry, Consumer<Registry<T>> listener) {
		final List<Consumer<Registry<T>>> listeners = ClassInstanceUtil.cast(AFTER_LOAD_LISTENERS.getOrDefault(registry, new ArrayList<>()));

		listeners.add(listener);

		AFTER_LOAD_LISTENERS.put(registry, ClassInstanceUtil.cast(listeners));
	}

	@Override
	public Identifier getFabricId() {
		return OriginsGenshin.identifier("reload_listener");
	}

	@Override
	public void reload(ResourceManager manager) {
		for (final ReloadableRegistry<?, ?> registry : REGISTRIES) {
			registry.clearRegistry();

			this.callBeforeLoadListeners(registry.getRegistry());

			final Set<Identifier> defaultKeys = registry.getRegistry().getKeys().stream().map(RegistryKey::getValue).collect(Collectors.toSet());

			for (Identifier id : manager.findResources(registry.getRegistry().getKey().getValue().getPath(), id -> id.toString().endsWith(".json")).keySet()) {
				try (final InputStream stream = manager.getResourceOrThrow(id).getInputStream()) {
					reloadOrLoad(registry, id, parseJson(stream), defaultKeys);
				} catch (Exception e) {
					LOGGER.error("An error occured while trying to load the entry at {}: ", id, e);
				}
			}

			this.callAfterLoadListeners(registry.getRegistry());
		}
	}

	private <D, T> void reloadOrLoad(ReloadableRegistry<D, T> reloadableRegistry, Identifier id, JsonElement json, Set<Identifier> defaultKeys) {
		if (!(reloadableRegistry.getRegistry() instanceof final SimpleRegistry<T> registry)) return;

		final Identifier registryId = this.getRegistryId(id, registry);
		final T value = reloadableRegistry.parse(registryId, reloadableRegistry.read(json));

		// Disable overwriting pre-loaded keys.
		if (defaultKeys.contains(registryId)) {
			LOGGER.warn("The entry at {} attempted to overwrite the preloaded entry {}, ignoring!", id, registryId);

			return;
		}

		if (registry.containsId(registryId)) {
			final T prev = registry.get(registryId);
			final int rawId = registry.getRawId(prev);
			final RegistryKey<T> key = registry.getKey(prev).get();
			final Lifecycle lifecycle = registry.getEntryLifecycle(prev);

			registry.set(rawId, key, value, lifecycle);
		} else {
			Registry.register(registry, registryId, value);
		}
	}

	private JsonElement parseJson(InputStream stream) throws IOException {
		try (JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
			return JsonParser.parseReader(reader);
		} catch (IOException e) {
			throw e;
		}
	}

	private Identifier getRegistryId(Identifier id, Registry<?> registry) {
		String path = id.getPath().split(registry.getKey().getValue().getPath() + "/")[1];
		path = path.substring(0, path.lastIndexOf("."));

		return id.withPath(path);
	}

	private void callBeforeLoadListeners(Registry<?> registry) {
		BEFORE_LOAD_LISTENERS
			.getOrDefault(registry, new ArrayList<>())
			.forEach(c -> c.accept(registry));
	}

	private void callAfterLoadListeners(Registry<?> registry) {
		AFTER_LOAD_LISTENERS
			.getOrDefault(registry, new ArrayList<>())
			.forEach(c -> c.accept(registry));
	}

	public static class ReloadableRegistry<D, T> {
		private final Registry<T> registry;
		private final BiFunction<D, Identifier, T> parser;
		private final @Nullable Codec<D> codec;
		private final @Nullable SerializableDataType<D> dataType;

		private ReloadableRegistry(Registry<T> registry, BiFunction<D, Identifier, T> parser, Codec<D> codec, SerializableDataType<D> dataType) {
			this.registry = registry;
			this.parser = parser;
			this.codec = codec;
			this.dataType = dataType;
		}

		public static <D, T> ReloadableRegistry<D, T> ofCodec(Registry<T> registry, BiFunction<D, Identifier, T> parser, Codec<D> codec) {
			return new ReloadableRegistry<>(registry, parser, codec, null);
		}

		public static <D, T> ReloadableRegistry<D, T> ofDataType(Registry<T> registry, BiFunction<D, Identifier, T> parser, SerializableDataType<D> dataType) {
			return new ReloadableRegistry<>(registry, parser, null, dataType);
		}

		protected Registry<T> getRegistry() {
			return registry;
		}

		protected void clearRegistry() {
			if (!(registry instanceof SimpleRegistry)) return;

			((SimpleRegistryAccess) registry).originsgenshin$clearEntries();
		}

		protected D read(JsonElement json) {
			return codec != null
				? codec.decode(JsonOps.INSTANCE, json).getOrThrow(false, s -> {}).getFirst()
				: dataType.read(json);
		}

		protected T parse(Identifier registryId, D builder) {
			return parser.apply(builder, registryId);
		}
	}
}
