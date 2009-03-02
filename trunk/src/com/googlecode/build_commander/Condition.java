package com.googlecode.build_commander;

import java.util.Properties;

public class Condition
{
    private String _property;
    private String _value;

    public Condition(String property, String value)
    {
        _property = property;
        _value = value;
    }

    public boolean evaluate(Properties properties)
    {
        // TODO

        return _value.equals(properties.getProperty(_property));
    }
}
