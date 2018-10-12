/*
 * Copyright 2018 Institut Laue–Langevin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.ill.preql.support;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Constraint functional interface for applying a constraint to the query
 *
 * @author Jamie Hall
 */
@FunctionalInterface
public interface ConstraintFunction<E> {
    /**
     * Apply the function
     *
     * @param criteriaBuilder the criteria builder
     * @param criteriaQuery   the criteria query
     * @param root            the root object
     * @param entityManager   the entity manager
     * @return a predicate
     */
    Predicate apply(CriteriaBuilder criteriaBuilder,
                    CriteriaQuery<E> criteriaQuery,
                    Root<E> root,
                    EntityManager entityManager);
}