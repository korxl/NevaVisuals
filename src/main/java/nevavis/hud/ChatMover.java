package nevavis.hud;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import nevavis.config.ConfigManager;
import nevavis.render.Gfx;
import nevavis.themes.ThemeManager;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Перетаскивание элементов HUD мышью, пока открыт чат (T). */
public final class ChatMover {
	private static HudElement dragging;
	private static int dx, dy;

	private ChatMover() {
	}

	public static void init() {
		ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
			if (!(screen instanceof ChatScreen)) {
				return;
			}
			HudManager.editing = true;
			ScreenEvents.remove(screen).register(s -> {
				HudManager.editing = false;
				dragging = null;
				ConfigManager.get().save();
			});
			ScreenMouseEvents.allowMouseClick(screen).register((s, mx, my, button) -> {
				if (button != 0) {
					return true;
				}
				List<HudElement> list = HudManager.all();
				for (int i = list.size() - 1; i >= 0; i--) {
					HudElement e = list.get(i);
					if (e.cfg().visible && e.hit(mx, my, s.width, s.height)) {
						dragging = e;
						dx = (int) mx - e.ax(s.width);
						dy = (int) my - e.ay(s.height);
						return false; // клик не уходит в чат
					}
				}
				return true;
			});
			ScreenMouseEvents.allowMouseRelease(screen).register((s, mx, my, button) -> {
				dragging = null;
				return true;
			});
			ScreenEvents.afterRender(screen).register((s, c, mx, my, delta) -> render(s, c, mx, my));
		});
	}

	private static void render(Screen s, DrawContext c, int mx, int my) {
		long handle = net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle();
		if (dragging != null && GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS) {
			dragging = null;
		}
		if (dragging != null) {
			float maxX = Math.max(0f, 1f - dragging.aw() / (float) s.width);
			float maxY = Math.max(0f, 1f - dragging.ah() / (float) s.height);
			dragging.cfg().fx = Math.max(0f, Math.min(maxX, (float) (mx - dx) / s.width));
			dragging.cfg().fy = Math.max(0f, Math.min(maxY, (float) (my - dy) / s.height));
		}
		Gfx.setFade(1f);
		for (HudElement e : HudManager.all()) {
			if (e.cfg().visible) {
				boolean sel = e == dragging;
				Gfx.roundedOutline(c, e.ax(s.width) - 2, e.ay(s.height) - 2, e.aw() + 4, e.ah() + 4, 4,
						sel ? ThemeManager.accent() : Gfx.accent(0.35f));
			}
		}
		Gfx.centered(c, "Drag HUD elements with the mouse", s.width / 2, 6, ThemeManager.dim());
	}
}
