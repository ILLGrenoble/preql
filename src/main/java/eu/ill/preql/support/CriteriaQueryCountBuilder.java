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
import javax.persistence.criteria.*;

import static java.lang.String.format;


public class CriteriaQueryCountBuilder {

    private final EntityManager entityManager;
    private volatile int aliasCount = 0;

    public CriteriaQueryCountBuilder(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Create a row count CriteriaQuery from a CriteriaQuery
     *
     * @param <T> the criteria query type
     * @param criteria the criteria to convert into a count query
     * @return the new criteria query
     */
    public <T> CriteriaQuery<Long> countCriteria(final CriteriaQuery<T> criteria) {
        final CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> countCriteria = builder.createQuery(Long.class);
        copyCriteriaWithoutSelectionAndOrder(criteria, countCriteria);
        final Root<T> root = findRoot(countCriteria, criteria.getResultType());
        if (criteria.isDistinct()) {
            return countCriteria.select(builder.countDistinct(root));
        } else {
            return countCriteria.select(builder.count(root));
        }
    }

    private boolean isEclipseLink(CriteriaQuery<?> from) {
        return from.getClass().getName().contains("org.eclipse.persistence");
    }

    /**
     * Copy Joins
     *
     * @param from source Join
     * @param to   destination Join
     */
    public void copyJoins(final From<?, ?> from, final From<?, ?> to) {
        for (final Join<?, ?> join : from.getJoins()) {
            final Join<?, ?> toJoin = to.join(join.getAttribute().getName(), join.getJoinType());
            toJoin.alias(getOrCreateAlias(join));
            copyJoins(join, toJoin);
        }
    }

    /**
     * Gets The result alias, if none set a default one and return it
     *
     * @param <T>       the selection type
     * @param selection the selection to use
     * @return root alias or generated one
     */
    public synchronized <T> String getOrCreateAlias(final Selection<T> selection) {
        String alias = selection.getAlias();
        if (alias == null) {
            alias = format("preql_generated_alias%d", aliasCount++);
            selection.alias(alias);
        }
        return alias;
    }

    /**
     * Find the Root with type class on CriteriaQuery Root Set
     *
     * @param <T>   the criteria object type
     * @param query criteria query
     * @param clazz root type
     * @return the root
     */
    public <T> Root<T> findRoot(final CriteriaQuery<?> query, final Class<T> clazz) {
        for (final Root<?> root : query.getRoots()) {
            if (clazz.equals(root.getJavaType())) {
                return (Root<T>) root.as(clazz);
            }
        }
        return null;
    }

    /**
     * Copy criteria without selection and order.
     *
     * @param from source Criteria.
     * @param to   destination Criteria.
     */
    private void copyCriteriaWithoutSelectionAndOrder(final CriteriaQuery<?> from, final CriteriaQuery<?> to) {
        if (isEclipseLink(from) && from.getRestriction() != null) {
            // EclipseLink adds roots from predicate paths to criteria. Skip copying
            // roots as workaround.
        } else {
            // Copy Roots
            for (final Root<?> root : from.getRoots()) {
                final Root<?> dest = to.from(root.getJavaType());
                dest.alias(getOrCreateAlias(root));
                copyJoins(root, dest);
            }
        }

        to.groupBy(from.getGroupList());
        to.distinct(from.isDistinct());

        if (from.getGroupRestriction() != null) {
            to.having(from.getGroupRestriction());
        }

        final Predicate predicate = from.getRestriction();
        if (predicate != null) {
            to.where(predicate);
        }
    }

}