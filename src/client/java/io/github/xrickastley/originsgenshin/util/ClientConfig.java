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
		@ConfigEntry.Gui.Tooltip
		@ConfigEntry.BoundedDiscrete(min = 4, max = 48)
		public int sphereResolution = 4;
	}

	public static class Developer {
		@ConfigEntry.Gui.Tooltip
		public boolean displayElementalGauges = false;
		@ConfigEntry.Gui.Tooltip
		public boolean displayGaugeRuler = false;
	}
}
