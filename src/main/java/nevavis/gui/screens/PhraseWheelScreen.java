package nevavis.gui.screens;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import nevavis.config.ConfigManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;

import java.util.ArrayList;
import java.util.List;

/**
 * Колесо фраз: удерживайте клавишу, наведите мышь на фразу и отпустите клавишу (или кликните) — фраза уйдёт в чат.
 * Фразы редактируются в Settings → Phrases. Esc — закрыть без отправки.
 */
public final class PhraseWheelScreen extends Screen {
	private final KeyBinding key;
	private final List<String> phrases = new ArrayList<>();
	private int hovered = -1;

	public PhraseWheelScreen(KeyBinding key) {
		super(Text.literal("NevaVis Phrase Wheel"));
		this.key = key;
		List<String> src = ConfigManager.get().data().phrases;
		for (int i = 0; i < 8; i++) {
			phrases.add(i < src.size() ? src.get(i) : "");
		}
	}

	private double angle(int i) {
		return Math.PI * 2 * i / 8.0;
	}

	private int boxW(int i) {
		return Math.max(84, Gfx.width(phrases.get(i)) + 28);
	}

	private int cx() {
		return width / 2;
	}

	private int cy() {
		return height / 2 - 6;
	}

	private int px(int i) {
		return cx() + (int) Math.round(Math.sin(angle(i)) * Math.min(width * 0.2, 190));
	}

	private int py(int i) {
		return cy() - (int) Math.round(Math.cos(angle(i)) * Math.min(height * 0.3, 120));
	}

	private int pick(double mx, double my) {
		double dx = mx - cx(), dy = my - cy();
		if (Math.hypot(dx, dy) < 26) {
			return -1;
		}
		double a = Math.atan2(dx, -dy);
		if (a < 0) {
			a += Math.PI * 2;
		}
		int idx = (int) Math.round(a / (Math.PI / 4)) % 8;
		return phrases.get(idx).isBlank() ? -1 : idx;
	}

	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
	}

	@Override
	public void render(DrawContext c, int mx, int my, float delta) {
		Gfx.setFade(1f);
		Gfx.setTransform(width / 2f, height / 2f, 1f, width, height);
		c.fill(0, 0, width, height, 0x66000000);
		hovered = pick(mx, my);
		for (int i = 0; i < 8; i++) {
			if (phrases.get(i).isBlank()) {
				continue;
			}
			int bw = boxW(i), bh = 22;
			int x = px(i) - bw / 2, y = py(i) - bh / 2;
			boolean on = i == hovered;
			Gfx.roundedRect(c, x, y, bw, bh, 8, on ? Gfx.accent(0.85f) : Colors.withAlpha(ThemeManager.bg(), 0.82f));
			Gfx.roundedOutline(c, x, y, bw, bh, 8, on ? Colors.withAlpha(0xFFFFFF, 0.9f) : Gfx.accent(0.45f));
			Gfx.centered(c, phrases.get(i), px(i), y + (bh - 8) / 2, ThemeManager.text());
		}
		Gfx.centered(c, "Phrase Wheel", cx(), cy() - 4, ThemeManager.text());
		Gfx.centered(c, "release the key to send", cx(), cy() + 8, ThemeManager.dim());
	}

	private void sendAndClose() {
		int idx = hovered;
		close();
		if (idx >= 0 && client != null && client.player != null) {
			String msg = phrases.get(idx).trim();
			if (msg.startsWith("/") && msg.length() > 1) {
				client.player.networkHandler.sendChatCommand(msg.substring(1));
			} else if (!msg.isEmpty()) {
				client.player.networkHandler.sendChatMessage(msg);
			}
		}
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		if (KeyBindingHelper.getBoundKeyOf(key).getCode() == keyCode) {
			sendAndClose();
			return true;
		}
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0) {
			hovered = pick(mx, my);
			sendAndClose();
			return true;
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
