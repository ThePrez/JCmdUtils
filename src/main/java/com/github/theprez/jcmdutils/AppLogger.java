/*
 * 
 */
package com.github.theprez.jcmdutils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.Flushable;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import com.github.theprez.jcmdutils.StringUtils.TerminalColor;

/**
 * Used to encapsulate console logging activity in verbose and non-verbose mode. This class
 * provides a default implementation that simply writes to standard error and standard out, but
 * an implementation can be made to write to any resource.
 *
 * @author Jesse Gorzinski
 */
public abstract class AppLogger {

    /**
     * The Class DefaultLogger.
     */
    public static class DefaultLogger extends AppLogger {

        /** The m err. */
        private final OutputHandler m_err;

        /** The m out. */
        private final OutputHandler m_out;

        /** The m verbose. */
        private final boolean m_verbose;

        /**
         * Instantiates an instance that will simply write output to <tt>System.out</tt> and <tt>System.err</tt>
         *
         * @param _verbose
         *            the verbose
         */
        public DefaultLogger(final boolean _verbose) {
            m_out = (_fmt, _args) -> System.out.printf(_fmt, _args);
            m_err = (_fmt, _args) -> System.err.printf(_fmt, _args);
            m_verbose = _verbose;
            // TODO: have options to write verbose output to file or log4j or something (that's the whole point of this class)
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.github.theprez.jcmdutils.AppLogger#getErr()
         */
        @Override
        protected OutputHandler getErr() {
            return m_err;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.github.theprez.jcmdutils.AppLogger#getOut()
         */
        @Override
        protected OutputHandler getOut() {
            return m_out;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.github.theprez.jcmdutils.AppLogger#isVerbose()
         */
        @Override
        protected boolean isVerbose() {
            return m_verbose;
        }
    }

    /**
     * An {@link AppLogger} implementation that allows the output to be queued up and then published at a later time,
     * by use of the {@link #flush()} method
     */
    public static class DeferredLogger extends AppLogger implements Flushable, Closeable {

        /** The m deferred err. */
        private final OutputHandler m_deferredErr;

        /** The m deferred events. */
        private final LinkedList<Runnable> m_deferredEvents = new LinkedList<Runnable>();

        /** The m deferred out. */
        private final OutputHandler m_deferredOut;

        /** The m parent. */
        private final AppLogger m_parent;

        /**
         * Instantiates a new deferred logger.
         *
         * @param _parent
         *            the parent
         */
        public DeferredLogger(final AppLogger _parent) {
            m_parent = _parent;
            m_deferredOut = (_fmt, _args) -> m_deferredEvents.add(() -> m_parent.getOut().printf(_fmt, _args));
            m_deferredErr = (_fmt, _args) -> m_deferredEvents.add(() -> m_parent.getErr().printf(_fmt, _args));
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.Closeable#close()
         */
        @Override
        public void close() {
            flush();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.io.Flushable#flush()
         */
        @Override
        public void flush() {
            while (true) {
                try {
                    m_deferredEvents.removeFirst().run();
                } catch (final NoSuchElementException e) {
                    return;
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.github.theprez.jcmdutils.AppLogger#getErr()
         */
        @Override
        protected OutputHandler getErr() {
            return m_deferredErr;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.github.theprez.jcmdutils.AppLogger#getOut()
         */
        @Override
        protected OutputHandler getOut() {
            return m_deferredOut;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.github.theprez.jcmdutils.AppLogger#isVerbose()
         */
        @Override
        protected boolean isVerbose() {
            return m_parent.isVerbose();
        }
    }

    /**
     * The Interface OutputHandler.
     */
    private interface OutputHandler {

        /**
         * Printf.
         *
         * @param _fmt
         *            the fmt
         * @param _args
         *            the args
         */
        void printf(String _fmt, Object... _args);

        /**
         * Println.
         *
         * @param _str
         *            the str
         */
        default void println(final String _str) {
            printf("%s\n", _str);
        }
    }

    /** The s singleton. */
    private static DefaultLogger s_singleton;

    /**
     * Gets a global, singleton instance of {@link AppLogger}. The <tt>_verbose</tt> parameter
     * identifies whether the logger is in verbose mode.
     * <br><b>IMPORTANT NOTE:</b>
     * There is only ever one singleton object returned by this method. The determination of
     * whether that logger is in verbose mode or not depends on the value of <tt>_verbose</tt>
     * on the very first invocation of this method.
     *
     * @param _verbose
     *            whether the logger is in verbose mode. Ignored for all calls except the
     *            first invocation
     * @return the singleton
     */
    public synchronized static AppLogger getSingleton(final boolean _verbose) {
        if (null != s_singleton) {
            return s_singleton;
        }
        return s_singleton = new DefaultLogger(_verbose);
    }

    /**
     * Print the exception stack trace
     * 
     * @param _exc
     *            the exc
     */
    public void exception(final Throwable _exc) {
        _exc.printStackTrace(System.err);
    }

    /**
     * Gets the err.
     *
     * @return the err
     */
    protected abstract OutputHandler getErr();

    /**
     * Gets the out.
     *
     * @return the out
     */
    protected abstract OutputHandler getOut();

    /**
     * Checks if is verbose.
     *
     * @return true, if is verbose
     */
    protected abstract boolean isVerbose();

    /**
     * Prints the exception stack if running in verbose mode.
     *
     * @param _causedBy
     *            the caused by
     */
    public void printExceptionStack_verbose(final Throwable _causedBy) {
        if (!isVerbose()) {
            return;
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintWriter pw = new PrintWriter(baos, false);
        _causedBy.printStackTrace(pw);
        pw.flush();
        getErr().println(new String(baos.toByteArray()));
    }

    /**
     * Classic printf implemenatation. See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printf(final String _fmt, final Object... _args) {
        getOut().printf(_fmt, _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will be colorized red if the terminal allows it
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printf_err(final String _fmt, final Object... _args) {
        getErr().printf(StringUtils.colorizeForTerminal(_fmt, TerminalColor.BRIGHT_RED), _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will be colorized red if the terminal allows it
     *  <li>Output will only be printed if running in verbose mode
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printf_err_verbose(final String _fmt, final Object... _args) {
        if (!isVerbose()) {
            return;
        }
        getErr().printf(StringUtils.colorizeForTerminal(_fmt, TerminalColor.BRIGHT_RED), _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will be colorized green if the terminal allows it
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printf_success(final String _fmt, final Object... _args) {
        printf(StringUtils.colorizeForTerminal(_fmt, TerminalColor.GREEN), _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will only be printed if running in verbose mode
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printf_verbose(final String _fmt, final Object... _args) {
        if (!isVerbose()) {
            return;
        }
        getOut().printf(_fmt, _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will be colorized yellow if the terminal allows it
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printf_warn(final String _fmt, final Object... _args) {
        getErr().printf(StringUtils.colorizeForTerminal(_fmt, TerminalColor.YELLOW), _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will be colorized yellow if the terminal allows it
     *  <li>Output will only be printed if running in verbose mode
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printf_warn_verbose(final String _fmt, final Object... _args) {
        if (!isVerbose()) {
            return;
        }
        getErr().printf(StringUtils.colorizeForTerminal(_fmt, TerminalColor.YELLOW), _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will automatically have a newline character appended
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printfln(final String _fmt, final Object... _args) {
        printf(_fmt + "\n", _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will automatically have a newline character appended
     *  <li>Output will be colorized red if the terminal allows it
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printfln_err(final String _fmt, final Object... _args) {
        printf_err(_fmt + "\n", _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will automatically have a newline character appended
     *  <li>Output will be colorized red if the terminal allows it
     *  <li>Output will only be printed if running in verbose mode
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printfln_err_verbose(final String _fmt, final Object... _args) {
        printf_err_verbose(_fmt + "\n", _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will automatically have a newline character appended
     *  <li>Output will only be printed if running in verbose mode
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printfln_verbose(final String _fmt, final Object... _args) {
        printf_verbose(_fmt + "\n", _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will automatically have a newline character appended
     *  <li>Output will be colorized yellow if the terminal allows it
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printfln_warn(final String _fmt, final Object... _args) {
        printf_warn(_fmt + "\n", _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will automatically have a newline character appended
     *  <li>Output will be colorized yellow if the terminal allows it
     *  <li>Output will only be printed if running in verbose mode
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     *
     * @param _fmt
     *            the fmt
     * @param _args
     *            the args
     */
    public void printfln_warn_verbose(final String _fmt, final Object... _args) {
        printf_warn_verbose(_fmt + "\n", _args);
    }

    /**
     * Classic printf implemenatation, except:
     * <ul>
     *  <li>Output will automatically have a newline character appended
     * </ul>
     * See {@link java.util.Formatter} for information on
     * how to use printf formatting.
     */
    public void println() {
        printf("\n");
    }

    /**
     * Simply prints a line containing the given string
     *
     * @param _str
     *            the str
     */
    public void println(final String _str) {
        getOut().println(_str);
    }

    /**
     * Simply prints a line containing the given string
     */
    public void println_err() {
        printf_err("\n");
    }

    /**
     * Simply prints a line containing the given string, except:
     * <ul>
     *  <li>Output will be colorized red if the terminal allows it
     * </ul>
     *
     * @param _str
     *            the str
     */
    public void println_err(final String _str) {
        getErr().println(StringUtils.colorizeForTerminal(_str, TerminalColor.BRIGHT_RED));
    }

    /**
     * Simply prints a line containing the given string, except:
     * <ul>
     *  <li>Output will be colorized red if the terminal allows it
     *  <li>Output will only be printed if running in verbose mode
     * </ul>
     *
     * @param _msg
     *            the msg
     */
    public void println_err_verbose(final String _msg) {
        if (!isVerbose()) {
            return;
        }
        getErr().println(StringUtils.colorizeForTerminal(_msg, TerminalColor.BRIGHT_RED));
    }

    /**
     * Simply prints a line containing the given string, except:
     * <ul>
     *  <li>Output will be colorized red if the terminal allows it
     *  <li>Output will only be printed if running in verbose mode
     * </ul>
     *
     * @param _msg
     *            the msg
     */
    public void println_success(final String _msg) {
        getOut().println(StringUtils.colorizeForTerminal(_msg, TerminalColor.GREEN));
    }

    /**
     * Simply prints a line containing the given string, except:
     * <ul>
     *  <li>Output will only be printed if running in verbose mode
     * </ul>
     *
     * @param _msg
     *            the msg
     */
    public void println_verbose(final String _msg) {
        if (!isVerbose()) {
            return;
        }
        getOut().println(_msg);
    }

    /**
     * Simply prints a line containing the given string, except:
     * <ul>
     *  <li>Output will be colorized yellow if the terminal allows it
     * </ul>
     *
     * @param _str
     *            the str
     */
    public void println_warn(final String _str) {
        getErr().println(StringUtils.colorizeForTerminal(_str, TerminalColor.YELLOW));
    }

    /**
     * Simply prints a line containing the given string, except:
     * <ul>
     *  <li>Output will be colorized yellow if the terminal allows it
     *  <li>Output will only be printed if running in verbose mode
     * </ul>
     * @param _msg
     *            the msg
     */
    public void println_warn_verbose(final String _msg) {
        if (!isVerbose()) {
            return;
        }
        getErr().println(StringUtils.colorizeForTerminal(_msg, TerminalColor.YELLOW));
    }
}
