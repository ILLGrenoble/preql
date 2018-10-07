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

import static java.lang.Float.parseFloat;
import static java.lang.String.format;

/**
 * Convert an object into a float
 *
 * @author Jamie Hall
 */
public class FloatValueParser implements ValueParser<Float> {

    private static final String TYPE_FLOAT = "float";

    @Override
    public Object[] getSupportedTypes() {
        return new Object[]{
                Float.class,
                Float.TYPE,
                Float.class.getName(),
                TYPE_FLOAT
        };
    }

    @Override
    public Float parse(final Object value) {
        try {
            if (value instanceof Float) {
                return (Float) value;
            }
            final String v = value.toString();
            if (v.trim().length() > 0) {
                return parseFloat(v);
            }
        } catch (Exception exception) {
            throw new InvalidQueryException(format("Could not parse '%s' into a float", value));
        }
        throw new InvalidQueryException(format("Could not parse '%s' into a float", value));

    }
}
