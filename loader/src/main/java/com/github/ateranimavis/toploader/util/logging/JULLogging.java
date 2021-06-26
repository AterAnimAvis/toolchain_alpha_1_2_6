package com.github.ateranimavis.toploader.util.logging;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.github.ateranimavis.toploader.util.ReflectionHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.jul.LogManager;

public class JULLogging {

    private static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

    public static void setProperty() {
        LOGGER.info("Setting JUL Manager");
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }

    //------------------------------------------------------------------------------------------------------------------
    // Hacks
    //------------------------------------------------------------------------------------------------------------------

    public static void forceLog4J() {
        LOGGER.info("Forcing JUL Manager");
        try {
            Constructor<LogManager> logManager = ReflectionHelper.getConstructor(LogManager.class);

            Field manager = ReflectionHelper.unfinalizeField(
                ReflectionHelper.getField(java.util.logging.LogManager.class, "manager")
            );

            manager.set(null, logManager.newInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
