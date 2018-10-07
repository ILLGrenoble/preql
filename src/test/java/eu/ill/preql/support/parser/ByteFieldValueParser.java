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
import eu.ill.preql.parser.value.LongValueParser;
import eu.ill.preql.parser.FieldValueParser;
import eu.ill.preql.parser.ValueParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Double.parseDouble;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;

public class ByteFieldValueParser implements FieldValueParser<Long> {

    private final static long KB_FACTOR  = 1000;
    private final static long KIB_FACTOR = 1024;
    private final static long MB_FACTOR  = 1000 * KB_FACTOR;
    private final static long MIB_FACTOR = 1024 * KIB_FACTOR;
    private final static long GB_FACTOR  = 1000 * MB_FACTOR;
    private final static long GIB_FACTOR = 1024 * MIB_FACTOR;

    private ValueParser<Long> fallbackValueParser = new LongValueParser();

    @Override
    public Long parse(final Object value) {
        if (value instanceof String) {
            final Pattern pattern = compile("^(\\d+\\.?\\d*)(KB|KIB|MB|MIB|GB|GIB)$");
            final Matcher matcher = pattern.matcher((String) value);
            if (matcher.matches()) {
                double ret = parseDouble(matcher.group(1));
                switch (matcher.group(2)) {
                    case "GB":
                        return (long) ret * GB_FACTOR;
                    case "GiB":
                        return (long) ret * GIB_FACTOR;
                    case "MB":
                        return (long) ret * MB_FACTOR;
                    case "MiB":
                        return (long) ret * MIB_FACTOR;
                    case "KB":
                        return (long) ret * KB_FACTOR;
                    case "KiB":
                        return (long) ret * KIB_FACTOR;
                }
            }
            throw new InvalidQueryException(format("Could not parse '%s' into bytes", value));
        }
        return fallbackValueParser.parse(value);
    }
}
