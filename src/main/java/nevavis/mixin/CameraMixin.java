package nevavis.mixin;

import net.minecraft.client.render.Camera;
import nevavis.modules.extra.FreeLook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** FreeLook: подмена углов камеры (yaw и pitch — два float-аргумента setRotation). */
@Mixin(Camera.class)
public abstract class CameraMixin {
	@ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float nevavis$yaw(float yaw) {
		return FreeLook.yaw(yaw);
	}

	@ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 1)
	private float nevavis$pitch(float pitch) {
		return FreeLook.pitch(pitch);
	}
}
