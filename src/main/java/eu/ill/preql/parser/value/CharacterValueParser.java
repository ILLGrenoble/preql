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

import static java.lang.String.format;

/**
 * Convert an object into a character
 *
 * @author Jamie Hall
 */
public class CharacterValueParser implements ValueParser<Character> {

    private static final String TYPE_CHAR      = "char";
    private static final String TYPE_CHARACTER = "character";

    @Override
    public Object[] getSupportedTypes() {
        return new Object[]{
                Character.class,
                Character.TYPE,
                Character.class.getName(),
                TYPE_CHAR,
                TYPE_CHARACTER
        };
    }

    @Override
    public Character parse(final Object value) {
        try {
            if (value instanceof Character) {
                return (Character) value;
            }
            final String v = value.toString();
            if (v.trim().length() > 0) {
                return v.charAt(0);
            }
        } catch (Exception exception) {
            throw new InvalidQueryException(format("Could not parse '%s' into a character", value));
        }
        throw new InvalidQueryException(format("Could not parse '%s' into a character", value));
    }
}
