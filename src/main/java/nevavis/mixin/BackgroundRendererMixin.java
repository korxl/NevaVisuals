package nevavis.mixin;

import net.minecraft.client.render.BackgroundRenderer;
import nevavis.modules.visuals.FogModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Custom Fog: подменяет результат applyFog. */
@Mixin(BackgroundRenderer.class)
public abstract class BackgroundRendererMixin {
	@Inject(method = "applyFog", at = @At("RETURN"), cancellable = true)
	private static void nevavis$fog(CallbackInfoReturnable<Object> cir) {
		Object r = FogModule.apply(cir.getReturnValue());
		if (r != null) {
			cir.setReturnValue(r);
		}
	}
}
