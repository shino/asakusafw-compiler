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
package com.asakusafw.lang.compiler.core.participant;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.asakusafw.lang.compiler.api.BatchProcessor;
import com.asakusafw.lang.compiler.api.JobflowProcessor;
import com.asakusafw.lang.compiler.api.reference.BatchReference;
import com.asakusafw.lang.compiler.core.BatchCompiler;
import com.asakusafw.lang.compiler.core.CompilerTestRoot;
import com.asakusafw.lang.compiler.core.basic.BasicBatchCompiler;
import com.asakusafw.lang.compiler.core.dummy.SimpleExternalPortProcessor;
import com.asakusafw.lang.compiler.core.dummy.SimpleJobflowProcessor;
import com.asakusafw.lang.compiler.inspection.driver.InspectionExtension;
import com.asakusafw.lang.compiler.model.graph.Batch;
import com.asakusafw.lang.compiler.model.graph.Jobflow;
import com.asakusafw.lang.compiler.packaging.FileContainer;

/**
 * Test for {@link InspectionExtensionParticipant}.
 */
public class InspectionExtensionParticipantTest extends CompilerTestRoot {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        final AtomicBoolean jobflowExecuted = new AtomicBoolean();
        final AtomicBoolean batchExecuted = new AtomicBoolean();
        externalPortProcessors.add(new SimpleExternalPortProcessor());
        compilerParticipants.add(new InspectionExtensionParticipant());
        jobflowProcessors.add(new JobflowProcessor() {
            @Override
            public void process(JobflowProcessor.Context context, Jobflow source) throws IOException {
                InspectionExtension extension = context.getExtension(InspectionExtension.class);
                assertThat(extension, is(notNullValue()));
                jobflowExecuted.set(true);
            }
        });
        batchProcessors.add(new BatchProcessor() {
            @Override
            public void process(BatchProcessor.Context context, BatchReference source) throws IOException {
                InspectionExtension extension = context.getExtension(InspectionExtension.class);
                assertThat(extension, is(notNullValue()));
                batchExecuted.set(true);
            }
        });

        FileContainer output = container();
        BatchCompiler.Context context = new BatchCompiler.Context(context(true), output);
        new BasicBatchCompiler().compile(context, batch());

        assertThat(jobflowExecuted.get(), is(true));
        assertThat(batchExecuted.get(), is(true));

        assertThat(output.toFile(InspectionExtensionParticipant.OUTPUT_DSL).isFile(), is(true));
        assertThat(output.toFile(InspectionExtensionParticipant.OUTPUT_TASK).isFile(), is(true));
    }

    /**
     * w/o DSL inspection.
     */
    @Test
    public void no_dsl() {
        options.withProperty(InspectionExtensionParticipant.KEY_DSL, "false");
        externalPortProcessors.add(new SimpleExternalPortProcessor());
        compilerParticipants.add(new InspectionExtensionParticipant());
        jobflowProcessors.add(new SimpleJobflowProcessor());

        FileContainer output = container();
        BatchCompiler.Context context = new BatchCompiler.Context(context(true), output);
        new BasicBatchCompiler().compile(context, batch());

        assertThat(output.toFile(InspectionExtensionParticipant.OUTPUT_DSL).isFile(), is(false));
        assertThat(output.toFile(InspectionExtensionParticipant.OUTPUT_TASK).isFile(), is(true));
    }

    /**
     * w/o task inspection.
     */
    @Test
    public void no_task() {
        options.withProperty(InspectionExtensionParticipant.KEY_TASK, "false");
        externalPortProcessors.add(new SimpleExternalPortProcessor());
        compilerParticipants.add(new InspectionExtensionParticipant());
        jobflowProcessors.add(new SimpleJobflowProcessor());

        FileContainer output = container();
        BatchCompiler.Context context = new BatchCompiler.Context(context(true), output);
        new BasicBatchCompiler().compile(context, batch());

        assertThat(output.toFile(InspectionExtensionParticipant.OUTPUT_DSL).isFile(), is(true));
        assertThat(output.toFile(InspectionExtensionParticipant.OUTPUT_TASK).isFile(), is(false));
    }

    private Batch batch() {
        Batch batch = new Batch(batchInfo("testing"));
        batch.addElement(jobflow("j0"));
        return batch;
    }
}
