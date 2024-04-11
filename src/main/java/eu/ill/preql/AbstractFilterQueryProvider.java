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
package eu.ill.preql;

import jakarta.persistence.EntityManager;

/**
 * @param <E> the root entity type
 * @author Jamie Hall
 */
public abstract class AbstractFilterQueryProvider<E> extends AbstractQueryProvider<E, E> {

    /**
     * @param objectType    the object type that the query will correspond to
     * @param entityManager the entity manager
     */
    public AbstractFilterQueryProvider(final Class<E> objectType, final EntityManager entityManager) {
        super(objectType, objectType, entityManager);
    }

    /**
     * Create a new query for the given object type
     *
     * @param preql - the expression query
     * @return a new filter instance
     */
    public FilterQuery<E> createQuery(final String preql) {
        return new FilterQuery<>(preql,
                entityManager,
                criteriaBuilder,
                criteria,
                root,
                fields);
    }

    /**
     * Create a new query for the given object type
     *
     * @return a new query instance
     */
    public FilterQuery<E> createQuery() {
        return createQuery(null);
    }
}
