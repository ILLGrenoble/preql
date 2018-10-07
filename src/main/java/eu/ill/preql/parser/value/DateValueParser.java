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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.String.format;

/**
 * Convert an object into a date
 *
 * @author Jamie Hall
 */
public class DateValueParser implements ValueParser<Date> {

    private static final String TYPE_DATE = "date";


    @Override
    public Object[] getSupportedTypes() {
        return new Object[]{
                Date.class,
                Date.class.getName(),
                TYPE_DATE
        };
    }

    @Override
    public Date parse(final Object value) {
        try {
            // Value must be in the "yyyy-mm-dd" format
            if (value instanceof Date) {
                return (Date) value;
            }
            final String v = value.toString();
            if (v.trim().length() > 0) {
                final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                return dateFormat.parse(v);
            }
        } catch (Exception exception) {
            throw new InvalidQueryException(format("Could not parse '%s' into a date", value));
        }
        throw new InvalidQueryException(format("Could not parse '%s' into a date", value));
    }
}
