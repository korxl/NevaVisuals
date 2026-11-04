package nevavis.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig;
import nevavis.gui.screens.HudEditorScreen;
import nevavis.modules.ModuleManager;
import nevavis.music.MusicPlayer;
import nevavis.music.Track;
import nevavis.notifications.NotificationManager;
import nevavis.render.CrosshairRenderer;
import nevavis.render.Gfx;
import nevavis.render.Projector;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

import java.util.List;

/** Рисует всё поверх игры: эффекты модулей, элементы HUD, прицел, панель музыки, уведомления. */
public final class HudRenderer {
	public static final List<String> MUSIC_VARIANTS = List.of("Glass", "Minimal", "Neon", "Compact", "Island", "Dark Pill");
	private static final int MUSIC_SHOW_MS = 5000;

	private static final SmoothValue MUSIC_SHOW = new SmoothValue(0f);
	private static final float[] SHOWN_BANDS = new float[8];
	private static long lastNs = System.nanoTime();

	private HudRenderer() {
	}

	public static void render(DrawContext c, float tickDelta) {
		MinecraftClient mc = MinecraftClient.getInstance();
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - lastNs) / 1_000_000_000f);
		lastNs = now;

		Gfx.setFade(1f);
		Gfx.setHudMode(true);
		if (!mc.options.hudHidden && mc.player != null) {
			ClickTracker.poll(mc);
			ModuleManager.hud(c, mc, tickDelta);
			if (!(mc.currentScreen instanceof HudEditorScreen)) {
				HudManager.render(c, mc);
			}
			drawCrosshair(c, mc);
			SkyLyrics.render(c, mc, dt);
			WorldParticles.render(c, mc, tickDelta);
		}
		NotificationManager.render(c);
		Gfx.setFade(1f);
		Gfx.setHudMode(false);
		Projector.endFrame();
	}

	private static void drawCrosshair(DrawContext c, MinecraftClient mc) {
		NevaVisConfig.CrosshairConfig k = ConfigManager.get().data().crosshair;
		if (!k.enabled || mc.currentScreen != null || !mc.options.getPerspective().isFirstPerson()) {
			return;
		}
		float t = (System.currentTimeMillis() % 100000L) / 1000f;
		float dyn = Math.min(1f, SessionStats.speed() / 6f);
		CrosshairRenderer.draw(c, c.getScaledWindowWidth() / 2, c.getScaledWindowHeight() / 2, k, t, dyn);
	}
}
