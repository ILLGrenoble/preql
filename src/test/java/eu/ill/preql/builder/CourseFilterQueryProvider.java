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
package eu.ill.preql.builder;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.preql.domain.Course;
import eu.ill.preql.support.parser.ByteFieldValueParser;
import eu.ill.preql.support.parser.CurrencyFieldValueParser;
import eu.ill.preql.support.parser.DurationFieldValueParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

public class CourseFilterQueryProvider extends AbstractFilterQueryProvider<Course> {
    private Logger logger = LoggerFactory.getLogger(CourseFilterQueryProvider.class);

    public CourseFilterQueryProvider(EntityManager entityManager) {
        super(Course.class, entityManager);
        // Register the fields that can be queried
        addFields(
                orderableField("id"),
                orderableField("name"),
                orderableField("description"),
                orderableField("code"),
                orderableField("active"),
                orderableField("credits"),
                orderableField("price", new CurrencyFieldValueParser()),
                orderableField("duration", new DurationFieldValueParser()),
                orderableField("details.summary", "summary"),
                orderableField("startDate"),
                orderableField("endDate"),
                field("tags.name", "tags"),
                field("teacher.name"),
                field("teacher.age"),
                field("teacher.affiliation.name"),
                field("attachments.size", new ByteFieldValueParser()),
                field("attachments.name")
        );
    }
}
