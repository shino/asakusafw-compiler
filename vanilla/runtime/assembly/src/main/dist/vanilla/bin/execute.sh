#!/bin/bash
#
# Copyright 2011-2016 Asakusa Framework Team.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


usage() {
    cat 1>&2 <<EOF
Asakusa Vanilla Command Line

Usage:
    $0 batch-id flow-id execution-id batch-arguments class-name [direct-arguments...]

Parameters:
    batch-id
        batch ID of current execution
    flow-id
        flow ID of current execution
    execution-id
        execution ID of current execution
    batch-arguments
        The arguments for this execution
        This must be form of "key1=value1,key2=value2,...",
        and the special characters '=', ',', '\' can be escaped by '\'.
    class-name
        Fully qualified class name of program entry
    direct-arguments...
        Direct arguments for Asakusa Vanilla launcher

Environment variables:
    ASAKUSA_VANILLA_OPTS
        Java VM options
    ASAKUSA_VANILLA_ARGS
        Extra launcher arguments
    ASAKUSA_VANILLA_LAUNCHER
        Java command qualifier
EOF
}

import() {
    _SCRIPT="$1"
    if [ -e "$_SCRIPT" ]
    then
        . "$_SCRIPT"
    else
        echo "$_SCRIPT is not found" 1>&2
        exit 1
    fi
}

if [ $# -lt 5 ]
then
    echo "$@" 1>&2
    usage
    exit 1
fi

_OPT_BATCH_ID="$1"
shift
_OPT_FLOW_ID="$1"
shift
_OPT_EXECUTION_ID="$1"
shift
_OPT_BATCH_ARGUMENTS="$1"
shift
_OPT_APPLICATION="$1"
shift

_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"
import "$_ROOT/conf/env.sh"
import "$_ROOT/libexec/validate-env.sh"

# Move to home directory
cd

_EXEC=()
_LIBRARYPATH=()
_CLASSPATH=()
_APP_OPTIONS=()

if [ -d "$_ROOT/lib/hadoop" ]
then
    _USE_HADOOP_CMD=0
    import "$_ROOT/libexec/configure-java-cmd.sh"
else
    _USE_HADOOP_CMD=1
    import "$_ROOT/libexec/configure-hadoop-cmd.sh"
fi

import "$_ROOT/libexec/configure-classpath.sh"
import "$_ROOT/libexec/configure-options.sh"

if [ "$ASAKUSA_VANILLA_ARGS" != "" ]
then
    _APP_OPTIONS+=($ASAKUSA_VANILLA_ARGS)
fi

if [ "$ASAKUSA_VANILLA_LAUNCHER" != "" ]
then
    _EXEC+=($ASAKUSA_VANILLA_LAUNCHER)
fi

if [ $_USE_HADOOP_CMD -eq 1 ]
then
    _EXEC+=("$HADOOP_CMD")
else
    _EXEC+=("$JAVA_CMD")
fi

echo "Starting Asakusa Vanilla:"
echo "              Launcher: ${_EXEC[@]}"
echo "              Batch ID: $_OPT_BATCH_ID"
echo "               Flow ID: $_OPT_FLOW_ID"
echo "       Batch Arguments: $_OPT_BATCH_ARGUMENTS"
echo "          Execution ID: $_OPT_EXECUTION_ID"
echo "           Application: $_OPT_APPLICATION"
echo "  ASAKUSA_VANILLA_OPTS: $ASAKUSA_VANILLA_OPTS"
echo "        System Options: ${_APP_OPTIONS[@]}"
echo "          User Options: $@"

if [ $_USE_HADOOP_CMD -eq 1 ]
then
    export HADOOP_CLIENT_OPTS="$HADOOP_CLIENT_OPTS $ASAKUSA_VANILLA_OPTS"
    export HADOOP_CLASSPATH="$HADOOP_CLASSPATH:$(IFS=:; echo "${_CLASSPATH[*]}")"
    "${_EXEC[@]}" \
        "com.asakusafw.vanilla.client.VanillaLauncher" \
        --client "$_OPT_APPLICATION" \
        --batch-id "$_OPT_BATCH_ID" \
        --flow-id "$_OPT_FLOW_ID" \
        --execution-id "$_OPT_EXECUTION_ID" \
        --batch-arguments "$_OPT_BATCH_ARGUMENTS," \
        "${_APP_OPTIONS[@]}" \
        "$@"
else
    "${_EXEC[@]}" \
        $ASAKUSA_VANILLA_OPTS \
        -classpath "$(IFS=:; echo "${_CLASSPATH[*]}")" \
        "com.asakusafw.vanilla.client.VanillaLauncher" \
        --client "$_OPT_APPLICATION" \
        --batch-id "$_OPT_BATCH_ID" \
        --flow-id "$_OPT_FLOW_ID" \
        --execution-id "$_OPT_EXECUTION_ID" \
        --batch-arguments "$_OPT_BATCH_ARGUMENTS," \
        "${_APP_OPTIONS[@]}" \
        "$@"
fi

_RET=$?
if [ $_RET -ne 0 ]
then
    echo "Asakusa Vanilla failed with exit code: $_RET" 1>&2
    echo "              Launcher: ${_EXEC[@]}" 1>&2
    echo "              Batch ID: $_OPT_BATCH_ID" 1>&2
    echo "               Flow ID: $_OPT_FLOW_ID" 1>&2
    echo "          Execution ID: $_OPT_EXECUTION_ID" 1>&2
    echo "       Batch Arguments: $_OPT_BATCH_ARGUMENTS" 1>&2
    echo "           Application: $_OPT_APPLICATION" 1>&2
    echo "        System Options: ${_APP_OPTIONS[@]}" 1>&2
    echo "  ASAKUSA_VANILLA_OPTS: $ASAKUSA_VANILLA_OPTS" 1>&2
    echo "          User Options: $@" 1>&2
    echo "           Librarypath: ${_LIBRARYPATH[@]}" 1>&2
    echo "             Classpath: ${_CLASSPATH[@]}" 1>&2
    exit $_RET
fi
