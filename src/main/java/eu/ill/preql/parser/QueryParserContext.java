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
package eu.ill.preql.parser;

import eu.ill.preql.support.Field;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;

/**
 * Provides the context for parsing the query
 *
 * @author Jamie Hall
 */
public class QueryParserContext {

    private final CriteriaBuilder criteriaBuilder;
    private final List<Predicate> expressions;
    private final Map<String, Object> parameters;
    private final ValueParsers valueParsers;
    private final Map<String, Field> fields;

    /**
     * Create a new context
     *
     * @param criteriaBuilder the criteria builder {@link CriteriaBuilder}
     * @param fields          the defined fields
     * @param parameters      the bound parameters
     * @param expressions     the predefined expressions
     * @param valueParsers    the value parsers for coercing parameter values
     */
    public QueryParserContext(final CriteriaBuilder criteriaBuilder,
                              final Map<String, Field> fields,
                              final Map<String, Object> parameters,
                              final List<Predicate> expressions,
                              final ValueParsers valueParsers) {
        this.criteriaBuilder = criteriaBuilder;
        this.parameters = parameters;
        this.fields = fields;
        this.expressions = expressions;
        this.valueParsers = valueParsers;
    }


    public CriteriaBuilder getCriteriaBuilder() {
        return criteriaBuilder;
    }

    public List<Predicate> getExpressions() {
        return expressions;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public ValueParsers getValueParsers() {
        return valueParsers;
    }

    public Map<String, Field> getFields() {
        return fields;
    }


}
