package nevavis.modules.extra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.render.Gfx;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

/** Hotbar Glide: плавно скользящая рамка выбранного слота хотбара. */
public final class HotbarGlide extends Module {
	private final SmoothValue pos = new SmoothValue(0f);
	private long lastNs = System.nanoTime();
	private boolean init;

	public HotbarGlide() {
		super("hotbar_glide", "Hotbar Glide", "Smooth sliding selector on the hotbar", ModuleCategory.ANIMATIONS);
		addChoice("style", "Style", java.util.List.of("Outline", "Underline", "Fill"), 0);
		addColor("color", "Color", 0x6FD3FF);
		addSetting("speed", "Speed", 4f, 40f, 1f, 18f);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		if (mc.player == null || mc.player.isSpectator()) {
			return;
		}
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - lastNs) / 1_000_000_000f);
		lastNs = now;
		int slot = mc.player.getInventory().selectedSlot;
		if (!init) {
			pos.value = slot;
			pos.target = slot;
			init = true;
		}
		pos.approach(slot, dt, get("speed"));
		int x = Math.round(c.getScaledWindowWidth() / 2f - 91f - 1f + pos.value * 20f);
		int y = c.getScaledWindowHeight() - 23;
		int col = 0xFF000000 | color("color");
		switch ((int) get("style")) {
			case 1 -> Gfx.roundedRect(c, x + 3, y + 21, 18, 2, 1, col);
			case 2 -> Gfx.roundedRect(c, x + 1, y + 1, 22, 22, 3, Colors.withAlpha(col, 0.35f));
			default -> Gfx.roundedOutline(c, x, y, 24, 24, 3, col);
		}
	}
}
