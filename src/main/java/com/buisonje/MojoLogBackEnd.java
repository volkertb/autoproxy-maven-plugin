/*
 * Copyright 2014 Volkert de Buisonj&eacute;
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * NOTE: proxy-vole has a different license. See the license.txt file bundled
 *       with the proxy-vole dependency in the bundled-repo folder.
 */
package com.buisonje;

import java.text.MessageFormat;

import org.apache.maven.plugin.logging.Log;

import com.github.markusbernhardt.proxy.util.Logger.LogBackEnd;
import com.github.markusbernhardt.proxy.util.Logger.LogLevel;

/**
 * Allows proxy-vole to log to Maven's Mojo logging mechanism.
 * 
 * @author Volkert de Buisonj&eacute;
 *
 */
public class MojoLogBackEnd implements LogBackEnd {

    private static final String PROXY_VOLE_LOG_MESSAGE_PREFIX = "[proxy-vole] ";

    final Log mojoLog;

    public MojoLogBackEnd(Log mojoLog) {
        this.mojoLog = mojoLog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.btr.proxy.util.Logger.LogBackEnd#isLogginEnabled(com.btr.proxy.util
     * .Logger.LogLevel)
     */
    @Override
    public boolean isLogginEnabled(LogLevel arg0) {
        // Why wouldn't we want logging?
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.btr.proxy.util.Logger.LogBackEnd#log(java.lang.Class,
     * com.btr.proxy.util.Logger.LogLevel, java.lang.String, java.lang.Object[])
     */
    @Override
    public void log(Class<?> relevantClass, LogLevel logLevel,
            String messagePattern, Object... params) {
        final String className = relevantClass.getClass().getName();
        final String assembledMessage = PROXY_VOLE_LOG_MESSAGE_PREFIX + " : "
                + MessageFormat.format(messagePattern, params);
        switch (logLevel) {
        case DEBUG:
            mojoLog.debug(assembledMessage);
            break;
        case ERROR:
            mojoLog.error(assembledMessage);
            break;
        case INFO:
            mojoLog.info(assembledMessage);
            break;
        case TRACE:
            mojoLog.debug(assembledMessage);
            break;
        case WARNING:
            mojoLog.warn(assembledMessage);
            break;
        default:
            mojoLog.info(assembledMessage);
            break;
        }

    }

}
