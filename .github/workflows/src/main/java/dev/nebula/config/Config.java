package dev.nebula.config;

import dev.nebula.NebulaClient;
import dev.nebula.module.Module;
import dev.nebula.module.ModuleManager;
import dev.nebula.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("nebula.properties");
    }

    public static void load() {
        Path path = file();
        if (!Files.exists(path)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
        } catch (IOException e) {
            NebulaClient.LOGGER.warn("Не удалось прочитать конфиг", e);
            return;
        }
        for (Module m : ModuleManager.all()) {
            String en = p.getProperty(m.name + ".enabled");
            if (en != null) m.loadEnabled(Boolean.parseBoolean(en));
            for (Setting s : m.settings) {
                String v = p.getProperty(m.name + "." + s.name);
                if (v != null) s.deserialize(v);
            }
        }
    }

    public static void save() {
        Properties p = new Properties();
        for (Module m : ModuleManager.all()) {
            p.setProperty(m.name + ".enabled", Boolean.toString(m.isEnabled()));
            for (Setting s : m.settings) {
                p.setProperty(m.name + "." + s.name, s.serialize());
            }
        }
        try (OutputStream out = Files.newOutputStream(file())) {
            p.store(out, "Nebula Client");
        } catch (IOException e) {
            NebulaClient.LOGGER.warn("Не удалось сохранить конфиг", e);
        }
    }
}
