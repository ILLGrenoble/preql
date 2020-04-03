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
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A class to allow the pagination of a query
 *
 * @author Jamie Hall
 */
public final class Pagination {

    public static final int NO_ROW_OFFSET = 0;
    public static final int NO_ROW_LIMIT = Integer.MAX_VALUE;
    public static final Pagination DEFAULT = new Pagination();

    private final int offset;
    private final int limit;

    private Pagination() {
        this.offset = NO_ROW_OFFSET;
        this.limit = NO_ROW_LIMIT;
    }

    /**
     * @param limit  maximum number of results to retrieve
     *               numbered from 0
     * @param offset position of the first result,
     * @throws InvalidQueryException if either argument is negative
     */
    public Pagination(int limit, int offset) {
        if (offset < 0) {
            throw new InvalidQueryException("Limit must be a positive value");
        }
        if (limit < 0) {
            throw new InvalidQueryException("Limit must be a positive value");
        }
        this.offset = offset;
        this.limit = limit;
    }

    /**
     * Gets the offset
     *
     * @return the offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Gets the limit
     *
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("offset", offset)
                .append("limit", limit)
                .toString();
    }
}
