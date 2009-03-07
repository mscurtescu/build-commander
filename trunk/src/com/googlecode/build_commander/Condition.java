package com.googlecode.build_commander;

import org.apache.tools.ant.PropertyHelper;

import java.util.logging.Logger;

public class Condition
{
    private Config _config;
    private Logger _logger;
    private String _property;
    private String _value;
    private boolean _negate;

    public static Condition create(Config config, String expression, String value, boolean negate)
    {
        return new Condition(config, expression, value, negate);
    }

    private Condition(Config config, String property, String value, boolean negate)
    {
        _config = config;
        _logger = config.getLogger();

        _property = property;
        _value = value;
        _negate = negate;

        _logger.finer("Condition created: " + property + " = " + value);
    }

    public boolean evaluate(PropertyHelper propertyHelper)
    {
        String left = (String) propertyHelper.getProperty(null, _property);
        String right = propertyHelper.replaceProperties(null, _value, null);

        _logger.finer("Condition evaluated: " + left + " = " + right);
        
        boolean eq = left.equals(right);

        if (_negate)
            eq = !eq;
        
        return eq;
    }
}
