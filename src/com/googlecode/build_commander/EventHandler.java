package com.googlecode.build_commander;

import org.apache.tools.ant.PropertyHelper;

import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.File;
import java.io.IOException;

public class EventHandler
{
    private String _source; // TODO should be an enum
    private String _event;
    private String _name;
    private List<String> _command;
    private List<Condition> _conditions = new ArrayList<Condition>();
    private String _dir = null;
    private String _redirectError = "false";
    private Logger _logger;
    private boolean _waitFor = false;

    public EventHandler(String source, String event, String name, List<String> command, Logger logger)
    {
        _source  = source;
        _event   = event;
        _name    = name;
        _command = command;
        _logger  = logger;
    }

    public String getSource()
    {
        return _source;
    }

    public String getEvent()
    {
        return _event;
    }

    public String getName()
    {
        return _name;
    }

    public List<String> getCommand()
    {
        return _command;
    }

    public List<Condition> getConditions()
    {
        return _conditions;
    }

    public void setConditions(List<Condition> conditions)
    {
        _conditions.clear();
        _conditions.addAll(conditions);
    }

    public void addCondition(Condition condition)
    {
        _conditions.add(condition);
    }

    public String getDir()
    {
        return _dir;
    }

    public void setDir(String dir)
    {
        _dir = dir;
    }

    public String getRedirectError()
    {
        return _redirectError;
    }

    public void setRedirectError(String redirectError)
    {
        _redirectError = redirectError;
    }

    public boolean isWaitFor()
    {
        return _waitFor;
    }

    public void setWaitFor(boolean waitFor)
    {
        _waitFor = waitFor;
    }

    public boolean evaluateConditions(PropertyHelper propertyHelper)
    {
        for (Condition condition : _conditions)
        {
            if (!condition.evaluate(propertyHelper))
                return false;
        }

        return true;
    }

    public void execute(PropertyHelper propertyHelper) throws IOException
    {
        List<String> expandedCmd = new ArrayList<String>(_command.size());

        for (String cmdPart : _command)
        {
            expandedCmd.add(propertyHelper.replaceProperties(null, cmdPart, null));
        }

        ProcessBuilder processBuilder = new ProcessBuilder(expandedCmd);

        if (_dir != null)
            processBuilder.directory(new File(propertyHelper.replaceProperties(null, _dir, null)));

        processBuilder.redirectErrorStream(Boolean.parseBoolean(propertyHelper.replaceProperties(null, _redirectError, null)));

        Process process = processBuilder.start();

        if (_waitFor)
        {
            try
            {
                int exitValue = process.waitFor();

                _logger.fine(toString() + " finished: " + exitValue);
            }
            catch (InterruptedException e)
            {
                _logger.log(Level.WARNING, "Command interrupted", e);
            }
        }
    }

    public void conditionalExecute(PropertyHelper propertyHelper) throws IOException
    {
        if (evaluateConditions(propertyHelper))
            execute(propertyHelper);
    }

    @Override
    public String toString()
    {
        return _source + ":" + _event + "." + _name;
    }
}
