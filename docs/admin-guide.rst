.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2021 Nordix Foundation

.. DO NOT CHANGE THIS LABEL FOR RELEASE NOTES - EVEN THOUGH IT GIVES A WARNING
.. _adminGuide:


DMI Plugin Admin Guide
######################

.. warning:: draft

.. toctree::
   :maxdepth: 1

Logging & Diagnostics
=====================

General Guidelines
------------------
In the REST API's when an exception is caught it is logged in detail, as information can be lost when converting in into a HTTP response code.
This is achieved using an Slf4j dependency in the projects POM. This is an API designed to give the DMI plugin generic access to many logging frameworks.

Logging Levels
--------------

+--------------------------------------+--------------------------------------------------------+
| Log Level                            |   Importance                                           |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| Fatal                                | One or more key business functionalities are not       |
|                                      | working. The system is currently not fulfilling        |
|                                      | business functionality.                                |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| Error                                | One or more functionalities are not working,           |
|                                      | potentially preventing other DMI                       |
|                                      | plugin functionalities from                            |
|                                      | functioning correctly.                                 |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| Warn                                 | Unexpected behaviour has occurred within               |
|                                      | the DMI plugin. The DMI plugin will                    |
|                                      | continue to work and functionalities will operate as   |
|                                      | normal.                                                |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| Info                                 | To inform the client of an action happening within     |
|                                      | the DMI plugin.                                        |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| Debug                                | Logging event used primarily for software debugging.   |
|                                      |                                                        |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| Trace                                | Used to describe step by step events during execution  |
|                                      | of code. This can be ignored during standard operations|
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+

Monitoring
==========
Once the DMI plugin is deployed, information related to the running instance of the application is available

.. code::

    http://ncmp-dmi-plugin:8081/manage/info/

Health
======

The DMI plugin health status and state can be checked using the following endpoint.
This also includes both the liveliness state and readiness state.

.. code::

    http://ncmp-dmi-plugin:8081/manage/health/

Metrics
=======

Prometheus Metrics can be checked at the following endpoint

.. code::

    http://ncmp-dmi-plugin/:8081/manage/prometheus
