package io.github.xrickastley.originsgenshin;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import io.github.xrickastley.originsgenshin.util.ClientConfig;

import me.shedaniel.autoconfig.AutoConfig;

public class OriginsGenshinModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> AutoConfig.getConfigScreen(ClientConfig.class, parent).get();
	}

}
