package nevavis.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.render.VignetteDraw;
import nevavis.utils.Colors;

/** Low Health Pulse: пульсирующая красная рамка, когда здоровья мало. */
public final class LowHealthPulse extends Module {
	public LowHealthPulse() {
		super("low_health_pulse", "Low Health Pulse",
				"Pulsing screen edges when your health is low", ModuleCategory.PLAYER);
		addSetting("threshold", "Hearts threshold", 1f, 10f, 1f, 3f);
		addColor("color", "Color", 0xFF2020);
		addToggle("heartbeat", "Heartbeat rhythm", true);
		addToggle("warning", "Show LOW HEALTH text", false);
		addSetting("intensity", "Intensity", 0.1f, 1f, 0.05f, 0.55f);
		addSetting("speed", "Pulse speed", 0.5f, 4f, 0.1f, 1.5f);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		ClientPlayerEntity p = mc.player;
		if (p == null || p.isDead() || p.getHealth() > get("threshold") * 2f) {
			return;
		}
		double t = System.currentTimeMillis() / 1000.0 * get("speed");
		float pulse;
		if (flag("heartbeat")) {
			double ph = t % 1.0;
			pulse = (float) Math.min(1.0, Math.exp(-Math.pow((ph - 0.05) * 14, 2)) + 0.7 * Math.exp(-Math.pow((ph - 0.28) * 14, 2)));
			pulse = 0.25f + 0.75f * pulse;
		} else {
			pulse = 0.55f + 0.45f * (float) Math.sin(t * Math.PI);
		}
		int size = Math.max(6, (int) (Math.min(c.getScaledWindowWidth(), c.getScaledWindowHeight()) * 0.22f));
		VignetteDraw.edges(c, color("color"), pulse * get("intensity"), size);
		if (flag("warning")) {
			String txt = "LOW HEALTH";
			nevavis.render.Gfx.centered(c, txt, c.getScaledWindowWidth() / 2, c.getScaledWindowHeight() - 62,
					Colors.withAlpha(color("color"), 0.4f + 0.6f * pulse));
		}
	}
}
