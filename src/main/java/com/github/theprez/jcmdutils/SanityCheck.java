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
        boolean isVerbose = false;
        final AppLogger logger = AppLogger.getSingleton(true);
        String objectName = "test";
        String objectName2 = "test2";
        // This will print in red text to standard error
        logger.printfln_err("Uh oh! The object '%s' does not exist", objectName);
        // This will print in yellow text to standard out
        logger.printfln_warn("Warning: duplicate entries exist: '%s' and '%s'", objectName, objectName2);
        // This will print in default color to standard out
        logger.println("Proceeding to step 2...");        
        // This will print in green text to standard out
        logger.println_success("SUCCESS!!");
        

        // This will print in red text to standard error only if running in verbose mode
        logger.printfln_err_verbose("Uh oh! The object '%s' does not exist", objectName);
        // This will print in yellow text to standard out only if running in verbose mode
        logger.printfln_warn_verbose("Warning: duplicate entries exist: '%s' and '%s'", objectName, objectName2);
        // This will print in default color to standard out only if running in verbose mode
        logger.println_verbose("Proceeding to step 2...");        
        // This will print in default color to standard out only if running in verbose mode
        logger.println_verbose("SUCCESS!!");
        
        
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
