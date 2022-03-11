#!/bin/bash
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
#
echo '================================== docker info =========================='
docker ps -a

echo '================================== CPS-NCMP Logs ========================'
docker logs cps-and-ncmp

echo '================================== DMI Logs ============================='
docker logs ncmp-dmi-plugin

echo '================================== SDNC Logs ============================'
docker logs sdnc

echo 'Stopping, Removing all running containers...'
docker stop $(docker ps -aq) && docker rm $(docker ps -aq)

echo 'Removing Volumes...'
echo y | docker volume prune

echo 'Removing Networks...'
echo y | docker network prune
