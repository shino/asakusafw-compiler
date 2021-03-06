/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dag.runtime.adapter;

import java.util.List;

/**
 * A data table.
 * @param <T> the data type
 * @since 0.4.0
 */
public interface DataTable<T> {

    /**
     * Creates a new key buffer for this table.
     * @return the created key buffer
     */
    KeyBuffer newKeyBuffer();

    /**
     * Returns the elements in this table.
     * @param key the search key
     * @return the elements about the specified key
     */
    List<T> getList(KeyBuffer key);

    /**
     * A builder for building {@link DataTable}.
     * @param <T> the data type
     * @since 0.4.0
     */
    public interface Builder<T> {

        /**
         * Creates a new key buffer for this table.
         * @return the created key buffer
         */
        KeyBuffer newKeyBuffer();

        /**
         * Adds an element.
         * @param key the target key
         * @param value the target element
         * @return this
         */
        Builder<T> add(KeyBuffer key, T value);

        /**
         * Builds a {@link DataTable} from the {@link #add(KeyBuffer, Object) added} elements.
         * @return the build table
         */
        DataTable<T> build();
    }
}
