package io.github.xrickastley.originsgenshin.integration;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Pair;

public interface ModIntegration {
	static final List<Pair<String, List<String>>> INTEGRATIONS = new ArrayList<>();

	public static void loadIntegrations() {
		final FabricLoader loader = FabricLoader.getInstance();

		for (final Pair<String, List<String>> integration : ModIntegration.INTEGRATIONS) {
			try {
				if (!integration.getRight().stream().allMatch(loader::isModLoaded)) continue;

				final Class<?> clazz = Class.forName(integration.getLeft(), false, ModIntegration.class.getClassLoader());

				if (!ModIntegration.class.isAssignableFrom(clazz))
					throw new ClassCastException("class " + integration.getLeft() + " cannot be cast to class " + ModIntegration.class.getName());

				((ModIntegration) clazz.getDeclaredConstructor().newInstance())
					.onIntegrationInitialize();
			} catch (Exception e) {
				final RuntimeException e2 = new RuntimeException("An exception occured while trying to initialize the integration: " + integration.getLeft());
				e2.addSuppressed(e);

				throw e2;
			}
		}
	}

	public static void registerIntegration(String integrationClass, String... requiredMods) {
		ModIntegration.INTEGRATIONS.add(new Pair<>(integrationClass, List.of(requiredMods)));
	}

	public void onIntegrationInitialize();
}
