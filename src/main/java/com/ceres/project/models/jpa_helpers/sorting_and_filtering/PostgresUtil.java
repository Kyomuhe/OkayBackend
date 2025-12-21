package com.ceres.project.models.jpa_helpers.sorting_and_filtering;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostgresUtil<T> {

    private final EntityManager entityManager;

    public List<T> searchSortAndPaginate(Class<T> entityClass, String searchField, String searchValue, String sortField, boolean ascending, Integer page, Integer size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<T> query = cb.createQuery(entityClass);

        Root<T> root = query.from(entityClass);
        if (StringUtils.isNotBlank(searchField) && StringUtils.isNotBlank(searchValue)) {
            Predicate searchPredicate;
            if ("id".equals(searchField)) {
                searchPredicate = cb.equal(root.get(searchField), searchValue);
            } else {
                searchPredicate = cb.like(cb.lower(root.get(searchField)), "%" + searchValue.toLowerCase() + "%");
            }

            query.select(root).where(searchPredicate);
        }

        if (StringUtils.isNotBlank(sortField)) {
            Order order = ascending ? cb.asc(root.get(sortField)) : cb.desc(root.get(sortField));
            query.orderBy(order);
        }

        TypedQuery<T> typedQuery = entityManager.createQuery(query);

        if (page != null && size != null) {
            typedQuery.setFirstResult((page - 1) * size);  // Skip to the desired page
            typedQuery.setMaxResults(size);   // Limit results to page size
        }

        return typedQuery.getResultList();
    }
}

