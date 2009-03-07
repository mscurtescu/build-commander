package com.googlecode.build_commander;

import org.apache.tools.ant.*;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.File;

public class CommanderListener implements SubBuildListener
{
    private Logger _logger;
    private Config _config = new Config();
    private ConfigParser _configParser;

    private static Map<Integer,String> _priorityNames = new HashMap<Integer, String>(5);

    public static final String SYS_PROJECT_CONFIG_FILENAME = "com.googlecode.build_commander.project.config.filename";
    public static final String SYS_USER_CONFIG_FILE = "com.googlecode.build_commander.user.config.file";
    public static final String SYS_SYSTEM_CONFIG_FILE = "com.googlecode.build_commander.system.config.file";
    public static final String SYS_LOG_FILE = "com.googlecode.build_commander.log.file";
    public static final String SYS_LOG_LEVEL = "com.googlecode.build_commander.log.level";

    static
    {
        _priorityNames.put(0, "error");
        _priorityNames.put(1, "warning");
        _priorityNames.put(2, "information");
        _priorityNames.put(3, "verbose");
        _priorityNames.put(4, "debug");
    }

    public CommanderListener()
    {
        _config = new Config();
        _logger = _config.getLogger();

        _configParser = new ConfigParser(_config);
    }

    public void subBuildStarted(BuildEvent event)
    {
        executeComands("subBuildStarted", event);
    }

    public void subBuildFinished(BuildEvent event)
    {
        executeComands("subBuildFinished", event);
    }

    public void buildStarted(BuildEvent event)
    {
        init(event.getProject());

        executeComands("buildStarted", event);
    }

    public void buildFinished(BuildEvent event)
    {
        executeComands("buildFinished", event);
    }

    public void targetStarted(BuildEvent event)
    {
        executeComands("targetStarted", event);
    }

    public void targetFinished(BuildEvent event)
    {
        executeComands("targetFinished", event);
    }

    public void taskStarted(BuildEvent event)
    {
        executeComands("taskStarted", event);
    }

    public void taskFinished(BuildEvent event)
    {
        executeComands("taskFinished", event);
    }

    public void messageLogged(BuildEvent event)
    {
        executeComands("messageLogged", event);
    }

    private void executeComands(String eventType, BuildEvent event)
    {
        _logger.finest("Event: " + eventType);

        PropertyHelper propertyHelper = createPropertyHelper(event);

        if (_config.getEventHandlers().containsKey(eventType))
        {
            for (EventHandler eventHandler : _config.getEventHandlers().get(eventType))
            {
                try
                {
                    _logger.fine("Firing handler: " + eventHandler);

                    eventHandler.conditionalExecute(propertyHelper);
                }
                catch (IOException e)
                {
                    // Avoid infinte loop
                    if (!"messageLogged".equals(eventType))
                        _logger.log(Level.SEVERE, "Cannot execute event handler: " + eventHandler.toString(), e);
                }
            }
        }
    }

    private PropertyHelper createPropertyHelper(BuildEvent event)
    {
        PropertyHelper propertyHelper = PropertyHelper.getPropertyHelper(new Project());
        Project project = event.getProject();

        try
        {
            addProperty(propertyHelper, "project.dir", project.getBaseDir().getCanonicalPath());
        }
        catch (IOException e)
        {
            _logger.log(Level.SEVERE, "Invalid base dir: " + project.getBaseDir(), e);
        }

        addProperty(propertyHelper, "project.default", project.getDefaultTarget());
        addProperty(propertyHelper, "project.description", project.getDescription());
        addProperty(propertyHelper, "project.name", project.getName());

        Target target = event.getTarget();
        if (target != null)
        {
            addProperty(propertyHelper, "target.description", target.getDescription());
            addProperty(propertyHelper, "target.name", target.getName());
            addProperty(propertyHelper, "target.if", target.getIf());
            addProperty(propertyHelper, "target.unless", target.getUnless());
        }

        Task task = event.getTask();
        if (task != null)
        {
            addProperty(propertyHelper, "task.description", task.getDescription());
            addProperty(propertyHelper, "task.name", task.getTaskName());
            addProperty(propertyHelper, "task.type", task.getTaskType());
        }

        String message = event.getMessage();
        if (message != null)
        {
            addProperty(propertyHelper, "message", message);

            int priority = event.getPriority();
            addProperty(propertyHelper, "priority.code", Integer.toString(priority));
            addProperty(propertyHelper, "priority.name", _priorityNames.get(priority));
        }

        Throwable exception = event.getException();
        if (exception != null)
        {
            addProperty(propertyHelper, "exception.message", exception.getMessage());

            try
            {
                StringWriter stackTraceWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stackTraceWriter);
                exception.printStackTrace(printWriter);
                printWriter.close();
                stackTraceWriter.close();

                addProperty(propertyHelper, "exception.stackTrace", stackTraceWriter.toString());
            }
            catch (IOException e)
            {
                _logger.log(Level.SEVERE, "Cannot capture exception stack trace!", e);
            }
        }

        return propertyHelper;
    }

    private void addProperty(PropertyHelper propertyHelper, String name, String value)
    {
        if (value != null)
        {
            propertyHelper.setUserProperty(null, name, value);
        }
    }

    private void init(Project project)
    {
        addHandlers(_configParser.getSystemConfig(), "system");
        addHandlers(_configParser.getUserConfig(), "user");
        addHandlers(_configParser.getProjectConfig(project.getBaseDir()), "project");
    }

    private void addHandlers(File cfgFile, String source)
    {
        if (cfgFile != null)
        {
            _logger.fine("Loading " + source + " config from: " + cfgFile.getAbsolutePath());

            try
            {
                _configParser.parseConfig(cfgFile, source);
            }
            catch (IOException e)
            {
                _logger.log(Level.SEVERE, "Cannot parse config file: " + cfgFile.getAbsolutePath(), e);
            }
        }
    }
}
