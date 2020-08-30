/******************************************************************************
 * Copyright (c) 2009-2020, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary 
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.remotehand.windows;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class SessionLogger implements Logger {
	
	private final String sessionId;
	private final String sessionIdPrefix;
	private final Logger logger;

	public SessionLogger(String sessionId, Logger logger) {
		this.logger = logger;
		this.sessionId = sessionId;
		this.sessionIdPrefix = "<" + sessionId + "> ";
	}

	@Override
	public String getName() {
		return logger.getName();
	}

	@Override
	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	@Override
	public void trace(String msg) {
		logger.trace(sessionIdPrefix + msg);
	}

	@Override
	public void trace(String format, Object arg) {
		logger.trace(sessionIdPrefix + format, arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		logger.trace(sessionIdPrefix + format, arg1, arg2);
	}

	@Override
	public void trace(String format, Object... arguments) {
		logger.trace(sessionIdPrefix + format, arguments);
	}

	@Override
	public void trace(String msg, Throwable t) {
		logger.trace(sessionIdPrefix + msg, t);
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return logger.isTraceEnabled(marker);
	}

	@Override
	public void trace(Marker marker, String msg) {
		logger.trace(marker, sessionIdPrefix + msg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		logger.trace(marker, sessionIdPrefix + format, arg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		logger.trace(marker, sessionIdPrefix + format, arg1, arg2);
	}

	@Override
	public void trace(Marker marker, String format, Object... argArray) {
		logger.trace(marker, sessionIdPrefix + format, argArray);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		logger.trace(marker, sessionIdPrefix + msg, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public void debug(String msg) {
		logger.debug(sessionIdPrefix + msg);
	}

	@Override
	public void debug(String format, Object arg) {
		logger.debug(sessionIdPrefix + format, arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		logger.debug(sessionIdPrefix + format, arg1, arg2);
	}

	@Override
	public void debug(String format, Object... arguments) {
		logger.debug(sessionIdPrefix + format, arguments);
	}

	@Override
	public void debug(String msg, Throwable t) {
		logger.debug(sessionIdPrefix + msg, t);
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return logger.isDebugEnabled(marker);
	}

	@Override
	public void debug(Marker marker, String msg) {
		logger.debug(marker, sessionIdPrefix + msg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		logger.debug(marker, sessionIdPrefix + format, arg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		logger.debug(marker, sessionIdPrefix + format, arg1, arg2);
	}

	@Override
	public void debug(Marker marker, String format, Object... arguments) {
		logger.debug(marker, sessionIdPrefix + format, arguments);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		logger.debug(marker, sessionIdPrefix + msg, t);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	@Override
	public void info(String msg) {
		logger.info(sessionIdPrefix + msg);
	}

	@Override
	public void info(String format, Object arg) {
		logger.info(sessionIdPrefix + format, arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		logger.info(sessionIdPrefix + format, arg1, arg2);
	}

	@Override
	public void info(String format, Object... arguments) {
		logger.info(sessionIdPrefix + format, arguments);
	}

	@Override
	public void info(String msg, Throwable t) {
		logger.info(sessionIdPrefix + msg, t);
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return logger.isInfoEnabled(marker);
	}

	@Override
	public void info(Marker marker, String msg) {
		logger.info(marker, sessionIdPrefix + msg);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		logger.info(marker, sessionIdPrefix + format, arg);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		logger.info(marker, sessionIdPrefix + format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String format, Object... arguments) {
		logger.info(marker, sessionIdPrefix + format, arguments);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		logger.info(marker, sessionIdPrefix + msg, t);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	@Override
	public void warn(String msg) {
		logger.warn(sessionIdPrefix + msg);
	}

	@Override
	public void warn(String format, Object arg) {
		logger.warn(sessionIdPrefix + format, arg);
	}

	@Override
	public void warn(String format, Object... arguments) {
		logger.warn(sessionIdPrefix + format, arguments);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		logger.warn(sessionIdPrefix + format, arg1, arg2);
	}

	@Override
	public void warn(String msg, Throwable t) {
		logger.warn(sessionIdPrefix + msg, t);
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return logger.isWarnEnabled(marker);
	}

	@Override
	public void warn(Marker marker, String msg) {
		logger.warn(marker, sessionIdPrefix + msg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		logger.warn(marker, sessionIdPrefix + format, arg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		logger.warn(marker, sessionIdPrefix + format, arg1, arg2);
	}

	@Override
	public void warn(Marker marker, String format, Object... arguments) {
		logger.warn(marker, sessionIdPrefix + format, arguments);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		logger.warn(marker, sessionIdPrefix + msg, t);
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isErrorEnabled();
	}

	@Override
	public void error(String msg) {
		logger.error(sessionIdPrefix + msg);
	}

	@Override
	public void error(String format, Object arg) {
		logger.error(sessionIdPrefix + format, arg);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		logger.error(sessionIdPrefix + format, arg1, arg2);
	}

	@Override
	public void error(String format, Object... arguments) {
		logger.error(sessionIdPrefix + format, arguments);
	}

	@Override
	public void error(String msg, Throwable t) {
		logger.error(sessionIdPrefix + msg, t);
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return logger.isErrorEnabled(marker);
	}

	@Override
	public void error(Marker marker, String msg) {
		logger.error(marker, sessionIdPrefix + msg);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		logger.error(marker, sessionIdPrefix + format, arg);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		logger.error(marker, sessionIdPrefix + format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String format, Object... arguments) {
		logger.error(marker, sessionIdPrefix + format, arguments);
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		logger.error(marker, sessionIdPrefix + msg, t);
	}
}
