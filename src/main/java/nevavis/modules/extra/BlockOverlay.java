package nevavis.modules.extra;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.render.Lines;
import nevavis.render.Projector;
import nevavis.utils.Colors;

/**
 * Block Overlay: обводка блока под прицелом (рисуется поверх экрана по проекции рёбер).
 * Стили: 0 Solid, 1 Pulse, 2 Rainbow, 3 Plasma, 4 Stars. Можно залить грань, на которую вы смотрите.
 */
public final class BlockOverlay extends Module {
	public BlockOverlay() {
		super("block_overlay", "Block Overlay",
				"Custom outline and fill for the block you look at", ModuleCategory.WORLD);
		addChoice("style", "Style", java.util.List.of("Solid", "Pulse", "Rainbow", "Plasma", "Stars"), 3);
		addColor("color", "Color", 0x3DA9FF);
		addSetting("alpha", "Outline opacity", 0.2f, 1f, 0.05f, 0.95f);
		addSetting("thickness", "Thickness", 1f, 3f, 1f, 1f);
		addSetting("fill", "Face fill", 0f, 0.6f, 0.05f, 0.2f);
		addSetting("speed", "Animation speed", 0.2f, 4f, 0.1f, 1f);
	}

	@Override
	public void onHud(DrawContext c, MinecraftClient mc, float td) {
		if (mc.world == null || mc.currentScreen != null || !(mc.crosshairTarget instanceof BlockHitResult hit)
				|| hit.getType() != HitResult.Type.BLOCK || !Projector.begin(mc, td, c)) {
			return;
		}
		BlockPos pos = hit.getBlockPos();
		BlockState st = mc.world.getBlockState(pos);
		Box b = st.getOutlineShape(mc.world, pos).isEmpty() ? new Box(0, 0, 0, 1, 1, 1)
				: st.getOutlineShape(mc.world, pos).getBoundingBox();
		b = b.offset(pos).expand(0.002);

		int style = (int) get("style");
		float base = Colors.rgbToHue(color("color")), alpha = get("alpha"), speed = get("speed");
		float time = (System.currentTimeMillis() % 100000L) / 1000f * speed;
		int thick = (int) get("thickness");
		Lines.Colorer col = switch (style) {
			case 1 -> t -> Colors.withAlpha(Colors.hsv(base, 0.85f, 1f), alpha * (0.55f + 0.45f * (float) Math.sin(time * 4f)));
			case 2 -> t -> Colors.withAlpha(Colors.hsv((time * 0.25f + t * 0.3f) % 1f, 0.9f, 1f), alpha);
			case 3 -> t -> Colors.withAlpha(Colors.hsv((base + 0.18f * (float) Math.sin(time * 2f + t * 9f)) % 1f, 0.9f, 1f), alpha);
			case 4 -> t -> Colors.withAlpha(Colors.hsv(base, 0.6f, 1f), alpha * (((int) (t * 14f + time * 6f)) % 2 == 0 ? 1f : 0.35f));
			default -> t -> Colors.withAlpha(Colors.hsv(base, 0.85f, 1f), alpha);
		};

		float fill = get("fill");
		if (fill > 0.01f) {
			Direction d = hit.getSide();
			double[][] q = face(b, d);
			float[][] pts = new float[4][3];
			boolean ok = true;
			for (int i = 0; i < 4 && ok; i++) {
				ok = Projector.project(q[i][0], q[i][1], q[i][2], pts[i]);
			}
			if (ok) {
				Lines.fillPoly(c, pts, Colors.withAlpha(color("color"), fill));
			}
		}

		double[] x = {b.minX, b.maxX}, y = {b.minY, b.maxY}, z = {b.minZ, b.maxZ};
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				Lines.edge(c, x[0], y[i], z[j], x[1], y[i], z[j], thick, col);
				Lines.edge(c, x[i], y[0], z[j], x[i], y[1], z[j], thick, col);
				Lines.edge(c, x[i], y[j], z[0], x[i], y[j], z[1], thick, col);
			}
		}
		if (style == 4) {
			float[] p = new float[3];
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					for (int k = 0; k < 2; k++) {
						if (Projector.project(x[i], y[j], z[k], p)) {
							float tw = 0.5f + 0.5f * (float) Math.sin(time * 5f + i * 2 + j * 3 + k * 5);
							int a = Colors.withAlpha(0xFFFFFF, tw);
							int px = Math.round(p[0]), py = Math.round(p[1]), r = 2 + Math.round(tw * 3);
							c.fill(px - r, py, px + r + 1, py + 1, a);
							c.fill(px, py - r, px + 1, py + r + 1, a);
						}
					}
				}
			}
		}
	}

	private static double[][] face(Box b, Direction d) {
		return switch (d) {
			case UP -> new double[][]{{b.minX, b.maxY, b.minZ}, {b.maxX, b.maxY, b.minZ}, {b.maxX, b.maxY, b.maxZ}, {b.minX, b.maxY, b.maxZ}};
			case DOWN -> new double[][]{{b.minX, b.minY, b.minZ}, {b.maxX, b.minY, b.minZ}, {b.maxX, b.minY, b.maxZ}, {b.minX, b.minY, b.maxZ}};
			case NORTH -> new double[][]{{b.minX, b.minY, b.minZ}, {b.maxX, b.minY, b.minZ}, {b.maxX, b.maxY, b.minZ}, {b.minX, b.maxY, b.minZ}};
			case SOUTH -> new double[][]{{b.minX, b.minY, b.maxZ}, {b.maxX, b.minY, b.maxZ}, {b.maxX, b.maxY, b.maxZ}, {b.minX, b.maxY, b.maxZ}};
			case WEST -> new double[][]{{b.minX, b.minY, b.minZ}, {b.minX, b.minY, b.maxZ}, {b.minX, b.maxY, b.maxZ}, {b.minX, b.maxY, b.minZ}};
			default -> new double[][]{{b.maxX, b.minY, b.minZ}, {b.maxX, b.minY, b.maxZ}, {b.maxX, b.maxY, b.maxZ}, {b.maxX, b.maxY, b.minZ}};
		};
	}
}
