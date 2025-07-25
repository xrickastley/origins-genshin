package io.github.xrickastley.originsgenshin.util;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "origins-genshin")
public class ClientConfig implements ConfigData {
	@ConfigEntry.Gui.CollapsibleObject
	public Renderers renderers = new Renderers();

	@ConfigEntry.Gui.CollapsibleObject
	public Developer developer = new Developer();

	public static class Renderers {
		public boolean showElementalSkill = true;
		public boolean showElementalBurst = true;
	}

	public static class Developer {
		public boolean displayElementalGauges = false;
		public boolean displayGaugeRuler = false;
	}
}
