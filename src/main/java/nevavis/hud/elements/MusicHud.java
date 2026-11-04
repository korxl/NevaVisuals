package nevavis.hud.elements;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig.MusicConfig;
import nevavis.hud.HudElement;
import nevavis.music.MusicPlayer;
import nevavis.music.Track;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

/** Панель «сейчас играет»: стили, режим показа (всегда / автоскрытие / пока играет), обложка, эквалайзер, прогресс. */
public final class MusicHud extends HudElement {
	public static final java.util.List<String> MODES = java.util.List.of("Auto-hide", "Always", "Only while playing");

	private final SmoothValue show = new SmoothValue(0f);
	private final float[] shown = new float[8];
	private long lastNs = System.nanoTime();

	public MusicHud() {
		super("music", "Now Playing", 0.5f - 0.1f, 0.012f, true);
	}

	private static boolean wanted(MusicConfig k, MusicPlayer mp, Track t, MinecraftClient mc) {
		return switch (k.hudMode) {
			case "Always" -> true;
			case "Only while playing" -> t != null && mp.isPlaying();
			default -> t != null && mp.isActive()
					&& (System.currentTimeMillis() - mp.changedAt() < k.hideSeconds * 1000f || mc.currentScreen != null);
		};
	}

	@Override
	protected void draw(DrawContext c, MinecraftClient mc) {
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - lastNs) / 1_000_000_000f);
		lastNs = now;

		MusicConfig k = ConfigManager.get().data().music;
		MusicPlayer mp = MusicPlayer.get();
		Track t = mp.current();
		String variant = k.hudVariant;
		int r;
		boolean artist = true, progress = k.showProgress, pill = "Dark Pill".equals(variant);
		float glow = 0.5f;
		switch (variant) {
			case "Minimal" -> { w = 190; h = 26; r = 5; glow = 0f; }
			case "Neon" -> { w = 220; h = 34; r = 9; glow = 1f; }
			case "Compact" -> { w = 160; h = 22; r = 11; artist = false; progress = false; glow = 0.3f; }
			case "Island" -> { w = 204; h = 32; r = 16; glow = 0.15f; }
			case "Dark Pill" -> { w = 210; h = 40; r = 16; glow = 0.1f; progress = false; }
			default -> { w = 220; h = 34; r = 10; }
		}
		show.approach(ghostMode || wanted(k, mp, t, mc) ? 1f : 0f, dt, 10f);
		float s = show.value;
		if (s < 0.02f) {
			return;
		}
		float vis = cfg().visible ? 1f : 0.35f;
		Gfx.setFade(s * vis);
		c.getMatrices().translate(0f, -(1f - s) * 10f, 0f);

		Gfx.glow(c, 0, 0, w, h, r, ThemeManager.accent(), glow);
		switch (variant) {
			case "Glass", "Neon" -> {
				Gfx.glassPanel(c, 0, 0, w, h, r, "Neon".equals(variant) ? 1f : 0.8f);
				if ("Neon".equals(variant)) {
					Gfx.roundedOutline(c, -1, -1, w + 2, h + 2, r + 1, Gfx.accent2(0.6f));
				}
			}
			case "Island", "Dark Pill" -> {
				Gfx.roundedRect(c, 0, 0, w, h, r, 0xF2000000);
				Gfx.roundedOutline(c, 0, 0, w, h, r, Colors.withAlpha(0xFFFFFF, pill ? 0.10f : 0.18f));
			}
			default -> {
				Gfx.roundedRect(c, 0, 0, w, h, r, Colors.withAlpha(ThemeManager.bg(), 0.55f));
				Gfx.roundedOutline(c, 0, 0, w, h, r, Gfx.accent(0.3f));
			}
		}

		int pad = pill ? 7 : 9;
		int tx = pad;
		if (k.showCover) {
			int cs = Math.max(14, h - 2 * pad + (pill ? 2 : 0));
			drawCover(c, pad, (h - cs) / 2, cs, t);
			tx += cs + 8;
		}
		int bars = pill ? 5 : 8, bw = pill ? 3 : 2, gap = pill ? 3 : 2;
		int eqW = bars * (bw + gap) - gap;
		if (k.showEq) {
			float[] b = mp.bands();
			int maxH = h - 2 * pad - 2;
			for (int i = 0; i < 8; i++) {
				shown[i] += (b[i] - shown[i]) * Math.min(1f, dt * 18f);
			}
			int ex = w - pad - eqW;
			for (int i = 0; i < bars; i++) {
				float v = pill ? (shown[i] + shown[Math.min(7, i + 3)]) * 0.5f : shown[i];
				int bh = Math.max(3, 3 + Math.round(v * (maxH - 3)));
				int bx = ex + i * (bw + gap), by = h / 2 - bh / 2;
				if (pill) {
					Gfx.roundedRect(c, bx, by, bw, bh, 1, 0xFFFFFFFF);
				} else {
					Gfx.roundedGradient(c, bx, by, bw, bh, 1, Gfx.accent(0.95f), Gfx.accent2(0.95f));
				}
			}
		}
		int tw = w - tx - pad - (k.showEq ? eqW + 8 : 0);
		String title = t == null ? "Nothing playing" : t.title();
		String line2 = t == null ? "Add tracks in the Music tab" : (mp.isPlaying() ? t.artist() : "Paused - " + t.artist());
		int main = pill ? 0xFFFFFFFF : ThemeManager.text();
		int sub = pill ? 0xFF8A8A8A : ThemeManager.dim();
		if (artist) {
			int ty = (h - 20) / 2 - (progress ? 1 : 0);
			Gfx.text(c, Gfx.fit(title, tw), tx, ty, main);
			Gfx.text(c, Gfx.fit(line2, tw), tx, ty + 11, sub);
		} else {
			Gfx.text(c, Gfx.fit(title, tw), tx, (h - 8) / 2, main);
		}
		long dur = mp.durationMs();
		if (progress && dur > 0) {
			int pw = w - 2 * r;
			Gfx.roundedRect(c, r, h - 4, pw, 2, 1, Colors.withAlpha(ThemeManager.dim(), 0.3f));
			int fw = Math.round(pw * Math.min(1f, mp.positionMs() / (float) dur));
			if (fw > 0) {
				Gfx.roundedGradient(c, r, h - 4, fw, 2, 1, Gfx.accent(1f), Gfx.accent2(1f));
			}
		}
	}

	/** Обложки из файлов не читаем: красивый градиент от названия трека + первая буква. */
	private static void drawCover(DrawContext c, int x, int y, int size, Track t) {
		int hash = t == null ? 7 : t.title().hashCode();
		float h1 = ((hash & 0x7FFFFFFF) % 360) / 360f;
		int a = Colors.hsv(h1, 0.65f, 0.95f) | 0xFF000000;
		int b = Colors.hsv((h1 + 0.18f) % 1f, 0.8f, 0.45f) | 0xFF000000;
		Gfx.roundedGradient(c, x, y, size, size, Math.max(4, size / 5), a, b);
		String ch = t == null || t.title().isEmpty() ? "?" : t.title().substring(0, 1).toUpperCase();
		Gfx.centered(c, ch, x + size / 2, y + (size - 8) / 2, 0xFFFFFFFF);
	}
}
