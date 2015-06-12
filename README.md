# Introduction #

Build Commander is an Ant build listener that allows you to run arbitrary system commands triggered by build events.

Based on how build listeners are implemented in Ant, these commands can be hooked-in without changing the build scripts, just by providing external configuration files and an environment variable to instruct Ant to use this build listener.

These transparent hook-in mechanism allows in a heterogeneous team, for example, each developer to run their own OS specific commands to display notifications and start external applications like browser without impacting each other.


# Typical Usages #

Here are some typical usages for this build listener:
  * display a pop-up notification when the build ends, with a different message if the build fails
  * only when JUnit fails, automatically start a browser with the JUnit HTML report index page
  * automatically start a browser with the generated Javadoc
  * show a link to the generated Javadoc in a popup message window
  * launch a local deployment script when the build successfully ends


# Requirements #

  * Java 1.5 and up
  * Ant 1.6.1 and up


# Installation #

See [INSTALL](INSTALL.md).


# Configuration Files #

Build Commander configuration files are using the standard Java .properties file format.

## General Configuration ##

  * **log.file** - full path of file to be used for logging
  * **log.level** - log level, one of: **FINEST**, **FINER**, **FINE**, **CONFIG**, **INFO**, **WARNING**, **SEVERE**; the default is **WARNING**

The log file and log level can also be set with Java system variables, see [INSTALL](INSTALL.md). The log file settings from a configuration file take precedence.

## Event Handler Configuration ##

The Ant BuildListener provides the following build events:
  * **subBuildStarted**
  * **subBuildFinished**
  * **buildStarted**
  * **buildFinished**
  * **targetStarted**
  * **targetFinished**
  * **taskStarted**
  * **taskFinished**
  * **messageLogged**

Build Commander also introduces a pseudo event:
  * **buildFirst** - fired with the first build event that has a complete Project definition (you can use all project properties, see below)

Event handlers associate build events with one or more commands, and are defined in Build Commander's configuration file with one or more lines having the following format:

**event.**_<event name>_**.**_<handler name>_**.**_<handler attribute>_=_`<value`>_

where:
  * _<event name>_ - is one of the event names from above
  * _<handler name>_ - is a handler name that defines one specific handler
  * _<handler attribute>_ and _`<value`>_ describe the action configured for a handler

Example:
```
event.buildFirst.popup.command=notify-send
```

Naming each handler allows you to do two things:
  1. Override specific handlers in configuration files. For example, if the user configuration file defines a buildFinished.popup event then you can define the same, and so override it, in a project configuration file.
  1. Create multiple handlers for the same event. For the buildFinished event for example you can create one event for successful build and another for failed build.

Handler attributes are:
  * **command** - the actual command to run, no arguments, you can leave this empty and this defines a no-op command, can be used to silence event handlers in higher level configuration files
  * **command.1**, **.command.2**... - command arguments
  * **dir** - the folder where the command should be run, the default is the project base directory
  * **redirectError** - **true** or **false**, to redirect the command error output to the command standard output, default is **false**
  * **wait** - **true** or **false**, if Build Commander should wait for this command to finish, in which case the return code will be logged, default is **false**
  * **if.**_property_ - fire this handler only if the specified property has the provided value
  * **unless.**_property_ - fire this handler only if the specified property does not have the provided value

Build Commander makes available extra information about each build event to the event handler through the following **event properties**; they can be used inside handler attribute values as **${<event property>}**.
  * **project.dir** - the project base dir
  * **project.default** - default target of the project
  * **project.description**
  * **project.name**
  * **target.description**
  * **target.name**
  * **target.if** - content of 'if' attribute of target
  * **target.unless** - content of 'unless' attribute of target
  * **task.description**
  * **task.name**
  * **task.type**
  * **message**
  * **priority.code** - message priority code: **0**, **1**, **2**, **3**, **4**
  * **priority.name** - symbolic name for above priority code, same order: **error**, **warning**, **information**, **verbose**, **debug**
  * **success** - **true** or **false**, depending if current event had errors or no, the _exception.`*`_ properties are set only if this is **false**
  * **exception.message**
  * **exception.stackTrace**

Example:
```
event.buildFirst.popup.command.5=Build Started: ${project.name}
```

See [samples](http://code.google.com/p/build-commander/source/browse/trunk/samples/linux/build-commander.properties) for more.