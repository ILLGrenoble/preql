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
package eu.ill.preql.parser;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.preql.parser.value.LongValueParser;
import eu.ill.preql.parser.value.UUIDValueParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueParsersTest {

    private ValueParsers valueParsers = new ValueParsers();

    @Test
    @DisplayName("should get a parser for a given type")
    void get() {
        ValueParser<?> parser = valueParsers.getParser(Long.class, "1");
        assertThat(parser).isInstanceOf(LongValueParser.class);
    }

    @Test
    @DisplayName("should fail to get a parser for a given type")
    void failGet() {
        assertThrows(InvalidQueryException.class, () -> valueParsers.getParser(Test.class, "1"));
    }

    @Test
    @DisplayName("should successfully unregisterParser a value parser for a given type")
    void remove() {
        ValueParsers.unregisterParser(UUID.class);
        assertThrows(InvalidQueryException.class, () -> valueParsers.getParser(UUID.class, "1"));
    }

    @Test
    @DisplayName("should successfully add a new value parser")
    void add() {
        ValueParsers.unregisterParser(UUID.class);
        ValueParsers.registerParser(new UUIDValueParser());
        ValueParser<?> parser = valueParsers.getParser(UUID.class, "1");
        assertThat(parser).isInstanceOf(UUIDValueParser.class);
    }

}
