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
package eu.ill.preql.exception;

/**
 * Thrown to indicate that there was an error executing the preql query
 *
 * @author Jamie Hall
 */
public class InvalidQueryException extends IllegalStateException {

    /**
     * Constructs an InvalidQueryException with the specified detail
     * message.  A detail message is a String that describes this particular
     * exception.
     *
     * @param message the String that contains a detailed message
     */
    public InvalidQueryException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     *
     * <p>Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this exception's detail
     * message.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public InvalidQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
