package dev.nebula.gui;

import dev.nebula.config.Config;
import dev.nebula.module.Category;
import dev.nebula.module.Module;
import dev.nebula.module.ModuleManager;
import dev.nebula.setting.BoolSetting;
import dev.nebula.setting.ModeSetting;
import dev.nebula.setting.NumberSetting;
import dev.nebula.setting.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleConsumer;

/** Меню в стиле скриншота: сайдбар с категориями, поиск сверху, карточки модулей в два столбца. */
public class ClickGui extends Screen {
    private static final int W = 560, H = 330, SIDE = 120;
    private static final int KEY_BACKSPACE = 259;

    private Category category = Category.RENDER;
    private String search = "";
    private double scroll;

    private final List<Hit> hits = new ArrayList<>();
    private Hit dragging;

    private record Hit(int x, int y, int w, int h, Runnable click, DoubleConsumer drag) {
        boolean in(double mx, double my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }
    }

    private record Placed(Module module, int col, int offset, int height) {
    }

    public ClickGui() {
        super(Component.literal("Nebula"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        Config.save();
    }

    // ------------------------------------------------------------------ отрисовка

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        hits.clear();
        int x = (width - W) / 2;
        int y = (height - H) / 2;

        Draw.rect(g, 0, 0, width, height, Theme.DIM);
        Draw.bordered(g, x, y, W, H, 9, Theme.BG, Theme.BORDER);

        drawSidebar(g, x, y);
        drawHeader(g, x, y);
        drawContent(g, x, y);
    }

    private void drawSidebar(GuiGraphicsExtractor g, int x, int y) {
        Draw.text(g, "NEBULA", x + 18, y + 16, Theme.ACCENT);
        Draw.rect(g, x + 8, y + 36, x + SIDE - 4, y + 37, Theme.DIVIDER);
        Draw.text(g, "ОБЩЕЕ", x + 16, y + 50, Theme.GRAY);

        int ry = y + 64;
        for (Category c : Category.values()) {
            final Category target = c;
            boolean active = c == category && search.isEmpty();
            if (active) Draw.round(g, x + 8, ry, SIDE - 16, 24, 6, Theme.SIDEBAR_ACTIVE);
            Draw.round(g, x + 16, ry + 9, 6, 6, 3, active ? Theme.ACCENT : Theme.TRACK);
            Draw.text(g, c.title, x + 28, ry + 8, active ? Theme.TEXT : Theme.GRAY);
            hits.add(new Hit(x + 8, ry, SIDE - 16, 24, () -> {
                category = target;
                scroll = 0;
                search = "";
            }, null));
            ry += 28;
        }
        Draw.rect(g, x + SIDE, y + 8, x + SIDE + 1, y + H - 8, Theme.DIVIDER);
    }

    private void drawHeader(GuiGraphicsExtractor g, int x, int y) {
        int hx = x + SIDE + 14;
        String crumb = "Client / ";
        Draw.text(g, crumb, hx, y + 16, Theme.GRAY);
        Draw.text(g, search.isEmpty() ? category.title : "Поиск", hx + Draw.width(crumb), y + 16, Theme.TEXT);

        int sw = 150;
        int sx = x + W - sw - 14;
        Draw.bordered(g, sx, y + 10, sw, 20, 6, Theme.INPUT, Theme.BORDER);
        boolean caret = (System.currentTimeMillis() / 500) % 2 == 0;
        String shown = search.isEmpty() ? "Поиск..." : search + (caret ? "_" : "");
        Draw.text(g, Draw.trim(shown, sw - 16), sx + 8, y + 16, search.isEmpty() ? Theme.GRAY : Theme.TEXT);
        Draw.rect(g, x + SIDE + 8, y + 38, x + W - 8, y + 39, Theme.DIVIDER);
    }

    private void drawContent(GuiGraphicsExtractor g, int x, int y) {
        int cx = x + SIDE + 12;
        int cy = y + 46;
        int cw = W - SIDE - 24;
        int ch = H - 56;
        int gap = 8;
        int colW = (cw - gap) / 2;

        // раскладка карточек по двум столбцам
        List<Module> list = visibleModules();
        List<Placed> placed = new ArrayList<>();
        int[] colH = {0, 0};
        for (Module m : list) {
            int h = cardHeight(m);
            int col = colH[0] <= colH[1] ? 0 : 1;
            placed.add(new Placed(m, col, colH[col], h));
            colH[col] += h + gap;
        }
        int maxScroll = Math.max(0, Math.max(colH[0], colH[1]) - gap - ch);
        scroll = Math.max(0, Math.min(maxScroll, scroll));

        Draw.scissorOn(g, cx - 2, cy, cx + cw + 2, cy + ch);
        for (Placed p : placed) {
            int px = cx + p.col() * (colW + gap);
            int py = cy + p.offset() - (int) scroll;
            if (py + p.height() < cy || py > cy + ch) continue;
            drawCard(g, p.module(), px, py, colW, p.height(), cy, cy + ch);
        }
        Draw.scissorOff(g);

        // тонкий индикатор прокрутки
        if (maxScroll > 0) {
            int trackH = ch - 8;
            int thumbH = Math.max(18, (int) (trackH * (double) ch / (ch + maxScroll)));
            int thumbY = cy + 4 + (int) ((trackH - thumbH) * (scroll / maxScroll));
            Draw.round(g, x + W - 7, thumbY, 2, thumbH, 1, Theme.ACCENT);
        }
    }

    private List<Module> visibleModules() {
        List<Module> out = new ArrayList<>();
        String q = search.toLowerCase(Locale.ROOT);
        for (Module m : ModuleManager.all()) {
            if (q.isEmpty()) {
                if (m.category == category) out.add(m);
            } else if (m.name.toLowerCase(Locale.ROOT).contains(q)
                    || m.description.toLowerCase(Locale.ROOT).contains(q)) {
                out.add(m);
            }
        }
        return out;
    }

    private int cardHeight(Module m) {
        int h = 36;
        if (m.expanded && !m.settings.isEmpty()) {
            for (Setting s : m.settings) h += rowHeight(s);
            h += 6;
        }
        return h;
    }

    private int rowHeight(Setting s) {
        if (s instanceof NumberSetting) return 26;
        if (s instanceof ModeSetting) return 20;
        return 18;
    }

    private void drawCard(GuiGraphicsExtractor g, Module m, int x, int y, int w, int h, int top, int bottom) {
        Draw.bordered(g, x, y, w, h, 6, Theme.CARD, Theme.BORDER);

        Draw.text(g, m.name, x + 10, y + 8, Theme.TEXT);
        Draw.text(g, Draw.trim(m.description, w - 56), x + 10, y + 21, Theme.GRAY);

        // переключатель модуля
        drawSwitch(g, x + w - 36, y + 10, 26, 14, m.isEnabled());
        addHit(x + w - 40, y + 4, 34, 26, top, bottom, m::toggle, null);
        // клик по шапке карточки разворачивает настройки
        if (!m.settings.isEmpty()) {
            addHit(x, y, w - 42, 34, top, bottom, () -> m.expanded = !m.expanded, null);
        }

        if (!m.expanded || m.settings.isEmpty()) return;

        Draw.rect(g, x + 8, y + 35, x + w - 8, y + 36, Theme.DIVIDER);
        int sy = y + 40;
        for (Setting s : m.settings) {
            int rh = rowHeight(s);
            if (s instanceof BoolSetting b) {
                Draw.text(g, s.name, x + 10, sy + 4, Theme.TEXT);
                drawSwitch(g, x + w - 32, sy + 3, 22, 11, b.value);
                addHit(x + 6, sy, w - 12, rh, top, bottom, () -> b.value = !b.value, null);
            } else if (s instanceof NumberSetting n) {
                String val = format(n);
                Draw.text(g, s.name, x + 10, sy + 2, Theme.TEXT);
                Draw.text(g, val, x + w - 10 - Draw.width(val), sy + 2, Theme.GRAY);
                int tx = x + 10;
                int tw = w - 20;
                int ty = sy + 16;
                Draw.round(g, tx, ty, tw, 3, 1, Theme.TRACK);
                int fw = (int) (tw * n.ratio());
                Draw.round(g, tx, ty, Math.max(2, fw), 3, 1, Theme.ACCENT);
                Draw.round(g, tx + fw - 3, ty - 2, 7, 7, 3, Theme.TEXT);
                final int fx = tx;
                final int fwTrack = tw;
                addHit(tx - 3, sy + 11, tw + 6, 12, top, bottom, null,
                        mx -> n.setFromRatio((mx - fx) / fwTrack));
            } else if (s instanceof ModeSetting ms) {
                Draw.text(g, s.name, x + 10, sy + 5, Theme.TEXT);
                String val = ms.get();
                int pw = Draw.width(val) + 16;
                Draw.bordered(g, x + w - 10 - pw, sy + 1, pw, 15, 5, Theme.INPUT, Theme.BORDER);
                Draw.text(g, val, x + w - 10 - pw + 8, sy + 5, Theme.TEXT);
                addHit(x + w - 10 - pw, sy + 1, pw, 15, top, bottom, ms::next, null);
            }
            sy += rh;
        }
    }

    private void drawSwitch(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean on) {
        Draw.round(g, x, y, w, h, h / 2, on ? Theme.ACCENT : Theme.TRACK);
        int k = h - 4;
        int kx = on ? x + w - k - 2 : x + 2;
        Draw.round(g, kx, y + 2, k, k, k / 2, Theme.TEXT);
    }

    private static String format(NumberSetting n) {
        if (n.step >= 1) return Integer.toString((int) Math.round(n.value));
        return String.format(Locale.ROOT, "%.1f", n.value);
    }

    /** Регистрирует кликабельную область, обрезая её по видимой области контента. */
    private void addHit(int x, int y, int w, int h, int top, int bottom, Runnable click, DoubleConsumer drag) {
        int y1 = Math.max(y, top);
        int y2 = Math.min(y + h, bottom);
        if (y2 <= y1) return;
        hits.add(new Hit(x, y1, w, y2 - y1, click, drag));
    }

    // ------------------------------------------------------------------ ввод

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            for (int i = hits.size() - 1; i >= 0; i--) {
                Hit hit = hits.get(i);
                if (!hit.in(event.x(), event.y())) continue;
                if (hit.click() != null) hit.click().run();
                if (hit.drag() != null) {
                    dragging = hit;
                    hit.drag().accept(event.x());
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging != null) {
            dragging.drag().accept(event.x());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll -= scrollY * 20;
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            scroll = 0;
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        int cp = event.codepoint();
        if (cp >= 32 && cp != 127 && search.length() < 24) {
            search += new String(Character.toChars(cp));
            scroll = 0;
            return true;
        }
        return false;
    }
}
