package de.computerstudienwerkstatt.tortuga;

import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import de.computerstudienwerkstatt.tortuga.util.ReflectionUtils;

import javax.persistence.Entity;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;


/**
 * @author Mischa Holz
 */
public class EntitiesDontUsePrimitivesTest {

    private static class EntityWithPrimitive {
        int primitive;
    }

    private static class EntityWithPrimitiveInSuper extends EntityWithPrimitive {}

    private static class Result {
        public boolean result;
        public String description;

        public Result(boolean result, String description) {
            this.result = result;
            this.description = description;
        }
    }

    @Test
    public void testEntityThatUsesPrimitives() throws Exception {
        Result result = testForPrimitivesInClass(EntityWithPrimitive.class);
        assertFalse("This result should be false", result.result);

        result = testForPrimitivesInClass(EntityWithPrimitiveInSuper.class);
        assertFalse("This result should be false as well", result.result);
    }

    @Test
    public void testAllClassesWithEntityAnnotation() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        List<Result> results = new ArrayList<>();

        for(BeanDefinition bd : scanner.findCandidateComponents("de.computerstudienwerkstatt.tortuga")) {
            String className = bd.getBeanClassName();
            Class<?> clazz = this.getClass().getClassLoader().loadClass(className);
            Result result = testForPrimitivesInClass(clazz);

            results.add(result);
        }

        StringBuilder builder = new StringBuilder();
        for(Result result : results) {
            if(!result.result) {
                builder.append(result.description);
                builder.append("\n\n\n");
            }
        }

        assertFalse(builder.toString(), results.stream().filter(r -> !r.result).findAny().isPresent());
    }

    private static final Class<?>[] primitives = { int.class, boolean.class, long.class, double.class, float.class, char.class, byte.class };

    private Result testForPrimitivesInClass(Class<?> checkingCLass) {
        List<String> primitiveFields = new ArrayList<>();

        List<Field> classFields = ReflectionUtils.getAllFieldsOfClass(checkingCLass);

        for(Field field : classFields) {
            for(Class<?> primitive : primitives) {
                if(primitive.equals(field.getType())) {
                    primitiveFields.add(field.getName());
                }
            }
        }

        String str = checkingCLass.getName() + " contains primitives: ";

        for(String primitiveField : primitiveFields) {
            str += "\n";
            str += primitiveField;
        }

        return new Result(primitiveFields.isEmpty(), str);
    }

}
