/**
 * 
 */
package com.buisonje;

import java.text.MessageFormat;

import org.apache.maven.plugin.logging.Log;

import com.btr.proxy.util.Logger.LogBackEnd;
import com.btr.proxy.util.Logger.LogLevel;

/**
 * @author buisonvo
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
