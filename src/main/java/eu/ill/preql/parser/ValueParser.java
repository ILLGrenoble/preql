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

/**
 * Represents a parser for coercing an object into a specified type
 *
 * @param <T> the type to be coerced into
 * @author Jamie Hall
 */
public interface ValueParser<T> {
    /**
     * Get the types supported by this value parser
     *
     * @return the supported types
     */
    Object[] getSupportedTypes();

    /**
     * Coerce an object into the generic type
     *
     * @param value the value to convert
     * @return the converted value
     */
    T parse(Object value);
}
