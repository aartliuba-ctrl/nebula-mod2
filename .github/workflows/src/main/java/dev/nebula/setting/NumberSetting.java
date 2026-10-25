package dev.nebula.setting;

public class NumberSetting extends Setting {
    public final double min, max, step;
    public double value;

    public NumberSetting(String name, double min, double max, double step, double def) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        this.value = def;
    }

    public void set(double v) {
        v = Math.round(v / step) * step;
        value = Math.max(min, Math.min(max, v));
    }

    public void setFromRatio(double r) {
        r = Math.max(0, Math.min(1, r));
        set(min + (max - min) * r);
    }

    public double ratio() {
        return (value - min) / (max - min);
    }

    @Override
    public String serialize() {
        return Double.toString(value);
    }

    @Override
    public void deserialize(String s) {
        try {
            set(Double.parseDouble(s));
        } catch (NumberFormatException ignored) {
        }
    }
}
