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

import eu.ill.preql.parser.FieldParser;

import javax.persistence.criteria.Path;

/**
 * Represents a field
 *
 * @author Jamie Hall
 */
public interface Field {
    /**
     * The name of the field that is used in the query expressions
     *
     * @return the name of the field
     */
    String getName();

    /**
     * Represents a simple or compound attribute path from a
     * bound type or collection, and is a "primitive" expression.
     *
     * @return path to the attribute of the field
     */
    Path<?> getAttribute();

    /**
     * A custom parameter parser for the field
     *
     * @return the parameter parser
     */
    FieldParser getValueParser();
}
