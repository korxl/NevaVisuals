package nevavis.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.render.VignetteDraw;
import nevavis.utils.Colors;

/** Neon Vignette: постоянная цветная рамка по краям экрана. */
public final class EdgeVignette extends Module {
	public EdgeVignette() {
		super("neon_vignette", "Neon Vignette",
				"Permanent colored glow at the screen edges", ModuleCategory.PLAYER);
		addColor("color", "Color", 0xB05CFF);
		addSetting("strength", "Strength", 0.05f, 1f, 0.05f, 0.35f);
		addSetting("size", "Edge size", 0.05f, 0.4f, 0.01f, 0.14f);
		addToggle("cycle", "Rainbow cycle", false);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		int col = flag("cycle") ? Colors.hsv((System.currentTimeMillis() % 8000L) / 8000f, 0.9f, 1f) : color("color");
		int size = Math.max(4, (int) (Math.min(c.getScaledWindowWidth(), c.getScaledWindowHeight()) * get("size")));
		VignetteDraw.edges(c, col, get("strength"), size);
	}
}
