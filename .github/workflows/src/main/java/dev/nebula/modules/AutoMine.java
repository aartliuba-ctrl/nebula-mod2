package dev.nebula.modules;

import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.ModeSetting;
import dev.nebula.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;

/** Автоматически копает блок, на который направлен прицел. */
public class AutoMine extends Module {
    public final ModeSetting mode = add(new ModeSetting("Режим", "Hold", "Pulse"));
    public final NumberSetting period = add(new NumberSetting("Период (тики)", 2, 20, 1, 6));
    public final BoolSetting pauseUse = add(new BoolSetting("Пауза при еде/использовании", true));

    private int timer;
    private boolean pressed;

    public AutoMine() {
        super("AutoMine", "Автоматически копает блок под прицелом", Category.MISC);
    }

    @Override
    protected void onDisable() {
        release(Minecraft.getInstance());
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.screen != null) {
            release(mc);
            return;
        }
        if (pauseUse.value && mc.player.isUsingItem()) {
            release(mc);
            return;
        }
        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            release(mc);
            return;
        }

        if (mode.is("Hold")) {
            mc.options.keyAttack.setDown(true);
            pressed = true;
        } else {
            int p = (int) period.value;
            timer = (timer + 1) % p;
            boolean down = timer < Math.max(1, p / 2);
            mc.options.keyAttack.setDown(down);
            pressed = true;
        }
    }

    private void release(Minecraft mc) {
        if (pressed && mc != null && mc.options != null) {
            mc.options.keyAttack.setDown(false);
        }
        pressed = false;
        timer = 0;
    }
}
