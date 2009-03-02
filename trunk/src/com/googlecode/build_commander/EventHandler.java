package com.googlecode.build_commander;

import java.util.List;
import java.util.Properties;
import java.io.File;
import java.io.IOException;

public class EventHandler
{
    private String _command;
    private List<Condition> _conditions;
    private File _dir = null;
    private boolean _redirectError = false;

    public EventHandler(String command)
    {
        _command = command;
    }

    public void setConditions(List<Condition> conditions)
    {
        _conditions = conditions;
    }

    public void setDir(File dir)
    {
        _dir = dir;
    }

    public void setRedirectError(boolean redirectError) 
    {
        _redirectError = redirectError;
    }

    public boolean evaluateConditions(Properties properties)
    {
        for (Condition condition : _conditions)
        {
            if (!condition.evaluate(properties))
                return false;
        }

        return true;
    }

    public void execute(Properties properties) throws IOException
    {
        ProcessBuilder processBuilder = new ProcessBuilder(_command);

        if (_dir != null)
            processBuilder.directory(_dir);

        processBuilder.redirectErrorStream(_redirectError);

        processBuilder.start();
    }

    public void conditionalExecute(Properties properties) throws IOException
    {
        if (evaluateConditions(properties))
            execute(properties);
    }
}
