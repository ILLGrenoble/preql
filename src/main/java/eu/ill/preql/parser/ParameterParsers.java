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
import eu.ill.preql.parser.parameter.*;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.synchronizedMap;

/**
 * Parameter parsers to coerce expression parameters into their respective attribute type
 *
 * @author Jamie Hall
 */
public class ParameterParsers {

    protected static final Map<Object, ParameterParser<?>> parsers = synchronizedMap(new HashMap<>());

    static {
        registerParser(new BigDecimalParameterParser());
        registerParser(new BooleanParameterParser());
        registerParser(new ByteParameterParser());
        registerParser(new CharacterParameterParser());
        registerParser(new DoubleParameterParser());
        registerParser(new FloatParameterParser());
        registerParser(new IntegerParameterParser());
        registerParser(new LongParameterParser());
        registerParser(new ShortParameterParser());
        registerParser(new StringParameterParser());
        registerParser(new DateParameterParser());
        registerParser(new UUIDParameterParser());
    }

    /**
     * Add a new parameter parser
     *
     * @param key    The object type
     * @param parser The parser to be added
     */
    public static void registerParser(final Object key, final ParameterParser<?> parser) {
        parsers.put(key, parser);
    }

    /**
     * Add a new parameter parser
     *
     * @param parser The parser to be added
     */
    public static void registerParser(final ParameterParser<?> parser) {
        final Object[] keys = parser.getSupportedTypes();
        if (keys == null) {
            return;
        }

        for (Object key : keys) {
            registerParser(key, parser);
        }
    }

    /**
     * Remove a parameter parser
     *
     * @param key The parameter parser to be removed
     */
    public static void unregisterParser(final Object key) {
        parsers.remove(key);
    }

    /**
     * Get all registered parses
     *
     * @return all of the registered parsers
     */
    public Map<Object, ParameterParser<?>> getParsers() {
        return parsers;
    }

    /**
     * Get a parameter parser for a given object type
     *
     * @param typeKey The type of object
     * @param value   The meter
     * @return the parser
     */
    public ParameterParser<?> getParser(final Class<?> typeKey, final Object value) {
        // Check if the provided parameter is already of the target type
        if (typeKey != null && typeKey != Object.class
                && typeKey.isInstance(value)) {
            return new IdentityParameterParser();
        }
        if (parsers.containsKey(typeKey)) {
            // Find the type conversion object
            return parsers.get(typeKey);
        }
        throw new InvalidQueryException(format("No parameter parser registered for type: %s", typeKey));
    }
}
