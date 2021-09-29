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
DMI-Plugin logs are sent to `STDOUT` in order to leverage the Kubernetes logging architecture.

These logs are available using the following command:

.. code:: bash

    kubectl logs <dmi-plugin-pod>

The default configuration for CPS logs is the INFO level.

This architecture also makes all logs ready to be sent to and ELK stack or similar.

Enabling tracing for all executed sql statements is done by changing hibernate
loggers log level.

Monitoring
==========
Once the DMI plugin is deployed, information related to the running instance of the application is available.

.. code::

    http://<cps-component-service-name>:8081/manage/info/

Health
======

The DMI plugin health status and state can be checked using the following endpoint.
This also includes both the liveliness state and readiness state.

.. code::

    http://<cps-component-service-name>:8081/manage/health/

Metrics
=======

Prometheus Metrics can be checked at the following endpoint.

.. code::

    http://<cps-component-service-name>/:8081/manage/prometheus

.. note::

    Command to retrieve <cps-component-service-name> can be got from the :ref:`deployment page. <deployment>`
