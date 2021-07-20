package com.latskap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Optional;

public class AnnotationChangerJavaAgent {
    private static final Logger log = LoggerFactory.getLogger(AnnotationChangerJavaAgent.class);

    public static void premain(String args, Instrumentation instrumentation) {
        ParsedArgs parsedArgs = new ParsedArgs(args);
        try {
            transform(parsedArgs, instrumentation);
        } catch (Exception e) {
            log.error("Not possible to change current run configuration." + e.getMessage());
        }
    }

    private static void transform(ParsedArgs parsedArgs, Instrumentation instrumentation) {
        getClassByName(parsedArgs.getTargetClass(), instrumentation)
                .ifPresent(targetClass -> {
                            instrumentation.addTransformer(new MyClassTransformer(targetClass, parsedArgs), true);
                            try {
                                instrumentation.retransformClasses(targetClass);
                            } catch (UnmodifiableClassException e) {
                                log.error("Not possible to change current run configuration. " +
                                        "Can not transform class: [" + targetClass.getName() + "]." + e.getMessage());
                            }
                        }
                );
    }

    private static Optional<Class<?>> getClassByName(String className, Instrumentation instrumentation) {
        try {
            return Optional.of(Class.forName(className));
        } catch (ClassNotFoundException e) {
            for (Class<?> clazz : instrumentation.getAllLoadedClasses())
                if (clazz.getName().equals(className))
                    return Optional.of(clazz);
        }
        return Optional.empty();
    }
}