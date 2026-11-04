package nevavis.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import nevavis.config.ConfigManager;
import nevavis.config.NevaVisConfig.ParticleConfig;
import nevavis.render.Projector;
import nevavis.utils.Colors;

import java.util.Random;

/**
 * Круглые частицы в мире. Настоящие 3D-позиции и физика, рисуются как мягкие круги по проекции камеры.
 * Закрытые блоками частицы скрываются проверкой луча (по кругу, несколько за кадр).
 */
public final class WorldParticles {
	private static final int MAX = 320;
	private static final double[] X = new double[MAX], Y = new double[MAX], Z = new double[MAX];
	private static final float[] VX = new float[MAX], VY = new float[MAX], VZ = new float[MAX];
	private static final float[] LIFE = new float[MAX], MAXLIFE = new float[MAX], SIZE = new float[MAX], HUE = new float[MAX];
	private static final int[] FIXED = new int[MAX];
	private static final boolean[] VIS = new boolean[MAX];
	private static int count, cursor;
	private static float spawnAcc;
	private static long lastNs = System.nanoTime();
	private static double lastX, lastZ;
	private static boolean hasLast;
	private static int lastHitSerial;
	private static final Random R = new Random();

	private WorldParticles() {
	}

	private static ParticleConfig cfg() {
		return ConfigManager.get().data().particles;
	}

	private static boolean visible(MinecraftClient mc, Vec3d eye, double x, double y, double z) {
		return mc.world.raycast(new RaycastContext(eye, new Vec3d(x, y, z), RaycastContext.ShapeType.COLLIDER,
				RaycastContext.FluidHandling.NONE, mc.player)).getType() == HitResult.Type.MISS;
	}

	private static void add(MinecraftClient mc, Vec3d eye, double x, double y, double z, float vx, float vy, float vz,
			float life, float size, int fixedColor) {
		if (count >= MAX) {
			return;
		}
		int i = count++;
		X[i] = x;
		Y[i] = y;
		Z[i] = z;
		VX[i] = vx;
		VY[i] = vy;
		VZ[i] = vz;
		LIFE[i] = life;
		MAXLIFE[i] = life;
		SIZE[i] = size;
		HUE[i] = R.nextFloat();
		FIXED[i] = fixedColor;
		VIS[i] = visible(mc, eye, x, y, z);
	}

	private static void remove(int i) {
		int l = --count;
		if (i != l) {
			X[i] = X[l]; Y[i] = Y[l]; Z[i] = Z[l];
			VX[i] = VX[l]; VY[i] = VY[l]; VZ[i] = VZ[l];
			LIFE[i] = LIFE[l]; MAXLIFE[i] = MAXLIFE[l]; SIZE[i] = SIZE[l]; HUE[i] = HUE[l];
			FIXED[i] = FIXED[l]; VIS[i] = VIS[l];
		}
	}

	/** Всплеск частиц в точке (удар, крит). */
	public static void burst(double x, double y, double z, int rgb, int n) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.world == null) {
			return;
		}
		Vec3d eye = mc.player.getEyePos();
		for (int k = 0; k < n; k++) {
			double a = R.nextDouble() * Math.PI * 2, e = (R.nextDouble() - 0.3) * 1.2;
			float sp = 1.5f + R.nextFloat() * 2.5f;
			add(mc, eye, x, y, z, (float) (Math.cos(a) * Math.cos(e)) * sp, (float) Math.sin(e) * sp + 1f,
					(float) (Math.sin(a) * Math.cos(e)) * sp, 0.5f + R.nextFloat() * 0.5f, 1.2f, 0xFF000000 | rgb);
		}
	}

	public static void render(DrawContext c, MinecraftClient mc, float td) {
		long now = System.nanoTime();
		float dt = Math.min(0.1f, (now - lastNs) / 1_000_000_000f);
		lastNs = now;
		ParticleConfig k = cfg();
		if (mc.world == null || mc.player == null || mc.isPaused()) {
			count = 0;
			hasLast = false;
			return;
		}
		if (!k.on) {
			count = 0;
			return;
		}
		Vec3d eye = mc.player.getCameraPosVec(td);
		float sp = k.speed;

		// всплеск при ударе
		int serial = CombatTracker.serial();
		if (serial != lastHitSerial) {
			lastHitSerial = serial;
			var t = CombatTracker.target();
			if (k.hits && t != null) {
				burst(t.getX(), t.getY() + t.getHeight() * 0.6, t.getZ(),
						CombatTracker.lastCrit() ? 0xFFC83C : (k.rainbow ? Colors.hsv(R.nextFloat(), 0.8f, 1f) : k.color),
						8 + (int) (k.intensity * 18) + (CombatTracker.lastCrit() ? 10 : 0));
			}
		}

		// спавн вокруг игрока
		if (k.ambient) {
			spawnAcc += dt * (4f + k.intensity * 70f);
			int cap = 25 + (int) (k.intensity * 230);
			while (spawnAcc >= 1f) {
				spawnAcc -= 1f;
				if (count >= cap) {
					continue;
				}
				double r = k.radius * (0.35 + 0.65 * R.nextDouble());
				double a = R.nextDouble() * Math.PI * 2, el = (R.nextDouble() - 0.4) * 1.3;
				double x = eye.x + Math.cos(a) * Math.cos(el) * r, y = eye.y + Math.sin(el) * r, z = eye.z + Math.sin(a) * Math.cos(el) * r;
				if (!mc.world.getBlockState(BlockPos.ofFloored(x, y, z)).isAir()) {
					continue;
				}
				add(mc, eye, x, y, z, (R.nextFloat() - 0.5f) * 0.5f * sp, (0.1f + R.nextFloat() * 0.35f) * sp,
						(R.nextFloat() - 0.5f) * 0.5f * sp, 3f + R.nextFloat() * 3.5f, 0.8f + R.nextFloat() * 0.7f, 0);
			}
		}
		// шлейф за игроком
		double px = mc.player.getX(), pz = mc.player.getZ();
		if (k.trail && hasLast && Math.hypot(px - lastX, pz - lastZ) > 0.03) {
			spawnAcc += 0f;
			int n = 1 + (int) (k.intensity * 3);
			for (int i = 0; i < n; i++) {
				add(mc, eye, px + (R.nextDouble() - 0.5) * 0.4, mc.player.getY() + 0.1 + R.nextDouble() * 0.15,
						pz + (R.nextDouble() - 0.5) * 0.4, (R.nextFloat() - 0.5f) * 0.2f, 0.15f * sp,
						(R.nextFloat() - 0.5f) * 0.2f, 0.8f + R.nextFloat() * 0.8f, 1.1f, 0);
			}
		}
		lastX = px;
		lastZ = pz;
		hasLast = true;

		// физика
		for (int i = count - 1; i >= 0; i--) {
			LIFE[i] -= dt;
			if (LIFE[i] <= 0f) {
				remove(i);
				continue;
			}
			X[i] += VX[i] * dt;
			Y[i] += VY[i] * dt;
			Z[i] += VZ[i] * dt;
			float drag = 1f - Math.min(1f, dt * 0.6f);
			VX[i] *= drag;
			VZ[i] *= drag;
			if (FIXED[i] != 0) {
				VY[i] -= dt * 4f; // частицы всплеска падают
			}
		}
		// проверка видимости по кругу
		for (int n = 0; n < 14 && count > 0; n++) {
			cursor = (cursor + 1) % count;
			VIS[cursor] = visible(mc, eye, X[cursor], Y[cursor], Z[cursor]);
		}

		if (!Projector.begin(mc, td, c)) {
			return;
		}
		float t = (System.currentTimeMillis() % 100000L) / 1000f;
		float[] p = new float[3];
		int sh = c.getScaledWindowHeight();
		for (int i = 0; i < count; i++) {
			if (!VIS[i] || !Projector.project(X[i], Y[i], Z[i], p)) {
				continue;
			}
			float age = 1f - LIFE[i] / MAXLIFE[i];
			float a = (float) Math.sin(Math.min(1f, age * 6f) * Math.PI / 2) * Math.min(1f, (1f - age) * 3f);
			int rgb = FIXED[i] != 0 ? FIXED[i] & 0xFFFFFF
					: (k.rainbow ? Colors.hsv((t * 0.15f + HUE[i]) % 1f, 0.7f, 1f) : k.color);
			float rad = Math.max(1f, Math.min(9f, SIZE[i] * k.size * sh * 0.011f / p[2]));
			disc(c, Math.round(p[0]), Math.round(p[1]), rad, rgb, a * 0.9f);
		}
	}

	private static void disc(DrawContext c, int cx, int cy, float r, int rgb, float alpha) {
		int ri = Math.max(1, Math.round(r));
		int soft = Colors.withAlpha(rgb, alpha * 0.28f), core = Colors.withAlpha(rgb, alpha);
		for (int dy = -ri - 1; dy <= ri + 1; dy++) {
			float h2 = (ri + 1) * (ri + 1) - dy * dy;
			if (h2 > 0) {
				int half = (int) Math.sqrt(h2);
				c.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, soft);
			}
			float c2 = ri * ri - dy * dy;
			if (c2 > 0) {
				int half = (int) Math.sqrt(c2);
				c.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, core);
			}
		}
	}
}
