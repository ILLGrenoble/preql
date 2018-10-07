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
package eu.ill.preql.parser.value;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.preql.parser.ValueParser;

import static java.lang.Double.parseDouble;
import static java.lang.String.format;

/**
 * Convert an object into a double
 *
 * @author Jamie Hall
 */
public class DoubleValueParser implements ValueParser<Double> {

    private static final String TYPE_DOUBLE = "double";

    @Override
    public Object[] getSupportedTypes() {
        return new Object[]{
                Double.class,
                Double.TYPE,
                Double.class.getName(),
                TYPE_DOUBLE
        };
    }

    @Override
    public Double parse(final Object value) {
        try {
            if (value instanceof Double) {
                return (Double) value;
            }
            final String v = value.toString();
            if (v.trim().length() > 0) {
                return parseDouble(v);
            }
        } catch (Exception exception) {
            throw new InvalidQueryException(format("Could not parse '%s' into a double", value));
        }
        throw new InvalidQueryException(format("Could not parse '%s' into a double", value));
    }
}
