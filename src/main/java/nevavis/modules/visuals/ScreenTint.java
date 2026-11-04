package nevavis.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.utils.Colors;

/** Screen Tint: лёгкий цветной оттенок всего экрана. */
public final class ScreenTint extends Module {
	public ScreenTint() {
		super("screen_tint", "Screen Tint",
				"Soft colored tint over the whole screen", ModuleCategory.PLAYER);
		addColor("color", "Color", 0x8C5CFF);
		addSetting("strength", "Strength", 0.02f, 0.45f, 0.01f, 0.12f);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		int a = (int) (255f * get("strength"));
		if (a < 2) {
			return;
		}
		int rgb = color("color");
		c.fill(0, 0, c.getScaledWindowWidth(), c.getScaledWindowHeight(), (a << 24) | rgb);
	}
}
