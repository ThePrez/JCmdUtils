# JCmdUtils
Small Utilities for helping build Java command line programs

## Functionality
The helper classes in this module can be summarized as providing the following high-level function:
- Make it easier to ask the user questions (or ask for passwords)
- Provide a primitive logging mechanism (though log4j is likely a better choice for nontrivial applications)
- Provide an interface that makes it easy to handle which output is shown and which is hidden when running in verbose mode (many command line utilities have a `-v`, for instance)
- Provide colorization (red, green, yellow) when the terminal supports it.
- Make it easier to call processes and handle output
- Do simple string manipulation/checking that is commonly needed for command line programs

### AppLogger
Instantiate a logger instance via the `getSingleton()` method. Upon first invocation of this method,
it is determined whether the global singleton object is running in verbose mode. 
```java
final AppLogger logger = AppLogger.getSingleton(isVerbose);
```
From there, you can use the `print` functions to provide colorized output to the user
```java
// This will print in red text to standard error
logger.printfln_err("Uh oh! The object '%s' does not exist", objectName);
// This will print in yellow text to standard out
logger.printfln_warn("Warning: duplicate entries exist: '%s' and '%s'", objectName, objectName2);
// This will print in default color to standard out
logger.println("Proceeding to step 2...");        
// This will print in green text to standard out
logger.println_success("SUCCESS!!");
```
To do something similar, but to only show the output in verbose mode, use the `_verbose` variants.
If not running in verbose mode, the output will not be shown.
```java
// This will print in red text to standard error only if running in verbose mode
logger.printfln_err_verbose("Uh oh! The object '%s' does not exist", objectName);
// This will print in yellow text to standard out only if running in verbose mode
logger.printfln_warn_verbose("Warning: duplicate entries exist: '%s' and '%s'", objectName, objectName2);
// This will print in default color to standard out only if running in verbose mode
logger.println_verbose("Proceeding to step 2...");        
// This will print in default color to standard out only if running in verbose mode
logger.println_verbose("SUCCESS!!");
```

### ConsoleQuestionAsker 

### ProcessLauncher

### StringUtils
