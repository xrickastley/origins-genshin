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
		public int sphereResolution = 16;
		@ConfigEntry.Gui.Tooltip
		public double globalTextScale = 1.0;
		public boolean showDamageText = true;
		public double normalDMGScale = 0.65;
		public double critDMGScale = 1.0;
	}

	public static class Developer {
		@ConfigEntry.Gui.Tooltip
		public boolean displayElementalGauges = false;
		@ConfigEntry.Gui.Tooltip
		public boolean displayGaugeRuler = false;
		@ConfigEntry.Gui.Tooltip
		public boolean genshinDamageLim = false;
		@ConfigEntry.Gui.Tooltip
		public boolean commafyDamage = false;
	}
}
