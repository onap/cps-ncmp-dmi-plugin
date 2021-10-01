.. This work is licensed under a Creative Commons Attribution 4.0 International License.
.. http://creativecommons.org/licenses/by/4.0
.. Copyright (C) 2021 Nordix Foundation

.. DO NOT CHANGE THIS LABEL FOR RELEASE NOTES - EVEN THOUGH IT GIVES A WARNING
.. _deployment:


DMI Plugin Deployment
#####################

.. warning:: draft

.. toctree::
   :maxdepth: 1

Deployment
==========

DMI Plugin OOM Charts
---------------------

The Dmi Plugin K8S charts are located within the `OOM Repo <https://github.com/onap/oom/tree/master/kubernetes/cps>`_

This chart includes the components listed below:

.. container:: ulist

    - `cps-core <https://github.com/onap/oom/tree/master/kubernetes/cps/components/cps-core>`_
    - `cps-temporal <https://github.com/onap/oom/tree/master/kubernetes/cps/components/cps-temporal>`_
    - `ncmp-dmi-plugin <https://github.com/onap/oom/tree/master/kubernetes/cps/components/ncmp-dmi-plugin>`_

Please refer to `OOM documentation <https://docs.onap.org/projects/onap-oom/en/latest/oom_user_guide.html>`_ on how to install and deploy ONAP.

Installing or Upgrading DMI Plugin Components
---------------------------------------------

Once you have cloned the charts from the OOM repository into a local directory.

**1. Go to the dmi-plugin charts**

.. code-block:: bash

    cd oom/kubuernetes/cps/components/ncmp-dmi-plugin

Edit properties in values.yaml files to make any changes if required.

**2. Build the charts**

.. code-block:: bash

    make dmi-plugin
    make SKIP_LINT=TRUE <namespace>

.. note:: SKIP_LINT is used to reduce the make time.

**3. Undeploy the DMI Plugin components**

    Once Undeployed, keep monitoring the DMI-Plugin pods until they stop running.

.. code-block:: bash

    helm del --purge <helm-release>-<dmiplugin-component-name>
    kubectl get pods -n <namespace> | grep <dmiplugin-component-name>

**4. Delete the NFS persisted data for the DMI-plugin components**

.. code-block:: bash

    rm -fr /dockerdata-nfs/<helm-release>/<dmiplugin-component-name>

**5. Re-Deploy DMI-Plugin pods**

    After deploying the DMI-Plugin, keep monitoring the pods until they appear as expected.

.. code-block:: bash

    helm deploy <helm-release> local/onap --namespace <namespace>
    kubectl get pods -n <namespace> | grep ncmp-dmi-plugin

Restarting a faulty component
-----------------------------

Each DMI-Plugin component can be restarted independently by issuing the following command.

.. code-block:: bash

    kubectl delete pod <dmi-plugin-pod> -n <namespace>

.. _credentials_retrieval:

Credentials Retrieval
---------------------

The commands below can be used to retrieve application property credentials. By default,
these credentials are automatically generated during deployment.

For DMI-plugin REST API authentication:

.. code::

   kubcetl get secret <my-helm-release>-ncmp-dmi-plugin-app-user-creds -n <namespace> -o json | jq '.data | map_values(@base64d)'

.. note::

   base64d works only with the jq version 1.6 or above.

DMI Plugin Pods
---------------
To get a listing of the DMI-plugin pods, run the following command:

.. code-block:: bash

    kubectl get pods -n <namespace> | grep ncmp-dmi-plugin

Additional DMI Plugin Core Customisations
=========================================

Application Properties
----------------------

The following table lists properties that can be specified as helm chart
values to configure for the application being deployed. This list is not exhaustive.

+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| Property                              | Description                                                                                             | Default Value                 |
+=======================================+=========================================================================================================+===============================+
| config.appUserName                    | User name used by the DMI-Plugin service to configure the authentication for REST APIs it exposes.      | ``ncmpuser``                  |
|                                       |                                                                                                         |                               |
|                                       | This is the user name to be used by the DMI-Plugin REST clients to authenticate themselves.             |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+
| config.appUserPassword                | Password used by the DMI-Plugin service to configure the authentication for REST API it exposes.        | Not defined                   |
|                                       |                                                                                                         |                               |
|                                       | This is the password to be used by the DMI-Plugin REST clients to authenticate themselves.              |                               |
|                                       |                                                                                                         |                               |
|                                       | If not defined, the password is generated when deploying the application.                               |                               |
|                                       |                                                                                                         |                               |
|                                       | See also :ref:`Credentials Retrieval <credentials_retrieval>`.                                          |                               |
+---------------------------------------+---------------------------------------------------------------------------------------------------------+-------------------------------+

DMI Plugin Docker Installation
==============================

DMI-Plugin can also be installed in a docker environment. Latest `docker-compose <https://github.com/onap/cps-ncmp-dmi-plugin/blob/master/docker-compose/docker-compose.yml>`_ is included in the repo to start all the relevant
services.
Latest instructions are covered in the `README <https://github.com/onap/cps-ncmp-dmi-plugin/blob/master/docker-compose/README.md>`_