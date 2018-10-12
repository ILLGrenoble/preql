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

@DisplayName("Character parameter parser tests")
class CharacterParameterParserTest {
    private final CharacterParameterParser parser = new CharacterParameterParser();

    @Test
    @DisplayName("should successfully convert valid values")
    void valid() {
        assertThat(parser.parse("1")).isInstanceOf(Character.class);
        assertThat(parser.parse("hello")).isInstanceOf(Character.class);
        assertThat(parser.parse("c")).isInstanceOf(Character.class);
        assertThat(parser.parse(1)).isInstanceOf(Character.class);
        assertThat(parser.parse(1L)).isInstanceOf(Character.class);
        assertThat(parser.parse(true)).isInstanceOf(Character.class);
    }

    @Test
    @DisplayName("should fail to convert invalid values")
    void invalid() {
        assertThrows(InvalidQueryException.class, () -> parser.parse(null));
    }
}
