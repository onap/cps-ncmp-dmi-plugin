#!/bin/bash -x
#
# Copyright (C) 2022 Nordix Foundation.
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
# ============LICENSE_END=========================================================
#
# Branched from ccsdk/distribution to this repository Feb 23, 2021
# $1 test options (passed on to run-csit.sh as such)

export TESTOPTIONS=${1}
export DMI_PLUGIN_HOME=$(git rev-parse --show-toplevel)
export WORKSPACE=$(git rev-parse --show-toplevel)/csit

rm -rf ${WORKSPACE}/archives
mkdir -p ${WORKSPACE}/archives
cd ${WORKSPACE}

source install-deps.sh

# Execute all test-suites defined under plans subdirectory
for dir in plans/*/
do
    dir=${dir%*/}  # remove the trailing /
   ./run-csit.sh ${dir} ${TESTOPTIONS}
done
