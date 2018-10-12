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

import java.math.BigDecimal;

import static java.lang.String.format;

/**
 * @author Jamie Hall
 */
public class BigDecimalParameterParser implements ParameterParser<BigDecimal> {

    private static final String TYPE_BIG_DECIMAL = "bigdecimal";

    @Override
    public Object[] getSupportedTypes() {
        return new Object[]{
                BigDecimal.class,
                BigDecimal.class.getName(),
                TYPE_BIG_DECIMAL
        };
    }

    @Override
    public BigDecimal parse(final Object value) {
        try {
            if (value instanceof BigDecimal) {
                return (BigDecimal) value;
            }
            final String v = value.toString();
            if (v.trim().length() > 0) {
                return new BigDecimal(v);
            }
        } catch (Exception exception) {
            throw new InvalidQueryException(format("Could not parse '%s' into a bigdecimal", value));
        }
        throw new InvalidQueryException(format("Could not parse '%s' into a bigdecimal", value));
    }
}
