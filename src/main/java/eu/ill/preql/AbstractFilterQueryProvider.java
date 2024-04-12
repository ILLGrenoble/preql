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

import eu.ill.preql.support.Field;
import eu.ill.preql.support.OrderableField;
import jakarta.persistence.EntityManager;

/**
 * @param <E> the root entity type
 * @author Jamie Hall
 */
public abstract class AbstractFilterQueryProvider<E> extends AbstractQueryProvider<E, E> {

    private final CountQueryProvider<E> countQueryProvider;

    /**
     * @param objectType    the object type that the query will correspond to
     * @param entityManager the entity manager
     */
    public AbstractFilterQueryProvider(final Class<E> objectType, final EntityManager entityManager) {
        super(objectType, objectType, entityManager);
        this.countQueryProvider = new CountQueryProvider<E>(objectType, entityManager);
    }

    /**
     * Create a new query for the given object type
     *
     * @param preql - the expression query
     * @return a new filter instance
     */
    public FilterQuery<E> createQuery(final String preql) {
        CountQuery<E> countQuery = this.countQueryProvider.createQuery(preql);

        return new FilterQuery<>(preql,
                entityManager,
                criteriaBuilder,
                criteria,
                root,
                fields,
                countQuery);
    }

    /**
     * Create a new query for the given object type
     *
     * @return a new query instance
     */
    public FilterQuery<E> createQuery() {
        return createQuery(null);
    }

    /**
     * Add a new field
     *
     * @param field The field to be added
     * @return this
     */
    public AbstractQueryProvider<E, E> addField(final Field field) {
        // Clone field for count query provider
        if (field instanceof OrderableField) {
            this.countQueryProvider.addField(this.countQueryProvider.field(field.getAttribute(), field.getName(), field.getValueParser()));
        } else {
            this.countQueryProvider.addField(this.countQueryProvider.orderableField(field.getAttribute(), field.getName(), field.getValueParser()));
        }

        return super.addField(field);
    }

}
