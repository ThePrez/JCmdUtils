/*
 * 
 */
package com.github.theprez.jcmdutils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import com.github.theprez.jcmdutils.StringUtils.TerminalColor;


/**
 * A utility class for asking the user questions (including passwords). While an object
 * can be directly instantiated, the expected usage is to access functions through a
 * singleton object returned by {@link #get()}.
 */
public class ConsoleQuestionAsker {

    private static ConsoleQuestionAsker m_singleton = new ConsoleQuestionAsker();
    
    /**
     * Gets the global singleton.
     *
     * @return the console question asker
     */
    public static ConsoleQuestionAsker get() {
        return m_singleton;
    }

    private final Console m_sysConsole;

    /**
     * Instantiates a new console question asker.
     */
    public ConsoleQuestionAsker() {
        m_sysConsole = System.console();
        if (null == m_sysConsole) {
            throw new RuntimeException("ERROR: Unable to allocate console for user input");
        }
    }

    /**
     * Ask a question for whick the answer is a simple boolean
     *
     * @param _logger the logger
     * @param _dft the default response to be used if the user simply presses enter. This can be <code>null</code> if no default response. 
     * @param _fmt a format string (see {@link Formatter}
     * @param _args the arguments to the format string
     * @return the user response as a boolean
     */
    public boolean askBooleanQuestion(final AppLogger _logger, final String _dft, final String _fmt, final Object... _args) {
        return askNonEmptyStringQuestion(_logger, _dft, _fmt, _args).matches("(?i)^(y.*|(tr).*|[1-9]+.*)$");
    }

    /**
     * Ask a question, for which the response is one of the values of the given enum type class. Will keep asking
     * until user input matches the name of an enum value for the given type.
     *
     * @param <T> the enum type
     * @param _logger the logger
     * @param _question the question
     * @param _type the type
     * @return the user response
     */
    public <T extends Enum<T>> T askEnumQuestion(final AppLogger _logger, final String _question, final Class<T> _type) {
        final T[] constants = _type.getEnumConstants();
        final String fmt = _question + " (one of: " + StringUtils.arrayToSpaceSeparatedString(constants).replace(' ', '/') + ")";

        while (true) {
            final String response = askNonEmptyStringQuestion(_logger, null, fmt);
            for (final T constant : constants) {
                if (response.trim().equalsIgnoreCase(constant.name())) {
                    return constant;
                }
            }
            _logger.printfln_err("User response does not match criteria. Must be '%s'.", "one of: " + StringUtils.arrayToSpaceSeparatedString(constants).replace(' ', '/'));
        }
    }

    /**
     * Ask a question for whick the answer is a simple integer
     *
     * @param _logger the logger
     * @param _dft the default response to be used if the user simply presses enter. This can be <code>null</code> if no default response. 
     * @param _fmt a format string (see {@link Formatter}
     * @param _args the arguments to the format string
     * @return the user response
     */
    public int askIntQuestion(final AppLogger _logger, final Integer _dft, final String _fmt, final Object... _args) {
        return Integer.valueOf(askStringMatchingRegexQuestion(_logger, (null == _dft ? null : "" + _dft), "^[0-9]+$", "an integer value", _fmt, _args));
    }

    /**
     * Ask a question for which the response can be a list of strings. Will keep asking the user for more information
     * until the user indicates the end of input by way of a blank line.
     *
     * @param _logger the logger
     * @param _q the question
     * @return the user response
     */
    public List<String> askListOfStringsQuestion(final AppLogger _logger, final String _q) {
        m_sysConsole.writer().println(_q);
        m_sysConsole.writer().println("        (press <enter> after each entry, leave blank to entering values)");
        final java.util.List<String> ret = new LinkedList<String>();
        for (long i = 1; true; ++i) {
            final String response = readLine("" + i + "> ");
            if (StringUtils.isEmpty(response)) {
                break;
            }
            ret.add(response.trim());
        }
        return ret;
    }

    /**
     * Ask a question for which an empty string is not a valid response. Will keep asking until a non-empty user response is achieved.
     *
     * @param _logger the logger
     * @param _dft the default response to be used if the user simply presses enter. This can be <code>null</code> if no default response. 
     * @param _fmt a format string (see {@link Formatter}
     * @param _args the arguments to the format string
     * @return the user response
     */
    public String askNonEmptyStringQuestion(final AppLogger _logger, final String _dft, final String _fmt, final Object... _args) {
        String response = "";
        String fmt = _fmt + " ";
        if (!StringUtils.isEmpty(_dft)) {
            fmt += "[" + _dft + "] ";
        }
        while (StringUtils.isEmpty((response = readLine(fmt, _args)))) {
            if (!StringUtils.isEmpty(_dft)) {
                return _dft.trim();
            }
            _logger.println_warn("Empty response. Asking again");
        }
        return response;
    }

    /**
     * Ask a question for which the response must satisfy the given regular expression. 
     *
     * @param _logger the logger
     * @param _dft the default response to be used if the user simply presses enter. This can be <code>null</code> if no default response. 
     * @param _regex the regex
     * @param _regexDesc the description, shown to the user, of the criteria
     * @param _fmt a format string (see {@link Formatter}
     * @param _args the arguments to the format string
     * @return the user response
     */
    public String askStringMatchingRegexQuestion(final AppLogger _logger, final String _dft, final String _regex, final String _regexDesc, final String _fmt, final Object... _args) {
        while (true) {
            final String response = askNonEmptyStringQuestion(_logger, _dft, _fmt, _args);
            if (response.matches(_regex)) {
                return response;
            }
            _logger.printfln_err("User response does not match criteria. Must be '%s'.", _regexDesc);
        }
    }

    /**
     * Ask a question.
     *
     * @param _logger the logger
     * @param _dft the default response to be used if the user simply presses enter. This can be <code>null</code> if no default response. 
     * @param _fmt a format string (see {@link Formatter}
     * @param _args the arguments to the format string
     * @return the user response
     */
    public String askStringQuestion(final AppLogger _logger, final String _dft, final String _fmt, final Object... _args) {
        final String response = readLine(_fmt + " ", _args);
        if (StringUtils.isEmpty(response) && !StringUtils.isEmpty(_dft)) {
            return _dft.trim();
        }
        return response.trim();
    }

    /**
     * Ask a question. This is different from {@link #askStringQuestion(AppLogger, String, String, Object...)} as it lacks the ability to use printf-style formatting
     * or to have a default response.
     *
     * @param _question the question
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String askUser(final String _question) throws IOException {
        if (null == m_sysConsole) {
            System.out.print(_question);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
                return br.readLine();
            } catch (final Exception e) {
                throw new IOException(e);
            }
        } else {
            return m_sysConsole.readLine("%s", _question);
        }
    }

    /**
     * Ask user for a password. If the terminal supports password masking, masking will be used. Otherwise,
     * an {@link IOException} will signal that we cannot securely ask for a password.
     *
     * @param _prompt the prompt
     * @return the password
     * @throws IOException if we cannot securely ask for a password, or if user input was empty
     */
    public String askUserForPwd(final String _prompt) throws IOException {
        if (null == m_sysConsole) {
            throw new IOException("Can't securely ask for password property).");
        } else {
            final char[] pw = m_sysConsole.readPassword(_prompt);
            if (null == pw) {
                throw new IOException("Password not entered");
            }
            return new String(pw);
        }
    }

    /**
     * Ask user or throw an {@link IOException} if the user didn't provide a non-empty response. 
     * This is different from {@link #askStringQuestion(AppLogger, String, String, Object...)} as it lacks the ability to use printf-style formatting
     * or to have a default response.
     *
     * @param _question the question
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String askUserOrThrow(final String _question) throws IOException {
        final String resp = askUser(_question);
        if (StringUtils.isEmpty(resp)) {
            throw new IOException("No response provided");
        }
        return resp.trim();
    }

    /**
     * Ask user with the ability to have a default response.
     *
     * @param _question the question
     * @param _default the default
     * @return the user response, or default
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public String askUserWithDefault(final String _question, final String _default) throws IOException {
        final String response = askUser(_question).trim();
        return StringUtils.isEmpty(response) ? _default : response;
    }

    /**
     * Same as {@link #askUser(String)} except with format args, and the prompt will be formatted green.
     * @param _fmt
     * @param _args
     * @return
     */
    private String readLine(final String _fmt, final Object... _args) {
        return m_sysConsole.readLine(StringUtils.colorizeForTerminal(_fmt, TerminalColor.GREEN), _args);
    }
}
