/*
 * 
 */
package com.github.theprez.jcmdutils;

/**
 * Simple implementation of a Java {@link #main(String[])} that does a small sanity check of this library
 */
public class SanityCheck {

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(final String[] args) {
        final AppLogger logger = AppLogger.getSingleton(true);
        try {
            final ConsoleQuestionAsker asker = new ConsoleQuestionAsker();
            final String questionResponse = asker.askNonEmptyStringQuestion(logger, "default response", "What do you think?");
            logger.printfln_warn("Your response was: '%s'", questionResponse);
            final String pw = asker.askUserForPwd("gimme your password:");
            logger.printfln_warn("Your password is: '%s'", pw);
            logger.println_success("SUCCESS!!");
        } catch (final Exception e) {
            logger.printExceptionStack_verbose(e);
        }
    }

}
