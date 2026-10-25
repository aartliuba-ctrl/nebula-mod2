package dev.nebula.setting;

public class ModeSetting extends Setting {
    public final String[] modes;
    public int index;

    public ModeSetting(String name, String... modes) {
        super(name);
        this.modes = modes;
    }

    public String get() {
        return modes[index];
    }

    public boolean is(String mode) {
        return modes[index].equals(mode);
    }

    public void next() {
        index = (index + 1) % modes.length;
    }

    @Override
    public String serialize() {
        return get();
    }

    @Override
    public void deserialize(String s) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(s)) {
                index = i;
                return;
            }
        }
    }
}
