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
package com.asakusafw.lang.compiler.operator.model;

import java.util.List;

import javax.lang.model.element.TypeElement;

/**
 * Represents a class with operators.
 */
public class OperatorClass {

    private final TypeElement declaration;

    private final List<OperatorElement> elements;

    /**
     * Creates a new instance.
     * @param declaration declaring type
     * @param elements operator elements
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public OperatorClass(TypeElement declaration, List<OperatorElement> elements) {
        if (declaration == null) {
            throw new IllegalArgumentException("declaring must not be null"); //$NON-NLS-1$
        }
        if (elements == null) {
            throw new IllegalArgumentException("elements must not be null"); //$NON-NLS-1$
        }
        this.declaration = declaration;
        this.elements = elements;
    }

    /**
     * Returns the declaring class of this.
     * @return the declaring class
     */
    public TypeElement getDeclaration() {
        return declaration;
    }

    /**
     * Returns the operator elements.
     * @return the elements
     */
    public List<OperatorElement> getElements() {
        return elements;
    }
}