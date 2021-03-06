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

import static com.asakusafw.lang.compiler.model.description.Descriptions.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.asakusafw.lang.compiler.model.graph.ExternalInput;
import com.asakusafw.lang.compiler.model.graph.Operator;
import com.asakusafw.lang.compiler.optimizer.OperatorCharacterizer;
import com.asakusafw.lang.compiler.optimizer.OptimizerTestRoot;
import com.asakusafw.lang.compiler.optimizer.basic.OperatorClass.InputType;

/**
 * Test for {@link BasicExternalInputClassifier}.
 */
public class BasicExternalInputClassifierTest extends OptimizerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        OperatorCharacterizer<OperatorClass> engine = new BasicExternalInputClassifier();
        Operator operator = ExternalInput.newInstance("testing", classOf(String.class));
        OperatorClass result = perform(context(), engine, operator);

        assertThat(result.getOperator(), is(operator));
        assertThat(result.getPrimaryInputType(), is(InputType.NOTHING));
    }

    /**
     * w/ input port.
     */
    @Test
    public void with_input_port() {
        OperatorCharacterizer<OperatorClass> engine = new BasicExternalInputClassifier();
        Operator operator = ExternalInput.builder("testing")
            .input("p", typeOf(String.class))
            .output("p", typeOf(String.class))
            .build();
        OperatorClass result = perform(context(), engine, operator);

        assertThat(result.getOperator(), is(operator));
        assertThat(result.getPrimaryInputType(), is(InputType.NOTHING));
        assertThat(result.getPrimaryInputs(), hasSize(1));
        assertThat(result.getSecondaryInputs(), hasSize(0));
    }
}
