package nevavis.gui.screens;

import net.minecraft.client.gui.DrawContext;
import nevavis.gui.components.ModulePopup;
import nevavis.gui.components.ModuleTile;
import nevavis.gui.components.UiComponent;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.modules.ModuleManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;

import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Плитки модулей: ЛКМ — включить/выключить, ПКМ — маленькое окно быстрых настроек.
 * Показываются модули выбранных категорий (или все — для поиска).
 */
public final class ModulesPage extends Page {
	private static final int TILE_H = 40, GAP = 6, HEAD_H = 16;

	private static final class Label extends UiComponent {
		private final String text;

		Label(String text) {
			this.text = text;
		}

		@Override
		protected void render(DrawContext c, double mx, double my, float dt) {
			Gfx.text(c, text.toUpperCase(Locale.ROOT), x + 2, y + 4, ThemeManager.accent());
		}
	}

	private final String title;
	private final Set<ModuleCategory> categories; // null = все
	private final String emptyMessage;
	private final Supplier<String> query;
	private final ModulePopup popup = new ModulePopup();
	private String lastSig = null;
	private String lastQuery = null;

	public ModulesPage(String title, Set<ModuleCategory> categories, String emptyMessage, Supplier<String> query) {
		this.title = title;
		this.categories = categories;
		this.emptyMessage = emptyMessage;
		this.query = query;
	}

	@Override
	public String title() {
		return title;
	}

	@Override
	public void onShow() {
		super.onShow();
		popup.close();
		lastSig = null;
	}

	private boolean matches(Module m, String q) {
		if (categories != null && !categories.contains(m.category())) {
			return false;
		}
		return q.isEmpty() || m.name().toLowerCase(Locale.ROOT).contains(q)
				|| m.description().toLowerCase(Locale.ROOT).contains(q);
	}

	@Override
	protected void update() {
		String q = query.get().toLowerCase(Locale.ROOT).trim();
		StringBuilder sig = new StringBuilder(q).append('#');
		for (Module m : ModuleManager.all()) {
			if (matches(m, q)) {
				sig.append(m.id());
			}
		}
		String s = sig.toString();
		if (s.equals(lastSig)) {
			return;
		}
		lastSig = s;
		if (!q.equals(lastQuery)) {
			lastQuery = q;
			scroll.reset();
			popup.close();
		}
		comps.clear();
		ModuleCategory last = null;
		for (ModuleCategory cat : ModuleCategory.values()) {
			for (Module m : ModuleManager.all()) {
				if (m.category() != cat || !matches(m, q)) {
					continue;
				}
				if (last != cat) {
					last = cat;
					comps.add(new Label(cat.display()));
				}
				comps.add(new ModuleTile(m, popup::open));
			}
		}
		comps.add(popup); // последним: рисуется поверх плиток
	}

	@Override
	protected int reflow(int top) {
		popup.area(x, y, w, h);
		int usable = w - 10;
		int tw = (usable - GAP) / 2;
		int cy = top, col = 0;
		for (var comp : comps) {
			if (comp instanceof Label) {
				if (col != 0) {
					cy += TILE_H + GAP;
					col = 0;
				}
				comp.bounds(x, cy, usable, HEAD_H);
				cy += HEAD_H;
			} else if (comp instanceof ModuleTile) {
				comp.bounds(x + col * (tw + GAP), cy, tw, TILE_H);
				if (++col == 2) {
					col = 0;
					cy += TILE_H + GAP;
				}
			}
		}
		if (col != 0) {
			cy += TILE_H + GAP;
		}
		return comps.size() <= 1 ? top + h : cy + 4;
	}

	@Override
	protected void renderBody(DrawContext c, double mx, double my, float dt) {
		if (comps.size() > 1) {
			return;
		}
		String q = lastQuery == null ? "" : lastQuery;
		String msg = q.isEmpty() ? emptyMessage : "Nothing found for \"" + q + "\"";
		int bw = w - 10;
		int bh = 54;
		int by = y + (h - bh) / 2;
		Gfx.glassPanel(c, x, by, bw, bh, Math.min(Gfx.radius(), 12), 0.35f);
		Gfx.centered(c, q.isEmpty() ? "NO MODULES YET" : "NO RESULTS", x + bw / 2, by + 14, ThemeManager.accent());
		Gfx.centered(c, Gfx.fit(msg, bw - 20), x + bw / 2, by + 30, ThemeManager.dim());
	}
}
