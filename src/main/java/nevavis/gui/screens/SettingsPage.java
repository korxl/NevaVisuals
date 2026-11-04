package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig;
import nevavis.gui.components.Button;
import nevavis.gui.components.KeybindSelector;
import nevavis.gui.components.SearchBar;
import nevavis.gui.components.Slider;
import nevavis.gui.components.Tabs;
import nevavis.gui.components.Toggle;
import nevavis.gui.components.UiComponent;
import nevavis.keybinds.KeybindManager;
import nevavis.music.MusicPlayer;
import nevavis.presence.PresenceManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

import java.util.ArrayList;
import java.util.List;

/** Настройки: интерфейс, онлайн, фразы колеса, клавиши. */
public final class SettingsPage extends Page {
	private final Tabs tabs;
	private int tab;
	private final List<UiComponent> interfaceItems = new ArrayList<>();
	private final List<UiComponent> onlineItems = new ArrayList<>();
	private final List<SearchBar> phraseBars = new ArrayList<>();
	private final List<KeybindSelector> keys = new ArrayList<>();
	private final SearchBar url = new SearchBar("https://your-worker.workers.dev", 96);
	private final Button reset;

	public SettingsPage() {
		NevaVisConfig cfg = ConfigManager.get().data();
		tabs = new Tabs(List.of("Interface", "Online", "Phrases", "Keybinds"), i -> {
			tab = i;
			rebuild();
			scroll.reset();
		});
		interfaceItems.add(new Slider("GUI Scale", 0.6f, 1.5f, 0.05f, () -> cfg.guiScale, v -> cfg.guiScale = v, "%.2fx"));
		interfaceItems.add(new Slider("Animation Speed", 0.25f, 3f, 0.05f, () -> cfg.animationSpeed, v -> cfg.animationSpeed = v, "%.2fx"));
		interfaceItems.add(new Slider("Menu Glow", 0f, 2f, 0.05f, () -> cfg.glowIntensity, v -> cfg.glowIntensity = v, "%.2f"));
		interfaceItems.add(new Slider("Transparency", 0.3f, 1f, 0.05f, () -> cfg.transparency, v -> cfg.transparency = v, "%.2f"));
		interfaceItems.add(new Slider("Corner Radius", 0f, 14f, 1f, () -> (float) cfg.cornerRadius, v -> cfg.cornerRadius = v.intValue(), "%.0f px"));
		interfaceItems.add(new Slider("HUD Scale", 0.5f, 2f, 0.05f, () -> cfg.hudScale, v -> cfg.hudScale = v, "%.2fx"));
		interfaceItems.add(new Slider("Notification time", 1f, 8f, 0.5f, () -> cfg.notificationSeconds, v -> cfg.notificationSeconds = v, "%.1f s"));
		interfaceItems.add(new Slider("Music volume", 0f, 1f, 0.05f, () -> MusicPlayer.get().volume(), v -> MusicPlayer.get().setVolume(v), "%.2f"));
		interfaceItems.add(new Toggle("GUI animations", () -> cfg.guiAnimations, v -> cfg.guiAnimations = v));
		interfaceItems.add(new Toggle("Menu glow effects", () -> cfg.glowEnabled, v -> cfg.glowEnabled = v));
		interfaceItems.add(new Toggle("Low performance mode", () -> cfg.lowPerformance, v -> cfg.lowPerformance = v));
		interfaceItems.add(new Toggle("FPS optimization", () -> cfg.fpsOptimization, v -> cfg.fpsOptimization = v));
		interfaceItems.add(new Toggle("Notifications", () -> cfg.notifications, v -> cfg.notifications = v));
		interfaceItems.add(new Toggle("Lyrics in the sky (.lrc / .txt next to track)", () -> cfg.skyLyrics, v -> cfg.skyLyrics = v));
		reset = new Button("Reset to defaults", () -> {
			cfg.resetInterface();
			ThemeManager.applyByName(cfg.theme);
			ConfigManager.get().noteChange("Settings reset to defaults");
		});
		interfaceItems.add(reset);

		onlineItems.add(new Toggle("Online counter (presence)", () -> cfg.presenceEnabled,
				v -> { cfg.presenceEnabled = v; PresenceManager.resetTimer(); }));
		onlineItems.add(new Toggle("Show N badge on nametags", () -> cfg.showBadge, v -> cfg.showBadge = v));
		onlineItems.add(new Toggle("NevaVis cape (for mod users)", () -> cfg.showCape, v -> cfg.showCape = v));
		onlineItems.add(url);
		url.setText(cfg.presenceUrl);

		for (int i = 0; i < 8; i++) {
			final int idx = i;
			SearchBar bar = new SearchBar("Phrase " + (i + 1) + " (empty = unused)", 64);
			while (cfg.phrases.size() <= idx) {
				cfg.phrases.add("");
			}
			bar.setText(cfg.phrases.get(idx));
			phraseBars.add(bar);
		}
		keys.add(new KeybindSelector("Open NevaVis GUI", KeybindManager.openGui()));
		keys.add(new KeybindSelector("FreeLook (hold)", KeybindManager.freeLook()));
		keys.add(new KeybindSelector("Phrase Wheel (hold)", KeybindManager.phraseWheel()));
		keys.add(new KeybindSelector("Music: Play / Pause", KeybindManager.musicToggle()));
		keys.add(new KeybindSelector("Music: Next", KeybindManager.musicNext()));
		keys.add(new KeybindSelector("Music: Previous", KeybindManager.musicPrev()));
		rebuild();
	}

	private void rebuild() {
		comps.clear();
		comps.add(tabs);
		switch (tab) {
			case 0 -> comps.addAll(interfaceItems);
			case 1 -> comps.addAll(onlineItems);
			case 2 -> comps.addAll(phraseBars);
			default -> comps.addAll(keys);
		}
	}

	@Override
	public String title() {
		return "Settings";
	}

	@Override
	public void onShow() {
		super.onShow();
		rebuild();
	}

	@Override
	protected int reflow(int top) {
		int usable = w - 10;
		tabs.bounds(x, top, 300, 20);
		int cy = top + 30;
		switch (tab) {
			case 0 -> {
				for (UiComponent u : interfaceItems) {
					int h = u instanceof Slider ? 26 : 24;
					u.bounds(x, cy, usable, u == reset ? 22 : h);
					cy += (u instanceof Slider ? 32 : 28);
				}
				return cy + 20;
			}
			case 1 -> {
				NevaVisConfig cfg = ConfigManager.get().data();
				for (int i = 0; i < 3; i++) {
					onlineItems.get(i).bounds(x, cy, usable, 24);
					cy += 28;
				}
				cy += 20;
				url.bounds(x, cy, usable, 20);
				if (!url.getText().equals(cfg.presenceUrl)) {
					cfg.presenceUrl = url.getText().trim();
					PresenceManager.resetTimer();
				}
				return cy + 60;
			}
			case 2 -> {
				NevaVisConfig cfg = ConfigManager.get().data();
				for (int i = 0; i < 8; i++) {
					phraseBars.get(i).bounds(x, cy, usable, 20);
					if (!phraseBars.get(i).getText().equals(cfg.phrases.get(i))) {
						cfg.phrases.set(i, phraseBars.get(i).getText());
					}
					cy += 26;
				}
				return cy + 40;
			}
			default -> {
				for (KeybindSelector k : keys) {
					k.bounds(x, cy, usable, 26);
					cy += 30;
				}
				return cy + 40;
			}
		}
	}

	@Override
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
		if (tab == 1) {
			Gfx.text(c, "Worker URL (see server/README.md):", x + 4, url.y - 12, ThemeManager.dim());
			Gfx.text(c, PresenceManager.statusText(), x + 4, url.y + 26, ThemeManager.accent());
		} else if (tab == 2 && !phraseBars.isEmpty()) {
			Gfx.text(c, "Hold the Phrase Wheel key in game, point at a phrase, release to send it to chat.",
					x + 4, phraseBars.get(7).y + 28, ThemeManager.dim());
		} else if (tab == 3 && !keys.isEmpty()) {
			Gfx.text(c, "Click a field, then press a key. Esc cancels.", x + 4, keys.get(keys.size() - 1).y + 34, ThemeManager.dim());
		}
	}
}
