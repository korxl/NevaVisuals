package nevavis.modules.extra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import nevavis.hud.CombatTracker;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.render.Lines;
import nevavis.render.Projector;
import nevavis.utils.Colors;

/**
 * Target ESP: рамка/кольцо вокруг цели, которую вы бьёте. Рисуется ТОЛЬКО пока цель в прямой видимости
 * (LivingEntity.canSee) — сквозь стены ничего не показывает.
 */
public final class TargetEsp extends Module {
	public TargetEsp() {
		super("target_esp", "Target ESP", "Marks the player or mob you are fighting (only while visible)",
				ModuleCategory.ENTITIES);
		addChoice("style", "Style", java.util.List.of("Brackets", "Ring", "Diamond"), 1);
		addColor("color", "Color", 0xFF4D4D);
		addToggle("health_color", "Color by health", true);
		addSetting("size", "Size", 0.6f, 1.6f, 0.05f, 1f);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		LivingEntity t = CombatTracker.target();
		if (t == null || mc.player == null || !t.isAlive() || !mc.player.canSee(t) || !Projector.begin(mc, td, c)) {
			return;
		}
		Vec3d pos = t.getLerpedPos(td);
		float[] foot = new float[3], head = new float[3];
		double h = t.getHeight();
		if (!Projector.project(pos.x, pos.y, pos.z, foot) || !Projector.project(pos.x, pos.y + h, pos.z, head)) {
			return;
		}
		float hp = Math.max(0f, Math.min(1f, t.getHealth() / Math.max(1f, t.getMaxHealth())));
		int rgb = flag("health_color") ? Colors.lerp(0xEF4444, 0x22C55E, hp) : color("color");
		float time = (System.currentTimeMillis() % 100000L) / 1000f;
		float hgt = Math.abs(foot[1] - head[1]) * get("size");
		float cx = (foot[0] + head[0]) / 2f, cy = (foot[1] + head[1]) / 2f;
		float half = hgt / 2f, hw = Math.max(6f, hgt * 0.32f);
		Lines.Colorer col = u -> Colors.withAlpha(rgb, 0.95f);
		switch ((int) get("style")) {
			case 0 -> {
				float l = Math.max(4f, hgt * 0.18f);
				float[][] corners = {{-hw, -half}, {hw, -half}, {hw, half}, {-hw, half}};
				int[][] dir = {{1, 1}, {-1, 1}, {-1, -1}, {1, -1}};
				for (int i = 0; i < 4; i++) {
					float x = cx + corners[i][0], y = cy + corners[i][1];
					Lines.line(c, x, y, x + dir[i][0] * l, y, 1, col);
					Lines.line(c, x, y, x, y + dir[i][1] * l, 1, col);
				}
			}
			case 1 -> {
				float rx = hw * 1.2f, ry = rx * 0.35f;
				float fy = foot[1];
				int n = 40;
				for (int i = 0; i < n; i++) {
					double a0 = Math.PI * 2 * i / n + time, a1 = Math.PI * 2 * (i + 1) / n + time;
					if (i % 4 == 3) {
						continue;
					}
					Lines.line(c, (float) (foot[0] + Math.cos(a0) * rx), (float) (fy + Math.sin(a0) * ry),
							(float) (foot[0] + Math.cos(a1) * rx), (float) (fy + Math.sin(a1) * ry), 1, col);
				}
			}
			default -> {
				float y = head[1] - 10f - (float) Math.sin(time * 3f) * 2f;
				float s = Math.max(4f, hgt * 0.1f);
				Lines.line(c, head[0], y + s, head[0] + s, y, 1, col);
				Lines.line(c, head[0] + s, y, head[0], y - s, 1, col);
				Lines.line(c, head[0], y - s, head[0] - s, y, 1, col);
				Lines.line(c, head[0] - s, y, head[0], y + s, 1, col);
			}
		}
	}
}
