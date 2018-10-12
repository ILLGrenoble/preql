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
package eu.ill.preql.parser.parameter;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.preql.parser.ParameterParser;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

/**
 * Convert an object into an integer
 *
 * @author Jamie Hall
 */
public class IntegerParameterParser implements ParameterParser<Integer> {

    private static final String TYPE_INT = "int";

    private static final String TYPE_INTEGER = "integer";

    @Override
    public Object[] getSupportedTypes() {
        return new Object[]{
                Integer.class,
                Integer.TYPE,
                Integer.class.getName(),
                TYPE_INT,
                TYPE_INTEGER
        };
    }

    @Override
    public Integer parse(final Object value) {
        try {
            if (value instanceof Integer) {
                return (Integer) value;
            }

            final String v = value.toString();
            if (v.trim().length() > 0) {
                return parseInt(v);
            }
        } catch (Exception exception) {
            throw new InvalidQueryException(format("Could not parse '%s' into an integer", value));
        }
        throw new InvalidQueryException(format("Could not parse '%s' into an integer", value));
    }
}
