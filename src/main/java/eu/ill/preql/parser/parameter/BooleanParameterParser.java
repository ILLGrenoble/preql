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
 * Convert an object into a boolean
 *
 * @author Jamie Hall
 */
public class BooleanParameterParser implements ParameterParser<Boolean> {

    private static final String BOOLEAN_TYPE = "boolean";
    private static final String TRUE_STR     = Boolean.TRUE.toString();
    private static final String FALSE_STR    = Boolean.FALSE.toString();

    @Override
    public Object[] getSupportedTypes() {
        return new Object[]{
                Boolean.class,
                Boolean.TYPE,
                Boolean.class.getName(),
                BOOLEAN_TYPE
        };
    }

    @Override
    public Boolean parse(final Object value) {
        try {
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            final String v = value.toString();
            if (v.trim().length() > 0) {
                if (TRUE_STR.equalsIgnoreCase(v)) {
                    return true;
                } else if (FALSE_STR.equalsIgnoreCase(v)) {
                    return false;
                }
            }
        } catch (Exception exception) {
            throw new InvalidQueryException(format("Could not parse '%s' into a boolean", value));
        }
        throw new InvalidQueryException(format("Could not parse '%s' into a boolean", value));
    }
}

