.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2021-2023 Nordix Foundation
.. _architecture:

DMI-Plugin Architecture
#######################

.. toctree::
   :maxdepth: 1


High Level Component Definition and Architectural Relationships
===============================================================

The DMI-Plugin provides a gateway for registration and syncing of CM Handles within CPS.

.. image:: _static/cps-r9-ncmp-dmi-plugin-interface-diagram.png

API definitions
===============

The DMI-Plugin provides following interfaces.

.. list-table::
   :header-rows: 1

   * - Interface name
     - Interface definition
     - Interface capabilities
     - Protocol
   * - CPS-E-05
     - Provides external clients with  xNF data access and module information
     - - Create data
       - Delete data
       - Update data
       - Read data
       - Query data
       - Query module references
     - REST
   * - CPS-NCMP-I-01
     - DMI-Plugin Inventory
     - - Register Plug-in CM-Handles
     - - Health Check API
     - REST
   * - DMI-I-01
     - Provides NCMP with  xNF data access and module information
     - - Create data
       - Delete data
       - Update data
       - Read data
       - Query data
       - Query module references
     - REST

More details on the CPS interface CPS-E-05 which is responsible for the DMI-Plugin can be found on the :ref:`CPS Architecture page<onap-cps:architecture>`
