/*
 * 
 */
package com.github.theprez.jcmdutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import com.github.theprez.jcmdutils.StringUtils.TerminalColor;

/**
 * Makes it easier to launch processes and process their output
 */
public class ProcessLauncher {

    /**
     * Encapsulates the result of a process invocation.
     */
    public static class ProcessResult {
        
        private final int m_exitStatus;
        
        private final List<String> m_stderr;
        
        private final List<String> m_stdout;

         ProcessResult(final List<String> m_stdout, final List<String> m_stderr, final int m_exitStatus) {
            super();
            this.m_stdout = m_stdout;
            this.m_stderr = m_stderr;
            this.m_exitStatus = m_exitStatus;
        }

        /**
         * Gets the exit status.
         *
         * @return the exit status
         */
        public int getExitStatus() {
            return m_exitStatus;
        }

        /**
         * Gets the stderr.
         *
         * @return the stderr
         */
        public List<String> getStderr() {
            return m_stderr;
        }

        /**
         * Gets the stdout.
         *
         * @return the stdout
         */
        public List<String> getStdout() {
            return m_stdout;
        }

        /**
         * Pretty print, formatting the stdout in green and the stderr in red. Note that this will always
         * be ordered such that all of the stdout precedes all of the stderr, regardless of the process's 
         * output order.
         */
        public void prettyPrint() {
            for (final String stdout : m_stdout) {
                System.out.println(StringUtils.colorizeForTerminal(stdout, TerminalColor.GREEN));
            }
            for (final String stderr : m_stderr) {
                System.out.println(StringUtils.colorizeForTerminal(stderr, TerminalColor.BRIGHT_RED));
            }
        }

    }

    /**
     * Execute a command!
     *
     * @param _cmd the cmd
     * @return the process result
     * @throws UnsupportedEncodingException the unsupported encoding exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static ProcessResult exec(final String _cmd) throws UnsupportedEncodingException, IOException {
        final Process p = Runtime.getRuntime().exec(_cmd);
        final List<String> stdout = new LinkedList<String>();
        final List<String> stderr = new LinkedList<String>();
        final Thread stderrThread = new Thread() {
            @Override
            public void run() {

                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream(), "UTF-8"))) {
                    String line;
                    while (null != (line = br.readLine())) {
                        stderr.add(line);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            };
        };
        stderrThread.setDaemon(true);
        stderrThread.start();
        p.getOutputStream().close();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"))) {
            String line;
            while (null != (line = br.readLine())) {
                stdout.add(line);
            }
        }
        int rc;
        try {
            rc = p.waitFor();
        } catch (final InterruptedException e) {
            throw new IOException(e);
        }
        return new ProcessResult(stdout, stderr, rc);
    }
    
    public static ProcessResult exec(final String... _cmd) throws UnsupportedEncodingException, IOException {
        final Process p = Runtime.getRuntime().exec(_cmd);
        final List<String> stdout = new LinkedList<String>();
        final List<String> stderr = new LinkedList<String>();
        final Thread stderrThread = new Thread() {
            @Override
            public void run() {

                try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream(), "UTF-8"))) {
                    String line;
                    while (null != (line = br.readLine())) {
                        stderr.add(line);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            };
        };
        stderrThread.setDaemon(true);
        stderrThread.start();
        p.getOutputStream().close();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"))) {
            String line;
            while (null != (line = br.readLine())) {
                stdout.add(line);
            }
        }
        int rc;
        try {
            rc = p.waitFor();
        } catch (final InterruptedException e) {
            throw new IOException(e);
        }
        return new ProcessResult(stdout, stderr, rc);
    }

    /**
     * Utility function to run a process and return the stdout. Note that it will also log this info in the {@link AppLogger} instance that is passed in
     *
     * @param _eyecatcher the eyecatcher
     * @param _p the p
     * @param _logger the logger
     * @return the stdout
     * @throws UnsupportedEncodingException the unsupported encoding exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static List<String> getStdout(final String _eyecatcher, final Process _p, final AppLogger _logger) throws UnsupportedEncodingException, IOException {
        final List<String> ret = new LinkedList<String>();
        final Thread stderrThread = new Thread() {
            @Override
            public void run() {
                handleStream(_eyecatcher, _p.getErrorStream(), _logger, true);
            };
        };
        stderrThread.setDaemon(true);
        stderrThread.start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(_p.getInputStream(), "UTF-8"))) {
            String line;
            while (null != (line = br.readLine())) {
                ret.add(line);
            }
        }
        return ret;
    }
    
    private static void handleStream(final String _eyeCatcher, final InputStream _stream, final AppLogger _logger, final boolean _isError) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(_stream))) {
            String read;
            while (null != (read = br.readLine())) {
                synchronized (_logger) {
                    if (_isError) {
                        _logger.println_err_verbose("child process " + _eyeCatcher + ":" + read);
                    } else {
                        _logger.println_verbose("child process " + _eyeCatcher + ":" + read);
                    }
                }
            }
        } catch (final IOException e) {
            synchronized (_logger) {
                _logger.exception(e);
            }
        }
    }

    /**
     * Run the process, but route the child's stdout and stderr to this process
     *
     * @param _eyecatcher the eyecatcher
     * @param _p the p
     * @param _logger the logger
     */
    public static void pipeStreamsToCurrentProcess(final String _eyecatcher, final Process _p, final AppLogger _logger) {
        final Thread stderrThread = new Thread() {
            @Override
            public void run() {
                handleStream(_eyecatcher, _p.getErrorStream(), _logger, true);
            };
        };
        stderrThread.setDaemon(true);
        stderrThread.start();
        final Thread stdoutThread = new Thread() {
            @Override
            public void run() {
                handleStream(_eyecatcher, _p.getInputStream(), _logger, false);
            };
        };
        stdoutThread.setDaemon(true);
        stdoutThread.start();
    }
}
