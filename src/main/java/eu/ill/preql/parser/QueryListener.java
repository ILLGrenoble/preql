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

import eu.ill.preql.FilterBaseListener;
import eu.ill.preql.FilterLexer;
import eu.ill.preql.FilterParser;
import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.preql.support.Field;
import org.antlr.v4.runtime.ParserRuleContext;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Jamie Hall
 */
public class QueryListener extends FilterBaseListener {

    private final QueryParser                                  parser;
    private final Map<ParserRuleContext, ArrayList<Predicate>> expressions = new HashMap<>();
    private final CriteriaBuilder                              cb;

    public QueryListener(final QueryParser parser) {
        this.parser = parser;
        this.cb = parser.getCriteriaBuilder();
    }

    /**
     * Evaluate a comparator expression
     *
     * @param context the comparator expression context
     */
    @Override
    @SuppressWarnings("unchecked")
    public void exitComparatorExpression(final FilterParser.ComparatorExpressionContext context) {
        final String     identifier = context.parameter().IDENTIFIER().getText();
        final Field field      = parser.getField(context.field().getText());
        final Expression path  = field.getPath();
        final Object     value      = parser.parseValue(field, identifier, parser.getParameter(identifier));
        final Expression expression = cb.literal(value);

        switch (context.operator.getType()) {
            case FilterLexer.GT:
                addExpression(context, cb.greaterThan(path, expression));
                break;
            case FilterLexer.GT_EQ:
                addExpression(context, cb.greaterThanOrEqualTo(path, expression));
                break;
            case FilterLexer.LT:
                addExpression(context, cb.lessThan(path, expression));
                break;
            case FilterLexer.LT_EQ:
                addExpression(context, cb.lessThanOrEqualTo(path, expression));
                break;
            case FilterLexer.EQ:
                addExpression(context, cb.equal(path, expression));
                break;
            case FilterLexer.NOT_EQ1:
            case FilterLexer.NOT_EQ2:
                addExpression(context, cb.notEqual(path, expression));
                break;
            default:
                throw new RuntimeException("Unexpected comparison operator");
        }
    }

    /**
     * Evaluate a binary expression
     *
     * @param context the binary expression context
     */
    @Override
    public void exitBinaryExpression(final FilterParser.BinaryExpressionContext context) {
        final Predicate[] expressions = this.expressions.get(context).toArray((new Predicate[0]));
        switch (context.operator.getType()) {
            case FilterLexer.AND:
                addExpression(context, cb.and(expressions));
                break;
            case FilterLexer.OR:
                addExpression(context, cb.or(expressions));
                break;
            default:
                throw new RuntimeException("Unexpected binary operator: " + context.operator.getText());
        }
    }

    /**
     * Evaluate a BETWEEN expression
     *
     * @param context the between expression content
     */
    @Override
    @SuppressWarnings("unchecked")
    public void exitBetweenExpression(final FilterParser.BetweenExpressionContext context) {
        final Field      field       = parser.getField(context.field().getText());
        final String     identifier1 = context.parameter(0).IDENTIFIER().getText();
        final String     identifier2 = context.parameter(1).IDENTIFIER().getText();
        final Expression path   = field.getPath();
        final Comparable lowerValue  = (Comparable) parser.parseValue(field, identifier1, parser.getParameter(identifier1));
        final Comparable upperValue  = (Comparable) parser.parseValue(field, identifier2, parser.getParameter(identifier2));
        if (context.NOT() == null) {
            addExpression(context, cb.between(path, lowerValue, upperValue));
        } else {
            addExpression(context, cb.not(cb.between(path, lowerValue, upperValue)));
        }
    }

    /**
     * Evaluate an IN expression
     *
     * @param context the in expression  context
     */
    @Override
    public void exitInExpression(final FilterParser.InExpressionContext context) {
        final String  identifier = context.parameter().IDENTIFIER().getText();
        final Field   field      = parser.getField(context.field().getText());
        final Path<?> path  = field.getPath();
        final Object  parameter  = parser.getParameter(identifier);
        if (parameter instanceof List) {
            final List<Object> values = new ArrayList<>();
            for (Object value : (List) parameter) {
                values.add(parser.parseValue(field, identifier, value));
            }
            if (context.NOT() == null) {
                addExpression(context, path.in(values));
            } else {
                addExpression(context, cb.not(path.in(values)));
            }
        } else {
            throw new InvalidQueryException(format("Expected a list of parameters for parameter: '%s'", field.getName()));
        }
    }

    /**
     * Evaluate a NULL expression
     *
     * @param context the null expression context
     */
    @Override
    public void exitNullExpression(final FilterParser.NullExpressionContext context) {
        final Field   field     = parser.getField(context.field().getText());
        final Path<?> path = field.getPath();
        if (context.NOT() == null) {
            addExpression(context, cb.isNull(path));
        } else {
            addExpression(context, cb.isNotNull(path));
        }
    }

    /**
     * Evaluate a LIKE expression
     *
     * @param context the like expression context
     */
    @Override
    @SuppressWarnings("unchecked")
    public void exitLikeExpression(final FilterParser.LikeExpressionContext context) {
        final String     identifier = context.parameter().IDENTIFIER().getText();
        final Field      field      = parser.getField(context.field().getText());
        final Expression path  = field.getPath();
        final String     value      = (String) parser.parseValue(field, identifier, parser.getParameter(identifier));
        if (context.NOT() == null) {
            addExpression(context, cb.like(path, value));
        } else {
            addExpression(context, cb.notLike(path, value));
        }
    }

    /**
     * Adds the given query to a list of child queries which have not yet been wrapped in a parent query.
     *
     * @param currentContext the current context
     * @param predicate      the predicate to be added
     */
    private void addExpression(final ParserRuleContext currentContext, final Predicate predicate) {
        // Retrieve the possibly null parent query...
        final ParserRuleContext    parentContext    = getParentContextOfType(currentContext, FilterParser.BinaryExpressionContext.class);
        final ArrayList<Predicate> childrenOfParent = expressions.computeIfAbsent(parentContext, k -> new ArrayList<>());
        childrenOfParent.add(predicate);
        if (parser.getMaxExpressions() != -1) {
            if (this.expressions.size() > parser.getMaxExpressions()) {
                throw new InvalidQueryException(format("Exceeded maximum number of expressions. " +
                        "Number of expressions can not exceed: %d", parser.getMaxExpressions()));
            }
        }
    }

    /**
     * Gets the parent context of a given type
     *
     * @param currentContext     the current context
     * @param parentContextTypes the parent content types
     * @return a parser rule context
     */
    private ParserRuleContext getParentContextOfType(ParserRuleContext currentContext, final Class<?>... parentContextTypes) {
        while (currentContext != null) {
            currentContext = currentContext.getParent();
            if (currentContext != null) {
                for (Class<?> parentContextType : parentContextTypes) {
                    if (parentContextType.isAssignableFrom(currentContext.getClass())) {
                        return currentContext;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns a list of parsed expressions
     *
     * @return a list of predicates
     */
    public List<Predicate> getExpressions() {
        return expressions.get(null);
    }
}
