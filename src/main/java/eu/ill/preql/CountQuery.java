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
import eu.ill.preql.parser.QueryParser;
import eu.ill.preql.parser.QueryParserContext;
import eu.ill.preql.parser.ValueParsers;
import eu.ill.preql.support.Field;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static java.lang.String.format;

/**
 * Defines a new query
 *
 * @param <E> the root entity type
 * @author Jamie Hall
 */
class CountQuery<E> {
    private final EntityManager       entityManager;
    private final CriteriaBuilder     criteriaBuilder;
    private final CriteriaQuery<Long> criteria;
    private final Root<E>             root;
    private final Map<String, Field>  fields;
    private final Map<String, Object> parameters   = new HashMap<>();
    private final String              query;
    private final List<Predicate>     expressions  = new ArrayList<>();
    private final QueryParser         parser;
    private final ValueParsers valueParsers = new ValueParsers();

    CountQuery(
            final String query,
            final EntityManager entityManager,
            final CriteriaBuilder criteriaBuilder,
            final CriteriaQuery<Long> criteria,
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
     * Get a field for a given name
     *
     * @param name The name of the field
     * @return the field
     */
    Field getField(final String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }
        throw new InvalidQueryException(format("Field %s does not exist", name));
    }

    /**
     * Add a predefined expression to the query
     * These expressions are added to the final query before being executed
     *
     * @param callback the expression callback
     * @return this
     */
    CountQuery<E> addExpression(final BiFunction<CriteriaBuilder, Root<E>, Predicate> callback) {
        final Predicate expression = callback.apply(criteriaBuilder, root);
        expressions.add(expression);
        return this;
    }

    /**
     * Create a SELECT query
     * @param distinct  distinct rows or not
     * @return the typed query of <E>
     */
    private TypedQuery<Long> createQuery(boolean distinct) {
        final Predicate[] expressions = parser.parse(query);

        criteria.where(expressions);
        if (distinct) {
            criteria.select(criteriaBuilder.countDistinct(root));
        } else {
            criteria.select(criteriaBuilder.count(root));
        }

        return entityManager.createQuery(criteria);
    }

    /**
     * Execute a SELECT query that returns a single result.
     *
     * @param distinct                      counts distinct results
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
    Long getSingleResult(final boolean distinct) {
        final TypedQuery<Long> query = createQuery(distinct);
        return query.getSingleResult();
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
    Long getSingleResult() {
        final TypedQuery<Long> query = createQuery(true);
        return query.getSingleResult();
    }


    /**
     * Bind an argument to a named parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @return this
     * @throws InvalidQueryException if the parameter has already been defined
     */
    CountQuery<E> setParameter(final String name, final Object value) {
        if (parameters.containsKey(name)) {
            throw new InvalidQueryException(format("Parameter '%s' has already been set", name));
        }
        parameters.put(name, value);
        return this;
    }

    /**
     * Set the bound parameters
     *
     * @param parameters the parameters to be bound
     * @return this
     */
    CountQuery<E> setParameters(final Map<String, Object> parameters) {
        parameters.forEach(this::setParameter);
        return this;
    }

    /**
     * Get the query parser
     *
     * @return the query parser
     */
    QueryParser getParser() {
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
