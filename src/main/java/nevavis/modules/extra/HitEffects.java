package nevavis.modules.extra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.hud.CombatTracker;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.render.Lines;
import nevavis.render.VignetteDraw;
import nevavis.utils.Colors;

/** Hit Effects: кольцо у прицела при ударе, золотая вспышка при крите и зелёная искра при получении опыта. */
public final class HitEffects extends Module {
	private int lastSerial, lastXp = -1;
	private long hitAt, critAt, xpAt;

	public HitEffects() {
		super("hit_effects", "Hit Effects", "Ripple on hit, flash on crit, sparkle on XP pickup",
				ModuleCategory.ENTITIES);
		addColor("color", "Hit color", 0xFFFFFF);
		addColor("crit_color", "Crit color", 0xFFC83C);
		addToggle("ripple", "Ripple on hit", true);
		addToggle("crit_flash", "Crit flash", true);
		addToggle("xp", "XP sparkle", true);
		addSetting("size", "Ripple size", 10f, 60f, 1f, 28f);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		if (mc.player == null) {
			return;
		}
		long now = System.currentTimeMillis();
		int serial = CombatTracker.serial();
		if (serial != lastSerial) {
			lastSerial = serial;
			hitAt = now;
			if (CombatTracker.lastCrit()) {
				critAt = now;
			}
		}
		int xp = mc.player.totalExperience;
		if (lastXp >= 0 && xp > lastXp) {
			xpAt = now;
		}
		lastXp = xp;

		int cx = c.getScaledWindowWidth() / 2, cy = c.getScaledWindowHeight() / 2;
		float age = now - hitAt;
		if (flag("ripple") && age < 380f) {
			float t = age / 380f;
			float r = 4f + get("size") * (1f - (1f - t) * (1f - t));
			int col = Colors.withAlpha(CombatTracker.lastCrit() ? color("crit_color") : color("color"), 1f - t);
			int n = Math.max(24, (int) (r * 4));
			for (int i = 0; i < n; i++) {
				double a = Math.PI * 2 * i / n;
				int x = cx + (int) Math.round(Math.cos(a) * r), y = cy + (int) Math.round(Math.sin(a) * r);
				c.fill(x, y, x + 2, y + 2, col);
			}
		}
		float ca = now - critAt;
		if (flag("crit_flash") && ca < 260f) {
			VignetteDraw.edges(c, color("crit_color"), (1f - ca / 260f) * 0.6f,
					Math.max(6, (int) (c.getScaledWindowHeight() * 0.12f)));
		}
		float xa = now - xpAt;
		if (flag("xp") && xa < 700f) {
			float t = xa / 700f;
			int by = c.getScaledWindowHeight() - 34;
			for (int i = 0; i < 9; i++) {
				float px = cx - 70 + i * 17 + (float) Math.sin(i * 2.1 + t * 6) * 3;
				float py = by - t * 18 - (i % 3) * 3;
				int col = Colors.withAlpha(0x7CFF4D, (1f - t) * 0.9f);
				c.fill(Math.round(px), Math.round(py), Math.round(px) + 2, Math.round(py) + 2, col);
			}
		}
	}
}
