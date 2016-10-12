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
package com.asakusafw.lang.utils.common;

/**
 * An abstract super interface of actions.
 * @param <T> the acceptable value type
 * @param <E> the throwable exception type
 * @since 0.4.0
 */
@FunctionalInterface
public interface Action<T, E extends Exception> {

    /**
     * Performs this action.
     * @param t the target value
     * @throws E if an exception was occurred while performing the action
     */
    void perform(T t) throws E;
}