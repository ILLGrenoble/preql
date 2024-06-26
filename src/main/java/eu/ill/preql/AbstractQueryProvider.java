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

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.preql.parser.FieldValueParser;
import eu.ill.preql.support.AttributeMapper;
import eu.ill.preql.support.Field;
import eu.ill.preql.support.OrderableField;
import eu.ill.preql.support.SimpleField;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * @param <E> the root entity type
 * @author Jamie Hall
 */
public abstract class AbstractQueryProvider<E, S> {
    protected final EntityManager      entityManager;
    protected final CriteriaBuilder    criteriaBuilder;
    protected final Map<String, Field> fields = new HashMap<>();
    protected final CriteriaQuery<S>   criteria;
    protected final Root<E>            root;
    protected final AttributeMapper<E> mapper;

    /**
     * @param rootObjectType    the object type that the query will correspond to
     * @param selectObjectType  the object type that the query will return
     * @param entityManager     the entity manager
     */
    public AbstractQueryProvider(final Class<E> rootObjectType, final Class<S> selectObjectType, final EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.criteria = criteriaBuilder.createQuery(selectObjectType);
        this.root = this.criteria.from(rootObjectType);
        this.mapper = new AttributeMapper<>(root, entityManager.getMetamodel());
    }

    /**
     * Add a new field
     *
     * @param field The field to be added
     * @return this
     */
    public AbstractQueryProvider<E, S> addField(final Field field) {
        final String name = field.getName();
        if (fields.containsKey(name)) {
            throw new InvalidQueryException(format("Field '%s' has already been registered to the query", name));
        }
        fields.put(name, field);
        return this;
    }

    /**
     * Add a list of fields
     *
     * @param fields the list of fields to be added
     * @return this
     */
    public AbstractQueryProvider<E, S> addFields(final Iterable<Field> fields) {
        for (final Field field : fields) {
            addField(field);
        }
        return this;
    }

    /**
     * Add a list of fields
     *
     * @param fields the list of fields to be added
     * @return this
     */
    public AbstractQueryProvider<E, S> addFields(final Field... fields) {
        for (final Field field : fields) {
            addField(field);
        }
        return this;
    }

    /**
     * Create a new simple field
     *
     * @param attribute the name of the represented attribute
     * @return a new simple field
     */
    public SimpleField field(final String attribute) {
        return field(attribute, attribute, null);
    }

    /**
     * Create a new simple field
     *
     * @param attribute the name of the represented attribute
     * @param alias     the field alias
     * @return a new simple field
     */
    public SimpleField field(final String attribute, final String alias) {
        return field(attribute, alias, null);
    }

    /**
     * Create a new simple field
     *
     * @param attribute   the name of the represented attribute
     * @param valueParser the custom value parser for the field
     * @return a new simple field
     */
    public SimpleField field(final String attribute, final FieldValueParser<?> valueParser) {
        return field(attribute, attribute, valueParser);
    }

    /**
     * Create a new simple field
     *
     * @param attribute   the name of the represented attribute
     * @param alias       the field alias
     * @param valueParser the custom value parser for the field
     * @return a new simple field
     */
    public SimpleField field(final String attribute, final String alias, final FieldValueParser<?> valueParser) {
        return new SimpleField(attribute, alias, mapper.get(attribute), valueParser);
    }

    /**
     * Create a new orderable field
     *
     * @param attribute the name of the represented attribute
     * @return a new orderable field
     */
    public OrderableField orderableField(final String attribute) {
        return orderableField(attribute, attribute, null);
    }

    /**
     * Create a new orderable field
     *
     * @param attribute the name of the represented attribute
     * @param alias     the field alias
     * @return a new orderable field
     */
    public OrderableField orderableField(final String attribute, final String alias) {
        return orderableField(attribute, alias, null);
    }

    /**
     * Create a new orderable field
     *
     * @param attribute   the name of the represented attribute
     * @param valueParser the custom value parser for the field
     * @return a new orderable field
     */
    public OrderableField orderableField(final String attribute, final FieldValueParser<?> valueParser) {
        return orderableField(attribute, attribute, valueParser);
    }

    /**
     * Create a new orderable field
     *
     * @param attribute   the name of the represented attribute
     * @param alias       the field alias
     * @param valueParser the custom value parser for the field
     * @return a new orderable field
     */
    public OrderableField orderableField(final String attribute, final String alias, final FieldValueParser<?> valueParser) {
        return new OrderableField(attribute, alias, mapper.get(attribute), valueParser);
    }


}
