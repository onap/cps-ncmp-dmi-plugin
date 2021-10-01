.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2021 Nordix Foundation

.. DO NOT CHANGE THIS LABEL FOR RELEASE NOTES - EVEN THOUGH IT GIVES A WARNING
.. _design:


DMI Plugin Design
#################

.. warning:: draft

.. toctree::
   :maxdepth: 1

Offered APIs
============

The DMI Plugin supports the public APIs listed in the link below:

:download:`DMI Rest OpenApi Specification <openapi/openapi.yml>`

Exposed API
-----------

The standard for API definition in the RESTful API world is the OpenAPI Specification (OAS).
The OAS 3, which is based on the original "Swagger Specification", is being widely used in API developments.

Specification can be accessed using following URI:

.. code-block:: bash

  "http://<hostname>:<port>/v3/api-docs?group=dmi-plugin-docket"

Additionally, the Swagger User Interface can be found at the following URI. The component may be changed between CPS-Core
and CPS-NCMP using the drop down table in the top right:

.. code-block:: bash

  "http://<hostname>:<port>/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#/"