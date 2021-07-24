package com.latskap;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Optional;

public class AnnotationChangerJavaAgent {

    public static void premain(String args, Instrumentation instrumentation) {
        try {
            transform(new ParsedArgs(args), instrumentation);
        } catch (Exception ignored) {}
    }

    private static void transform(ParsedArgs parsedArgs, Instrumentation instrumentation) throws UnmodifiableClassException {
        Optional<Class<?>> classOptional = getClassByName(parsedArgs.getTargetClass(), instrumentation);
        if (classOptional.isPresent()) {
            Class<?> targetClass = classOptional.get();
            instrumentation.addTransformer(new TestAnnotationTransformer(targetClass, parsedArgs), true);
            instrumentation.retransformClasses(targetClass);
        }
    }

    private static Optional<Class<?>> getClassByName(String className, Instrumentation instrumentation) {
        try {
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException e) {
            for (Class<?> cl : instrumentation.getAllLoadedClasses())
                if (cl.getName().equals(className))
                    return Optional.of(cl);
        }
        return Optional.empty();
    }
}