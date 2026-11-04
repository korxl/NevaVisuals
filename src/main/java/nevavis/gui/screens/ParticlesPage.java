package nevavis.gui.screens;

import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig.ParticleConfig;
import nevavis.gui.components.ColorPicker;
import nevavis.gui.components.Slider;
import nevavis.gui.components.Toggle;

/** Круглые частицы в мире: вкл/выкл, цвет, интенсивность, размер, скорость и радиус. */
public final class ParticlesPage extends Page {
	private final Toggle on, ambient, trail, hits, rainbow;
	private final Slider intensity, size, speed, radius;
	private final ColorPicker color;

	private static ParticleConfig p() {
		return ConfigManager.get().data().particles;
	}

	public ParticlesPage() {
		on = new Toggle("World particles", () -> p().on, v -> p().on = v);
		ambient = new Toggle("Around me", () -> p().ambient, v -> p().ambient = v);
		trail = new Toggle("Trail behind me", () -> p().trail, v -> p().trail = v);
		hits = new Toggle("Burst on hit", () -> p().hits, v -> p().hits = v);
		rainbow = new Toggle("Rainbow color", () -> p().rainbow, v -> p().rainbow = v);
		intensity = new Slider("Intensity", 0.05f, 1f, 0.05f, () -> p().intensity, v -> p().intensity = v, "%.2f");
		size = new Slider("Size", 0.5f, 3f, 0.1f, () -> p().size, v -> p().size = v, "%.1fx");
		speed = new Slider("Speed", 0.2f, 3f, 0.1f, () -> p().speed, v -> p().speed = v, "%.1fx");
		radius = new Slider("Radius", 3f, 16f, 1f, () -> p().radius, v -> p().radius = v, "%.0f blocks");
		color = new ColorPicker("Color", () -> p().color, v -> p().color = v);
		comps.add(on);
		comps.add(ambient);
		comps.add(trail);
		comps.add(hits);
		comps.add(intensity);
		comps.add(size);
		comps.add(speed);
		comps.add(radius);
		comps.add(rainbow);
		comps.add(color);
	}

	@Override
	public String title() {
		return "Particles";
	}

	@Override
	protected int reflow(int top) {
		int usable = w - 10;
		int cy = top;
		for (Toggle t : new Toggle[]{on, ambient, trail, hits}) {
			t.bounds(x, cy, usable, 22);
			cy += 26;
		}
		cy += 4;
		for (Slider s : new Slider[]{intensity, size, speed, radius}) {
			s.bounds(x, cy, usable, 26);
			cy += 30;
		}
		rainbow.bounds(x, cy, usable, 22);
		cy += 30;
		color.bounds(x, cy, usable, ColorPicker.HEIGHT);
		return cy + ColorPicker.HEIGHT + 12;
	}
}
