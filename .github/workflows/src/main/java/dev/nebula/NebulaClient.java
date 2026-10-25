package dev.nebula;

import com.mojang.blaze3d.platform.InputConstants;
import dev.nebula.config.Config;
import dev.nebula.gui.ClickGui;
import dev.nebula.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NebulaClient implements ClientModInitializer {
    public static final String MOD_ID = "nebula";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyMapping openGui;

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        Config.load();

        openGui = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.nebula.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openGui.consumeClick()) {
                if (mc.screen == null) mc.setScreen(new ClickGui());
            }
            if (mc.player != null && mc.level != null) {
                ModuleManager.tick(mc);
            }
        });

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.CHAT,
                Identifier.fromNamespaceAndPath(MOD_ID, "overlay"),
                HudRenderer::render);

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> Config.save());
        LOGGER.info("Nebula Client загружен. Открыть меню: Right Shift");
    }
}
