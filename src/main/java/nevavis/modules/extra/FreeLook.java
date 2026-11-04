package nevavis.modules.extra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import nevavis.keybinds.KeybindManager;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.modules.ModuleManager;

/**
 * FreeLook: пока зажата клавиша, камера крутится вокруг персонажа, а его тело/взгляд остаются прежними
 * (на сервер уходит прежний поворот). Состояние обновляется из CameraMixin раз за кадр.
 */
public final class FreeLook extends Module {
	public static final String ID = "freelook";
	private static boolean active;
	private static float saveYaw, savePitch, offYaw, offPitch;
	private static Perspective prev = Perspective.FIRST_PERSON;

	public FreeLook() {
		super(ID, "FreeLook", "Hold the key (default Left Alt) to look around freely without turning your body",
				ModuleCategory.PLAYER);
		addToggle("third_person", "Switch to third person", true);
	}

	public static boolean active() {
		return active;
	}

	public static float camYaw() {
		return saveYaw + offYaw;
	}

	public static float camPitch() {
		return Math.max(-90f, Math.min(90f, savePitch + offPitch));
	}

	private static void update() {
		MinecraftClient mc = MinecraftClient.getInstance();
		ClientPlayerEntity p = mc.player;
		Module m = ModuleManager.byId(ID);
		boolean want = p != null && m != null && m.isEnabled() && mc.currentScreen == null
				&& KeybindManager.freeLook() != null && KeybindManager.freeLook().isPressed();
		if (want && !active) {
			active = true;
			saveYaw = p.getYaw();
			savePitch = p.getPitch();
			offYaw = 0f;
			offPitch = 0f;
			prev = mc.options.getPerspective();
			if (m.flag("third_person")) {
				mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
			}
		} else if (!want && active) {
			active = false;
			mc.options.setPerspective(prev);
			if (p != null) {
				p.setYaw(saveYaw);
				p.setPitch(savePitch);
			}
		}
		if (active && p != null) {
			offYaw += p.getYaw() - saveYaw;
			offPitch += p.getPitch() - savePitch;
			p.setYaw(saveYaw);
			p.setPitch(savePitch);
			p.prevYaw = saveYaw;
			p.prevPitch = savePitch;
		}
	}

	/** Вызывается первым при Camera.setRotation (yaw). */
	public static float yaw(float original) {
		update();
		return active ? camYaw() : original;
	}

	public static float pitch(float original) {
		return active ? camPitch() : original;
	}
}
