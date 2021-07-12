package com.latskap;

import java.lang.instrument.Instrumentation;

public class JavaAgent {
    public static void premain(String args, Instrumentation inst) {
        System.out.println("Args passed: " + args);
        String[] argsArray = args.split(",");
        String className = argsArray[0];
        System.out.println("ClassName: " + className);
        String methodName = null;
        if (argsArray.length > 1) {
            methodName = argsArray[1];
            System.out.println("MethodName: " + methodName);
        }
        transform(className, methodName, inst, new Integer(argsArray[2]));//binaryTree.TreeNodeTest
    }

    public static void transform(String className, String methodName, Instrumentation instrumentation, Integer invocationCount) {
        Class<?> targetCls;
        ClassLoader targetClassLoader;
        // see if we can get the class using forName
        try {
            targetCls = Class.forName(className);
            targetClassLoader = targetCls.getClassLoader();
            System.out.println("Found requested class with Class.forName.");
            transform(targetCls, targetClassLoader, instrumentation, methodName, invocationCount);
            return;
        } catch (Exception ex) {
            System.out.println("Can't get with Class.forName");
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