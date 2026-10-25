package dev.nebula.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * ВСЕ вызовы рендер-API Minecraft сосредоточены здесь.
 * Если в 26.2 какой-то метод называется иначе (fill / text / enableScissor ...),
 * править нужно только этот файл.
 */
public final class Draw {
    private Draw() {
    }

    // ---------- примитивы Minecraft API ----------

    public static void rect(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y2, color);
    }

    public static void text(GuiGraphicsExtractor g, String s, int x, int y, int color) {
        g.text(Minecraft.getInstance().font, s, x, y, color, false);
    }

    public static int width(String s) {
        return Minecraft.getInstance().font.width(s);
    }

    public static void scissorOn(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2) {
        g.enableScissor(x1, y1, x2, y2);
    }

    public static void scissorOff(GuiGraphicsExtractor g) {
        g.disableScissor();
    }

    // ---------- производные ----------

    /** Прямоугольник со скруглёнными углами (радиус r, пиксельная аппроксимация). */
    public static void round(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int color) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        rect(g, x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int inset = (int) Math.ceil(r - Math.sqrt(r * r - dy * dy));
            rect(g, x + inset, y + i, x + w - inset, y + i + 1, color);
            rect(g, x + inset, y + h - i - 1, x + w - inset, y + h - i, color);
        }
    }

    public static void bordered(GuiGraphicsExtractor g, int x, int y, int w, int h, int r, int fill, int border) {
        round(g, x - 1, y - 1, w + 2, h + 2, r + 1, border);
        round(g, x, y, w, h, r, fill);
    }

    public static String trim(String s, int maxWidth) {
        if (width(s) <= maxWidth) return s;
        while (s.length() > 1 && width(s + "...") > maxWidth) {
            s = s.substring(0, s.length() - 1);
        }
        return s + "...";
    }
}
