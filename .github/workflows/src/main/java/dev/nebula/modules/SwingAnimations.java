package dev.nebula.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.ModeSetting;
import dev.nebula.setting.NumberSetting;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

/** Свои анимации взмаха рукой/предметом от первого лица + управление скоростью взмаха. */
public class SwingAnimations extends Module {
    public final ModeSetting mode = add(new ModeSetting("Анимация", "Smooth", "Spin", "Poke", "Chop", "Slide"));
    public final NumberSetting scale = add(new NumberSetting("Сила", 0.2, 2.0, 0.1, 1.0));
    public final NumberSetting speed = add(new NumberSetting("Скорость взмаха", 0.5, 3.0, 0.1, 1.0));

    public SwingAnimations() {
        super("SwingAnimations", "Кастомные анимации взмаха и скорость свинга", Category.RENDER);
    }

    public double speed() {
        return Math.max(0.1, speed.value);
    }

    /** Вызывается из миксина вместо ванильного applyItemArmAttackTransform. */
    public void apply(PoseStack pose, HumanoidArm arm, float p) {
        float i = arm == HumanoidArm.RIGHT ? 1f : -1f;
        float s = (float) scale.value;
        float sinP = Mth.sin(p * Mth.PI);

        pose.mulPose(Axis.YP.rotationDegrees(i * 45f));
        switch (mode.get()) {
            case "Spin" -> pose.mulPose(Axis.XP.rotationDegrees(-p * 360f * s));
            case "Poke" -> {
                pose.translate(0f, 0f, -sinP * 0.6f * s);
                pose.mulPose(Axis.XP.rotationDegrees(-sinP * 15f * s));
            }
            case "Chop" -> {
                pose.translate(0f, -sinP * 0.2f * s, 0f);
                pose.mulPose(Axis.XP.rotationDegrees(-sinP * 110f * s));
            }
            case "Slide" -> {
                pose.translate(i * -sinP * 0.5f * s, 0f, 0f);
                pose.mulPose(Axis.ZP.rotationDegrees(i * -sinP * 60f * s));
            }
            default -> { // Smooth
                pose.mulPose(Axis.XP.rotationDegrees(-sinP * 70f * s));
                pose.mulPose(Axis.ZP.rotationDegrees(i * -sinP * 15f * s));
            }
        }
        pose.mulPose(Axis.YP.rotationDegrees(i * -45f));
    }
}
