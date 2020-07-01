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
package eu.ill.preql;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.DBUnitExtension;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import eu.ill.preql.builder.CourseFilterQueryProvider;
import eu.ill.preql.domain.Course;
import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.preql.parser.QueryParser;
import eu.ill.preql.support.Pagination;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.instance;
import static com.google.common.collect.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(DBUnitExtension.class)
@RunWith(JUnitPlatform.class)
@DisplayName("Filter query tests")
public class FilterQueryTest {
    private final Logger logger = LoggerFactory.getLogger(FilterQueryTest.class);
    private ConnectionHolder connectionHolder = () -> instance("persistenceUnit").connection();

    private FilterQueryTest() {
        QueryParser.setMaxExpressions(3);
    }

    @Test
    @DisplayName("should successfully execute valid queries")
    @DataSet("data.yml")
    void validQueries() {
        assertThat(execute("id = :id", of("id", 1))).hasSize(1);
        assertThat(execute("id IS NOT NULL")).hasSize(5);
        assertThat(execute("id IS NULL")).hasSize(0);
        assertThat(execute("code = :code", of("code", "C-JAVA"))).hasSize(1);
        assertThat(execute("description LIKE :description", of("description", "%discovering web%"))).hasSize(1);
        assertThat(execute("description NOT LIKE :description", of("description", "%discovering web%"))).hasSize(4);
        assertThat(execute("price <= :price", of("price", 100.00))).hasSize(2);
        assertThat(execute("price <= :price", of("price", "90GBP"))).hasSize(2);
        assertThat(execute("summary = :summary", of("summary", "Discovering web development"))).hasSize(1);
        assertThat(execute("duration = :duration", of("duration", "10HOURS"))).hasSize(2);
        assertThat(execute("duration <= :duration", of("duration", "45MINS"))).hasSize(1);
        assertThat(execute("credits < :credits", of("credits", 2000))).hasSize(2);
        assertThat(execute("credits <= :credits", of("credits", 2000))).hasSize(5);
        assertThat(execute("active = :active", of("active", false))).hasSize(1);
        assertThat(execute("id IN :ids", of("ids", ImmutableList.of(1, 2, 3, 4)))).hasSize(4);
        assertThat(execute("id NOT IN :ids", of("ids", ImmutableList.of(1, 2, 3, 4)))).hasSize(1);
        assertThat(execute("tags = :tags", of("tags", "programming"))).hasSize(1);
        assertThat(execute("tags IS NOT NULL")).hasSize(1);
        assertThat(execute("tags = :tags", of("tags", "computing"))).hasSize(1);
        assertThat(execute("tags IN :tags", of("tags", ImmutableList.of("computing", "programming")))).hasSize(1);
        assertThat(execute("teacher.name =  :name", of("name", "Jamie Hall"))).hasSize(2);
        assertThat(execute("teacher.name = :teacher1 or  teacher.name = :teacher2", of("teacher1", "Jamie Hall", "teacher2", "Joe Bloggs"))).hasSize(4);
        assertThat(execute("teacher.name = :name AND credits <= :credits", of("name", "Jamie Hall", "credits", 1000))).hasSize(1);
        assertThat(execute("teacher.age <= :age", of("age", 50))).hasSize(5);
        assertThat(execute("credits BETWEEN :lowerBound AND :upperBound", of("lowerBound", 1000, "upperBound", 10000))).hasSize(5);
        assertThat(execute("credits NOT BETWEEN :lowerBound AND :upperBound", of("lowerBound", 1000, "upperBound", 10000))).hasSize(0);
        assertThat(execute("attachments.size >= :size", of("size", "1MB"))).hasSize(1);
        assertThat(execute("attachments.size >= :size", of("size", 2000))).hasSize(1);
        assertThat(execute("attachments.name = :name", of("name", "1.pdf"))).hasSize(1);
        assertThat(execute("startDate >= :startDate", of("startDate", "2017-01-01T00:00:00"))).hasSize(5);
        assertThat(execute("startDate BETWEEN :startDate AND :endDate", of("startDate", "2017-01-01T00:00:00", "endDate", "2018-03-01T23:59:59"))).hasSize(3);
        assertThat(execute("startDate BETWEEN :startDate AND :endDate", of("startDate", "2017-01-01", "endDate", "2018-03-01"))).hasSize(3);
    }

    @Test
    @DisplayName("should successfully execute valid count queries")
    @DataSet("data.yml")
    void validCountQueries() {
        assertThat(executeCount("id = :id", of("id", 1))).isEqualTo(1);
        assertThat(executeCount("id IS NOT NULL")).isEqualTo(5);
        assertThat(executeCount("id IS NULL")).isEqualTo(0);
        assertThat(executeCount("code = :code", of("code", "C-JAVA"))).isEqualTo(1);
        assertThat(executeCount("description LIKE :description", of("description", "%discovering web%"))).isEqualTo(1);
        assertThat(executeCount("description NOT LIKE :description", of("description", "%discovering web%"))).isEqualTo(4);
        assertThat(executeCount("price <= :price", of("price", 100.00))).isEqualTo(2);
        assertThat(executeCount("price <= :price", of("price", "90GBP"))).isEqualTo(2);
        assertThat(executeCount("price >= :lowerBound AND price <= :upperBound", of("lowerBound", "100GBP", "upperBound", "250GBP"))).isEqualTo(3);
        assertThat(executeCount("summary = :summary", of("summary", "Discovering web development"))).isEqualTo(1);
        assertThat(executeCount("duration = :duration", of("duration", "10HOURS"))).isEqualTo(2);
        assertThat(executeCount("duration <= :duration", of("duration", "45MINS"))).isEqualTo(1);
        assertThat(executeCount("credits < :credits", of("credits", 2000))).isEqualTo(2);
        assertThat(executeCount("credits <= :credits", of("credits", 2000))).isEqualTo(5);
        assertThat(executeCount("active = :active", of("active", false))).isEqualTo(1);
        assertThat(executeCount("id IN :ids", of("ids", ImmutableList.of(1, 2, 3, 4)))).isEqualTo(4);
        assertThat(executeCount("id NOT IN :ids", of("ids", ImmutableList.of(1, 2, 3, 4)))).isEqualTo(1);
        assertThat(executeCount("tags = :tags", of("tags", "programming"))).isEqualTo(1);
        assertThat(executeCount("tags IS NOT NULL")).isEqualTo(1);
        assertThat(executeCount("tags = :tags", of("tags", "computing"))).isEqualTo(1);
        assertThat(executeCount("tags IN :tags", of("tags", ImmutableList.of("computing", "programming")))).isEqualTo(1);
        assertThat(executeCount("teacher.name =  :name", of("name", "Jamie Hall"))).isEqualTo(2);
        assertThat(executeCount("teacher.name = :teacher1 or  teacher.name = :teacher2", of("teacher1", "Jamie Hall", "teacher2", "Joe Bloggs"))).isEqualTo(4);
        assertThat(executeCount("teacher.name = :name AND credits <= :credits", of("name", "Jamie Hall", "credits", 1000))).isEqualTo(1);
        assertThat(executeCount("teacher.age <= :age", of("age", 50))).isEqualTo(5);
        assertThat(executeCount("credits BETWEEN :lowerBound AND :upperBound", of("lowerBound", 1000, "upperBound", 10000))).isEqualTo(5);
        assertThat(executeCount("credits NOT BETWEEN :lowerBound AND :upperBound", of("lowerBound", 1000, "upperBound", 10000))).isEqualTo(0);
        assertThat(executeCount("attachments.size >= :size", of("size", "1MB"))).isEqualTo(1);
        assertThat(executeCount("attachments.size >= :size", of("size", 2000))).isEqualTo(1);
        assertThat(executeCount("attachments.name = :name", of("name", "1.pdf"))).isEqualTo(1);
        assertThat(executeCount("startDate >= :startDate", of("startDate", "2017-01-01T00:00:00"))).isEqualTo(5);
        assertThat(executeCount("startDate BETWEEN :startDate AND :endDate", of("startDate", "2017-01-01T00:00:00", "endDate", "2018-03-01T23:59:59"))).isEqualTo(3);
        assertThat(executeCount("startDate BETWEEN :startDate AND :endDate", of("startDate", "2017-01-01", "endDate", "2018-03-01"))).isEqualTo(3);
    }

    @Test
    @DisplayName("should fail to execute query because of duplicate query")
    void duplicateQuery() {
        assertThrows(InvalidQueryException.class, () -> execute(
                "name LIKE :name name LIKE :name",
                of("name", "Bob")));
    }

    @Test
    @DisplayName("should fail to execute query because of leading gibberish")
    void leadingGibberish() {
        assertThrows(InvalidQueryException.class, () -> execute(
                "abc name LIKE :name",
                of("name", "Bob")));
    }

    @Test
    @DisplayName("should fail to execute query because the query is unclosed")
    void unclosed() {
        assertThrows(InvalidQueryException.class, () -> execute(
                "name LIKE :name AND",
                of("name", "Bob")));

    }

    @Test
    @DisplayName("should fail to execute query because of unknown field")
    void unknownField() {
        assertThrows(InvalidQueryException.class, () -> execute(
                "name1 LIKE :name AND",
                of("name", "Bob")));

    }

    @Test
    @DisplayName("should fail to execute query because a specified parameter was not bound")
    void unboundParameter() {
        assertThrows(InvalidQueryException.class, () -> execute("name LIKE :name"));
    }

    @Test
    @DisplayName("should accept comments in the query")
    @DataSet("data.yml")
    void comments() {
        assertThat(execute("/** Get attachments by size */ attachments.size >= :size", of("size", 2000))).hasSize(1);
    }

    @Test
    @DisplayName("should successfully get a list of results")
    @DataSet("data.yml")
    void resultList() {
        final FilterQuery<Course> query = createQuery();
        assertThat(query.getResultList()).isInstanceOf(List.class).hasSize(5);
    }

    @Test
    @DisplayName("should successfully order results")
    @DataSet("data.yml")
    void orderByResultList() {
        final FilterQuery<Course> query = createQuery();
        query.setOrder("id", "desc");
        assertThat(query.getResultList())
                .isInstanceOf(List.class)
                .hasSize(5)
                .first()
                .hasFieldOrPropertyWithValue("id", 5L);
    }

    @Test
    @DisplayName("should successfully order results by joined column")
    @DataSet("data.yml")
    void orderResultsByJoinedColumn() {
        final FilterQuery<Course> query = createQuery();
        query.setOrder("teacher.name", "asc");
        final List<Course> results = query.getResultList();
        assertThat(results.get(0).getTeacher().getName()).isEqualTo("Jamie Hall");
        query.setOrder("teacher.name", "desc");
        final List<Course> results2 = query.getResultList();
        assertThat(results2.get(0).getTeacher().getName()).isEqualTo("Joe Bloggs");
    }


    @Test
    @DisplayName("should fail to order results because the order field has not been defined")
    @DataSet("data.yml")
    void failOrderByResultList() {
        final FilterQuery<Course> query = createQuery();
        assertThrows(InvalidQueryException.class, () -> query.setOrder("tags", "desc"));
    }

    @Test
    @DisplayName("should successfully limit results")
    @DataSet("data.yml")
    void limitResults() {
        final FilterQuery<Course> query = createQuery();
        query.setPagination(new Pagination(2, 0));
        assertThat(query.getResultList()).isInstanceOf(List.class).hasSize(2);
    }

    @Test
    @DisplayName("should successfully get a stream of results")
    @DataSet("data.yml")
    void resultStream() {
        final FilterQuery<Course> query = createQuery();
        assertThat(query.getResultStream()).isInstanceOf(Stream.class).hasSize(5);
    }

    @Test
    @DisplayName("should fail to execute query because expressions exceeds maximum expressions")
    void maximumExpressions() {
        assertThrows(InvalidQueryException.class, () -> execute(
                "teacher.age <= :age1 AND teacher.age >= :age2 AND teacher.age >= :age3 AND teacher.age >= :age4",
                of("age1", 50, "age2", 50, "age3", 50, "age4", 50)));
    }

    @Test
    @DisplayName("should fail to execute a query because the specified parameter type could not be coerced")
    void parameterCoercion() {
        assertThrows(InvalidQueryException.class, () -> execute("id = :id", of("id", "hello")));
    }

    @Test
    @DisplayName("should successfully get a single result")
    @DataSet("data.yml")
    void singleResult() {
        final FilterQuery<Course> query = createQuery("id = :id", ImmutableMap.of("id", 1));
        assertThat(query.getSingleResult())
                .isInstanceOf(Course.class)
                .hasFieldOrPropertyWithValue("id", 1L);
    }

    @Test
    @DisplayName("should successfully get a count result")
    @DataSet("data.yml")
    void countResult() {
        final FilterQuery<Course> query = createQuery("id = :id AND tags = :tag", ImmutableMap.of("id", 1, "tag", "computing"));
        assertThat(query.count())
                .isInstanceOf(Long.class)
                .isEqualTo(1L);
    }

    private List<Course> execute(final String preql) {
        return execute(preql, of());
    }

    private FilterQuery<Course> createQuery(final String preql, final Map<String, Object> parameters) {

        final CourseFilterQueryProvider provider = new CourseFilterQueryProvider(em());
        final FilterQuery<Course> query = provider.createQuery(preql);
        query.setParameters(parameters)
                .setPagination(new Pagination(100, 0));
        return query;
    }

    private FilterQuery<Course> createQuery(final Map<String, Object> parameters) {
        return createQuery(null, parameters);
    }

    private FilterQuery<Course> createQuery() {
        return createQuery(null, ImmutableMap.of());
    }

    private List<Course> execute(final String preql, final Map<String, Object> parameters) {
        final CourseFilterQueryProvider provider = new CourseFilterQueryProvider(em());
        final FilterQuery<Course> query = provider.createQuery(preql);
        query.setParameters(parameters)
                .addExpression((cb, root) -> cb.equal(root.get("tenant").get("id"), 1))
                .setPagination(new Pagination(100, 0));
        return query.getResultList();
    }

    private Long executeCount(final String preql) {
        return executeCount(preql, of());
    }

    private Long executeCount(final String preql, final Map<String, Object> parameters) {
        final CourseFilterQueryProvider provider = new CourseFilterQueryProvider(em());
        final FilterQuery<Course> query = provider.createQuery(preql);
        query.setParameters(parameters)
                .addExpression((cb, root) -> cb.equal(root.get("tenant").get("id"), 1))
                .setPagination(new Pagination(100, 0));
        return query.count();
    }


}