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

import eu.ill.preql.domain.Attachment;
import eu.ill.preql.domain.Course;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.Date;

import static javax.persistence.Persistence.createEntityManagerFactory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@RunWith(JUnitPlatform.class)
@DisplayName("Attribute mapper tests")
@TestInstance(Lifecycle.PER_CLASS)
class AttributeMapperTest {

    private Root<Course>            root;
    private AttributeMapper<Course> mapper;

    @BeforeAll
    public void setup() {
        final EntityManagerFactory  entityManagerFactory = createEntityManagerFactory("persistenceUnit");
        final EntityManager         em                   = entityManagerFactory.createEntityManager();
        final CriteriaBuilder       criteriaBuilder      = em.getCriteriaBuilder();
        final CriteriaQuery<Course> query                = criteriaBuilder.createQuery(Course.class);

        this.root = query.from(Course.class);
        this.mapper = new AttributeMapper<>(root, em.getMetamodel());
    }

    @Test
    @DisplayName("should successfully return the path for a given attribute")
    void attribute() {
        assertThat(mapper.get("name").getJavaType()).isEqualTo(String.class);
        assertThat(mapper.get("code").getJavaType()).isEqualTo(String.class);
        assertThat(mapper.get("credits").getJavaType()).isEqualTo(Integer.class);
        assertThat(mapper.get("startDate").getJavaType()).isEqualTo(Date.class);
    }

    @Test
    @DisplayName("should successfully return the path for a given attribute that belongs to a collection")
    void collectionAttribute() {
        final Path<?> path = mapper.get("attachments.size");
        assertThat(path.getJavaType()).isEqualTo(Long.class);
        assertThat(path.getParentPath().getJavaType()).isEqualTo(Attachment.class);
        assertThat(path.getParentPath().getParentPath().getJavaType()).isEqualTo(Course.class);
    }

    @Test
    @DisplayName("should successfully return one join for two defined attributes that belong to the same collection")
    void correctNumberJoins() {
        assertThat(mapper.get("attachments.size").getJavaType()).isEqualTo(Long.class);
        assertThat(mapper.get("attachments.name").getJavaType()).isEqualTo(String.class);
        assertThat(root.getJoins()).hasSize(1);
    }

    @Test
    @DisplayName("should fail to return the path for a given attribute because it does not exist")
    void attributeDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> mapper.get("dummy"));
    }

    @Test
    @DisplayName("should throw an exception for a given attribute because it is null")
    void attributeIsNull() {
        assertThrows(NullPointerException.class, () -> mapper.get(null));
    }

}
