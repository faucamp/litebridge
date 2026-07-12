package org.litebridge.maven;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DebugMojoLog implements Log {

    private final Logger mojoLogger;

    public DebugMojoLog(final Class<?> mojoClass) {
        mojoLogger = LoggerFactory.getLogger(mojoClass);
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public void debug(final CharSequence content) {
        mojoLogger.debug(content.toString());
    }

    @Override
    public void debug(final CharSequence content, final Throwable error) {
        mojoLogger.debug(content.toString(), error);
    }

    @Override
    public void debug(final Throwable error) {
        mojoLogger.debug("", error);
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(final CharSequence content) {
        mojoLogger.info(content.toString());
    }

    @Override
    public void info(final CharSequence content, final Throwable error) {
        mojoLogger.info(content.toString(), error);
    }

    @Override
    public void info(final Throwable error) {
        mojoLogger.info("", error);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public void warn(final CharSequence content) {
        mojoLogger.warn(content.toString());
    }

    @Override
    public void warn(final CharSequence content, final Throwable error) {
        mojoLogger.warn(content.toString(), error);
    }

    @Override
    public void warn(final Throwable error) {
        mojoLogger.warn("", error);
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public void error(final CharSequence content) {
        mojoLogger.error(content.toString());
    }

    @Override
    public void error(final CharSequence content, final Throwable error) {
        mojoLogger.error(content.toString(), error);
    }

    @Override
    public void error(final Throwable error) {
        mojoLogger.error("", error);
    }
}