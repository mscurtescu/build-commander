package com.googlecode.build_commander;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Marius Scurtescu
 */
public class ConfigParser
{
    private static Pattern EVENT_PATTERN = Pattern.compile("event\\.(\\w+)\\.(\\w+)\\..+");
    private static Set<String> EVENT_NAMES = new HashSet<String>();

    private Set<String> _invalidEventNames = new HashSet<String>();

    private Config _config;
    private Logger _logger;

    static
    {
        EVENT_NAMES.add("subBuildStarted");
        EVENT_NAMES.add("subBuildFinished");
        EVENT_NAMES.add("buildStarted");
        EVENT_NAMES.add("buildFirst");
        EVENT_NAMES.add("buildFinished");
        EVENT_NAMES.add("targetStarted");
        EVENT_NAMES.add("targetFinished");
        EVENT_NAMES.add("taskStarted");
        EVENT_NAMES.add("taskFinished");
        EVENT_NAMES.add("messageLogged");
    }

    public ConfigParser(Config config)
    {
        _config = config;
        _logger = config.getLogger();
    }

    public void parseConfig(File file, String source) throws IOException
    {
        parseConfig(new FileInputStream (file), source);
    }

    public void parseConfig(InputStream configSource, String source) throws IOException
    {
        Map<String,EventHandler> handlerMap = new HashMap<String, EventHandler>();

        Properties configProps = new Properties();
        configProps.load(configSource);

        Enumeration keys = configProps.keys();
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            String value = configProps.getProperty(key);

            Matcher eventMatcher = EVENT_PATTERN.matcher(key);

            if (eventMatcher.matches())
            {
                String eventName = eventMatcher.group(1);
                String handlerName = eventMatcher.group(2);

                if (!EVENT_NAMES.contains(eventName))
                {
                    if (!_invalidEventNames.contains(eventName)) // log only once
                    {
                        _logger.severe("Invalid event name: " + eventName);

                        _invalidEventNames.add(eventName);
                    }

                    continue;
                }

                String keyPrefix = "event." + eventName + "." + handlerName;

                EventHandler handler;

                if (handlerMap.containsKey(keyPrefix))
                {
                    handler = handlerMap.get(keyPrefix);
                }
                else
                {
                    String cmdPart = configProps.getProperty(keyPrefix + ".command");

                    if (cmdPart == null)
                    {
                        _logger.severe("Event handler with no command: " + keyPrefix);

                        continue;
                    }

                    List<String> command = new ArrayList<String>();

                    command.add(cmdPart);

                    int argIdx = 1;
                    cmdPart = configProps.getProperty(keyPrefix + ".command." + argIdx);
                    while (cmdPart != null)
                    {
                        command.add(cmdPart);

                        argIdx++;
                        cmdPart = configProps.getProperty(keyPrefix + ".command." + argIdx);
                    }

                    handler = new EventHandler(source, eventName, handlerName, command, _logger);

                    handlerMap.put(keyPrefix, handler);
                    _config.addEventHandler(handler);
                }

                if (key.equals(keyPrefix + ".dir"))
                {
                    handler.setDir(value);
                }
                else if (key.equals(keyPrefix + ".redirectError"))
                {
                    handler.setRedirectError(value);
                }
                else if (key.equals(keyPrefix + ".wait"))
                {
                    try
                    {
                        handler.setWaitFor(Boolean.parseBoolean(value));
                    }
                    catch (Exception e)
                    {
                        _logger.log(Level.SEVERE, "Invalid wait flag for event handler: " + value, e);
                    }
                }
                else if (key.startsWith(keyPrefix + ".if."))
                {
                    handler.addCondition(Condition.create(_config, key.substring((keyPrefix + ".if.").length()), value, false));
                }
                else if (key.startsWith(keyPrefix + ".unless."))
                {
                    handler.addCondition(Condition.create(_config, key.substring((keyPrefix + ".unless.").length()), value, true));
                }
                else if (key.startsWith(keyPrefix + ".command"))
                {
                    // ignore
                }
                else
                {
                    _logger.severe("Invalid key: " + key);
                }
            }
            else if ("log.file".equals(key))
            {
                _config.setLogFile(value);
            }
            else if ("log.level".equals(key))
            {
                _config.setLogLevel(value);
            }
        }
    }

    public File getProjectConfig(File projectDir)
    {
        String fileName = System.getProperty(CommanderListener.SYS_PROJECT_CONFIG_FILENAME, "build-commander.properties");
        File config = new File(projectDir, fileName);

        return config.exists() ? config : null;
    }

    public File getUserConfig()
    {
        String homeFolder = System.getProperty("user.home");

        String userCfgFile = System.getProperty(CommanderListener.SYS_USER_CONFIG_FILE);

        if (userCfgFile != null)
        {
            File cfg = new File(userCfgFile);

            if (cfg.exists())
                return cfg;

            _logger.severe("User config file set through system property does not exist: " + userCfgFile);

            return null;
        }

        if (isLinux())
        {
            File cfg = new File(homeFolder, ".config/build-commander/build-commander.properties");

            if (cfg.exists())
                return cfg;
        }

        // for all other platforms now try in home folder for .build-commander/build-commander.properties
        File config = new File(homeFolder, ".build-commander" + File.separator + "build-commander.properties");

        return config.exists() ? config : null;
    }

    public File getSystemConfig()
    {
        String sysCfgFile = System.getProperty(CommanderListener.SYS_SYSTEM_CONFIG_FILE);

        if (sysCfgFile != null)
        {
            File cfg = new File(sysCfgFile);

            if (cfg.exists())
                return cfg;

            _logger.severe("System config file set through system property does not exist: " + sysCfgFile);

            return null;
        }

        if (isLinux())
        {
            File config = new File("/etc/build-commander.properties");

            return config.exists() ? config : null;
        }

        return null;
    }

    public static boolean isLinux()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0;
    }

    public static boolean isOsx()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    }

    public static boolean isWindows()
    {
        return System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0;
    }
}
