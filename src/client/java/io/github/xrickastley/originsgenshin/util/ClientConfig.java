package io.github.xrickastley.originsgenshin.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "origins-genshin")
public class ClientConfig implements ConfigData {
	@ConfigEntry.Gui.CollapsibleObject
	public Renderers renderers = new Renderers();

	public static class Renderers {
		public boolean showElementalSkill = true;
		public boolean showElementalBurst = true;
	}
}
