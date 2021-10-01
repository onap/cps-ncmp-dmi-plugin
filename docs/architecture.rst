.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2021 Nordix Foundation
.. _architecture:

DMI Plugin Architecture
#######################

.. toctree::
   :maxdepth: 1


High Level Component Definition and Architectural Relationships
===============================================================

The DMI plugin provides a gateway for registration and syncing of CM Handles within CPS

.. image:: _static/cps-r9-ncmp-dmi-plugin-interface-diagram.png

API definitions
===============

The DMI plugin provides following interfaces.

.. list-table::
   :header-rows: 1

   * - Interface name
     - Interface definition
     - Interface capabilities
     - Protocol
   * - DMI-E-01
     - Administrative Data Management
     - - write data for a CM-Handle
     - REST
   * - DMI-E-02
     - Generic Data Access
     - - get resource data from passthrough-operational for cm handle
       - get resource data from passthrough-running for cm handle
     - REST
   * - DMI-E-03
     - Generic Data Search
     - - get all modules for cm handle by cm handle id
       - retrieve all module resources by cm handle id
     - REST

Details on the CPS interface CPS-E-05 which is responsible for the DMI Plugin can be found on the `CPS Architecture Page <https://docs.onap.org/projects/onap-cps/en/latest/architecture.html>`_.