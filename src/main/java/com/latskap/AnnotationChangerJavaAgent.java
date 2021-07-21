package com.latskap;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class AnnotationChangerJavaAgent {

    public static void premain(String args, Instrumentation instrumentation) {
        try {
            transform(new ParsedArgs(args), instrumentation);
        } catch (Exception ignored) {
        }
    }

    private static void transform(ParsedArgs parsedArgs, Instrumentation instrumentation) throws UnmodifiableClassException {
        Class<?> targetClass = getClassByName(parsedArgs.getTargetClass(), instrumentation);
        instrumentation.addTransformer(new TestAnnotationTransformer(targetClass, parsedArgs), true);
        instrumentation.retransformClasses(targetClass);
    }

    private static Class<?> getClassByName(String className, Instrumentation instrumentation) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            for (Class<?> cl : instrumentation.getAllLoadedClasses())
                if (cl.getName().equals(className))
                    return cl;
        }
        throw new RuntimeException("Failed to find class [" + className + "].");
    }
}