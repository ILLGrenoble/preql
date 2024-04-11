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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.ManagedType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.PluralAttribute;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static jakarta.persistence.criteria.JoinType.LEFT;
import static jakarta.persistence.metamodel.Attribute.PersistentAttributeType.EMBEDDED;

/**
 * A mapper to map attributes to their respective paths in the entity object graph
 *
 * @author Jamie Hall
 */
public class AttributeMapper<E> {

    private static final Logger    logger = LoggerFactory.getLogger(AttributeMapper.class);
    private final        Root<E>   root;
    private final        Metamodel metamodel;

    /**
     * @param root      the root type in the from clause.
     * @param metamodel provides access to the metamodel of persistent
     *                  entities in the persistence unit.
     */
    public AttributeMapper(final Root<E> root, final Metamodel metamodel) {
        this.metamodel = metamodel;
        this.root = root;
    }

    /**
     * Get the path for a given attribute
     *
     * @param attribute the name of the represented attribute
     * @return the path for the property
     */
    public Path<?> get(final String attribute) {
        requireNonNull(attribute, "Attribute cannot be null");
        return traverse(attribute);
    }

    /**
     * Traverse the attribute
     *
     * @param attribute he name of the represented attribute to be traversed
     * @return the path to the attribute
     */
    private Path<?> traverse(final String attribute) {
        final String[] attributes = splitAttributes(attribute);
        return traverse(attributes, this.root);
    }

    /**
     * Split the attribute name by a dot
     *
     * @param attribute the name of the represented attribute
     * @return an array of attribute names
     */
    private String[] splitAttributes(final String attribute) {
        return attribute.split("\\.");
    }

    /**
     * @param attributes the name of the represented attribute
     * @param startRoot  the root to start from
     * @return the path for the given attribute
     */
    private Path<?> traverse(final String[] attributes, final Path<?> startRoot) {
        ManagedType<?> metadata = metamodel.managedType(startRoot.getJavaType());
        Path<?>        root     = startRoot;

        for (final String attribute : attributes) {
            hasAttributeName(attribute, metadata);
            if (isAssociation(attribute, metadata)) {
                Class<?>       association      = getType(attribute, metadata);
                ManagedType<?> previousMetadata = metadata;
                metadata = metamodel.managedType(association);
                Join<?, ?> join = hasJoin(metadata, previousMetadata);
                if (join == null) {
                    logger.debug("Creating join between {} and {}", previousMetadata.getJavaType(), metadata.getJavaType());
                    root = ((From) root).join(attribute, LEFT);
                    continue;
                }
                root = join;
                continue;
            }
            logger.debug("Create attribute path for type {} attribute  {}", metadata.getJavaType(), attribute);
            root = root.get(attribute);
            if (isEmbedded(attribute, metadata)) {
                Class<?> embedded = getType(attribute, metadata);
                metadata = metamodel.managedType(embedded);
            }
        }
        return root;
    }

    /**
     * Check that a join has already been added for a given association
     *
     * @param metadata         the metadata
     * @param previousMetadata the parent metadata
     * @return a join or null if not found
     */
    private Join<?, ?> hasJoin(final ManagedType<?> metadata, final ManagedType<?> previousMetadata) {
        for (Join<?, ?> join : root.getJoins()) {
            logger.debug("Join type {} and parent type: {}", join.getJavaType(), join.getParent().getJavaType());
            if ((join.getJavaType().equals(metadata.getJavaType()))
                    && join.getParent().getJavaType().equals(previousMetadata.getJavaType())) {
                logger.debug("Found existing join for {} and {}", metadata.getJavaType(), previousMetadata.getJavaType());
                return join;
            }
        }
        return null;
    }

    /**
     * Check that an attribute name exists for a given type
     *
     * @param attribute the name of the represented attribute
     * @param metadata  the metadata that represents the entity, mapped superclass or embeddable for the given attribute
     */
    private <T> void hasAttributeName(final String attribute, final ManagedType<T> metadata) {
        final Set<Attribute<? super T, ?>> names = metadata.getAttributes();
        for (final Attribute<? super T, ?> name : names) {
            if (name.getName().equals(attribute)) {
                return;
            }
        }
        throw new IllegalArgumentException(format("Unknown attribute %s on %s", attribute, metadata.getJavaType()));
    }

    /**
     * Find the java type for a given attribute
     *
     * @param attribute the name of the represented attribute
     * @param metadata  the metadata that represents the entity, mapped superclass or embeddable for the given attribute
     * @return the Java type of the represented attribute.
     */
    private <T> Class<?> getType(final String attribute, final ManagedType<T> metadata) {
        if (metadata.getAttribute(attribute).isCollection()) {
            return ((PluralAttribute) metadata.getAttribute(attribute)).getBindableJavaType();
        }
        return metadata.getAttribute(attribute).getJavaType();
    }

    /**
     * Check if the given attribute is an association
     *
     * @param attribute the name of the represented attribute
     * @param metadata  the metadata that represents the entity of the given property
     * @return true if it is an association, otherwise false
     */
    private <T> boolean isAssociation(final String attribute, final ManagedType<T> metadata) {
        return metadata.getAttribute(attribute).isAssociation();
    }

    /**
     * @param attribute the name of the represented attribute
     * @param metadata  the metadata that represents the entity, mapped superclass or embeddable for the given attribute
     * @return true if it is embedded otherwise false
     */
    private <T> boolean isEmbedded(final String attribute, final ManagedType<T> metadata) {
        return metadata.getAttribute(attribute).getPersistentAttributeType() == EMBEDDED;
    }

}
