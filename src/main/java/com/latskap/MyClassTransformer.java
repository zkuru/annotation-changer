package com.latskap;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class MyClassTransformer implements ClassFileTransformer {
    private final Class<?> targetClass;
    private final ClassLoader targetClassLoader;
    private final String methodName;
    private final Integer invocationCount;

    public MyClassTransformer(Class<?> targetClass, ClassLoader targetClassLoader, String methodName,
                              Integer invocationCount) {
        this.targetClass = targetClass;
        this.targetClassLoader = targetClassLoader;
        this.methodName = methodName;
        this.invocationCount = invocationCount;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        String targetClassName = targetClass.getName();
        String finalTargetClassName = targetClassName
                .replaceAll("\\.", "/");
        if (className.equals(finalTargetClassName) && loader.equals(targetClassLoader)) {
            ClassPool cp = null;
            try {
                cp = ClassPool.getDefault();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                CtClass cc = cp.get(targetClassName);
                CtMethod m = cc.getDeclaredMethod(methodName);
                MethodInfo methodInfo = m.getMethodInfo();
                AnnotationsAttribute attr = (AnnotationsAttribute)
                        methodInfo.getAttribute(AnnotationsAttribute.visibleTag);

                ClassFile classFile = cc.getClassFile();
                ConstPool constPool = classFile.getConstPool();
                Annotation a = new Annotation("org.testng.annotations.Test", constPool);
                a.addMemberValue("invocationCount", new IntegerMemberValue(constPool, invocationCount));
                attr.setAnnotation(a);
                classFile.addAttribute(attr);
                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return byteCode;
    }
}