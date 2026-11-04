package nevavis.modules.extra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.render.Projector;
import nevavis.utils.Colors;

import java.util.Random;

/**
 * Space Sky: ночью поверх неба — звёзды, северное сияние, падающие звёзды (оверлей по проекции).
 * Точки, закрытые рельефом/блоками, скрываются проверкой луча (по кругу, несколько за кадр).
 */
public final class SpaceSky extends Module {
	private static final int STARS = 420, COLS = 70;
	private final double[][] star = new double[STARS][3];
	private final boolean[] starVis = new boolean[STARS];
	private final boolean[] colVis = new boolean[COLS];
	private int cursor, colCursor;
	private long shootAt, nextShoot = System.currentTimeMillis() + 5000;
	private float shootX, shootY;

	public SpaceSky() {
		super("space_sky", "Space Sky", "Night sky overlay: stars, aurora, shooting stars", ModuleCategory.WORLD);
		addSetting("stars", "Stars", 0f, 1f, 0.05f, 0.8f);
		addToggle("aurora", "Aurora", true);
		addColor("aurora_color", "Aurora color", 0x3DFF9A);
		addSetting("brightness", "Brightness", 0.1f, 1f, 0.05f, 0.8f);
		addToggle("shooting", "Shooting stars", true);
		Random r = new Random(12345);
		for (int i = 0; i < STARS; i++) {
			double yaw = r.nextDouble() * Math.PI * 2, el = Math.asin(0.04 + r.nextDouble() * 0.96);
			star[i][0] = Math.cos(el) * Math.cos(yaw);
			star[i][1] = Math.sin(el);
			star[i][2] = Math.cos(el) * Math.sin(yaw);
		}
	}

	private static boolean clear(MinecraftClient mc, Vec3d eye, double dx, double dy, double dz) {
		Vec3d end = eye.add(dx * 150, dy * 150, dz * 150);
		return mc.world.raycast(new RaycastContext(eye, end, RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS;
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		if (mc.world == null || mc.player == null || mc.world.getRegistryKey() != World.OVERWORLD
				|| mc.currentScreen != null || !Projector.begin(mc, td, c)) {
			return;
		}
		long tod = mc.world.getTimeOfDay() % 24000L;
		float night = tod >= 12500 && tod <= 23500 ? 1f : 0f;
		if (tod > 12000 && tod < 12500) {
			night = (tod - 12000) / 500f;
		} else if (tod > 23500 && tod < 24000) {
			night = (24000 - tod) / 500f;
		}
		if (night <= 0f || mc.world.isRaining() || !mc.world.isSkyVisible(mc.player.getBlockPos())) {
			return;
		}
		float bright = get("brightness") * night;
		Vec3d eye = mc.player.getCameraPosVec(td);
		float time = (System.currentTimeMillis() % 1000000L) / 1000f;

		for (int n = 0; n < 24; n++) {
			cursor = (cursor + 1) % STARS;
			starVis[cursor] = clear(mc, eye, star[cursor][0], star[cursor][1], star[cursor][2]);
		}
		float[] p = new float[3];
		int count = (int) (STARS * get("stars"));
		for (int i = 0; i < count; i++) {
			if (!starVis[i] || !Projector.project(eye.x + star[i][0] * 100, eye.y + star[i][1] * 100, eye.z + star[i][2] * 100, p)) {
				continue;
			}
			float tw = 0.55f + 0.45f * (float) Math.sin(time * (1.5f + (i % 7) * 0.4f) + i);
			int a = Colors.withAlpha(i % 5 == 0 ? 0xCFE3FF : 0xFFFFFF, bright * tw);
			int px = Math.round(p[0]), py = Math.round(p[1]);
			c.fill(px, py, px + 1 + (i % 11 == 0 ? 1 : 0), py + 1 + (i % 11 == 0 ? 1 : 0), a);
		}

		if (flag("aurora")) {
			for (int n = 0; n < 6; n++) {
				colCursor = (colCursor + 1) % COLS;
				double az = azimuth(colCursor, time);
				colVis[colCursor] = clear(mc, eye, Math.cos(az) * 0.6, 0.8, Math.sin(az) * 0.6);
			}
			float[] top = new float[3], bot = new float[3];
			for (int i = 0; i < COLS; i++) {
				if (!colVis[i]) {
					continue;
				}
				double az = azimuth(i, time);
				double elLow = Math.toRadians(14 + 6 * Math.sin(time * 0.4 + i * 0.25));
				double elHigh = Math.toRadians(38 + 10 * Math.sin(time * 0.3 + i * 0.18 + 1.0));
				double bx = Math.cos(az) * Math.cos(elLow), by = Math.sin(elLow), bz = Math.sin(az) * Math.cos(elLow);
				double tx = Math.cos(az) * Math.cos(elHigh), ty = Math.sin(elHigh), tz = Math.sin(az) * Math.cos(elHigh);
				if (Projector.project(eye.x + bx * 100, eye.y + by * 100, eye.z + bz * 100, bot)
						&& Projector.project(eye.x + tx * 100, eye.y + ty * 100, eye.z + tz * 100, top)) {
					float wave = 0.5f + 0.5f * (float) Math.sin(time * 0.9 + i * 0.35);
					int hue = Colors.lerp(color("aurora_color"), 0xFFFFFF, 0.18f * wave);
					int x = Math.round(bot[0]);
					c.fillGradient(x, Math.round(top[1]), x + 4, Math.round(bot[1]),
							Colors.withAlpha(hue, 0f), Colors.withAlpha(hue, bright * 0.38f * (0.5f + 0.5f * wave)));
				}
			}
		}

		if (flag("shooting")) {
			long now = System.currentTimeMillis();
			if (now > nextShoot) {
				shootAt = now;
				nextShoot = now + 9000 + (long) (Math.random() * 14000);
				shootX = (float) Math.random();
				shootY = (float) (0.05 + Math.random() * 0.3);
			}
			float age = (now - shootAt) / 700f;
			if (age >= 0f && age < 1f) {
				int sw = Projector.width(), sh = Projector.height();
				float x0 = sw * (shootX + age * 0.25f), y0 = sh * (shootY + age * 0.18f);
				for (int k = 0; k < 18; k++) {
					float fa = bright * (1f - age) * (1f - k / 18f);
					int px = Math.round(x0 - k * 3.2f), py = Math.round(y0 - k * 2.3f);
					c.fill(px, py, px + 2, py + 1, Colors.withAlpha(0xFFFFFF, fa));
				}
			}
		}
	}

	private static double azimuth(int i, float time) {
		return Math.toRadians(-70 + 140.0 * i / COLS + 8 * Math.sin(time * 0.2));
	}
}
