package nevavis.modules;

import nevavis.modules.visuals.EdgeVignette;
import nevavis.modules.visuals.HitMarker;
import nevavis.modules.visuals.HurtEffect;
import nevavis.modules.visuals.LowHealthPulse;
import nevavis.modules.visuals.ScreenTint;
import nevavis.modules.visuals.SkyTintModule;
import nevavis.modules.visuals.SwingModule;
import nevavis.modules.extra.*;
import nevavis.modules.visuals.FogModule;
import nevavis.modules.visuals.DurabilityAlert;

/** Список всех реализованных модулей. */
public final class Modules {
	private Modules() {
	}

	public static void registerAll() {
		ModuleManager.register(new HurtEffect());
		ModuleManager.register(new LowHealthPulse());
		ModuleManager.register(new ScreenTint());
		ModuleManager.register(new EdgeVignette());
		ModuleManager.register(new HitMarker());
		ModuleManager.register(new SwingModule());
		ModuleManager.register(new CustomHand());
		ModuleManager.register(new HotbarGlide());
		ModuleManager.register(new ChatSlide());
		ModuleManager.register(new PerspectiveFlash());
		ModuleManager.register(new TotemPop());
		ModuleManager.register(new HitEffects());
		ModuleManager.register(new TargetEsp());
		ModuleManager.register(new FreeLook());
		ModuleManager.register(new SoundControl());
		ModuleManager.register(new Fullbright());
		ModuleManager.register(new AspectRatioModule());
		ModuleManager.register(new BlockOverlay());
		ModuleManager.register(new SpaceSky());
		ModuleManager.register(new FogModule());
		ModuleManager.register(new DurabilityAlert());
		ModuleManager.register(new SkyTintModule("custom_sky", "Custom Sky",
				"Your own sky color", 0x8C6BFF));
		ModuleManager.register(new SkyTintModule("custom_clouds", "Custom Clouds",
				"Your own cloud color", 0xFF9AD5));
	}
}
