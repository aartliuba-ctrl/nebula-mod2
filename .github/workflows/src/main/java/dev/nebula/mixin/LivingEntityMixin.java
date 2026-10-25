package dev.nebula.mixin;

import dev.nebula.module.ModuleManager;
import dev.nebula.modules.SwingAnimations;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "getCurrentSwingDuration", at = @At("RETURN"), cancellable = true, require = 0)
    private void nebula$swingDuration(CallbackInfoReturnable<Integer> cir) {
        if (!((Object) this instanceof LocalPlayer)) return;
        SwingAnimations module = ModuleManager.get(SwingAnimations.class);
        if (module == null || !module.isEnabled()) return;
        int base = cir.getReturnValue();
        cir.setReturnValue(Math.max(1, (int) Math.round(base / module.speed())));
    }
}
