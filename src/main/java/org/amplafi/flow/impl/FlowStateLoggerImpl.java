/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for
 * the specific language governing permissions and limitations under the
 * License.
 */
package org.amplafi.flow.impl;

import org.amplafi.flow.FlowManagement;
import org.amplafi.flow.FlowState;
import org.apache.commons.logging.Log;

/**
 * Logs the state of the flows. Useful for dumping out the state when there is an error.
 *
 */
public class FlowStateLoggerImpl implements Log {

    private Log log;

    private FlowManagement flowManagement;

    public FlowStateLoggerImpl() {

    }
    public FlowStateLoggerImpl(Log log) {
        this.setLog(log);
    }
    /**
     * @param message
     * @return
     */
    protected StringBuilder getFlowStatesString(Object message) {
        StringBuilder stringBuilder = new StringBuilder().append(message);
        for(FlowState flowState: getFlowManagement().getFlowStates()) {
            stringBuilder.append(flowState).append("\n");
        }
        return stringBuilder;
    }

    /**
     * @param log the log to set
     */
    public void setLog(Log log) {
        this.log = log;
    }

    /**
     * @return the log
     */
    public Log getLog() {
        return log;
    }

    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object)
     */
    @Override
    public void debug(Object message) {
        this.debug(null, message, null);
    }
    public void error(Log logger, Object message, Throwable throwable) {
        if ( logger == null) {
            logger = getLog();
        }
        StringBuilder stringBuilder = getFlowStatesString(message);
        logger.error(stringBuilder, throwable);
    }
    public void fatal(Log logger, Object message, Throwable throwable) {
        if ( logger == null) {
            logger = getLog();
        }
        StringBuilder stringBuilder = getFlowStatesString(message);
        logger.fatal(stringBuilder, throwable);
    }
    public void warn(Log logger, Object message, Throwable throwable) {
        if ( logger == null) {
            logger = getLog();
        }
        StringBuilder stringBuilder = getFlowStatesString(message);
        logger.warn(stringBuilder, throwable);
    }
    public void info(Log logger, Object message, Throwable throwable) {
        if ( logger == null) {
            logger = getLog();
        }
        StringBuilder stringBuilder = getFlowStatesString(message);
        logger.info(stringBuilder, throwable);
    }
    public void debug(Log logger, Object message, Throwable throwable) {
        if ( logger == null) {
            logger = getLog();
        }
        StringBuilder stringBuilder = getFlowStatesString(message);
        logger.debug(stringBuilder, throwable);
    }
    public void trace(Log logger, Object message, Throwable throwable) {
        if ( logger == null) {
            logger = getLog();
        }
        StringBuilder stringBuilder = getFlowStatesString(message);
        logger.trace(stringBuilder, throwable);
    }
    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
     */
    @Override
    public void debug(Object message, Throwable throwable) {
        this.debug(null, message, throwable);
    }

    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object)
     */
    @Override
    public void error(Object message) {
        this.error(null, message, null);
    }

    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
     */
    @Override
    public void error(Object message, Throwable throwable) {
        this.error(null, message, throwable);
    }

    /**
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
     */
    @Override
    public void fatal(Object message) {
        this.fatal(null, message, null);
    }

    /**
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
     */
    @Override
    public void fatal(Object message, Throwable throwable) {
        this.fatal(null, message, throwable);
    }

    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object)
     */
    @Override
    public void info(Object message) {
        this.info(null, message, null);
    }

    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
     */
    @Override
    public void info(Object message, Throwable throwable) {
        this.info(null, message, throwable);
    }

    /**
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled() {
        return getLog().isDebugEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    @Override
    public boolean isErrorEnabled() {
        return getLog().isErrorEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isFatalEnabled()
     */
    @Override
    public boolean isFatalEnabled() {
        return getLog().isFatalEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    @Override
    public boolean isInfoEnabled() {
        return getLog().isInfoEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isTraceEnabled()
     */
    @Override
    public boolean isTraceEnabled() {
        return getLog().isTraceEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    @Override
    public boolean isWarnEnabled() {
        return getLog().isWarnEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#trace(java.lang.Object)
     */
    @Override
    public void trace(Object message) {
        this.trace(null, message, null);
    }

    /**
     * @see org.apache.commons.logging.Log#trace(java.lang.Object, java.lang.Throwable)
     */
    @Override
    public void trace(Object message, Throwable throwable) {
        this.trace(null, message, throwable);
    }

    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object)
     */
    @Override
    public void warn(Object message) {
        this.warn(null, message, null);
    }

    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
     */
    @Override
    public void warn(Object message, Throwable throwable) {
        this.warn(null, message, throwable);
    }

    /**
     * @param flowManagement the flowManagement to set
     */
    public void setFlowManagement(FlowManagement flowManagement) {
        this.flowManagement = flowManagement;
    }

    /**
     * @return the flowManagement
     */
    public FlowManagement getFlowManagement() {
        return flowManagement;
    }

}
