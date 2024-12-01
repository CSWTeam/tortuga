package de.computerstudienwerkstatt.tortuga.controller.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import de.computerstudienwerkstatt.tortuga.Main;
import de.computerstudienwerkstatt.tortuga.controller.base.response.NotFoundResponse;
import de.computerstudienwerkstatt.tortuga.model.base.PersistentEntity;
import de.computerstudienwerkstatt.tortuga.patch.Patch;
import de.computerstudienwerkstatt.tortuga.repository.base.JpaSpecificationRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Mischa Holz
 */
public abstract class AbstractCRUDCtrl<T extends PersistentEntity> {

    protected JpaSpecificationRepository<T, String> repository;

    protected abstract String getApiBase();

    protected Sort buildSortObject(HttpServletRequest request) {
        Sort.Direction dir = Sort.DEFAULT_DIRECTION;
        String dirValue = request.getParameter("direction");
        if(dirValue != null) {
            dir = Sort.Direction.fromString(dirValue);
        }

        String sortValue = request.getParameter("sort");
        if(sortValue == null) {
            return null;
        }

        return new Sort(dir, sortValue.split(","));
    }

    public List<T> findAll() {
        return repository.findAll();
    }

    public List<T> findAll(HttpServletRequest request) {
        return findAll(request.getParameterMap(), buildSortObject(request));
    }

    public List<T> findAll(Map<String, String[]> params, Sort sort) {
        List<PersistentEntitySpecification<T>> specifications = new ArrayList<>();

        params = new HashMap<>(params);

        params.remove("direction");
        params.remove("sort");

        for (Map.Entry<String, String[]> stringEntry : params.entrySet()) {
            String key = stringEntry.getKey();
            String[] valueArray = stringEntry.getValue();

            for(String value : valueArray) {
                FilterCriteria criteria;
                if(value.startsWith("<")) {
                    criteria = new FilterCriteria();
                    criteria.setKey(key);
                    criteria.setOperation(FilterCriteria.Operation.LESS_THAN);
                    criteria.setValue(value.substring(1));
                } else if(value.startsWith(">")) {
                    criteria = new FilterCriteria();
                    criteria.setKey(key);
                    criteria.setOperation(FilterCriteria.Operation.GREATER_THAN);
                    criteria.setValue(value.substring(1));
                } else {
                    criteria = new FilterCriteria();
                    criteria.setKey(key);
                    criteria.setOperation(FilterCriteria.Operation.EQUALS);
                    criteria.setValue(value);
                }
                specifications.add(new PersistentEntitySpecification<T>(criteria));
            }
        }

        if(specifications.size() > 0) {
            boolean first = true;
            Specifications<T> spec = null;
            for(PersistentEntitySpecification<T> specification : specifications) {
                if(first) {
                    spec = Specifications.where(specification);
                    first = false;
                } else {
                    spec = spec.and(specification);
                }
            }

            if(sort == null) {
                try {
                    return repository.findAll(spec);
                } catch(InvalidDataAccessApiUsageException e) {
                    throw (RuntimeException) e.getCause();
                }
            }
            try {
                return repository.findAll(spec, sort);
            } catch(InvalidDataAccessApiUsageException e) {
                throw (RuntimeException) e.getCause();
            }
        }

        if(sort == null) {
            return repository.findAll();
        }
        return repository.findAll(sort);
    }

    public T findOne(String id) {
        T ret = repository.findOne(id);
        if(ret == null) {
            throw new NotFoundResponse("Resource mit ID '" + id + "' konnte nicht gefunden werden");
        }

        return ret;
    }

    public ResponseEntity<T> post(T newEntity, HttpServletResponse response) {
        if(repository.findOne(newEntity.getId()) != null) {
            return new ResponseEntity<T>(HttpStatus.NOT_ACCEPTABLE);
        }

        T ret = repository.save(newEntity);
        response.setHeader(HttpHeaders.LOCATION, Main.getApiBase() + "/" + getApiBase() + "/" + ret.getId());

        return new ResponseEntity<>(ret, HttpStatus.CREATED);
    }

    public ResponseEntity delete(String id) {
        repository.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public T patch(String id, ChangeSet<T> changeSet) {
        T original = repository.findOne(id);

        return patch(original, changeSet);
    }

    public T patch(T original, ChangeSet<T> changeSet) {
        if(original == null) {
            throw new NotFoundResponse("Resource konnte nicht gefunden werden");
        }

        T patched = Patch.patch(original, changeSet);

        return repository.save(patched);
    }

    @Autowired
    public void setRepository(JpaSpecificationRepository<T, String> repository) {
        this.repository = repository;
    }
}