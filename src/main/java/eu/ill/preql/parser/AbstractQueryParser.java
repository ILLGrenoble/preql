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

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.preql.support.Field;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Jamie Hall
 */
public abstract class AbstractQueryParser {
    protected static final BaseErrorListener   SYNTAX_ERROR_LISTENER = new BaseErrorListener() {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
                throws ParseCancellationException {
            throw new InvalidQueryException("Failed to parse query at line " + line + ":" + charPositionInLine + ": " + msg);
        }
    };
    protected final        Map<String, Object> parameters;
    protected final        CriteriaBuilder     criteriaBuilder;
    protected final        Map<String, Field>  fields;
    protected final        List<Predicate>     expressions;
    protected final        ParameterParsers    parsers;
    protected final        Integer             maxExpressions;

    /**
     * Create a new instance
     *
     * @param context the query context
     */
    public AbstractQueryParser(final QueryParserContext context) {
        this.criteriaBuilder = context.getCriteriaBuilder();
        this.fields = context.getFields();
        this.parameters = context.getParameters();
        this.expressions = context.getExpressions();
        this.parsers = context.getParsers();
        this.maxExpressions = context.getMaxExpressions();
    }

    /**
     * Parses the given query
     *
     * @param query The query to be parsed
     * @return A list of expressions (predicates)
     */
    public abstract Predicate[] parse(final String query);

    /**
     * Parse a parameter
     *
     * @param field     The field
     * @param parameter The name of the parameter
     * @param value     The value of the parameter
     * @return the parsed parameter
     */
    public Object parseValue(final Field field, final String parameter, final Object value) {
        final Class<?> valueType = field.getAttribute().getJavaType();
        try {
            if (value == null) {
                throw new InvalidQueryException("Parameter cannot be null");
            }
            final FieldParser fieldParser = field.getValueParser();
            if (fieldParser == null) {
                final ParameterParser<?> parameterParser = parsers.getParser(valueType, value);
                return parameterParser.parse(value);
            } else {
                return fieldParser.parse(value);
            }

        } catch (InvalidQueryException exception) {
            throw new InvalidQueryException(format("Error parsing parameter '%s'. %s.", parameter, exception.getMessage()));
        }
    }

    /**
     * Merge the parsed expressions and pre defined expressions
     *
     * @param parsedExpressions The parsed expressions
     * @return a list of expressions
     */
    protected Predicate[] mergeExpressions(final List<Predicate> parsedExpressions) {
        final List<Predicate> expressions       = this.expressions;
        final List<Predicate> mergedExpressions = new ArrayList<>(expressions);
        if (parsedExpressions != null) {
            mergedExpressions.addAll(parsedExpressions);
        }
        return mergedExpressions.toArray(new Predicate[0]);
    }


    /**
     * Merge the parsed expressions and pre defined expressions
     *
     * @return a list of expressions
     */
    protected Predicate[] mergeExpressions() {
        return mergeExpressions(null);
    }


    /**
     * Get a parameter
     *
     * @param name parameter name
     * @return the parameter
     */
    public Object getParameter(final String name) {
        if (parameters.containsKey(name)) {
            return parameters.get(name);
        }
        throw new InvalidQueryException("Parameter " + name + " not found in the bound parameters");
    }

    /**
     * Get the maximum number of expressions
     *
     * @return the max expressions
     */
    public int getMaxExpressions() {
        return maxExpressions;
    }


    /**
     * Get a field for a given name
     *
     * @param name The name of the field
     * @return the field
     */
    public Field getField(final String name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        }
        throw new InvalidQueryException(format("Field %s does not exist", name));
    }

    /**
     * Get the criteria builder
     *
     * @return the criteria builder
     */
    public CriteriaBuilder getCriteriaBuilder() {
        return criteriaBuilder;
    }
}
