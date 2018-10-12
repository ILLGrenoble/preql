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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Big decimal parameter parser tests")
class BigDecimalParameterParserTest {
    private final BigDecimalParameterParser parser = new BigDecimalParameterParser();

    @Test
    @DisplayName("should successfully convert valid values")
    void valid() {
        assertThat(parser.parse(1L)).isInstanceOf(BigDecimal.class);
        assertThat(parser.parse(1)).isInstanceOf(BigDecimal.class);
        assertThat(parser.parse(1.5)).isInstanceOf(BigDecimal.class);
        assertThat(parser.parse("1.0")).isInstanceOf(BigDecimal.class);
    }

    @Test
    @DisplayName("should fail to convert invalid values")
    void invalid() {
        assertThrows(InvalidQueryException.class, () -> parser.parse("hello"));
        assertThrows(InvalidQueryException.class, () -> parser.parse(true));
        assertThrows(InvalidQueryException.class, () -> parser.parse(null));
    }
}
