package nevavis.mixin;

import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import nevavis.modules.extra.CustomHand;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import nevavis.modules.visuals.SwingModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Меняет прогресс взмаха руки (единственный float-аргумент applySwingOffset). */
@Mixin(HeldItemRenderer.class)
public abstract class SwingMixin {
	@ModifyVariable(method = "applySwingOffset", at = @At("HEAD"), argsOnly = true)
	private float nevavis$swing(float progress) {
		return SwingModule.remap(progress);
	}

	/** Custom Hand: после стандартного смещения руки применяем размер/положение/наклон. */
	@Inject(method = "applyEquipOffset", at = @At("RETURN"))
	private void nevavis$hand(MatrixStack matrices, Arm arm, float equipProgress, CallbackInfo ci) {
		CustomHand.apply(matrices, arm);
	}
}
