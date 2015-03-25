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
package com.asakusafw.lang.compiler.operator;

import java.text.MessageFormat;

import com.asakusafw.lang.compiler.model.description.ClassDescription;

/**
 * Available constant values in this project.
 */
public final class Constants {

    private static final String BASE = "com.asakusafw.vocabulary."; //$NON-NLS-1$

    private static ClassDescription classOf(String name) {
        return new ClassDescription(BASE + name);
    }

    /**
     * {@code OperatorHelper} annotation type name.
     */
    public static final ClassDescription TYPE_OPERATOR_HELPER = classOf("operator.OperatorHelper"); //$NON-NLS-1$

    /**
     * {@code In} type name.
     */
    public static final ClassDescription TYPE_IN = classOf("flow.In"); //$NON-NLS-1$

    /**
     * {@code Out} type name.
     */
    public static final ClassDescription TYPE_OUT = classOf("flow.Out"); //$NON-NLS-1$

    /**
     * {@code Source} type name.
     */
    public static final ClassDescription TYPE_SOURCE = classOf("flow.Source"); //$NON-NLS-1$

    /**
     * {@code Result} type name.
     */
    public static final ClassDescription TYPE_RESULT =
            new ClassDescription("com.asakusafw.runtime.core.Result"); //$NON-NLS-1$

    /**
     * {@code Key} type name.
     */
    public static final ClassDescription TYPE_KEY = classOf("model.Key"); //$NON-NLS-1$

    /**
     * {@code Joined} type name.
     */
    public static final ClassDescription TYPE_JOINED = classOf("model.Joined"); //$NON-NLS-1$

    /**
     * {@code Summarized} type name.
     */
    public static final ClassDescription TYPE_SUMMARIZED = classOf("model.Summarized"); //$NON-NLS-1$

    /**
     * {@code FlowPart} annotation type name.
     */
    public static final ClassDescription TYPE_FLOW_PART = classOf("flow.FlowPart"); //$NON-NLS-1$

    /**
     * {@code FlowDescription} type name.
     */
    public static final ClassDescription TYPE_FLOW_DESCRIPTION = classOf("flow.FlowDescription"); //$NON-NLS-1$

    /**
     * {@code Import} type name.
     */
    public static final ClassDescription TYPE_IMPORT = classOf("flow.Import"); //$NON-NLS-1$

    /**
     * {@code Export} type name.
     */
    public static final ClassDescription TYPE_EXPORT = classOf("flow.Export"); //$NON-NLS-1$

    /**
     * {@code ImporterDescription} type name.
     */
    public static final ClassDescription TYPE_IMPORTER_DESC = classOf("external.ImporterDescription"); //$NON-NLS-1$

    /**
     * {@code ExporterDescription} type name.
     */
    public static final ClassDescription TYPE_EXPORTER_DESC = classOf("external.ExporterDescription"); //$NON-NLS-1$

    /**
     * {@code FlowFragmentBuilder} type name.
     */
    public static final ClassDescription TYPE_ELEMENT_BUILDER =
            classOf("flow.builder.FlowElementBuilder"); //$NON-NLS-1$

    /**
     * {@code FlowFragmentEditor} type name.
     */
    public static final ClassDescription TYPE_ELEMENT_EDITOR = classOf("flow.builder.FlowElementEditor"); //$NON-NLS-1$

    /**
     * {@code KeyInfo} type name.
     */
    public static final ClassDescription TYPE_KEY_INFO = classOf("flow.builder.KeyInfo"); //$NON-NLS-1$

    /**
     * {@code ExternInfo} type name.
     */
    public static final ClassDescription TYPE_EXTERN_INFO = classOf("flow.builder.ExternInfo"); //$NON-NLS-1$

    /**
     * singleton name of flow-part factory method.
     */
    public static final String NAME_FLOW_PART_FACTORY_METHOD = "create"; //$NON-NLS-1$

    /**
     * Simple name pattern for operator implementation class (0: simple name of operator class).
     */
    private static final String PATTERN_IMPLEMENTATION_CLASS = "{0}Impl"; //$NON-NLS-1$

    /**
     * Simple name pattern for operator factory class (0: simple name of operator/flow-part class).
     */
    private static final String PATTERN_FACTORY_CLASS = "{0}Factory"; //$NON-NLS-1$

    /**
     * Simple name pattern for built-in operator annotation class (0: simple name).
     */
    private static final String PATTERN_BUILTIN_OPERATOR_ANNOTATION_CLASS = BASE + "operator.{0}"; //$NON-NLS-1$

    /**
     * Returns the implementation class name of target class with the specified name.
     * @param simpleName the simple class name of the operator annotation
     * @return qualified name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ClassDescription getBuiltinOperatorClass(String simpleName) {
        if (simpleName == null) {
            throw new IllegalArgumentException("simpleName must not be null"); //$NON-NLS-1$
        }
        return new ClassDescription(MessageFormat.format(PATTERN_BUILTIN_OPERATOR_ANNOTATION_CLASS, simpleName));
    }

    /**
     * Returns the implementation class name of target class with the specified name.
     * @param originalName the original class name
     * @return related implementation class name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ClassDescription getImplementationClass(CharSequence originalName) {
        if (originalName == null) {
            throw new IllegalArgumentException("originalName must not be null"); //$NON-NLS-1$
        }
        return new ClassDescription(MessageFormat.format(PATTERN_IMPLEMENTATION_CLASS, originalName));
    }

    /**
     * Returns the factory class name of target class with the specified name.
     * @param originalName the original class name
     * @return related factory class name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static ClassDescription getFactoryClass(CharSequence originalName) {
        if (originalName == null) {
            throw new IllegalArgumentException("originalName must not be null"); //$NON-NLS-1$
        }
        return new ClassDescription(MessageFormat.format(PATTERN_FACTORY_CLASS, originalName));
    }

    /**
     * Returns generator message.
     * @return generator message
     */
    public static String getGeneratorMessage() {
        return MessageFormat.format(
                "by Asakusa Operator DSL Compiler {0}", //$NON-NLS-1$
                "1.0.0"); //$NON-NLS-1$
    }

    private Constants() {
        return;
    }
}
