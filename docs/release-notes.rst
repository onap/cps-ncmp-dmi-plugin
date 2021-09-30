.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

.. DO NOT CHANGE THIS LABEL FOR RELEASE NOTES - EVEN THOUGH IT GIVES A WARNING
.. _release_notes:



========================
DMI Plugin Release Notes
========================

.. warning:: draft

.. contents::
    :depth: 2
..

..      ========================
..      * * *   ISTANBUL   * * *
..      ========================

Version: 1.0.0
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
| **Release designation**              | 1.0.1 Istanbul                                         |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| **Release date**                     | 2021-14-10                                             |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+

Features
--------
* Implement plugin registration.
* Retrieve data from CM-Handles using ncmp-datastores passthrough.
* Retrieve Yang Resources from one or more modules of a CM Handle.
* Retrieve Yang Modules for for a cm-handle.


.. _istanbul_deliverable:

Deliverables
------------

Software Deliverables

.. csv-table::
   :header: "Repository", "SubModules", "Version & Docker Image (if applicable)"
   :widths: auto

   "ncmp-dmi-plugin", "", "onap/ncmp-dmi-plugin:1.0.1"

Bug Fixes
---------
   - `CPS-589 <https://jira.onap.org/browse/CPS-589>`_ Json for Yang Resources does not contain name and revision tags
   - `CPS-617 <https://jira.onap.org/browse/CPS-617>`_ DMI base path does not conform to agreed API URL
   - `CPS-669 <https://jira.onap.org/browse/CPS-669>`_ Improvements in the NCMP-DMI plugin OOM charts

This document provides the release notes for Istanbul release.

Summary
-------

Following DMI plugin components are available with default ONAP/DMI-plugin installation.


    * Platform components

        - NCMP DMI Plugin (Helm Charts)

    * Service components

        - DMI Plugin

Below service components (mS) are available to be deployed on-demand.

    - CPS-TBDMT

Under OOM (Kubernetes) all CPS component containers are deployed as Kubernetes Pods/Deployments/Services into Kubernetes cluster.

Known Limitations, Issues and Workarounds
-----------------------------------------

*System Limitations*

None

*Known Vulnerabilities*

   - `CPS-719 <https://jira.onap.org/browse/CPS-719>`_ Passthrough query options do not support comma (,) token in values

*Workarounds*

None

Security Notes
--------------

*Fixed Security Issues*

None

*Known Security Issues*

None

Test Results
------------

None

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
        - `CPS project page <https://wiki.onap.org/pages/viewpage.action?pageId=71834216>`_
        - `Dmi Plugin implementation page <https://wiki.onap.org/display/DW/CPS-390+Spike%3A+Define+and+Agree+DMI+Plugin+REST+Interface>`_
        - `Passing Badge information for CPS <https://bestpractices.coreinfrastructure.org/en/projects/4398>`_
