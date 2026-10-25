package dev.nebula;

import dev.nebula.module.ModuleManager;
import dev.nebula.modules.Ambience;
import dev.nebula.modules.NameTags;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HudRenderer {
    private HudRenderer() {
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker tracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        Ambience ambience = ModuleManager.get(Ambience.class);
        if (ambience != null && ambience.isEnabled()) {
            ambience.renderTint(g, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
        }

        NameTags tags = ModuleManager.get(NameTags.class);
        if (tags != null && tags.isEnabled()) {
            tags.render(g, mc, tracker.getGameTimeDeltaPartialTick(false));
        }
    }
}
