package com.asakusafw.lang.compiler.extension.windgate;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.lang.compiler.api.Diagnostic;
import com.asakusafw.lang.compiler.api.DiagnosticException;
import com.asakusafw.lang.compiler.api.ExternalIoProcessor;
import com.asakusafw.lang.compiler.api.reference.CommandTaskReference;
import com.asakusafw.lang.compiler.api.reference.CommandToken;
import com.asakusafw.lang.compiler.api.reference.ExternalInputReference;
import com.asakusafw.lang.compiler.api.reference.ExternalOutputReference;
import com.asakusafw.lang.compiler.api.reference.TaskReference;
import com.asakusafw.lang.compiler.extension.externalio.AbstractExternalIoProcessor;
import com.asakusafw.lang.compiler.model.Location;
import com.asakusafw.lang.compiler.model.description.ClassDescription;
import com.asakusafw.lang.compiler.model.description.Descriptions;
import com.asakusafw.lang.compiler.model.description.ValueDescription;
import com.asakusafw.lang.compiler.model.info.ExternalPortInfo;
import com.asakusafw.vocabulary.windgate.Constants;
import com.asakusafw.vocabulary.windgate.WindGateExporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateImporterDescription;
import com.asakusafw.vocabulary.windgate.WindGateProcessDescription;
import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.vocabulary.FileProcess;

/**
 * An implementation of {@link ExternalIoProcessor} for WindGate.
 */
public class WindGateIoProcessor
        extends AbstractExternalIoProcessor<WindGateImporterDescription, WindGateExporterDescription> {

    static final Logger LOG = LoggerFactory.getLogger(WindGateIoProcessor.class);

    static final String MODULE_NAME = "windgate"; //$NON-NLS-1$

    static final Location CMD_PROCESS = Location.of("windgate/bin/process.sh"); //$NON-NLS-1$

    static final Location CMD_FINALIZE = Location.of("windgate/bin/finalize.sh"); //$NON-NLS-1$

    static final String OPT_IMPORT = "import"; //$NON-NLS-1$

    static final String OPT_EXPORT = "export"; //$NON-NLS-1$

    static final String PATTERN_SCRIPT_LOCATION = "META-INF/windgate/{0}-{1}.properties"; //$NON-NLS-1$

    static final String OPT_BEGIN = "begin"; //$NON-NLS-1$

    static final String OPT_END = "end"; //$NON-NLS-1$

    static final String OPT_ONESHOT = "oneshot"; //$NON-NLS-1$

    static final String KEY_MODEL = "model"; //$NON-NLS-1$

    private static final ClassDescription MODEL_CLASS = Descriptions.classOf(DescriptionModel.class);

    @Override
    protected String getModuleName() {
        return MODULE_NAME;
    }

    @Override
    protected Class<WindGateImporterDescription> getInputDescriptionType() {
        return WindGateImporterDescription.class;
    }

    @Override
    protected Class<WindGateExporterDescription> getOutputDescriptionType() {
        return WindGateExporterDescription.class;
    }

    @Override
    protected ValueDescription resolveInputProperties(
            Context context, String name, WindGateImporterDescription description) {
        try {
            return extract(description);
        } catch (IllegalStateException e) {
            throw new DiagnosticException(Diagnostic.Level.ERROR, MessageFormat.format(
                    "importer description \"{0}\" is not valid: {1}",
                    name,
                    e.getMessage()));
        }
    }

    @Override
    protected ValueDescription resolveOutputProperties(
            Context context, String name, WindGateExporterDescription description) {
        try {
            return extract(description);
        } catch (IllegalStateException e) {
            throw new DiagnosticException(Diagnostic.Level.ERROR, MessageFormat.format(
                    "exporter description \"{0}\" is not valid: {1}",
                    name,
                    e.getMessage()));
        }
    }

    @Override
    public void process(
            Context context,
            List<ExternalInputReference> inputs,
            List<ExternalOutputReference> outputs) throws IOException {
        Map<String, GateScript> importers = toImporterScripts(context, inputs);
        Map<String, GateScript> exporters = toExporterScripts(context, outputs);
        for (Map.Entry<String, GateScript> entry : importers.entrySet()) {
            Location location = getImportScriptLocation(entry.getKey());
            emitScript(context, location, entry.getValue());
        }
        for (Map.Entry<String, GateScript> entry : exporters.entrySet()) {
            Location location = getExportScriptLocation(entry.getKey());
            emitScript(context, location, entry.getValue());
        }
        Set<String> profiles = new TreeSet<>();
        profiles.addAll(importers.keySet());
        profiles.addAll(exporters.keySet());
        for (String profileName : profiles) {
            boolean doImport = importers.containsKey(profileName);
            boolean doExport = exporters.containsKey(profileName);
            registerTasks(context, profileName, doImport, doExport);
        }
    }

    private void registerTasks(Context context, String profileName, boolean doImport, boolean doExport) {
        boolean doBoth = doImport & doExport;
        if (doImport) {
            context.addTask(TaskReference.Phase.IMPORT, new CommandTaskReference(
                    getModuleName(),
                    profileName,
                    CMD_PROCESS,
                    Arrays.asList(new CommandToken[] {
                            CommandToken.of(profileName),
                            CommandToken.of(doBoth ? OPT_BEGIN : OPT_ONESHOT),
                            CommandToken.of(getScriptUri(true, profileName)),
                            CommandToken.BATCH_ID,
                            CommandToken.FLOW_ID,
                            CommandToken.EXECUTION_ID,
                            CommandToken.BATCH_ARGUMENTS,
                    }),
                    Collections.<TaskReference>emptyList()));
        }
        if (doExport) {
            context.addTask(TaskReference.Phase.EXPORT, new CommandTaskReference(
                    getModuleName(),
                    profileName,
                    CMD_PROCESS,
                    Arrays.asList(new CommandToken[] {
                            CommandToken.of(profileName),
                            CommandToken.of(doBoth ? OPT_END : OPT_ONESHOT),
                            CommandToken.of(getScriptUri(false, profileName)),
                            CommandToken.BATCH_ID,
                            CommandToken.FLOW_ID,
                            CommandToken.EXECUTION_ID,
                            CommandToken.BATCH_ARGUMENTS,
                    }),
                    Collections.<TaskReference>emptyList()));
        }
        context.addTask(TaskReference.Phase.FINALIZE, new CommandTaskReference(
                getModuleName(),
                profileName,
                CMD_FINALIZE,
                Arrays.asList(new CommandToken[] {
                        CommandToken.of(profileName),
                        CommandToken.BATCH_ID,
                        CommandToken.FLOW_ID,
                        CommandToken.EXECUTION_ID,
                }),
                Collections.<TaskReference>emptyList()));
    }

    private static String getScriptUri(boolean importer, String profileName) {
        Location location = getScriptLocation(importer, profileName);
        return String.format("classpath:%s", location.toPath()); //$NON-NLS-1$
    }

    private Map<String, GateScript> toImporterScripts(Context context, List<ExternalInputReference> inputs) {
        Map<String, List<ProcessScript<?>>> processes = new LinkedHashMap<>();
        for (ExternalInputReference input : inputs) {
            DescriptionModel model = restore(input);
            String profileName = model.getProfileName();
            ProcessScript<?> process = toProcessScript(context, input, model);
            List<ProcessScript<?>> list = processes.get(profileName);
            if (list == null) {
                list = new ArrayList<>();
                processes.put(profileName, list);
            }
            list.add(process);
        }
        return toGateScripts(processes);
    }

    private Map<String, GateScript> toExporterScripts(Context context, List<ExternalOutputReference> outputs) {
        Map<String, List<ProcessScript<?>>> processes = new LinkedHashMap<>();
        for (ExternalOutputReference output : outputs) {
            DescriptionModel model = restore(output);
            String profileName = model.getProfileName();
            ProcessScript<?> process = toProcessScript(context, output, model);
            List<ProcessScript<?>> list = processes.get(profileName);
            if (list == null) {
                list = new ArrayList<>();
                processes.put(profileName, list);
            }
            list.add(process);
        }
        return toGateScripts(processes);
    }

    private ProcessScript<?> toProcessScript(
            Context context, ExternalInputReference reference, DescriptionModel model) {
        Set<String> paths = reference.getPaths();
        if (paths.size() != 1) {
            throw new IllegalStateException(MessageFormat.format(
                    "WindGate importer must have only one input path: {0}",
                    reference));
        }
        String profileName = model.getProfileName();
        Class<?> dataModelType;
        try {
            dataModelType = reference.getDataModelClass().resolve(context.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new DiagnosticException(Diagnostic.Level.ERROR, MessageFormat.format(
                    "failed to resolve data model type: {0}",
                    reference.getDataModelClass()), e);
        }
        String location = paths.iterator().next();
        DriverScript source = model.getDriverScript();
        DriverScript drain = new DriverScript(
                Constants.HADOOP_FILE_RESOURCE_NAME,
                Collections.singletonMap(FileProcess.FILE.key(), location));
        return createProcessScript(profileName, dataModelType, source, drain);
    }

    private ProcessScript<?> toProcessScript(
            Context context, ExternalOutputReference reference, DescriptionModel model) {
        StringBuilder locations = new StringBuilder();
        for (String path : reference.getPaths()) {
            if (locations.length() > 0) {
                locations.append('\n');
            }
            locations.append(path);
        }
        String profileName = model.getProfileName();
        Class<?> dataModelType;
        try {
            dataModelType = reference.getDataModelClass().resolve(context.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new DiagnosticException(Diagnostic.Level.ERROR, MessageFormat.format(
                    "failed to resolve data model type: {0}",
                    reference.getDataModelClass()), e);
        }
        DriverScript source = new DriverScript(
                Constants.HADOOP_FILE_RESOURCE_NAME,
                Collections.singletonMap(FileProcess.FILE.key(), locations.toString()));
        DriverScript drain = model.getDriverScript();
        return createProcessScript(profileName, dataModelType, source, drain);
    }

    static Location getImportScriptLocation(String profileName) {
        return getScriptLocation(true, profileName);
    }

    static Location getExportScriptLocation(String profileName) {
        return getScriptLocation(false, profileName);
    }

    private static Location getScriptLocation(boolean importer, String profileName) {
        assert profileName != null;
        return Location.of(MessageFormat.format(
                PATTERN_SCRIPT_LOCATION,
                importer ? OPT_IMPORT : OPT_EXPORT,
                profileName));
    }

    private ProcessScript<?> createProcessScript(
            String profileName,
            Class<?> dataModelType,
            DriverScript source,
            DriverScript drain) {
        assert profileName != null;
        assert dataModelType != null;
        assert source != null;
        assert drain != null;
        return new ProcessScript<>(
                profileName,
                Constants.DEFAULT_PROCESS_NAME,
                dataModelType,
                source,
                drain);
    }

    private Map<String, GateScript> toGateScripts(Map<String, List<ProcessScript<?>>> processes) {
        assert processes != null;
        Map<String, GateScript> results = new TreeMap<>();
        for (Map.Entry<String, List<ProcessScript<?>>> entry : processes.entrySet()) {
            results.put(entry.getKey(), new GateScript(entry.getKey(), entry.getValue()));
        }
        return results;
    }

    private void emitScript(Context context, Location path, GateScript script) throws IOException {
        assert path != null;
        assert script != null;
        Properties properties = new Properties();
        script.storeTo(properties);
        OutputStream output = context.addResourceFile(path);
        try {
            properties.store(output, context.getOptions().getBuildId());
        } finally {
            output.close();
        }
    }

    private ValueDescription extract(WindGateProcessDescription description) {
        DescriptionModel model = new DescriptionModel(description);
        ValueDescription value = Descriptions.valueOf(model);
        return value;
    }

    private DescriptionModel restore(ExternalPortInfo info) {
        ValueDescription value = info.getContents();
        if (value == null || value.getValueType().equals(MODEL_CLASS) == false) {
            throw new IllegalStateException();
        }
        try {
            return (DescriptionModel) value.resolve(DescriptionModel.class.getClassLoader());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
