package nevavis.modules.visuals;

import net.minecraft.util.math.Vec3d;
import nevavis.modules.Module;
import nevavis.modules.ModuleManager;
import nevavis.utils.Colors;

/** Логика окраски неба/облаков. Принимает исходное значение (int или Vec3d) и возвращает изменённое либо null. */
public final class SkyHooks {
	private SkyHooks() {
	}

	public static Object tint(String moduleId, Object original) {
		Module m = ModuleManager.byId(moduleId);
		if (m == null || !m.isEnabled()) {
			return null;
		}
		float strength = m.get("strength");
		if (original instanceof Integer i) {
			int mixed = mix(i, m, strength);
			return (i & 0xFF000000) | (mixed & 0xFFFFFF);
		}
		if (original instanceof Vec3d v) {
			int o = Colors.rgb((int) (v.x * 255.0), (int) (v.y * 255.0), (int) (v.z * 255.0));
			int mixed = mix(o, m, strength);
			return new Vec3d(Colors.red(mixed) / 255.0, Colors.green(mixed) / 255.0, Colors.blue(mixed) / 255.0);
		}
		return null;
	}

	/** Цель — выбранный цвет; при «Keep brightness» затемняется по яркости исходного (ночью небо остаётся тёмным). */
	private static int mix(int original, Module m, float strength) {
		int o = 0xFF000000 | original;
		int target = m.flag("rainbow")
				? Colors.hsv(((System.currentTimeMillis() / 1000f) * 0.08f * m.get("speed")) % 1f, 0.75f, 1f)
				: m.color("color");
		if (m.flag("keep_light")) {
			float lum = Math.max(Colors.red(o), Math.max(Colors.green(o), Colors.blue(o))) / 255f;
			target = Colors.rgb((int) (Colors.red(target) * lum), (int) (Colors.green(target) * lum), (int) (Colors.blue(target) * lum));
		}
		return Colors.lerp(o, 0xFF000000 | target, strength);
	}
}
