package dev.nebula.modules;

import dev.nebula.gui.Draw;
import dev.nebula.gui.Theme;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Неймтеги поверх экрана (видны сквозь стены) с полоской здоровья.
 * Рисуются в HUD через проекцию мировых координат на экран.
 */
public class NameTags extends Module {
    public final NumberSetting range = add(new NumberSetting("Дистанция", 8, 128, 4, 64));
    public final BoolSetting health = add(new BoolSetting("Здоровье", true));
    public final BoolSetting distance = add(new BoolSetting("Дистанция в тексте", true));

    public NameTags() {
        super("NameTags", "Неймтеги игроков со здоровьем, видны сквозь стены", Category.RENDER);
    }

    public void render(GuiGraphicsExtractor g, Minecraft mc, float partial) {
        if (!mc.options.getCameraType().isFirstPerson()) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        double aspect = (double) sw / sh;
        double tanHalf = Math.tan(Math.toRadians(mc.options.fov().get()) / 2.0);

        Vec3 eye = mc.player.getEyePosition(partial);
        Vec3 look = mc.player.getViewVector(partial).normalize();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 up = right.cross(look);

        for (AbstractClientPlayer p : mc.level.players()) {
            if (p == mc.player || p.isInvisible() && p.isSpectator()) continue;
            float dist = mc.player.distanceTo(p);
            if (dist > range.value) continue;

            Vec3 pos = p.getPosition(partial).add(0, p.getBbHeight() + 0.35, 0);
            Vec3 d = pos.subtract(eye);
            double zc = d.dot(look);
            if (zc < 0.2) continue;
            double xc = d.dot(right);
            double yc = d.dot(up);

            double ndcX = xc / (zc * tanHalf * aspect);
            double ndcY = yc / (zc * tanHalf);
            int x = (int) (sw / 2.0 * (1 + ndcX));
            int y = (int) (sh / 2.0 * (1 - ndcY));
            if (x < -100 || x > sw + 100 || y < -50 || y > sh + 50) continue;

            drawTag(g, p, x, y, dist);
        }
    }

    private void drawTag(GuiGraphicsExtractor g, AbstractClientPlayer p, int cx, int bottom, float dist) {
        String name = p.getName().getString();
        String info = "";
        if (health.value) info += (int) Math.ceil(p.getHealth()) + " HP";
        if (distance.value) info += (info.isEmpty() ? "" : " · ") + (int) dist + "m";

        int nameW = Draw.width(name);
        int infoW = info.isEmpty() ? 0 : Draw.width(info) + 6;
        int w = nameW + infoW + 8;
        int h = 13;
        int x = cx - w / 2;
        int y = bottom - h - 4;

        Draw.bordered(g, x, y, w, h, 3, 0xCC0E0E12, 0xAA000000);
        Draw.text(g, name, x + 4, y + 3, Theme.TEXT);
        if (!info.isEmpty()) {
            Draw.text(g, info, x + 4 + nameW + 6, y + 3, Theme.GRAY);
        }

        if (health.value) {
            float ratio = Math.max(0f, Math.min(1f, p.getHealth() / p.getMaxHealth()));
            int barW = w - 2;
            int rr = (int) (255 * (1 - ratio));
            int gg = (int) (255 * ratio);
            Draw.rect(g, x + 1, y + h - 2, x + 1 + barW, y + h - 1, 0xFF2A2A30);
            Draw.rect(g, x + 1, y + h - 2, x + 1 + (int) (barW * ratio), y + h - 1,
                    0xFF000000 | (rr << 16) | (gg << 8) | 0x50);
        }
    }
}
