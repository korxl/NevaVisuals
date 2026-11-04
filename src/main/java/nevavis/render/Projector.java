package nevavis.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;
import nevavis.modules.extra.AspectRatioModule;
import nevavis.modules.extra.FreeLook;

/**
 * Проекция мировых точек на экран (только от первого лица). Используется оверлеями (обводка блока, небо, ESP цели).
 * FOV берётся из реальной проекции игры (см. GameRendererMixin), поэтому метки совпадают с картинкой.
 */
public final class Projector {
	private static float fov = 70f;
	private static boolean captured;
	private static double ex, ey, ez, fx, fy, fz, rx, ry, rz, ux, uy, uz, tanHalf, aspect;
	private static int sw, sh;

	private Projector() {
	}

	public static void captureFov(float f) {
		if (!captured) {
			fov = f;
			captured = true;
		}
	}

	public static void endFrame() {
		captured = false;
	}

	public static boolean begin(MinecraftClient mc, float td, DrawContext c) {
		if (mc.player == null || !mc.options.getPerspective().isFirstPerson()) {
			return false;
		}
		Vec3d e = mc.player.getCameraPosVec(td);
		ex = e.x;
		ey = e.y;
		ez = e.z;
		double yaw = Math.toRadians(FreeLook.active() ? FreeLook.camYaw() : mc.player.getYaw(td));
		double pitch = Math.toRadians(FreeLook.active() ? FreeLook.camPitch() : mc.player.getPitch(td));
		double cp = Math.cos(pitch);
		fx = -Math.sin(yaw) * cp;
		fy = -Math.sin(pitch);
		fz = Math.cos(yaw) * cp;
		rx = -Math.cos(yaw);
		ry = 0;
		rz = -Math.sin(yaw);
		ux = ry * fz - rz * fy;
		uy = rz * fx - rx * fz;
		uz = rx * fy - ry * fx;
		tanHalf = Math.tan(Math.toRadians(fov) / 2.0);
		sw = c.getScaledWindowWidth();
		sh = c.getScaledWindowHeight();
		double target = AspectRatioModule.ratio();
		aspect = target > 0 ? target : sw / (double) sh;
		return true;
	}

	public static int width() {
		return sw;
	}

	public static int height() {
		return sh;
	}

	public static double eyeX() {
		return ex;
	}

	public static double eyeY() {
		return ey;
	}

	public static double eyeZ() {
		return ez;
	}

	/** out = {x, y, глубина}. false, если точка позади камеры. */
	public static boolean project(double x, double y, double z, float[] out) {
		double px = x - ex, py = y - ey, pz = z - ez;
		double d = px * fx + py * fy + pz * fz;
		if (d < 0.05) {
			return false;
		}
		double sx = (px * rx + py * ry + pz * rz) / d / (tanHalf * aspect);
		double sy = (px * ux + py * uy + pz * uz) / d / tanHalf;
		out[0] = (float) (sw / 2.0 * (1.0 + sx));
		out[1] = (float) (sh / 2.0 * (1.0 - sy));
		out[2] = (float) d;
		return true;
	}

	/** Глубина точки вдоль взгляда (для отсечения ребра near-плоскостью). */
	public static double depth(double x, double y, double z) {
		return (x - ex) * fx + (y - ey) * fy + (z - ez) * fz;
	}
}
