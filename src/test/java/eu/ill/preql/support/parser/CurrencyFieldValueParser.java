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
package eu.ill.preql.support.parser;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.preql.parser.ValueParser;
import eu.ill.preql.parser.value.DoubleValueParser;
import eu.ill.preql.parser.FieldValueParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Double.parseDouble;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;

/**
 * Convert dollars into EUR or GBP
 */
public class CurrencyFieldValueParser implements FieldValueParser<Double> {

    private final static double EUR_RATE = 1.12;
    private final static double GBP_RATE = 1.30;

    /**
     * If we cannot convert, then fallback to this value parser
     */
    private ValueParser<Double> fallbackValueParser = new DoubleValueParser();

    @Override
    public Double parse(final Object value) {
        if (value instanceof String) {
            final Pattern pattern = compile("^(?<value>\\d+.\\d{0,2})(?<currency>GBP|EUR)$");
            final Matcher matcher = pattern.matcher((String) value);
            if (matcher.matches()) {
                double ret = parseDouble(matcher.group("value"));
                switch (matcher.group("currency")) {
                    case "GBP":
                        return ret * GBP_RATE;
                    case "EUR":
                        return ret * EUR_RATE;
                }
            }
            throw new InvalidQueryException(format("Could not parse '%s' into currency", value));
        }
        return fallbackValueParser.parse(value);
    }
}
