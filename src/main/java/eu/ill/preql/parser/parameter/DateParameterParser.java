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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static java.lang.String.format;

/**
 * Convert an object into a date
 *
 * @author Jamie Hall
 */
public class DateParameterParser implements ParameterParser<Date> {

    private static final String                        TYPE_DATE = "date";
    private static final Map<String, SimpleDateFormat> formats   = new HashMap<>();

    static {
        registerFormat("yyyy-MM-dd'T'HH:mm:ss");
        registerFormat("yyyy-MM-dd");
    }

    public static Map<String, SimpleDateFormat> getFormats() {
        return formats;
    }

    /**
     * Register a new date format pattern
     *
     * @param pattern the pattern to register
     */
    public static void registerFormat(final String pattern) {
        final SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setLenient(false);
        formats.put(pattern, format);
    }

    /**
     * Unregister a date format pattern
     *
     * @param pattern the pattern to unregister
     */
    public static void unregisterFormat(final String pattern) {
        formats.remove(pattern);
    }

    /**
     * Unregister all formats
     */
    public static void unregisterFormats() {
        formats.clear();
    }

    @Override
    public Object[] getSupportedTypes() {
        return new Object[]{
                Date.class,
                Date.class.getName(),
                TYPE_DATE
        };
    }

    /**
     * Try to parse the given string into a data object
     *
     * @param value the string to parse
     * @return a date object
     */
    private Date parseDate(final String value) {
        for (Entry<String, SimpleDateFormat> entry : formats.entrySet()) {
            final SimpleDateFormat format = entry.getValue();
            try {
                return new Date(format.parse(value).getTime());
            } catch (ParseException pe) {
                // keep trying other formats...
            }
        }
        throw new InvalidQueryException(format("Could not parse '%s' into a date", value));
    }

    @Override
    public Date parse(final Object value) {
        try {
            if (value instanceof Date) {
                return (Date) value;
            }
            final String v = value.toString();
            if (v.trim().length() > 0) {
                return parseDate(v);
            }
        } catch (Exception exception) {
            throw new InvalidQueryException(format("Could not parse '%s' into a date", value));
        }
        throw new InvalidQueryException(format("Could not parse '%s' into a date", value));
    }
}
