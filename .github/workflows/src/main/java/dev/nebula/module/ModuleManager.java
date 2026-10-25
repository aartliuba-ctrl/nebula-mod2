package dev.nebula.module;

import dev.nebula.NebulaClient;
import dev.nebula.modules.Ambience;
import dev.nebula.modules.AutoMine;
import dev.nebula.modules.NameTags;
import dev.nebula.modules.SwingAnimations;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    private ModuleManager() {
    }

    public static void init() {
        MODULES.add(new SwingAnimations());
        MODULES.add(new NameTags());
        MODULES.add(new Ambience());
        MODULES.add(new AutoMine());
    }

    public static List<Module> all() {
        return Collections.unmodifiableList(MODULES);
    }

    public static List<Module> byCategory(Category category) {
        List<Module> out = new ArrayList<>();
        for (Module m : MODULES) {
            if (m.category == category) out.add(m);
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(Class<T> type) {
        for (Module m : MODULES) {
            if (m.getClass() == type) return (T) m;
        }
        return null;
    }

    public static void tick(Minecraft mc) {
        for (Module m : MODULES) {
            if (!m.isEnabled()) continue;
            try {
                m.onTick(mc);
            } catch (Throwable t) {
                NebulaClient.LOGGER.error("Ошибка в модуле {}", m.name, t);
            }
        }
    }
}
