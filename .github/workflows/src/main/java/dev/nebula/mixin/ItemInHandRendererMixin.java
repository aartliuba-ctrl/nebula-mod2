package dev.nebula.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.nebula.module.ModuleManager;
import dev.nebula.modules.SwingAnimations;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    // require = 0: если метод в 26.2 называется иначе, мод всё равно загрузится (анимации просто не сработают)
    @Inject(method = "applyItemArmAttackTransform", at = @At("HEAD"), cancellable = true, require = 0)
    private void nebula$swing(PoseStack poseStack, HumanoidArm arm, float attackValue, CallbackInfo ci) {
        SwingAnimations module = ModuleManager.get(SwingAnimations.class);
        if (module != null && module.isEnabled()) {
            module.apply(poseStack, arm, attackValue);
            ci.cancel();
        }
    }
}
