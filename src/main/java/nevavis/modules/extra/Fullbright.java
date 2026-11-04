package nevavis.modules.extra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.modules.ModuleManager;

/** Fullbright: всегда светло (локальное «ночное зрение», сервер о нём не знает). */
public final class Fullbright extends Module {
	public static final String ID = "fullbright";
	private static boolean added;

	public Fullbright() {
		super(ID, "Fullbright", "Always bright, like night vision (client-side only)", ModuleCategory.WORLD);
	}

	/** Раз в тик: выдаёт/снимает локальный эффект ночного зрения. */
	public static void tick(MinecraftClient mc) {
		Module m = ModuleManager.byId(ID);
		boolean want = m != null && m.isEnabled() && mc.player != null;
		if (want) {
			if (!mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
				mc.player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1_000_000, 0, false, false, false));
				added = true;
			}
		} else if (added) {
			if (mc.player != null) {
				mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
			}
			added = false;
		}
	}
}
