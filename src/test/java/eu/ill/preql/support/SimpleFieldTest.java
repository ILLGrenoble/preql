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
package eu.ill.preql.support;

import eu.ill.preql.parser.FieldValueParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.persistence.criteria.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@DisplayName("Simple field tests")
class SimpleFieldTest {

    private final Path                     path   = mock(Path.class);
    private final FieldValueParser<String> parser = value -> "hello";

    @Test
    @DisplayName("should successfully create a new field instance")
    void create() {
        assertThat(new SimpleField("name", "name", path)).isInstanceOf(SimpleField.class);
        assertThat(new SimpleField("name", "name", path, parser)).isInstanceOf(SimpleField.class);
    }

    @Test
    @DisplayName("should successfully test getters")
    void getters() {
        final SimpleField name = new SimpleField("name", "name", path, parser);
        assertThat(name.getName()).isEqualTo("name");
        assertThat(name.getPath()).isEqualTo(path);
        assertThat(name.getValueParser()).isEqualTo(parser);
    }

    @Test
    @DisplayName("should fail to create a new field instance because the name is null")
    void nameIsNull() {
        assertThrows(NullPointerException.class, () -> new SimpleField("name", null, path, parser));
        assertThrows(NullPointerException.class, () -> new SimpleField("name", "name", null, parser));
    }

    @Test
    @DisplayName("should fail to create a new field instance because the attribute is null")
    void attributeIsNull() {
        assertThrows(NullPointerException.class, () -> new SimpleField(null, "name", path, parser));
        assertThrows(NullPointerException.class, () -> new SimpleField(null, "name", null, parser));
    }
}
