package nevavis.modules.extra;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ChatScreen;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.modules.ModuleManager;

/** Chat Slide: поле ввода чата плавно выезжает снизу при открытии. Регистрируйте init() до ChatMover.init(). */
public final class ChatSlide extends Module {
	public static final String ID = "chat_slide";
	private static long openedAt;

	public ChatSlide() {
		super(ID, "Chat Slide", "Chat input slides in smoothly when opened", ModuleCategory.ANIMATIONS);
		addSetting("distance", "Distance (px)", 4f, 60f, 1f, 24f);
		addSetting("time", "Duration (ms)", 80f, 600f, 10f, 220f);
	}

	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
			if (!(screen instanceof ChatScreen)) {
				return;
			}
			openedAt = System.currentTimeMillis();
			ScreenEvents.beforeRender(screen).register((s, c, mx, my, d) -> {
				Module m = ModuleManager.byId(ID);
				if (m == null || !m.isEnabled()) {
					return;
				}
				float t = Math.min(1f, (System.currentTimeMillis() - openedAt) / m.get("time"));
				float e = 1f - (1f - t) * (1f - t) * (1f - t);
				c.getMatrices().push();
				c.getMatrices().translate(0f, (1f - e) * m.get("distance"), 0f);
			});
			ScreenEvents.afterRender(screen).register((s, c, mx, my, d) -> {
				Module m = ModuleManager.byId(ID);
				if (m != null && m.isEnabled()) {
					c.getMatrices().pop();
				}
			});
		});
	}
}
