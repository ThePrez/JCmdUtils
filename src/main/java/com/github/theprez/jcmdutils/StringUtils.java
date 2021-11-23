/*
 * 
 */
package com.github.theprez.jcmdutils;

import java.util.UUID;

/**
 * Simple string utilities. Not much here.
 *
 * @author Jesse Gorzinski
 *
 */
public class StringUtils {

    /**
     * The Enum TerminalColor.
     */
    public enum TerminalColor {

        /** The blue. */
        BLUE("\u001b[34m"),
        /** The bright red. */
        BRIGHT_RED("\u001b[31;1m"),
        /** The cyan. */
        CYAN("\u001B[36m"),
        /** The green. */
        GREEN("\u001B[32m"),
        /** The purple. */
        PURPLE("\u001B[35m"),
        /** The red. */
        RED("\u001B[31m"),
        /** The white. */
        WHITE("\u001B[37m"),
        /** The yellow. */
        YELLOW("\u001B[33m");

        /**
         * Strip terminal color codes from a string that may have them.
         *
         * @param _str
         *            the str
         * @return the string
         */
        public static String stripCodesFromString(final String _str) {
            if (!s_isTerminalColorsSupported) {
                return _str;
            }
            String ret = _str;
            for (final TerminalColor color : values()) {
                ret = ret.replace(color.m_code, "");
            }
            ret.replace(TERM_COLOR_RESET, "");
            return ret;
        }

        private final String m_code;

        TerminalColor(final String _code) {
            m_code = _code;
        }

        String getCode() {
            return m_code;
        }
    }

    private static final String LOTSA_SPACES = "                                             ";

    /** System property that can be used for disabling terminal colorizations. */
    public static final String PROP_DISABLE_COLORS = "jcmdutils.disablecolors";

    // We can print emojis (maybe) if we're running in a UTF-8 SSH terminal
    private static final boolean s_isEmojiSupported = (null != System.console() && !isEmpty(System.getenv("SSH_TTY")) && System.getProperty("file.encoding", "").equalsIgnoreCase("UTF-8"));

    // SSH_TTY will be unset in non-SSH environments, and System.console() returns null when output is being piped
    private static final boolean s_isTerminalColorsSupported = (null != System.console() && !isEmpty(System.getenv("SSH_TTY")) && !Boolean.getBoolean(PROP_DISABLE_COLORS));

    private static final String TERM_COLOR_RESET = "\u001B[0m";

    /**
     * Convert an array of objects (or a variable-length argument list) to a space-separated string.
     *
     * @param <T>
     *            the generic type
     * @param _arr
     *            the arr
     * @return the string
     */
    @SafeVarargs
    public static <T extends Object> String arrayToSpaceSeparatedString(final T... _arr) {
        final StringBuilder ret = new StringBuilder();
        for (final T o : _arr) {
            final String str = ("" + o);
            if (str.matches("^[\\w\\.\\-]+$")) {
                ret.append(str);
            } else {
                final char delim = str.contains("'") ? '\"' : '\'';
                ret.append("" + delim + str + delim);
            }
            ret.append(' ');
        }
        return ret.toString().trim();
    }

    /**
     * Surround the given string with terminal color codes, if supported. Can be disabled by setting the {@value #PROP_DISABLE_COLORS} system property to <tt>true</tt>
     *
     * @param _str
     *            the str
     * @param _color
     *            the color
     * @return the string
     */
    public static String colorizeForTerminal(final String _str, final TerminalColor _color) {
        if (s_isTerminalColorsSupported) {
            return _color.getCode() + _str + TERM_COLOR_RESET;
        } else {
            return _str;
        }
    }

    /**
     * Generate random string.
     *
     * @param _len
     *            the length of string to generate
     * @return the string
     */
    public static String generateRandomString(final int _len) {
        String ret = "";
        while (ret.length() < _len) {
            ret += UUID.randomUUID().toString().replace("-", "");
        }
        return ret.substring(0, _len);
    }

    /**
     * Gets the shrug emoji.
     *
     * @return the shrug for output
     */
    public static String getShrugForOutput() {
        return s_isEmojiSupported ? "¯\\_\uD83D\uDE00_/¯" : "<unknown>";
    }

    /**
     * Checks if the given string is empty (containing only whitespace) or null
     *
     * @param _str
     *            the str
     * @return true, if is empty
     */
    public static boolean isEmpty(final char[] _str) {
        return null == _str || isEmpty(new String(_str));
    }

    /**
     * Checks if the given string is empty (containing only whitespace) or null
     *
     * @param _str
     *            the str
     * @return true, if is empty
     */
    public static boolean isEmpty(final String _str) {
        return (null == _str) || (_str.trim().isEmpty());
    }

    /**
     * Checks if the given string is not empty (not null and containing non-whitespace characters)
     *
     * @param _str
     *            the str
     * @return true, if is non empty
     */
    public static boolean isNonEmpty(final String _str) {
        return !isEmpty(_str);
    }

    /**
     * Checks if the given string is not empty (not null and containing non-whitespace characters)
     *
     * @param _str
     *            the str
     * @return true, if is non empty
     */
    public static boolean isNonEmpty(final char[] _str) {
        return !isEmpty(_str);
    }

    /**
     * Pad the given string with spaces until it is the provided length.
     *
     * @param _str
     *            the string
     * @param _len
     *            the length
     * @return the space-padded string
     */
    public static String spacePad(final String _str, final int _len) {
        if (0 == _len) {
            return "";
        }
        String ret = _str + LOTSA_SPACES;
        while (ret.length() < _len) {
            ret += LOTSA_SPACES;
        }
        return ret.substring(0, _len);
    }
}
