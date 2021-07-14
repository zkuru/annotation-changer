package com.latskap;

import java.util.HashMap;
import java.util.Map;

class ParsedArgs {
    private static final String CLASS = "class";
    private static final String METHOD = "method";
    private static final String INVOCATION_COUNT = "count";

    private final Map<String, String> parsedArgs;

    public ParsedArgs(String args) {
        parsedArgs = new HashMap<>();
        for (String s : args.split(",")) {
            String entry = s.trim();
            int separatorIndex = entry.indexOf("=");
            parsedArgs.put(entry.substring(0, separatorIndex).trim(), entry.substring(separatorIndex + 1).trim());
        }
    }

    public String getTargetClass() {
        return parsedArgs.get(CLASS);
    }

    public String getMethod() {
        return parsedArgs.get(METHOD);
    }

    public Integer getInvocationCount() {
        return new Integer(parsedArgs.get(INVOCATION_COUNT));
    }
}