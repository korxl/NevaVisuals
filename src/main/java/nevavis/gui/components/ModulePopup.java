package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.modules.Module;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

import java.util.ArrayList;
import java.util.List;

/** Небольшое окно быстрых настроек модуля (открывается по ПКМ на плитке). */
public final class ModulePopup extends UiComponent {
	private static final int PW = 214, PAD = 8, GAP = 5, HEAD = 20;

	private boolean open;
	private ModuleTile anchor;
	private Module module;
	private final List<UiComponent> items = new ArrayList<>();
	private final Button reset;
	private int ax, ay, aw, ah;
	private int px, py, ph;
	private final SmoothValue anim = new SmoothValue(0f);

	public ModulePopup() {
		reset = new Button("Reset to defaults", () -> {
			if (module != null) {
				for (Module.Setting s : module.settings()) {
					module.set(s.key(), s.def());
				}
			}
		});
	}

	/** Область страницы (для ограничения положения). */
	public void area(int x, int y, int w, int h) {
		ax = x;
		ay = y;
		aw = w;
		ah = h;
	}

	public boolean isOpen() {
		return open;
	}

	public void close() {
		open = false;
	}

	public void open(ModuleTile tile) {
		anchor = tile;
		module = tile.module();
		items.clear();
		for (Module.Setting s : module.settings()) {
			items.add(switch (s.kind()) {
				case TOGGLE -> new Toggle(s.label(), () -> module.flag(s.key()), v -> module.set(s.key(), v ? 1f : 0f));
				case CHOICE -> new Dropdown(s.label(), s.choices(), () -> s.choices().get(
						Math.max(0, Math.min(s.choices().size() - 1, (int) module.get(s.key())))),
						v -> module.set(s.key(), s.choices().indexOf(v)));
				case COLOR -> new ColorPicker(s.label(), () -> module.color(s.key()), v -> module.setColor(s.key(), v));
				default -> new Slider(s.label(), s.min(), s.max(), s.step(), () -> module.get(s.key()),
						v -> module.set(s.key(), v), "%.2f", true);
			});
		}
		open = true;
	}

	private int itemH(UiComponent u) {
		if (u instanceof ColorPicker) {
			return ColorPicker.HEIGHT;
		}
		return u instanceof Slider ? 26 : 22;
	}

	private void layout() {
		int total = HEAD + PAD;
		for (UiComponent u : items) {
			total += itemH(u) + GAP;
		}
		total += 22 + PAD;
		ph = total;
		px = anchor.x + anchor.w / 2 - PW / 2;
		px = Math.max(ax + 2, Math.min(ax + aw - PW - 2, px));
		py = anchor.y + anchor.h + 4;
		if (py + ph > ay + ah) {
			py = Math.max(ay + 2, anchor.y - ph - 4);
		}
		if (py + ph > ay + ah) {
			py = Math.max(ay + 2, ay + ah - ph);
		}
		int cy = py + HEAD;
		for (UiComponent u : items) {
			u.bounds(px + PAD, cy, PW - 2 * PAD, itemH(u));
			cy += itemH(u) + GAP;
		}
		reset.bounds(px + PAD, cy, PW - 2 * PAD, 20);
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
	}

	@Override
	public void renderOverlay(DrawContext c, double mx, double my, float dt) {
		anim.approach(open ? 1f : 0f, dt, 20f);
		if (anim.value < 0.02f || anchor == null) {
			return;
		}
		layout();
		float oldFade = Gfx.fade();
		Gfx.setFade(oldFade * anim.value);
		int r = Math.min(Gfx.radius() + 2, 12);
		Gfx.roundedRect(c, px, py, PW, ph, r, Colors.withAlpha(ThemeManager.bg(), 0.97f));
		Gfx.roundedOutline(c, px, py, PW, ph, r, Gfx.accent(0.85f));
		Gfx.text(c, module.name(), px + PAD + 2, py + 7, ThemeManager.accent());
		for (UiComponent u : items) {
			u.draw(c, mx, my, dt);
		}
		reset.draw(c, mx, my, dt);
		for (UiComponent u : items) {
			u.renderOverlay(c, mx, my, dt);
		}
		Gfx.setFade(oldFade);
	}

	@Override
	public boolean capturesInput() {
		return open;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (!open) {
			return false;
		}
		layout();
		for (UiComponent u : items) {
			if (u.capturesInput() && u.mouseClicked(mx, my, button)) {
				return true;
			}
		}
		boolean inside = mx >= px && mx < px + PW && my >= py && my < py + ph;
		if (inside) {
			for (UiComponent u : items) {
				if (u.mouseClicked(mx, my, button)) {
					return true;
				}
			}
			reset.mouseClicked(mx, my, button);
			return true;
		}
		close();
		return button == 0;
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button) {
		boolean used = false;
		if (open) {
			for (UiComponent u : items) {
				used |= u.mouseDragged(mx, my, button);
			}
		}
		return used;
	}

	@Override
	public boolean mouseReleased(double mx, double my, int button) {
		boolean used = false;
		for (UiComponent u : items) {
			used |= u.mouseReleased(mx, my, button);
		}
		return used;
	}
}
