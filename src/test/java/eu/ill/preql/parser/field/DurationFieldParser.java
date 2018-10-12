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
package eu.ill.preql.parser.field;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.preql.parser.parameter.LongParameterParser;
import eu.ill.preql.parser.FieldParser;
import eu.ill.preql.parser.ParameterParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.util.concurrent.TimeUnit.*;
import static java.util.regex.Pattern.compile;

public class DurationFieldParser implements FieldParser<Long> {

    /**
     * If we cannot convert, then fallback to this parameter parser
     */
    private ParameterParser<Long> fallbackParameterParser = new LongParameterParser();

    @Override
    public Long parse(Object value) {
        if (value instanceof String) {
            final Pattern pattern = compile("^(?<value>\\d+)(?<unit>MINS|SECONDS|HOURS|DAYS)$");
            final Matcher matcher = pattern.matcher((String) value);
            if (matcher.matches()) {
                long ret = parseLong(matcher.group("value"));
                switch (matcher.group("unit")) {
                    case "MINS":
                        return MINUTES.toMillis(ret);
                    case "SECONDS":
                        return SECONDS.toMillis(ret);
                    case "HOURS":
                        return HOURS.toMillis(ret);
                    case "DAYS":
                        return DAYS.toMillis(ret);
                }
            }
            throw new InvalidQueryException(format("Could not parse '%s' into duration", value));
        }
        return fallbackParameterParser.parse(value);
    }
}
