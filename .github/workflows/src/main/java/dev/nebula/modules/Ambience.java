package dev.nebula.modules;

import dev.nebula.NebulaClient;
import dev.nebula.gui.Draw;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.ModeSetting;
import dev.nebula.setting.NumberSetting;
import dev.nebula.util.Compat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Клиентская атмосфера: своё время суток, погода и цветной фильтр экрана. */
public class Ambience extends Module {
    public final BoolSetting customTime = add(new BoolSetting("Своё время", true));
    public final NumberSetting time = add(new NumberSetting("Время суток", 0, 24000, 500, 18000));
    public final ModeSetting weather = add(new ModeSetting("Погода", "Default", "Clear", "Rain", "Thunder"));
    public final ModeSetting filter = add(new ModeSetting("Фильтр", "None", "Sunset", "Night", "Neon"));
    public final NumberSetting intensity = add(new NumberSetting("Интенсивность", 5, 100, 5, 40));

    private boolean warnedTime;

    public Ambience() {
        super("Ambience", "Время суток, погода и цветной фильтр (только у вас)", Category.RENDER);
    }

    @Override
    public void onTick(Minecraft mc) {
        Object level = mc.level;
        if (level == null) return;

        if (customTime.value) {
            long t = (long) time.value;
            boolean ok = Compat.call(level, "setDayTime", t);
            if (!ok) {
                Object gt = Compat.get(level, "getGameTime");
                long game = gt instanceof Number n ? n.longValue() : 0L;
                ok = Compat.call(level, "setTimeFromServer", game, t, false);
            }
            if (!ok && !warnedTime) {
                warnedTime = true;
                NebulaClient.LOGGER.warn("Ambience: не нашёл метод установки времени в этой версии игры");
            }
        }

        switch (weather.get()) {
            case "Clear" -> setWeather(level, 0f, 0f);
            case "Rain" -> setWeather(level, 1f, 0f);
            case "Thunder" -> setWeather(level, 1f, 1f);
            default -> {
            }
        }
    }

    private static void setWeather(Object level, float rain, float thunder) {
        Compat.call(level, "setRainLevel", rain);
        Compat.call(level, "setThunderLevel", thunder);
    }

    public void renderTint(GuiGraphicsExtractor g, int w, int h) {
        int rgb;
        switch (filter.get()) {
            case "Sunset" -> rgb = 0xFF7A3C;
            case "Night" -> rgb = 0x1A2A6C;
            case "Neon" -> rgb = 0xB400FF;
            default -> {
                return;
            }
        }
        int alpha = (int) (intensity.value / 100.0 * 0.4 * 255);
        Draw.rect(g, 0, 0, w, h, (alpha << 24) | rgb);
    }
}
