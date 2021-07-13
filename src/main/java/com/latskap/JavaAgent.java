package com.latskap;

import java.lang.instrument.Instrumentation;

public class JavaAgent {
    public static void premain(String args, Instrumentation inst) {
        System.out.println("Args passed: " + args);
        String[] argsArray = args.split(",");
        String className = argsArray[0];
        String methodName = null;
        if (argsArray.length > 1) {
            methodName = argsArray[1];
        }
        transform(className, methodName, inst, new Integer(argsArray[2]));
    }

    public static void transform(String className, String methodName, Instrumentation instrumentation, Integer invocationCount) {
        Class<?> targetCls;
        ClassLoader targetClassLoader;
        try {
            targetCls = Class.forName(className);
            targetClassLoader = targetCls.getClassLoader();
            transform(targetCls, targetClassLoader, instrumentation, methodName, invocationCount);
            return;
        } catch (Exception ex) {
            // otherwise iterate all loaded classes and find what we want
            for(Class<?> clazz: instrumentation.getAllLoadedClasses()) {
                if(clazz.getName().equals(className)) {
                    targetCls = clazz;
                    targetClassLoader = targetCls.getClassLoader();
                    transform(targetCls, targetClassLoader, instrumentation, methodName, invocationCount);
                    return;
                }
            }
        }
        throw new RuntimeException("Failed to find class [" + className + "]");
    }

    private static void transform(Class<?> clazz, ClassLoader classLoader, Instrumentation instrumentation,
                                  String methodName, Integer invocationCount) {
        MyClassTransformer myClassTransformer = new MyClassTransformer(clazz, classLoader, methodName, invocationCount);
        instrumentation.addTransformer(myClassTransformer, true);
        try {
            instrumentation.retransformClasses(clazz);
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Transform failed for: [" + clazz.getName() + "]", ex);
        }
    }
}