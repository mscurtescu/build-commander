# Ant build listerner that can run arbitrary commands
> Automatically exported from code.google.com/p/build-commander

An Ant build listener that can run OS commands for each build event.

For example, it can show notification message when the build ends (and show a different message if it failed), it can start a browser to show the generated javadoc or it can start a browser if unit tests failed showing corresponding the HTML report.

Because it is a build listener all this can be done without any changes to the actual build file, so each developer can implement these commands on his own platform.
