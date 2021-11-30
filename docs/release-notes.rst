.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0

.. DO NOT CHANGE THIS LABEL FOR RELEASE NOTES - EVEN THOUGH IT GIVES A WARNING
.. _release_notes:



========================
DMI-Plugin Release Notes
========================

.. contents::
    :depth: 2
..

..      ========================
..      * * *   JAKARTA   * * *
..      ========================

Version: 1.1.0-SNAPSHOT
=======================

This section lists the main changes & fixes merged into master (snapshot) version of NCMP-DMI-Plugin. This information is here to assist developers that want experiment/test using our latest code bases directly. Stability of this is not guaranteed.

Features
--------
   - `CPS-637 <https://jira.onap.org/browse/CPS-637>`_  Support Update operation for datastore pass-through running
   - `CPS-639 <https://jira.onap.org/browse/CPS-639>`_  Support Delete operation for datastore pass-through running
   - `CPS-784 <https://jira.onap.org/browse/CPS-784>`_  Add examples to DMI-Plugin API Spec

Bug Fixes
---------

None

Known Limitations, Issues and Workarounds
-----------------------------------------

*System Limitations*

None

*Known Vulnerabilities*

None

*Workarounds*

None

Security Notes
--------------

*Fixed Security Issues*

None

*Known Security Issues*

None

..      ========================
..      * * *   ISTANBUL   * * *
..      ========================

Version: 1.0.1
==============

Release Data
------------

+--------------------------------------+--------------------------------------------------------+
| **CPS Project**                      |  DMI-Plugin                                            |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| **Docker images**                    |  onap/ncmp-dmi-plugin:1.0.1                            |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| **Release designation**              | 1.0.1 Istanbul                                         |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| **Release date**                     | 2021-14-10                                             |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+

Bug Fixes
---------

   - `CPS-653 <https://jira.onap.org/browse/CPS-653>`_ cmHandleProperties not supported by dmi in fetch modules
   - `CPS-659 <https://jira.onap.org/browse/CPS-659>`_ DMI does not set CREATE Response code for passthrough-running create use-case
   - `CPS-669 <https://jira.onap.org/browse/CPS-669>`_ Improvements in the NCMP-DMI plugin OOM charts
   - `CPS-678 <https://jira.onap.org/browse/CPS-678>`_ Passthrough read only supports known parameters (depth&field)
   - `CPS-679 <https://jira.onap.org/browse/CPS-679>`_ Passthrough does not support resourceIdentifier with / tokens
   - `CPS-706 <https://jira.onap.org/browse/CPS-706>`_ get moduleschema/yangresouce endpoint not working

Known Limitations, Issues and Workarounds
-----------------------------------------

*System Limitations*

  - `CPS-719 <https://jira.onap.org/browse/CPS-719>`_ Passthrough query options do not support comma (,) token in values

*Known Vulnerabilities*

None

*Workarounds*

None

Security Notes
--------------

*Fixed Security Issues*

None

*Known Security Issues*

None

Version: 1.0.0
==============

Release Data
------------

+--------------------------------------+--------------------------------------------------------+
| **CPS Project**                      |  DMI-Plugin                                            |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| **Docker images**                    |  onap/ncmp-dmi-plugin:1.0.0                            |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| **Release designation**              | 1.0.0 Istanbul                                         |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+
| **Release date**                     | 2021-14-09                                             |
|                                      |                                                        |
+--------------------------------------+--------------------------------------------------------+

Features
--------
* Implement plugin registration.
* Retrieve data from cmHandles using ncmp-datastores passthrough.
* Retrieve Yang Resources from one or more modules of a  CM Handle.

.. _istanbul_deliverable:

Deliverables
------------

Software Deliverables

.. csv-table::
   :header: "Repository", "SubModules", "Version & Docker Image (if applicable)"
   :widths: auto

   "cps/ncmp-dmi-plugin", "", "onap/ncmp-dmi-plugin:1.0.0"

Bug Fixes
---------

   - `CPS-504 <https://jira.onap.org/browse/CPS-504>`_ Checkstyle rules are not enforced for cps-ncmp-dmi-plugin
   - `CPS-589 <https://jira.onap.org/browse/CPS-589>`_ Json for Yang Resources does not contain name and revision tags
   - `CPS-617 <https://jira.onap.org/browse/CPS-617>`_ DMI base path does not conform to agreed API URL

Summary
-------

Following DMI-Plugin components are available with default ONAP/DMI-plugin installation.

    * Platform components

    * Service components

    * Additional resources that CPS utilizes deployed using ONAP common charts

Below service components (mS) are available to be deployed on-demand.

Under OOM (Kubernetes) all CPS component containers are deployed as Kubernetes Pods/Deployments/Services into Kubernetes cluster.

Known Limitations, Issues and Workarounds
-----------------------------------------

*System Limitations*

None

*Known Vulnerabilities*

   - `CPS-653 <https://jira.onap.org/browse/CPS-653>`_ cmHandleProperties not supported by dmi in fetch modules
   - `CPS-659 <https://jira.onap.org/browse/CPS-659>`_ DMI does not set CREATE Response code for passthrough-running create use-case

*Workarounds*

None

Security Notes
--------------

*Fixed Security Issues*

None

*Known Security Issues*

None

References
----------

For more information on the ONAP Istanbul release, please see:

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
        - `Passing Badge information for CPS <https://bestpractices.coreinfrastructure.org/en/projects/4398>`_
