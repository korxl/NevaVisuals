package nevavis.modules.extra;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.modules.ModuleManager;

/** Sound Controller: выключение выбранных групп звуков (удары, опыт, мобы, окружение, шаги, погода). */
public final class SoundControl extends Module {
	public static final String ID = "sound_control";

	public SoundControl() {
		super(ID, "Sound Controller", "Mute chosen groups of in-game sounds", ModuleCategory.PLAYER);
		addToggle("hits", "Mute hit / crit sounds", false);
		addToggle("xp", "Mute XP sounds", false);
		addToggle("steps", "Mute footsteps", false);
		addToggle("hostile", "Mute hostile mobs", false);
		addToggle("ambient", "Mute ambient / cave", false);
		addToggle("weather", "Mute weather", false);
	}

	public static boolean muted(SoundInstance s) {
		Module m = ModuleManager.byId(ID);
		if (m == null || !m.isEnabled() || s == null) {
			return false;
		}
		String id = s.getId().getPath();
		SoundCategory cat = s.getCategory();
		return (m.flag("hits") && id.contains("player.attack"))
				|| (m.flag("xp") && (id.contains("experience_orb") || id.contains("player.levelup")))
				|| (m.flag("steps") && id.endsWith(".step"))
				|| (m.flag("hostile") && cat == SoundCategory.HOSTILE)
				|| (m.flag("ambient") && cat == SoundCategory.AMBIENT)
				|| (m.flag("weather") && cat == SoundCategory.WEATHER);
	}
}
