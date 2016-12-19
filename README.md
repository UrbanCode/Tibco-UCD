# IBM UrbanCode Deploy TIBCO Plug-in [![Build Status](https://travis-ci.org/IBM-UrbanCode/Tibco-UCD.svg?branch=master)](https://travis-ci.org/IBM-UrbanCode/Tibco-UCD)


*NOTE: This is not the plugin distributable! This is the source code. To find the
installable plugin, either go to the [Releases](https://github.com/IBM-UrbanCode/Tibco-UCD/releases)
and download the Tibco zip or follow the installation instructions below.*

### License
This plugin is protected under the [Eclipse Public 1.0 License](http://www.eclipse.org/legal/epl-v10.html)

## Overview

TIBCO Software empowers executives, developers, and business users with Fast Data solutions that make the right data available in real time for faster answers, better decisions, and smarter action.

The IBM UrbanCode Deploy TIBCO Plug-in is an automation plug-in that provides smooth and transparent interaction between TIBCO Administrator and IBM UrbanCode Deploy tool. It automates the application management process for TIBCO applications by providing simplified process.

This community plug-in is supported on a best effort basis. For a commercially supported integration, please see the [Partner Plugin](https://developer.ibm.com/urbancode/plugin/tibco/) provided by ScaleFocus.

## Steps

* Build Multiple Tibco Enterprise Archives
* Build Tibco Enterprise Archive (.ear)
* Create EMS JNDI Names
* Create or update EMS Queues
* Create or update EMS Topics
* Delete JNDI Names
* Delete Queues
* Delete Topics
* Deploy Multiple Tibco Enterprise Archives
* Deploy Tibco Enterprise Archive (.ear)

## Compatibility
This plug-in requires version 6.1.1 or later of IBM UrbanCode Deploy and version 5.7 or later of TIBCO.

## Build
Download the entire Tibco-UCD and run the `gradle` command in the top level folder.
This should compile the code and create a new distributable zip within the build/distributions folder.

## Installation
Once you have the distributable zip, no special steps are required for installation.
See [Installing plug-ins in UrbanCode Deploy](https://developer.ibm.com/urbancode/docs/installing-plugins-ucd/#ucd).

## History
* Version 4

    Travis CI Support.
    Added missing groovy-plugin-utils-1.0.jar to classpath.

* Version 3

    Add steps to create/delete queues, topics and JNDI Names

* Version 2

    Add steps for Tibco EMS

* Version 1

    Initial release of the Tibco plugin.
