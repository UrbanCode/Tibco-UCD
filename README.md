# uDeploy-Tibco-Plugin


*NOTE: This is not the plugin distributable! This is the source code. To find the
installable plugin, go https://developer.ibm.com/urbancode/plugins/ibm-urbancode-deploy/
and search for Tibco.*

## Overview

TIBCO Software empowers executives, developers, and business users with Fast Data solutions that make the right data available in real time for faster answers, better decisions, and smarter action.

The UCD Tibco Plugin is an automation plugin that provides smooth and transparent interaction between TIBCO Administrator and IBM UrbanCode Deploy tool. It automates the app management process for TIBCO applications by providing simplified process

## Steps

* Build Tibco Enterprise Archive (.ear)
* Build Multiple Tibco Enterprise Archives
* Deploy Tibco Enterprise Archive (.ear)
* Deploy Multiple Tibco Enterprise Archives
* Create or update EMS Queues
* Create or update EMS Topics
* Create EMS JNDI Names
* Delete Queues
* Delete Topics
* Delete JNDI Names

## Compatibility
This plug-in requires version 6.1.1 or later of IBM UrbanCode Deploy.

## Installation
Download the entire uDeploy-Tibco-Plugin and run the "ant" command in the top level folder.
This should compile the code and create a new distributable zip within the dist folder.
Once you have the distributable zip, no special steps are required for installation.
See Installing plug-ins in UrbanCode Deploy.

## History
Add steps to create/delete queues, topics and JNDI Names
* Version 3

    Add steps to create/delete queues, topics and JNDI Names

* Version 2

    Add steps for Tibco EMS

* Version 1

    Initial release of the Tibco plugin.