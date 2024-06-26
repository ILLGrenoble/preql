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

import jakarta.persistence.criteria.Path;

/**
 * Defines a field that can be ordered
 *
 * @author Jamie Hall
 */
public class OrderableField extends SimpleField {

    public OrderableField(String attribute, String name, final Path<?> path) {
        super(attribute, name, path);
    }

    public OrderableField(String attribute, String name, final Path<?> path, FieldValueParser valueParser) {
        super(attribute, name, path, valueParser);
    }
}
