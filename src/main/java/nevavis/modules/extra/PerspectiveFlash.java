package nevavis.modules.extra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.render.VignetteDraw;

/** Perspective Flash: короткая вспышка по краям экрана при смене вида (F5). */
public final class PerspectiveFlash extends Module {
	private Perspective last;
	private long at;

	public PerspectiveFlash() {
		super("perspective_flash", "Perspective Flash", "Quick edge flash when you switch camera view",
				ModuleCategory.ANIMATIONS);
		addColor("color", "Color", 0xFFFFFF);
		addSetting("duration", "Duration (ms)", 100f, 800f, 25f, 300f);
		addSetting("intensity", "Intensity", 0.1f, 1f, 0.05f, 0.6f);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		Perspective p = mc.options.getPerspective();
		if (last != null && p != last) {
			at = System.currentTimeMillis();
		}
		last = p;
		float age = System.currentTimeMillis() - at;
		float dur = get("duration");
		if (age < dur) {
			int size = Math.max(6, (int) (Math.min(c.getScaledWindowWidth(), c.getScaledWindowHeight()) * 0.2f));
			VignetteDraw.edges(c, color("color"), (1f - age / dur) * get("intensity"), size);
		}
	}
}
