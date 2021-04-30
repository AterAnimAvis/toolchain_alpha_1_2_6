package net.minecraft.src.modloader;

import java.lang.reflect.Field;

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
}
