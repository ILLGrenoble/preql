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
import eu.ill.preql.parser.value.*;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Collections.synchronizedMap;

/**
 * Value parsers to coerce expression parameters into their respective attribute type
 *
 * @author Jamie Hall
 */
public class ValueParsers {

    protected static final Map<Object, ValueParser<?>> parsers = synchronizedMap(new HashMap<>());

    static {
        registerParser(new BigDecimalValueParser());
        registerParser(new BooleanValueParser());
        registerParser(new ByteValueParser());
        registerParser(new CharacterValueParser());
        registerParser(new DoubleValueParser());
        registerParser(new FloatValueParser());
        registerParser(new IntegerValueParser());
        registerParser(new LongValueParser());
        registerParser(new ShortValueParser());
        registerParser(new StringValueParser());
        registerParser(new DateValueParser());
        registerParser(new UUIDValueParser());
    }

    /**
     * Add a new value parser
     *
     * @param key    The object type
     * @param parser The parser to be added
     */
    public static void registerParser(final Object key, final ValueParser<?> parser) {
        parsers.put(key, parser);
    }

    /**
     * Add a new value parser
     *
     * @param parser The parser to be added
     */
    public static void registerParser(final ValueParser<?> parser) {
        final Object[] keys = parser.getSupportedTypes();
        if (keys == null) {
            return;
        }

        for (Object key : keys) {
            registerParser(key, parser);
        }
    }

    /**
     * Remove a value parser
     *
     * @param key The value parser to be removed
     */
    public static void unregisterParser(final Object key) {
        parsers.remove(key);
    }

    /**
     * Get all registered parses
     *
     * @return all of the registered parsers
     */
    public Map<Object, ValueParser<?>> getParsers() {
        return parsers;
    }

    /**
     * Get a value parser for a given object type
     *
     * @param typeKey The type of object
     * @param value   The parameter value
     * @return the parser
     */
    public ValueParser<?> getParser(final Class<?> typeKey, final Object value) {
        // Check if the provided value is already of the target type
        if (typeKey != null && typeKey != Object.class
                && typeKey.isInstance(value)) {
            return new IdentityValueParser();
        }
        if (parsers.containsKey(typeKey)) {
            // Find the type conversion object
            return parsers.get(typeKey);
        }
        throw new InvalidQueryException(format("No value parser registered for type: %s", typeKey));
    }
}
