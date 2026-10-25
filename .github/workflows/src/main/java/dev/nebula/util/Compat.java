package dev.nebula.util;

import java.lang.reflect.Method;

/**
 * Вызов методов по имени через рефлексию. Нужен там, где сигнатуры в Minecraft 26.x
 * могли поменяться: если метода нет, мод не падает при сборке/запуске, а просто молча
 * пропускает действие.
 */
public final class Compat {
    private Compat() {
    }

    public static boolean call(Object target, String name, Object... args) {
        return invoke(target, name, args) != FAILED;
    }

    private static final Object FAILED = new Object();

    /** Возвращает результат вызова либо null, если метод не найден или упал. */
    public static Object get(Object target, String name, Object... args) {
        Object r = invoke(target, name, args);
        return r == FAILED ? null : r;
    }

    private static Object invoke(Object target, String name, Object[] args) {
        if (target == null) return FAILED;
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (!m.getName().equals(name) || m.getParameterCount() != args.length) continue;
                Object[] conv = convert(m.getParameterTypes(), args);
                if (conv == null) continue;
                try {
                    m.setAccessible(true);
                    return m.invoke(target, conv);
                } catch (Throwable ignored) {
                }
            }
        }
        return FAILED;
    }

    private static Object[] convert(Class<?>[] types, Object[] args) {
        Object[] out = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object a = args[i];
            Class<?> t = types[i];
            if (t == long.class && a instanceof Number n) out[i] = n.longValue();
            else if (t == int.class && a instanceof Number n) out[i] = n.intValue();
            else if (t == float.class && a instanceof Number n) out[i] = n.floatValue();
            else if (t == double.class && a instanceof Number n) out[i] = n.doubleValue();
            else if (t == boolean.class && a instanceof Boolean b) out[i] = b;
            else if (!t.isPrimitive() && t.isInstance(a)) out[i] = a;
            else return null;
        }
        return out;
    }
}
