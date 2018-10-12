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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Date parameter parser tests")
class DateParameterParserTest {
    private final DateParameterParser parser = new DateParameterParser();

    @Test
    @DisplayName("should successfully convert valid values")
    void valid() {
        assertThat(parser.parse("2018-01-01T13:30:01")).isEqualTo("2018-01-01T13:30:01.000");
        assertThat(parser.parse("2018-01-01")).isEqualTo("2018-01-01T00:00:00.000");
    }

    @Test
    @DisplayName("should successfully get all formats")
    void getFormats() {
        assertThat(DateParameterParser.getFormats()).hasSize(2);
    }

    @Test
    @DisplayName("should successfully register and unregister a new format")
    void registerAndUnregisterFormat() {
        DateParameterParser.registerFormat("yyyy-MM");
        assertThat(parser.parse("2018-06")).isEqualTo("2018-06-01T00:00:00.000");
        DateParameterParser.unregisterFormat("yyyy-MM");
        assertThrows(InvalidQueryException.class, () -> parser.parse("2018-06"));
    }

    @Test
    @DisplayName("should fail to convert invalid values")
    void invalid() {
        assertThrows(InvalidQueryException.class, () -> parser.parse("hello"));
        assertThrows(InvalidQueryException.class, () -> parser.parse(true));
        assertThrows(InvalidQueryException.class, () -> parser.parse(1.5));
        assertThrows(InvalidQueryException.class, () -> parser.parse(null));
    }
}
