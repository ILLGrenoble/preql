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

import eu.ill.preql.FilterLexer;
import eu.ill.preql.FilterParser;
import eu.ill.preql.exception.InvalidQueryException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.antlr.v4.runtime.CharStreams.fromStream;

/**
 * A parser for Preql queries that are converted into JPA criteria expressions
 *
 * @author Jamie Hall
 */
public class QueryParser extends AbstractQueryParser {

    public QueryParser(final QueryParserContext context) {
        super(context);
    }

    /**
     * Parse the query
     *
     * @param preql the query to be parsed
     * @return a list of predicates
     */
    @Override
    public Predicate[] parse(final String preql) {
        try {
            if (preql == null) {
                return mergeExpressions();
            }

            final CharStream stream = createStream(preql);
            final FilterLexer lexer = createLexer(stream);
            final FilterParser parser = createParser(new CommonTokenStream(lexer));
            final FilterParser.QueryContext queryContext = parser.query();
            final ParseTreeWalker walker = new ParseTreeWalker();
            final QueryListener listener = new QueryListener(this);

            walker.walk(listener, queryContext);

            return mergeExpressions(listener.getExpressions());
        } catch (InvalidQueryException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidQueryException("Failed to parse query", exception);
        }
    }

    /**
     * Create a new lexer
     *
     * @param stream the character stream
     * @return the lexer
     */
    private FilterLexer createLexer(final CharStream stream) {
        final FilterLexer lexer = new FilterLexer(stream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(SYNTAX_ERROR_LISTENER);
        return lexer;
    }


    /**
     * Create a new parser
     *
     * @param input the query input
     * @return the parser
     */
    private FilterParser createParser(final TokenStream input) {
        final FilterParser parser = new FilterParser(input);
        parser.removeErrorListeners();
        parser.addErrorListener(SYNTAX_ERROR_LISTENER);
        return parser;
    }

    /**
     * Create a new character stream for a given query
     *
     * @param preql the query
     * @return a character stream
     */
    private CharStream createStream(final String preql) throws IOException {
        final InputStream stream = new ByteArrayInputStream(preql.getBytes());
        return fromStream(stream);
    }

}
