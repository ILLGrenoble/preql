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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("UUID value parser tests")
class UUIDValueParserTest {
    private UUIDValueParser parser = new UUIDValueParser();

    @Test
    @DisplayName("should successfully convert valid values")
    void valid() {
        assertThat(parser.parse(randomUUID())).isInstanceOf(UUID.class);
        assertThat(parser.parse("a77b735f-8357-4dae-a124-56604b165041")).isInstanceOf(UUID.class);
    }

    @Test
    @DisplayName("should fail to convert invalid values")
    void invalid() {
        assertThrows(InvalidQueryException.class, () -> parser.parse("invalid-uuid"));
        assertThrows(InvalidQueryException.class, () -> parser.parse(1L));
        assertThrows(InvalidQueryException.class, () -> parser.parse(0.0));
        assertThrows(InvalidQueryException.class, () -> parser.parse(true));
        assertThrows(InvalidQueryException.class, () -> parser.parse(null));
    }
}
