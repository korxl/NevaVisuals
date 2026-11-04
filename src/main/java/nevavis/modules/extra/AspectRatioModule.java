package nevavis.modules.extra;

import net.minecraft.client.MinecraftClient;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.modules.ModuleManager;

/** Aspect Ratio: растягивает картинку под выбранное соотношение (4:3 = 1.33, 5:4 = 1.25, 16:9 = 1.78, 21:9 = 2.33). */
public final class AspectRatioModule extends Module {
	public static final String ID = "aspect_ratio";

	public AspectRatioModule() {
		super(ID, "Aspect Ratio", "Stretches the image to a custom ratio (1.33 = 4:3, 1.25 = 5:4, 2.33 = 21:9)",
				ModuleCategory.WORLD);
		addSetting("ratio", "Ratio (width / height)", 0.8f, 3f, 0.01f, 1.33f);
	}

	/** Целевое соотношение или 0, если модуль выключен. */
	public static double ratio() {
		Module m = ModuleManager.byId(ID);
		return m != null && m.isEnabled() ? m.get("ratio") : 0.0;
	}

	/** Множитель по X для матрицы проекции. */
	public static float factor() {
		double r = ratio();
		if (r <= 0) {
			return 1f;
		}
		var w = MinecraftClient.getInstance().getWindow();
		double win = w.getFramebufferWidth() / (double) Math.max(1, w.getFramebufferHeight());
		return (float) (win / r);
	}
}
