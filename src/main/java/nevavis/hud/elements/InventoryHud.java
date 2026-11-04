package nevavis.hud.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import nevavis.hud.HudElement;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

/** Inventory HUD: основной инвентарь (3 ряда) на экране; двигается в редакторе или в чате. */
public final class InventoryHud extends HudElement {
	public InventoryHud() {
		super("inventory", "Inventory", 0.62f, 0.70f, false);
	}

	@Override
	protected void draw(DrawContext c, MinecraftClient mc) {
		w = 9 * 18 + 6;
		h = 3 * 18 + 6;
		background(c, w, h);
		if (mc.player == null) {
			return;
		}
		var inv = mc.player.getInventory();
		int empty = 0;
		for (int i = 0; i < 27; i++) {
			ItemStack s = inv.getStack(9 + i);
			int x = 3 + (i % 9) * 18 + 1, y = 3 + (i / 9) * 18 + 1;
			if (s.isEmpty()) {
				empty++;
				Gfx.roundedRect(c, x, y, 16, 16, 3, nevavis.utils.Colors.withAlpha(ThemeManager.dim(), 0.12f));
				continue;
			}
			c.drawItem(s, x, y);
			c.drawStackOverlay(Gfx.tr(), s, x, y);
		}
	}
}
