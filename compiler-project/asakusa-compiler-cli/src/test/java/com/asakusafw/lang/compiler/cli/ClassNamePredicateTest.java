/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.lang.compiler.cli;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Test;

/**
 * Test for {@link ClassNamePredicate}.
 */
public class ClassNamePredicateTest {

    /**
     * just a literal.
     */
    @Test
    public void literal() {
        ClassNamePredicate predicate = ClassNamePredicate.parse("java.lang.String");
        assertThat(predicate.apply(String.class), is(true));
        assertThat(predicate.apply(Object.class), is(false));
        assertThat(predicate.apply(List.class), is(false));
    }

    /**
     * trailing wildcard.
     */
    @Test
    public void trailing() {
        ClassNamePredicate predicate = ClassNamePredicate.parse("java.lang.*");
        assertThat(predicate.apply(String.class), is(true));
        assertThat(predicate.apply(Object.class), is(true));
        assertThat(predicate.apply(List.class), is(false));
    }

    /**
     * leading wildcard.
     */
    @Test
    public void first() {
        ClassNamePredicate predicate = ClassNamePredicate.parse("*Buffer");
        assertThat(predicate.apply(StringBuffer.class), is(true));
        assertThat(predicate.apply(ByteBuffer.class), is(true));
        assertThat(predicate.apply(StringBuilder.class), is(false));
    }

    /**
     * wildcard in middle.
     */
    @Test
    public void middle() {
        ClassNamePredicate predicate = ClassNamePredicate.parse("java.*Buffer");
        assertThat(predicate.apply(StringBuffer.class), is(true));
        assertThat(predicate.apply(ByteBuffer.class), is(true));
        assertThat(predicate.apply(StringBuilder.class), is(false));
    }

    /**
     * multiple wildcards.
     */
    @Test
    public void multiple() {
        ClassNamePredicate predicate = ClassNamePredicate.parse("*.util.*");
        assertThat(predicate.apply(List.class), is(true));
        assertThat(predicate.apply(java.util.Date.class), is(true));
        assertThat(predicate.apply(java.sql.Date.class), is(false));
    }
}