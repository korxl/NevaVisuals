package nevavis.gui.components;

import net.minecraft.client.gui.DrawContext;
import nevavis.modules.Module;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import nevavis.utils.Colors;
import nevavis.utils.SmoothValue;

import java.util.function.Consumer;

/** Плитка модуля: ЛКМ — включить/выключить, ПКМ — открыть быстрые настройки. */
public final class ModuleTile extends UiComponent {
	private final Module module;
	private final Consumer<ModuleTile> onRight;
	private final SmoothValue on = new SmoothValue(0f);

	public ModuleTile(Module module, Consumer<ModuleTile> onRight) {
		this.module = module;
		this.onRight = onRight;
		this.on.value = module.isEnabled() ? 1f : 0f;
		this.on.target = this.on.value;
	}

	public Module module() {
		return module;
	}

	@Override
	protected void render(DrawContext c, double mx, double my, float dt) {
		on.approach(module.isEnabled() ? 1f : 0f, dt, 14f);
		int r = Math.min(Gfx.radius(), 10);
		float a = on.value;
		int base = Colors.lerp(Gfx.surface(0.6f), Gfx.accent(0.28f), a);
		Gfx.roundedRect(c, x, y, w, h, r, Colors.lerp(base, Gfx.accent(0.18f), hover.value * 0.5f));
		Gfx.roundedOutline(c, x, y, w, h, r, Colors.lerp(Gfx.accent(0.18f), Gfx.accent(0.9f), a));

		Gfx.text(c, Gfx.fit(module.name(), w - 54), x + 10, y + 8, ThemeManager.text());
		Gfx.text(c, Gfx.fit(module.description(), w - 20), x + 10, y + 22, ThemeManager.dim());

		// переключатель-«таблетка»
		int pw = 26, ph = 12, px = x + w - pw - 10, py = y + 8;
		Gfx.roundedRect(c, px, py, pw, ph, 6, Colors.lerp(Colors.withAlpha(ThemeManager.dim(), 0.35f), Gfx.accent(1f), a));
		int kx = px + 2 + Math.round(a * (pw - ph));
		Gfx.roundedRect(c, kx, py + 2, ph - 4, ph - 4, 4, 0xFFFFFFFF);
		if (!module.settings().isEmpty()) {
			Gfx.text(c, "RMB", x + w - 26, y + h - 12, Colors.withAlpha(ThemeManager.dim(), 0.7f));
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (!enabled || !contains(mx, my)) {
			return false;
		}
		if (button == 0) {
			module.setEnabled(!module.isEnabled());
			return true;
		}
		if (button == 1 && !module.settings().isEmpty()) {
			onRight.accept(this);
			return true;
		}
		return false;
	}
}
