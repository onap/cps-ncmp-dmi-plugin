.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2021 Nordix Foundation

.. DO NOT CHANGE THIS LABEL FOR RELEASE NOTES - EVEN THOUGH IT GIVES A WARNING
.. _deployment:


DMI-Plugin Deployment
#####################

.. toctree::
   :maxdepth: 1

Deployment
==========

Refer to `CPS Deployment <https://docs.onap.org/projects/onap-cps/en/latest/deployment.html>`_
page for deployment documentation related to DMI-Plugin and all CPS components.

Additional DMI-Plugin Core Customisations
=========================================

Application Properties
----------------------

The following table lists properties that can be specified as helm chart
values to configure for the application being deployed. This list is not exhaustive.

+---------------------------------------+-------------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| Property                              | Description                                                                                                 | Default Value                                   |
+=======================================+=============================================================================================================+=================================================+
| config.appUserName                    | User name used by the DMI-Plugin to authenticate users for the REST APIs that it exposes.                   | ``ncmpuser``                                    |
+---------------------------------------+-------------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.appUserPassword                | Password used by the DMI-Plugin to authenticate users for the REST APIs that it exposes.                    | Not defined                                     |
|                                       | If not defined, the password is generated when deploying the application.                                   |                                                 |
|                                       | See also `Credentials Retrieval <https://docs.onap.org/projects/onap-cps/en/latest/deployment.html>`_       |                                                 |
+---------------------------------------+-------------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.dmiServiceName                 | DMI-Plugin hostname and port.                                                                               | Not defined                                     |
+---------------------------------------+-------------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.cpsCore.username               | Internal user name used by DMI-Plugin to connect to the CPS-Core service.                                   | ``cpsuser``                                     |
+---------------------------------------+-------------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.cpsCore.password               | Internal password used by DMI-Plugin to connect to CPS-Core service.                                        | Not defined                                     |
+---------------------------------------+-------------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.sdnc.url                       | SDNC host name and port.                                                                                    | ``http://sdnc:8181``                            |
+---------------------------------------+-------------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.sdnc.username                  | Internal user name used by DMI-Plugin to connect to the SDNC.                                               | ``admin``                                       |
+---------------------------------------+-------------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.sdnc.Password                  | Internal password used by DMI-Plugin to connect to the SDNC.                                                | ``Kp8bJ4SXszM0WXlhak3eHlcse2gAw84vaoGGmJvUy2U`` |
+---------------------------------------+-------------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.sdnc.topologyId                | SDNC topology Id.                                                                                           | ``topology-netconf``                            |
+---------------------------------------+-------------------------------------------------------------------------------------------------------------+-------------------------------------------------+

DMI-Plugin Docker Installation
==============================

DMI-Plugin can also be installed in a docker environment. Latest `docker-compose <https://github.com/onap/cps-ncmp-dmi-plugin/blob/master/docker-compose/docker-compose.yml>`_ is included in the repo to start all the relevant
services.
Latest instructions are covered in the `README <https://github.com/onap/cps-ncmp-dmi-plugin/blob/master/docker-compose/README.md>`_