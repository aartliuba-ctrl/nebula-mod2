package dev.nebula.module;

import dev.nebula.setting.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();

    private boolean enabled;
    public boolean expanded;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected <T extends Setting> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        if (value) onEnable();
        else onDisable();
    }

    /** Используется при загрузке конфига: без вызова onEnable. */
    public void loadEnabled(boolean value) {
        enabled = value;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    public void onTick(Minecraft mc) {
    }
}
