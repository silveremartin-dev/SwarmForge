/*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jants.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// stores all the statistics generated from running a simulation
public class StatisticsCollector {

    private static Logger logger;

    private StatisticsCollector() {
    }

    public static void init() {
        System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.BasicAsyncLoggerContextSelector");
        logger = LogManager.getLogger(StatisticsCollector.class);
        //logger.trace("Configuration File Defined To Be: "+System.getProperty("log4j.configurationFile"));
    }

    // for a slightly different possible configuration, see https://medium.com/@ktimes90/extending-log4j-2-with-new-asynchronous-logging-and-customized-configuration-6bb8b0985c0f

    public static void logAny(String log) {
        logger.log(Level.INFO, log);
    }

    public static void logAtLevel(Level logLevel, String log) {
        logger.log(logLevel, log);
    }

}
