package de.computerstudienwerkstatt.tortuga.patch;

import de.computerstudienwerkstatt.tortuga.controller.base.ChangeSet;
import de.computerstudienwerkstatt.tortuga.model.base.PersistentEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mischa Holz
 */
public class Patch {

    public static <T extends PersistentEntity> T patch(T base, ChangeSet<T> changeSet) {
        T patch = changeSet.getPatch();

        if(!base.getClass().equals(patch.getClass())) {
            throw new IllegalArgumentException("Both objects need to be the exact same class");
        }

        List<Field> fields = new ArrayList<>();

        Class<?> clazz = base.getClass();
        while(!clazz.equals(PersistentEntity.class)) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

            clazz = clazz.getSuperclass();
        }

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value;
                value = field.get(patch);
                if(value != null || changeSet.getPatchedFields().contains(field.getName())) {
                    field.set(base, value);
                }
            } catch (IllegalAccessException e) {
                // impossible
            }
        }

        return base;
    }

}
