package nevavis.modules.visuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffects;
import nevavis.core.NevaVis;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.modules.ModuleManager;
import nevavis.utils.Colors;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;

/** Custom Fog: дальность и цвет тумана. Туман — record (start, end, shape, r, g, b, a), правим по порядку полей. */
public final class FogModule extends Module {
	public static final String ID = "custom_fog";
	private static boolean broken;

	public FogModule() {
		super(ID, "Custom Fog", "Fog distance and color (not under water / lava)", ModuleCategory.WORLD);
		addSetting("distance", "Distance (x vanilla)", 0.1f, 1.5f, 0.05f, 0.6f);
		addColor("color", "Color", 0x9CB8FF);
		addSetting("strength", "Color strength", 0f, 1f, 0.05f, 0.6f);
		addToggle("rainbow", "Rainbow cycle", false);
	}

	/** Принимает исходный объект тумана, возвращает изменённый или null. */
	public static Object apply(Object fog) {
		Module m = ModuleManager.byId(ID);
		MinecraftClient mc = MinecraftClient.getInstance();
		if (broken || fog == null || m == null || !m.isEnabled() || mc.player == null
				|| mc.player.isSubmergedInWater() || mc.player.isInLava()
				|| mc.player.hasStatusEffect(StatusEffects.BLINDNESS)) {
			return null;
		}
		try {
			Class<?> k = fog.getClass();
			RecordComponent[] rc = k.getRecordComponents();
			if (rc == null || rc.length < 7) {
				broken = true;
				return null;
			}
			Object[] v = new Object[rc.length];
			Class<?>[] types = new Class<?>[rc.length];
			for (int i = 0; i < rc.length; i++) {
				var acc = rc[i].getAccessor();
				acc.setAccessible(true);
				v[i] = acc.invoke(fog);
				types[i] = rc[i].getType();
			}
			if (types[0] != float.class || types[1] != float.class) {
				broken = true;
				return null;
			}
			float d = m.get("distance");
			float start = (float) v[0], end = (float) v[1];
			if (end <= 0f || end > 4000f) {
				return null; // «бесконечный» туман (небо и т.п.) не трогаем
			}
			v[0] = start * d;
			v[1] = end * d;
			float s = m.get("strength");
			int target = m.flag("rainbow") ? Colors.hsv(((System.currentTimeMillis() / 1000f) * 0.08f) % 1f, 0.6f, 1f) : m.color("color");
			float[] tc = {Colors.red(target) / 255f, Colors.green(target) / 255f, Colors.blue(target) / 255f};
			for (int i = 0; i < 3; i++) {
				float o = (float) v[3 + i];
				v[3 + i] = o + (tc[i] - o) * s;
			}
			Constructor<?> ctor = k.getDeclaredConstructor(types);
			ctor.setAccessible(true);
			return ctor.newInstance(v);
		} catch (ReflectiveOperationException | RuntimeException e) {
			broken = true;
			NevaVis.LOGGER.warn("Custom Fog disabled: {}", e.toString());
			return null;
		}
	}
}
