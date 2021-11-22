package com.github.theprez.jcmdutils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import com.github.theprez.jcmdutils.StringUtils.TerminalColor;

public class ConsoleQuestionAsker {

    private static ConsoleQuestionAsker m_singleton = new ConsoleQuestionAsker();
    public static ConsoleQuestionAsker get() {
        return m_singleton;
    }

    private final Console m_sysConsole;

    public ConsoleQuestionAsker() {
        m_sysConsole = System.console();
        if (null == m_sysConsole) {
            throw new RuntimeException("ERROR: Unable to allocate console for user input");
        }
    }

    public boolean askBooleanQuestion(final AppLogger _logger, final String _dft, final String _fmt, final Object... _args) {
        return askNonEmptyStringQuestion(_logger, _dft, _fmt, _args).matches("(?i)^(y.*|(tr).*|[1-9]+.*)$");
    }

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

    public int askIntQuestion(final AppLogger _logger, final Integer _dft, final String _fmt, final Object... _args) {
        return Integer.valueOf(askStringMatchingRegexQuestion(_logger, (null == _dft ? null : "" + _dft), "^[0-9]+$", "an integer value", _fmt, _args));
    }

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

    public String askStringMatchingRegexQuestion(final AppLogger _logger, final String _dft, final String _regex, final String _regexDesc, final String _fmt, final Object... _args) {
        while (true) {
            final String response = askNonEmptyStringQuestion(_logger, _dft, _fmt, _args);
            if (response.matches(_regex)) {
                return response;
            }
            _logger.printfln_err("User response does not match criteria. Must be '%s'.", _regexDesc);
        }
    }

    public String askStringQuestion(final AppLogger _logger, final String _dft, final String _fmt, final Object... _args) {
        final String response = readLine(_fmt + " ", _args);
        if (StringUtils.isEmpty(response) && !StringUtils.isEmpty(_dft)) {
            return _dft.trim();
        }
        return response.trim();
    }

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

    public String askUserOrThrow(final String _question) throws IOException {
        final String resp = askUser(_question);
        if (StringUtils.isEmpty(resp)) {
            throw new IOException("No response provided");
        }
        return resp.trim();
    }

    public String askUserWithDefault(final String _question, final String _default) throws IOException {
        final String response = askUser(_question).trim();
        return StringUtils.isEmpty(response) ? _default : response;
    }

    private String readLine(final String _fmt, final Object... _args) {
        return m_sysConsole.readLine(StringUtils.colorizeForTerminal(_fmt, TerminalColor.GREEN), _args);
    }
}
