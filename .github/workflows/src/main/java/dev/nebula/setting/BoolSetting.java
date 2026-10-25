package dev.nebula.setting;

public class BoolSetting extends Setting {
    public boolean value;

    public BoolSetting(String name, boolean def) {
        super(name);
        this.value = def;
    }

    @Override
    public String serialize() {
        return Boolean.toString(value);
    }

    @Override
    public void deserialize(String s) {
        value = Boolean.parseBoolean(s);
    }
}
