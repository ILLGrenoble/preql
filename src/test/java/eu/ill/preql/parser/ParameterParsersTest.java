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
import eu.ill.preql.parser.parameter.LongParameterParser;
import eu.ill.preql.parser.parameter.UUIDParameterParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParameterParsersTest {

    private ParameterParsers parameterParsers = new ParameterParsers();

    @Test
    @DisplayName("should get a parser for a given type")
    void get() {
        ParameterParser<?> parser = parameterParsers.getParser(Long.class, "1");
        assertThat(parser).isInstanceOf(LongParameterParser.class);
    }

    @Test
    @DisplayName("should fail to get a parser for a given type")
    void failGet() {
        assertThrows(InvalidQueryException.class, () -> parameterParsers.getParser(Test.class, "1"));
    }

    @Test
    @DisplayName("should successfully unregisterParser a parameter parser for a given type")
    void remove() {
        ParameterParsers.unregisterParser(UUID.class);
        assertThrows(InvalidQueryException.class, () -> parameterParsers.getParser(UUID.class, "1"));
    }

    @Test
    @DisplayName("should successfully add a new parameter parser")
    void add() {
        ParameterParsers.unregisterParser(UUID.class);
        ParameterParsers.registerParser(new UUIDParameterParser());
        ParameterParser<?> parser = parameterParsers.getParser(UUID.class, "1");
        assertThat(parser).isInstanceOf(UUIDParameterParser.class);
    }

}
