package com.googlecode.build_commander;

import org.apache.tools.ant.PropertyHelper;

public class Condition
{
    private String _property;
    private String _value;
    private boolean _negate;

    public static Condition create(String expression, String value, boolean negate)
    {
        return new Condition(expression, value, negate);
    }

    private Condition(String property, String value, boolean negate)
    {
        _property = property;
        _value = value;
        _negate = negate;
    }

    public boolean evaluate(PropertyHelper propertyHelper)
    {
        String left = (String) propertyHelper.getProperty(null, _property);
        String right = propertyHelper.replaceProperties(null, _value, null);

        boolean eq = left.equals(right);

        if (_negate)
            eq = !eq;
        
        return eq;
    }
}
