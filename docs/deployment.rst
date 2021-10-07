.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2021 Nordix Foundation

.. DO NOT CHANGE THIS LABEL FOR RELEASE NOTES - EVEN THOUGH IT GIVES A WARNING
.. _deployment:


DMI Plugin Deployment
#####################

.. toctree::
   :maxdepth: 1

Deployment
==========

Refer to `CPS Deployment <https://docs.onap.org/projects/onap-cps/en/latest/deployment.html>`_
page for deployment documentation related to DMI Plugin and all CPS components.

Additional DMI Plugin Core Customisations
=========================================

Application Properties
----------------------

The following table lists properties that can be specified as helm chart
values to configure for the application being deployed. This list is not exhaustive.

+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| Property                              | Description                                                                                             | Default Value                                   |
+=======================================+=========================================================================================================+=================================================+
| config.appUserName                    | User name used by the DMI-Plugin service to configure the authentication for REST APIs it exposes.      | ``ncmpuser``                                    |
|                                       |                                                                                                         |                                                 |
|                                       | This is the user name to be used by the DMI-Plugin REST clients to authenticate themselves.             |                                                 |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.appUserPassword                | Password used by the DMI-Plugin service to configure the authentication for REST API it exposes.        | Not defined                                     |
|                                       |                                                                                                         |                                                 |
|                                       | This is the password to be used by the DMI-Plugin REST clients to authenticate themselves.              |                                                 |
|                                       |                                                                                                         |                                                 |
|                                       | If not defined, the password is generated when deploying the application.                               |                                                 |
|                                       |                                                                                                         |                                                 |
|                                       | See also :ref:`Credentials Retrieval <credentials_retrieval>`.                                          |                                                 |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.dmiServiceName                 | DMI  hostname and port.                                                                                 | Not defined                                     |
|                                       |                                                                                                         |                                                 |
|                                       | If not defined, the password is generated when deploying the application.                               |                                                 |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.cpsCore.username               | User name used by DMI-Plugin to authenticate themselves for using CPS-Core service.                     | ``cpsuser``                                     |
|                                       |                                                                                                         |                                                 |
|                                       | If not defined, the password is generated when deploying the application.                               |                                                 |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.cpsCore.password               | Internal password used by DMI-Plugin to connect to CPS-Core service.                                    | Not defined                                     |
|                                       |                                                                                                         |                                                 |
|                                       | If not defined, the password is generated when deploying the application.                               |                                                 |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.sdnc.url                       | SDNC host name and port.                                                                                | ``http://sdnc:8181``                            |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.sdnc.username                  | Internal user name used by DMI-Plugin to connect to the SDNC.                                           | ``admin``                                       |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.sdnc.Password                  | Internal password used by DMI-Plugin to connect to the SDNC.                                            | ``Kp8bJ4SXszM0WXlhak3eHlcse2gAw84vaoGGmJvUy2U`` |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------------------------+
| config.sdnc.topologyId                | SDNC topology Id.                                                                                       | ``topology-netconf``                            |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------------------------+

DMI Plugin Docker Installation
==============================

DMI-Plugin can also be installed in a docker environment. Latest `docker-compose <https://github.com/onap/cps-ncmp-dmi-plugin/blob/master/docker-compose/docker-compose.yml>`_ is included in the repo to start all the relevant
services.
Latest instructions are covered in the `README <https://github.com/onap/cps-ncmp-dmi-plugin/blob/master/docker-compose/README.md>`_