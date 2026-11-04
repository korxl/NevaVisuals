package nevavis.modules.extra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import nevavis.hud.WorldParticles;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.render.Gfx;
import nevavis.utils.Colors;

/** Totem Pop: своя анимация при срабатывании тотема (лучи, кольцо, текст, всплеск частиц). */
public final class TotemPop extends Module {
	private int lastCount = -1;
	private long at;

	public TotemPop() {
		super("totem_pop", "Totem Pop", "Extra animation when a totem saves you", ModuleCategory.ANIMATIONS);
		addColor("color", "Color", 0xFFD24A);
		addSetting("duration", "Duration (ms)", 400f, 2500f, 50f, 1100f);
		addToggle("text", "Show text", true);
	}

	private static int totems(MinecraftClient mc) {
		var inv = mc.player.getInventory();
		int n = 0;
		for (int i = 0; i < inv.size(); i++) {
			if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
				n += inv.getStack(i).getCount();
			}
		}
		return n;
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		if (mc.player == null) {
			return;
		}
		int n = totems(mc);
		if (lastCount >= 0 && n < lastCount && mc.player.getHealth() <= 2.5f && mc.player.isAlive()) {
			at = System.currentTimeMillis();
			WorldParticles.burst(mc.player.getX(), mc.player.getY() + 1.0, mc.player.getZ(), color("color"), 40);
		}
		lastCount = n;
		float dur = get("duration");
		float age = System.currentTimeMillis() - at;
		if (age >= dur) {
			return;
		}
		float t = age / dur, fade = 1f - t;
		int cx = c.getScaledWindowWidth() / 2, cy = c.getScaledWindowHeight() / 2;
		int rgb = color("color");
		c.fill(0, 0, c.getScaledWindowWidth(), c.getScaledWindowHeight(), Colors.withAlpha(rgb, 0.22f * fade * fade));
		float radius = 20f + 130f * (1f - fade * fade);
		for (int i = 0; i < 48; i++) {
			double a = Math.PI * 2 * i / 48;
			int x = cx + (int) Math.round(Math.cos(a) * radius), y = cy + (int) Math.round(Math.sin(a) * radius);
			c.fill(x - 1, y - 1, x + 2, y + 2, Colors.withAlpha(rgb, fade));
		}
		for (int i = 0; i < 12; i++) {
			double a = Math.PI * 2 * i / 12 + t;
			float r0 = radius * 0.45f, r1 = radius * 0.9f;
			Lines2.line(c, cx + (float) Math.cos(a) * r0, cy + (float) Math.sin(a) * r0,
					cx + (float) Math.cos(a) * r1, cy + (float) Math.sin(a) * r1, Colors.withAlpha(rgb, fade * 0.8f));
		}
		if (flag("text")) {
			Gfx.centered(c, "TOTEM POPPED", cx, cy - 4, Colors.withAlpha(0xFFFFFF, Math.min(1f, fade * 2f)));
		}
	}

	private static final class Lines2 {
		static void line(DrawContext c, float x0, float y0, float x1, float y1, int col) {
			nevavis.render.Lines.line(c, x0, y0, x1, y1, 1, tt -> col);
		}
	}
}
