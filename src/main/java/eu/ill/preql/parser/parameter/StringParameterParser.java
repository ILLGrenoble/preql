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

import static java.lang.String.format;

/**
 * Convert an object into a string
 *
 * @author Jamie Hall
 */
public class StringParameterParser implements ParameterParser<String> {

    private static final String TYPE_STRING = "string";


    @Override
    public Object[] getSupportedTypes() {
        return new Object[]{
                String.class,
                String.class.getName(),
                TYPE_STRING
        };
    }

    @Override
    public String parse(final Object value) {
        try {
            if (value instanceof String) {
                return (String) value;
            }
            if (value.getClass().isArray()) {
                // This is a byte array; we can parse it to a string
                if (value.getClass().getComponentType() == Byte.TYPE) {
                    return new String((byte[]) value);
                } else if (value.getClass().getComponentType() == Character.TYPE) {
                    return new String((char[]) value);
                }
            }
            return value.toString();
        } catch (Exception exception) {
            throw new InvalidQueryException(format("Could not parse '%s' into a string", value));
        }
    }
}
