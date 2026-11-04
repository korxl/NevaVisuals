package nevavis.render;

import net.minecraft.client.gui.DrawContext;

/** Тонкие линии на экране (пиксельный проход) и мировые рёбра с отсечением near-плоскостью. */
public final class Lines {
	public interface Colorer {
		int at(float t);
	}

	private Lines() {
	}

	public static void line(DrawContext c, float x0, float y0, float x1, float y1, int thick, Colorer col) {
		float dx = x1 - x0, dy = y1 - y0;
		int n = (int) Math.max(Math.abs(dx), Math.abs(dy));
		if (n <= 0 || n > 3000) {
			return;
		}
		int sw = c.getScaledWindowWidth(), sh = c.getScaledWindowHeight();
		for (int i = 0; i <= n; i++) {
			float t = i / (float) n;
			int x = Math.round(x0 + dx * t), y = Math.round(y0 + dy * t);
			if (x < -2 || y < -2 || x > sw || y > sh) {
				continue;
			}
			c.fill(x, y, x + thick, y + thick, col.at(t));
		}
	}

	/** Ребро между двумя мировыми точками. along — смещение параметра цвета (для градиента по рёбрам). */
	public static void edge(DrawContext c, double ax, double ay, double az, double bx, double by, double bz,
			int thick, Colorer col) {
		double da = Projector.depth(ax, ay, az), db = Projector.depth(bx, by, bz);
		double near = 0.06;
		if (da < near && db < near) {
			return;
		}
		if (da < near) {
			double t = (near - da) / (db - da);
			ax += (bx - ax) * t;
			ay += (by - ay) * t;
			az += (bz - az) * t;
		} else if (db < near) {
			double t = (near - db) / (da - db);
			bx += (ax - bx) * t;
			by += (ay - by) * t;
			bz += (az - bz) * t;
		}
		float[] p = new float[3], q = new float[3];
		if (Projector.project(ax, ay, az, p) && Projector.project(bx, by, bz, q)) {
			line(c, p[0], p[1], q[0], q[1], thick, col);
		}
	}

	/** Заливка выпуклого многоугольника (экранные координаты) строками. */
	public static void fillPoly(DrawContext c, float[][] pts, int color) {
		float minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
		for (float[] p : pts) {
			minY = Math.min(minY, p[1]);
			maxY = Math.max(maxY, p[1]);
		}
		int sh = c.getScaledWindowHeight(), sw = c.getScaledWindowWidth();
		int y0 = Math.max(0, (int) Math.floor(minY)), y1 = Math.min(sh - 1, (int) Math.ceil(maxY));
		for (int y = y0; y <= y1; y++) {
			float lo = Float.MAX_VALUE, hi = -Float.MAX_VALUE;
			float yc = y + 0.5f;
			for (int i = 0; i < pts.length; i++) {
				float[] a = pts[i], b = pts[(i + 1) % pts.length];
				if ((a[1] <= yc && b[1] > yc) || (b[1] <= yc && a[1] > yc)) {
					float x = a[0] + (yc - a[1]) / (b[1] - a[1]) * (b[0] - a[0]);
					lo = Math.min(lo, x);
					hi = Math.max(hi, x);
				}
			}
			if (hi >= lo) {
				int xa = Math.max(0, Math.round(lo)), xb = Math.min(sw, Math.round(hi));
				if (xb > xa) {
					c.fill(xa, y, xb, y + 1, color);
				}
			}
		}
	}
}
