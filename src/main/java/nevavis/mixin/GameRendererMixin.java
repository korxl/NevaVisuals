package nevavis.mixin;

import net.minecraft.client.render.GameRenderer;
import nevavis.modules.extra.AspectRatioModule;
import nevavis.render.Projector;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Aspect Ratio (масштаб проекции по X) и запоминание реального FOV для оверлеев. */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@ModifyVariable(method = "getBasicProjectionMatrix", at = @At("HEAD"), argsOnly = true)
	private float nevavis$fov(float fov) {
		Projector.captureFov(fov);
		return fov;
	}

	@Inject(method = "getBasicProjectionMatrix", at = @At("RETURN"), cancellable = true)
	private void nevavis$aspect(CallbackInfoReturnable<Matrix4f> cir) {
		float k = AspectRatioModule.factor();
		if (k != 1f) {
			cir.setReturnValue(cir.getReturnValue().scale(k, 1f, 1f));
		}
	}
}
