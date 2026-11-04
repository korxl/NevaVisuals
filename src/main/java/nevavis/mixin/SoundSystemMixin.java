package nevavis.mixin;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import nevavis.modules.extra.SoundControl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Sound Controller: отменяет воспроизведение выключенных групп звуков. */
@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {
	@Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
	private void nevavis$mute(SoundInstance sound, CallbackInfo ci) {
		if (SoundControl.muted(sound)) {
			ci.cancel();
		}
	}
}
