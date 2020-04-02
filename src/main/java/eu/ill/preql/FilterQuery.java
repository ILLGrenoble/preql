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
import eu.ill.preql.support.Field;
import eu.ill.preql.support.OrderableField;
import eu.ill.preql.support.Pagination;
import eu.ill.preql.parser.QueryParser;
import eu.ill.preql.parser.QueryParserContext;
import eu.ill.preql.parser.ValueParsers;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * Defines a new query
 *
 * @param <E> the root entity type
 * @author Jamie Hall
 */
public class FilterQuery<E> {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;
    private final CriteriaQuery<E> criteria;
    private final Root<E> root;
    private final Map<String, Field> fields;
    private final Map<String, Object> parameters = new HashMap<>();
    private final String query;
    private final List<Predicate> expressions = new ArrayList<>();
    private final QueryParser parser;
    private Pagination pagination = Pagination.DEFAULT;
    private ValueParsers valueParsers = new ValueParsers();

    public FilterQuery(
            final String query,
            final EntityManager entityManager,
            final CriteriaBuilder criteriaBuilder,
            final CriteriaQuery<E> criteria,
            final Root<E> root,
            final Map<String, Field> fields) {
        this.query = query;
        this.entityManager = entityManager;
        this.criteriaBuilder = criteriaBuilder;
        this.criteria = criteria;
        this.root = root;
        this.fields = fields;
        this.parser = createParser();
    }

    /**
     * Get an order field for a given name
     *
     * @param name The name of the order field
     * @return the order field
     */
    public Field getOrderField(final String name) {
        if (fields.containsKey(name)) {
            final Field field = fields.get(name);
            if (field instanceof OrderableField) {
                return field;
            }
        }
        throw new InvalidQueryException(format("Order field %s does not exist", name));
    }

    /**
     * Set the pagination
     *
     * @param pagination the pagination object
     */
    public void setPagination(final Pagination pagination) {
        this.pagination = pagination;
    }
    
    /**
     * Set the pagination
     *
     * @param limit 
     * @param offset
     */
    public void setPagination(int limit, int offset) {
        this.pagination = new Pagination(limit, offset);
    }

    /**
     * Add a predefined expression to the query
     * These expressions are added to the final query before being executed
     *
     * @param callback the expression callback
     * @return this
     */
    public FilterQuery<E> addExpression(final BiFunction<CriteriaBuilder, Root<E>, Predicate> callback) {
        final Predicate expression = callback.apply(criteriaBuilder, root);
        expressions.add(expression);
        return this;
    }

    /**
     * Create a COUNT query
     *
     * @return the typed query of long
     */
    private TypedQuery<Long> createCountQuery() {
        final Predicate[] expressions = parser.parse(query);
        final CriteriaQuery<Long> criteria = criteriaBuilder.createQuery(Long.class);
        criteria.select(criteriaBuilder.count(criteria.from(root.getJavaType())))
                .where(expressions)
                .distinct(true);

        return entityManager.createQuery(criteria);
    }

    /**
     * Create a SELECT query
     *
     * @return the typed query of <E>
     */
    private TypedQuery<E> createQuery() {
        final Predicate[] expressions = parser.parse(query);
        criteria.where(expressions)
                .distinct(true);
        final TypedQuery<E> query = entityManager.createQuery(criteria);

        query.setMaxResults(pagination.getLimit());
        query.setFirstResult(pagination.getOffset());

        return query;
    }

    /**
     * Execute a SELECT query and return the query results
     * as a typed List.
     *
     * @return a list of the results
     * @throws IllegalStateException        if called for a Java
     *                                      Persistence query language UPDATE or DELETE statement
     * @throws QueryTimeoutException        if the query execution exceeds
     *                                      the query timeout value set and only the statement is
     *                                      rolled back
     * @throws TransactionRequiredException if a lock mode other than
     *                                      <code>NONE</code> has been set and there is no transaction
     *                                      or the persistence context has not been joined to the
     *                                      transaction
     * @throws PessimisticLockException     if pessimistic locking
     *                                      fails and the transaction is rolled back
     * @throws LockTimeoutException         if pessimistic locking
     *                                      fails and only the statement is rolled back
     * @throws PersistenceException         if the query execution exceeds
     *                                      the query timeout value set and the transaction
     *                                      is rolled back
     */
    public List<E> getResultList() {
        final TypedQuery<E> query = createQuery();
        return query.getResultList();
    }

    /**
     * Execute a SELECT query and return the query results
     * as a typed <code>java.util.stream.Stream</code>.
     * By default this method delegates to <code>getResultList().stream()</code>,
     * however persistence provider may choose to override this method
     * to provide additional capabilities.
     *
     * @return a stream of the results
     * @throws IllegalStateException        if called for a Java
     *                                      Persistence query language UPDATE or DELETE statement
     * @throws QueryTimeoutException        if the query execution exceeds
     *                                      the query timeout value set and only the statement is
     *                                      rolled back
     * @throws TransactionRequiredException if a lock mode other than
     *                                      <code>NONE</code> has been set and there is no transaction
     *                                      or the persistence context has not been joined to the transaction
     * @throws PessimisticLockException     if pessimistic locking
     *                                      fails and the transaction is rolled back
     * @throws LockTimeoutException         if pessimistic locking
     *                                      fails and only the statement is rolled back
     * @throws PersistenceException         if the query execution exceeds
     *                                      the query timeout value set and the transaction
     *                                      is rolled back
     */
    public Stream<E> getResultStream() {
        final TypedQuery<E> query = createQuery();
        return query.getResultStream();
    }

    /**
     * Execute a SELECT query that returns a single result.
     *
     * @return the result
     * @throws NoResultException            if there is no result
     * @throws NonUniqueResultException     if more than one result
     * @throws IllegalStateException        if called for a Java
     *                                      Persistence query language UPDATE or DELETE statement
     * @throws QueryTimeoutException        if the query execution exceeds
     *                                      the query timeout value set and only the statement is
     *                                      rolled back
     * @throws TransactionRequiredException if a lock mode other than
     *                                      <code>NONE</code> has been set and there is no transaction
     *                                      or the persistence context has not been joined to the
     *                                      transaction
     * @throws PessimisticLockException     if pessimistic locking
     *                                      fails and the transaction is rolled back
     * @throws LockTimeoutException         if pessimistic locking
     *                                      fails and only the statement is rolled back
     * @throws PersistenceException         if the query execution exceeds
     *                                      the query timeout value set and the transaction
     *                                      is rolled back
     */
    public E getSingleResult() {
        final TypedQuery<E> query = createQuery();
        return query.getSingleResult();
    }

    /**
     * Execute that a SELECT query that returns a count of records
     *
     * @return the number of records
     */
    public Long count() {
        final TypedQuery<Long> countQuery = createCountQuery();
        return countQuery.getSingleResult();
    }

    /**
     * Bind an argument to a named parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @return this
     * @throws InvalidQueryException if the parameter has already been defined
     */
    public FilterQuery<E> setParameter(final String name, final Object value) {
        if (parameters.containsKey(name)) {
            throw new InvalidQueryException(format("Parameter '%s' has already been set", name));
        }
        parameters.put(name, value);
        return this;
    }

    /**
     * Set the order field
     *
     * @param name      order field name
     * @param direction the direction (asc or desc)
     * @return this
     */
    public FilterQuery<E> setOrder(final String name, final String direction) {
        if (!direction.matches("^(asc|desc)$")) {
            throw new InvalidQueryException("Order direction must be asc or desc");
        }
        final Field field = getOrderField(name);
        if ("asc".equals(direction)) {
            criteria.orderBy(criteriaBuilder.asc(field.getAttribute()));
        } else {
            criteria.orderBy(criteriaBuilder.desc(field.getAttribute()));
        }
        return this;
    }


    /**
     * Set the bound parameters
     *
     * @param parameters the parameters to be bound
     * @return this
     */
    public FilterQuery<E> setParameters(final Map<String, Object> parameters) {
        parameters.forEach(this::setParameter);
        return this;
    }


    /**
     * Get the query parser
     *
     * @return the query parser
     */
    public QueryParser getParser() {
        return parser;
    }

    /**
     * Create a new parser
     *
     * @return the query parser
     */
    private QueryParser createParser() {
        final QueryParserContext context = new QueryParserContext(criteriaBuilder, fields, parameters, expressions, valueParsers);
        return new QueryParser(context);
    }


}
