package com.googlecode.build_commander;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.*;
import java.io.IOException;

/**
 * @author Marius Scurtescu
 */
public class Config
{
    private Map<String, List<EventHandler>> _eventHandlers = new HashMap<String, List<EventHandler>>();
    private Logger _logger;
    private Handler _handler;

    public Config()
    {
        _logger = Logger.getLogger("build-commander");
        _logger.setLevel(Level.WARNING);

        String logFile = System.getProperty(CommanderListener.SYS_LOG_FILE);

        if (logFile != null)
            setLogFile(logFile);

        String logLevelStr = System.getProperty(CommanderListener.SYS_LOG_LEVEL);

        if (logLevelStr != null)
            setLogLevel(logLevelStr);
    }

    public Logger getLogger()
    {
        return _logger;
    }

    public void setLogLevel(String logLevelStr)
    {
        try
        {
            Level logLevel = Level.parse(logLevelStr.toUpperCase());

            _logger.setLevel(logLevel);
        }
        catch (IllegalArgumentException e)
        {
            _logger.severe("Invalid log level set with system property: " + logLevelStr);
        }
    }

    public void setLogFile(String logFile)
    {
        try
        {
            if (_handler != null)
                _logger.removeHandler(_handler);

            _handler = new FileHandler(logFile, true);
            _handler.setFormatter(new SimpleFormatter());
            _handler.setLevel(Level.ALL);

            _logger.addHandler(_handler);
        }
        catch (IOException e)
        {
            _logger.log(Level.SEVERE, "Cannot set file handler for logger: " + logFile, e);
        }

    }

    public Map<String, List<EventHandler>> getEventHandlers()
    {
        return _eventHandlers;
    }

    public void addEventHandler(EventHandler handler)
    {
        String evenType = handler.getEvent();
        List<EventHandler> handlers;

        if (_eventHandlers.containsKey(evenType))
        {
            handlers = _eventHandlers.get(evenType);
        }
        else
        {
            handlers = new ArrayList<EventHandler>(1);

            _eventHandlers.put(evenType, handlers);
        }

        for (int i = 0; i < handlers.size(); i++)
        {
            EventHandler existingHandler = handlers.get(i);

            if (existingHandler.getName().equals(handler.getName()))
            {
                _logger.fine("Handler overwritten: " + handler);
                handlers.remove(i);

                break;
            }
        }

        _logger.fine("Handler added: " + handler);

        handlers.add(handler);
    }
}
