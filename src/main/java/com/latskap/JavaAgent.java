package com.latskap;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class JavaAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        ParsedArgs parsedArgs = new ParsedArgs(args);
        transform(parsedArgs, instrumentation);
    }

    private static void transform(ParsedArgs parsedArgs, Instrumentation instrumentation) {
        Class<?> targetClass = getClassByName(parsedArgs.getTargetClass(), instrumentation);
        MyClassTransformer myClassTransformer = new MyClassTransformer(targetClass, parsedArgs);
        instrumentation.addTransformer(myClassTransformer, true);
        try {
            instrumentation.retransformClasses(targetClass);
        } catch (UnmodifiableClassException e) {
            throw new RuntimeException("Can not transform class: [" + targetClass.getName() + "].", e);
        }
    }

    private static Class<?> getClassByName(String className, Instrumentation instrumentation) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            for (Class<?> clazz : instrumentation.getAllLoadedClasses())
                if (clazz.getName().equals(className))
                    return clazz;
        }
        throw new RuntimeException("Failed to find class [" + className + "].");
    }
}