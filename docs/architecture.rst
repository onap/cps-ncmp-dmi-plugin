.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2021 Nordix Foundation, Pantheon.tech
.. _architecture:

DMI Plugin Architecture
#######################

.. toctree::
   :maxdepth: 1


High Level Component Definition and Architectural Relationships
===============================================================

The DMI plugin provides a gateway for registration of CM Handles within CPS

Project implementation proposal page describing scope is here:
`CPS-390 Spike: Define and Agree DMI Plugin REST Interface <https://wiki.onap.org/display/DW/CPS-390+Spike%3A+Define+and+Agree+DMI+Plugin+REST+Interface>`_

This page reflects the state for Istanbul-R9 release.

.. image:: _static/dmi-plugin-r9-arch-diagram.png

API definitions
===============

The DMI plugin provides following interfaces.

.. list-table::
   :header-rows: 0

   * - Interface name
     - Interface definition
     - Interface capabilities
     - Protocol

The DMI plugin Basic Concepts are described in :doc:`modeling`.
