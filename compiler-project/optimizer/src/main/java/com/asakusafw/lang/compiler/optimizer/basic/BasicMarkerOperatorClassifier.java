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
package com.asakusafw.lang.compiler.optimizer.basic;

import com.asakusafw.lang.compiler.model.graph.MarkerOperator;
import com.asakusafw.lang.compiler.model.graph.Operator;
import com.asakusafw.lang.compiler.model.graph.Operator.OperatorKind;
import com.asakusafw.lang.compiler.optimizer.OperatorCharacterizer;
import com.asakusafw.lang.compiler.optimizer.basic.OperatorClass.InputAttribute;
import com.asakusafw.lang.compiler.optimizer.basic.OperatorClass.InputType;

/**
 * Provides {@link OperatorClass} for {@link MarkerOperator}.
 */
public class BasicMarkerOperatorClassifier implements OperatorCharacterizer<OperatorClass> {

    @Override
    public OperatorClass extract(Context context, Operator operator) {
        if (operator.getOperatorKind() != OperatorKind.MARKER) {
            throw new IllegalArgumentException();
        }
        MarkerOperator target = (MarkerOperator) operator;
        return OperatorClass.builder(operator, InputType.RECORD)
                .with(target.getInput(), InputAttribute.PRIMARY)
                .build();
    }
}
