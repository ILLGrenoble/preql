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

import eu.ill.preql.exception.InvalidQueryException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Pagination tests")
class PaginationTest {

    @Test
    @DisplayName("should throw an illegal argument because limit is a negative")
    void limitIsNegative() {
        assertThrows(InvalidQueryException.class, () -> new Pagination(-1, 0), "Limit must be a positive value");
    }

    @Test
    @DisplayName("should throw an illegal argument because offset is negative")
    void offsetIsNegative() {
        assertThrows(InvalidQueryException.class, () -> new Pagination(0, -100), "Offset must be a positive value");
    }

    @Test
    @DisplayName("should create a default instance")
    void defaultInstance() {
        assertThat(Pagination.DEFAULT)
                .isInstanceOf(Pagination.class)
                .hasFieldOrPropertyWithValue("NO_ROW_OFFSET", 0)
                .hasFieldOrPropertyWithValue("NO_ROW_LIMIT", Integer.MAX_VALUE);
    }

}
