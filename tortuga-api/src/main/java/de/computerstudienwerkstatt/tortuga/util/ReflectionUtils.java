package de.computerstudienwerkstatt.tortuga.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mischa Holz
 */
public class ReflectionUtils {


    public static List<Field> getAllFieldsOfClass(Class<?> checkingCLass) {
        List<Field> fields = new ArrayList<>();

        Class<?> clazz = checkingCLass;
        while(clazz.getSuperclass() != null) {
            for(Field field : clazz.getDeclaredFields()) {
                if(Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if(Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                fields.add(field);
            }

            clazz = clazz.getSuperclass();
        }

        return fields;
    }
}
