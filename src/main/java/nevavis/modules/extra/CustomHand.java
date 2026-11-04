package nevavis.modules.extra;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.RotationAxis;
import nevavis.modules.Module;
import nevavis.modules.ModuleCategory;
import nevavis.modules.ModuleManager;

/** Custom Hand: размер, положение и наклон руки/предмета от первого лица. */
public final class CustomHand extends Module {
	public static final String ID = "custom_hand";

	public CustomHand() {
		super(ID, "Custom Hand", "Size, position and tilt of your hand and held item", ModuleCategory.ANIMATIONS);
		addSetting("scale", "Size", 0.3f, 1.6f, 0.05f, 0.9f);
		addSetting("x", "Offset X", -0.5f, 0.5f, 0.02f, 0f);
		addSetting("y", "Offset Y", -0.5f, 0.5f, 0.02f, 0f);
		addSetting("z", "Offset Z (depth)", -0.6f, 0.4f, 0.02f, 0f);
		addSetting("tilt", "Tilt (deg)", -45f, 45f, 1f, 0f);
	}

	public static void apply(MatrixStack m, Arm arm) {
		Module mod = ModuleManager.byId(ID);
		if (mod == null || !mod.isEnabled()) {
			return;
		}
		float side = arm == Arm.RIGHT ? 1f : -1f;
		m.translate(side * mod.get("x"), mod.get("y"), mod.get("z"));
		float s = mod.get("scale");
		m.scale(s, s, s);
		m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * mod.get("tilt")));
	}
}
