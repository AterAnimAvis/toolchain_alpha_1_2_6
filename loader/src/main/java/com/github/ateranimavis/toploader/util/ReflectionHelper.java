package com.github.ateranimavis.toploader.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectionHelper {

    public static Field getFieldByIndex(Class<?> clazz, int index) throws SecurityException, NoSuchFieldException {
        try {
            Field field = clazz.getDeclaredFields()[index];
            field.setAccessible(true);
            return field;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NoSuchFieldException(index + " is out of bounds in " + clazz.getSimpleName() + ".");
        }
    }

    public static Field getField(Class<?> clazz, String name) throws SecurityException, NoSuchFieldException {
        //TODO: Obfuscation Handling?
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... paramTypes) throws NoSuchMethodException {
        Constructor<T> constructor = clazz.getDeclaredConstructor(paramTypes);
        constructor.setAccessible(true);
        return constructor;
    }

    public static Field unfinalizeField(Field field) throws SecurityException, NoSuchFieldException, IllegalAccessException {
        Field modifiers = ReflectionHelper.getField(Field.class, "modifiers");
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        return field;
    }
}
