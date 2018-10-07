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

import java.util.UUID;

import static java.lang.String.format;
import static java.util.UUID.fromString;

/**
 * Convert an object into a UUID
 *
 * @author Jamie Hall
 */
public class UUIDValueParser implements ValueParser<UUID> {

    private static final String TYPE_UUID = "uuid";

    @Override
    public Object[] getSupportedTypes() {
        return new Object[]{
                UUID.class,
                UUID.class.getName(),
                TYPE_UUID
        };
    }

    @Override
    public UUID parse(final Object value) {
        try {
            if (value instanceof UUID) {
                return (UUID) value;
            }

            final String v = value.toString();
            if (v.trim().length() > 0) {
                return fromString(v);
            }
        } catch (Exception exception) {
            throw new InvalidQueryException(format("Could not parse '%s' into a UUID", value));
        }
        throw new InvalidQueryException(format("Could not parse '%s' into a UUID", value));
    }
}
