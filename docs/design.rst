.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2021-2025 OpenInfra Foundation Europe. All rights reserved.

.. DO NOT CHANGE THIS LABEL FOR RELEASE NOTES - EVEN THOUGH IT GIVES A WARNING
.. _design:


DMI-Plugin Design
#################

.. toctree::
   :maxdepth: 1

Offered APIs
============

The DMI-Plugin supports the public APIs listed in the link below:

:download:`DMI Rest OpenApi Specification <|dmi_openapi_url|>`

:download:`DMI Datajob OpenApi Specification <|dmi_openapi_datajob_url|>`

View Offered APIs
-----------------

The standard for API definition in the RESTful API world is the OpenAPI Specification (OAS).
The OAS 3, which is based on the original "Swagger Specification", is being widely used in API developments.

Specification can be accessed using following URI:

.. code-block:: bash

  http://<hostname>:<port>/v3/api-docs?group=dmi-plugin-docket

Additionally, the Swagger User Interface can be found at the following URI. The component may be changed between CPS-Core
and CPS-NCMP using the drop down table in the top right:

.. code-block:: bash

  http://<hostname>:<port>/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#/

Health Check APIs
=================

The healthcheck URL can be accessed using following URI:

.. code-block:: bash

  http://<hostname>:<port>/actuator/health

The Health check endpoint is essential for monitoring the status of DMI-Plugins.
CPS is using a Spring Boot pattern here which can easily be configured for any spring boot application.
Springboot provides a built-in Health Check feature through the Spring Boot Actuator module.

Consumed APIs
=============

- SDNC: REST based interface exposed by the SDNC client. This is used to retrieve the yang resources and modules for CPS.
