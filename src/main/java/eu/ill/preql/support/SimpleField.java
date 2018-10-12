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
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.criteria.Path;

import static java.util.Objects.requireNonNull;

/**
 * Defines a standard field
 * Use {@link OrderableField} if you want a field that can be ordered
 *
 * @author Jamie Hall
 */
public class SimpleField implements Field {

    private String      name;
    private Path<?>     attribute;
    private FieldParser valueParser = null;

    /**
     * Create a new field
     *
     * @param name      - the name of the field
     * @param attribute - attribute to the field
     */
    public SimpleField(final String name, final Path<?> attribute) {
        this.name = requireNonNull(name, "Name cannot be null");
        requireNonNull(attribute, "Path cannot be null");
        this.attribute = attribute;

    }

    /**
     * Create a new field
     *
     * @param name        - the name of the field
     * @param attribute   - attribute to the field
     * @param valueParser - a custom parameter parser for the field
     */
    public SimpleField(final String name, final Path<?> attribute, final FieldParser valueParser) {
        this(name, attribute);
        this.valueParser = valueParser;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Path<?> getAttribute() {
        return attribute;
    }

    @Override
    public FieldParser getValueParser() {
        return valueParser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SimpleField field = (SimpleField) o;

        return new EqualsBuilder()
                .append(name, field.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("attribute", attribute)
                .append("valueParser", valueParser)
                .toString();
    }
}
