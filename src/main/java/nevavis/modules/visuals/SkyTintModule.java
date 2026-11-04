package nevavis.modules.visuals;

import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;

/** Custom Sky / Custom Clouds: свой цвет неба или облаков (миксин ClientWorld), опционально радужная смена. */
public final class SkyTintModule extends Module {
	public SkyTintModule(String id, String name, String description, int defColor) {
		super(id, name, description, ModuleCategory.WORLD);
		addColor("color", "Color", defColor);
		addSetting("strength", "Strength", 0.05f, 1f, 0.05f, 0.7f);
		addToggle("keep_light", "Keep day / night brightness", true);
		addToggle("rainbow", "Rainbow cycle", false);
		addSetting("speed", "Cycle speed", 0.1f, 3f, 0.1f, 1f);
	}
}
