# Basic Configuration #

There are three things you need to do:
  1. install build-commander.jar
  1. tell Ant to use the Build Commander listener
  1. create configuration files

## Install build-commander.jar ##

Copy build-commander.jar to Ant's lib folder. There are two ways to do that:
  1. in your home folder to `.ant/lib/`
  1. directly to Ant's installation folder to `$ANT_HOME/lib/`

## Tell Ant to use Build Commander ##

Instruct Ant to use this listener. The simplest way, but not very convenient, is to add a command line argument when launching Ant:
```
ant -listener com.googlecode.build_commander.CommanderListener
```

The more elegant solution is to use the **ANT\_ARGS** environment variable. If this environment variable is present then Ant will use its contents is if it was specified on the command line. On Mac OS X and Linux, under bash, you can set this variable in your `.profile` or `.bashrc` file like:
```
export ANT_ARGS="-listener com.googlecode.build_commander.CommanderListener"
```

## Create Configuration Files ##

The last thing you need to do is to create a Build Commander configuration file. Use one of the files in the samples folder as a starter. You can place them in one of three locations:
  1. the Ant project base folder, in most cases this is where the Ant script is
  1. in your home folder under `.config/build-commander/` or `.build-commander/`, the former is the preferred location on Linux, this applies to all Ant projects for this user
  1. on Linux you can also place it at `/etc/build-commander.properties`, this applies to all Ant project and all users on this system


# Advanced Configuration #

There are a few Java system variables that control how Build Commander works. Same like with the listener these can either be set on the command line when you start Ant, or you can put them in an environment variable called **ANT\_OPTS** (as oppose to the previous **ANT\_ARGS**) that will be used by Ant.

The special variables that you can use are:
  * `com.googlecode.build_commander.project.config.filename` - the file name, not the full path, of project level configuration files; the default is `build-commander.properties`
  * `com.googlecode.build_commander.user.config.file` - the full path of the user level configuration file, this one applies to all projects for this user; the default is to first check `~/.config/build-commander/build-commander.properties` then  `~/.build-commander/build-commander.properties`
  * `com.googlecode.build_commander.system.config.file` - the full path of the system level config file; the default on Linux systems is to check `/etc/build-commander.properties`
  * `com.googlecode.build_commander.log.file` - the full path of the log files, by default there is no logging
  * `com.googlecode.build_commander.log.level` - the logging level, one of: **FINEST**, **FINER**, **FINE**, **CONFIG**, **INFO**, **WARNING**, **SEVERE**; the default is **WARNING**