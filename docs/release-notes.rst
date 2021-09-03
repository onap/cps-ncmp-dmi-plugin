.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

.. DO NOT CHANGE THIS LABEL FOR RELEASE NOTES - EVEN THOUGH IT GIVES A WARNING
.. _release_notes:



========================
DMI Plugin Release Notes
========================

.. contents::
    :depth: 2
..

..      ========================
..      * * *   ISTANBUL   * * *
..      ========================

Version: 2.0.0
==============

Abstract
--------

This document provides the release notes for Istanbul release.

Release Data
------------

+--------------------------------------+--------------------------------------------------------+
| **CPS Project**                      |                                                        |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| **Docker images**                    |  placeholder                                           |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| **Release designation**              | 2.0.0 Istanbul                                         |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| **Release date**                     | 2021-14-10                                             |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+

Features
--------
* Implement plugin registration.
* Retrieve data from cmHandles using ncmp-datastores passthorugh.
* Retrieve Yang Resources from one or more modules of a  CM Handle.

.. _istanbul_deliverable:

Deliverables
------------

Software Deliverables

.. csv-table::
   :header: "Repository", "SubModules", "Version & Docker Image (if applicable)"
   :widths: auto

   "cps/ncmp-dmi-plugin", "", "placeholder"

Bug Fixes
---------

This document provides the release notes for Istanbul release.

Summary
-------

Following DMI plugin components are available with default ONAP/DMI-plugin installation.


    * Platform components

    * Service components

    * Additional resources that CPS utilizes deployed using ONAP common charts

Below service components (mS) are available to be deployed on-demand.

Under OOM (Kubernetes) all CPS component containers are deployed as Kubernetes Pods/Deployments/Services into Kubernetes cluster.

Known Limitations, Issues and Workarounds
-----------------------------------------

*System Limitations*

*Known Vulnerabilities*

None

*Workarounds*

Security Notes
--------------

*Fixed Security Issues*

*Known Security Issues*

Test Results
------------
    * `Integration tests`

References
----------

For more information on the ONAP Honolulu release, please see:

#. `ONAP Home Page`_
#. `ONAP Documentation`_
#. `ONAP Release Downloads`_
#. `ONAP Wiki Page`_


.. _`ONAP Home Page`: https://www.onap.org
.. _`ONAP Wiki Page`: https://wiki.onap.org
.. _`ONAP Documentation`: https://docs.onap.org
.. _`ONAP Release Downloads`: https://git.onap.org

Quick Links:

        - `Dmi Plugin implementation page <https://wiki.onap.org/display/DW/CPS-390+Spike%3A+Define+and+Agree+DMI+Plugin+REST+Interface>`_
        - `Passing Badge information for CPS <https://bestpractices.coreinfrastructure.org/en/projects/4398>`_
