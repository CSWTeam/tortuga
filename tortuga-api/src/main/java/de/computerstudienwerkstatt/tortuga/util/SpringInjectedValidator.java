package de.computerstudienwerkstatt.tortuga.util;

import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * @author Mischa Holz
 */
public abstract class SpringInjectedValidator<ANNOTATION extends Annotation, MODEL> implements ConstraintValidator<ANNOTATION, MODEL> {

    private EntityManager em;

    @Override
    public void initialize(ANNOTATION constraintAnnotation) {
        em = ApplicationContextProvider.getContext().getBean(EntityManagerHolder.class).getEntityManager();

        for(Field field : this.getClass().getDeclaredFields()) {
            if(field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                try {
                    field.set(this, ApplicationContextProvider.getContext().getBean(field.getType()));
                } catch(IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    protected abstract boolean _isValid(MODEL value, ConstraintValidatorContext context);

    @Override
    final public boolean isValid(MODEL value, ConstraintValidatorContext context) {
        try {
            em.setFlushMode(FlushModeType.COMMIT);

            return _isValid(value, context);
        } finally {
            em.setFlushMode(FlushModeType.AUTO);
        }
    }
}
