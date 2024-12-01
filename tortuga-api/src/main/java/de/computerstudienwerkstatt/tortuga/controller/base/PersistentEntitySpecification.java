package de.computerstudienwerkstatt.tortuga.controller.base;

import de.computerstudienwerkstatt.tortuga.controller.base.response.BadRequestResponse;
import de.computerstudienwerkstatt.tortuga.controller.base.response.IllegalFilterResponse;
import de.computerstudienwerkstatt.tortuga.model.base.PersistentEntity;
import org.hibernate.jpa.criteria.path.PluralAttributePath;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
import java.util.*;

/**
 * @author Mischa Holz
 */
public class PersistentEntitySpecification<T extends PersistentEntity> implements Specification<T> {

    private FilterCriteria criteria;

    public PersistentEntitySpecification(FilterCriteria criteria) {
        this.criteria = criteria;
    }

    private Path getPath(Path root, String key) {
        Path path = root;
        String[] strPath = key.split("\\.");

        int i = 0;
        try {
            for (; i < strPath.length; i++) {
                path = path.get(strPath[i]);
                if(path instanceof PluralAttributePath) {
                    return path;
                }
            }

            Class nodeType = path.getJavaType();

            if (PersistentEntity.class.isAssignableFrom(nodeType)) {
                path = path.get("id");
            }
        } catch (IllegalArgumentException e) {
            String type = (i - 1 >= 0) ? strPath[i - 1] : "";
            String field = strPath[i];

            throw new IllegalFilterResponse(type, field);
        }

        return path;
    }

    private List<String> getRestPath(String[] strPath, Path path) {
        List<String> rest = new ArrayList<>();
        boolean add = false;
        for (String s : strPath) {
            if(add) {
                rest.add(s);
            }
            if(s.equals(((PluralAttributePath) path).getAttribute().getName())) {
                add = true;
            }
        }

        return rest;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        Path path = getPath(root, criteria.getKey());

        Object value = convertTypeFromString(path.getJavaType(), (String) criteria.getValue());

        if (path.getJavaType().equals(Date.class)) {
            Date date = (Date) value;
            if (criteria.getOperation() == FilterCriteria.Operation.GREATER_THAN) {
                return builder.greaterThanOrEqualTo(path, date);
            } else if (criteria.getOperation() == FilterCriteria.Operation.LESS_THAN) {
                return builder.lessThanOrEqualTo(path, date);
            } else if (criteria.getOperation() == FilterCriteria.Operation.EQUALS) {
                return builder.equal(path, date);
            }
            return null;
        }

        if (path.getJavaType().equals(Boolean.class)) {
            Boolean bool = (Boolean) value;

            if (criteria.getOperation() == FilterCriteria.Operation.EQUALS) {
                return builder.equal(path, bool);
            }

            return null;
        }

        if (Collection.class.isAssignableFrom(path.getJavaType())) {
            String[] strPath = criteria.getKey().split("\\.");
            String lastField = strPath[strPath.length - 1];

            if(path instanceof PluralAttributePath) {
                if(((PluralAttributePath) path).getAttribute().getName().equals(lastField)) {
                    Class<?> type = ((PluralAttributePath) path).getAttribute().getElementType().getJavaType();

                    value = convertTypeFromString(type, (String) criteria.getValue());

                    return builder.isMember(value, path);
                } else {
                    List<String> rest = getRestPath(strPath, path);
                    String restPath = StringUtils.collectionToDelimitedString(rest, ".");

                    Subquery<?> subquery = query.subquery(((PluralAttributePath) path).getAttribute().getElementType().getJavaType());
                    Root subQueryPath = subquery.from(((PluralAttributePath) path).getAttribute().getElementType().getJavaType());

                    FilterCriteria mine = this.criteria;
                    this.criteria = new FilterCriteria();
                    this.criteria.setKey(restPath);
                    this.criteria.setValue(mine.getValue());
                    this.criteria.setOperation(mine.getOperation());
                    subquery.where(toPredicate(subQueryPath, query, builder));
                    this.criteria = mine;

                    Set<SingularAttribute> attributes = subQueryPath.getModel().getDeclaredSingularAttributes();

                    SingularAttribute attributeOfManyToOne = null;
                    for (SingularAttribute attribute : attributes) {
                        if(attribute.getJavaType().equals(path.getParentPath().getJavaType())) {
                            if(attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_ONE) {
                                attributeOfManyToOne = attribute;
                                break;
                            }
                        }
                    }
                    if(attributeOfManyToOne == null) {
                        throw new UnsupportedOperationException("Did not find many to one attribute. Maybe I need more smarts? Or maybe you need to fix your model?");
                    }

                    subquery.select(subQueryPath.get(attributeOfManyToOne.getName()).get("id"));

                    return builder.in(path.getParentPath().get("id")).value(subquery);
                }
            }
        }

        if (Map.class.isAssignableFrom(path.getJavaType())) {
            String[] strPath = criteria.getKey().split("\\.");
            List<String> rest = getRestPath(strPath, path);

            if(rest.size() != 1) {
                throw new BadRequestResponse("Can't descend into map keys");
            }

            if(criteria.getValue() != null && !criteria.getValue().toString().isEmpty()) {
                MapJoin mapJoin = root.joinMap(((PluralAttributePath) path).getAttribute().getName());

                return builder.and(builder.equal(mapJoin.key(), rest.get(0)), builder.equal(mapJoin.value(), criteria.getValue()));
            }


            // "select * from onix_code  where id not in (select id from onix_code onixcode0_ inner join onix_code_translations translatio1_ on onixcode0_.id=translatio1_.onix_code_id where translations_key='de') "

            Subquery subquery = query.subquery(root.getJavaType());
            Root subQueryPath = subquery.from(root.getJavaType());

            MapJoin mapJoin = subQueryPath.joinMap(((PluralAttributePath) path).getAttribute().getName());

            subquery.where(builder.equal(mapJoin.key(), rest.get(0)));
            subquery.select(subQueryPath.get("id"));

            return builder.not(builder.in(root.get("id")).value(subquery));
        }

        if (criteria.getOperation() == FilterCriteria.Operation.GREATER_THAN) {
            return builder.greaterThanOrEqualTo(path, criteria.getValue().toString());
        } else if (criteria.getOperation() == FilterCriteria.Operation.LESS_THAN) {
            return builder.lessThanOrEqualTo(path, criteria.getValue().toString());
        } else if (criteria.getOperation() == FilterCriteria.Operation.EQUALS) {
            if (path.getJavaType() == String.class) {
                return builder.like(path, "" + criteria.getValue());
            } else {
                return builder.equal(path, criteria.getValue());
            }
        }
        return null;
    }

    private Object convertTypeFromString(Class<?> target, String source) {
        if (target.equals(Date.class)) {
            Date date = new Date(Long.parseLong(source));

            return date;
        }

        if (target.equals(Boolean.class)) {
            Boolean bool = criteria.getValue().toString().toLowerCase().equals("true");
            //noinspection unchecked
            return bool;
        }

        if (Enum.class.isAssignableFrom(target)) {
            Class unsafeClass = (Class) target;
            try {
                return Enum.valueOf(unsafeClass, source);
            } catch (IllegalArgumentException e) {
                try {
                    return Enum.valueOf(unsafeClass, source.toUpperCase());
                } catch(IllegalArgumentException e2) {
                    throw new BadRequestResponse(source + " is not a legal choice for that field");
                }
            }
        }

        return source;
    }
}