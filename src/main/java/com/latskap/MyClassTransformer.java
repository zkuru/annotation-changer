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
    private final ParsedArgs parsedArgs;

    public MyClassTransformer(Class<?> targetClass, ParsedArgs parsedArgs) {
        this.targetClass = targetClass;
        this.parsedArgs = parsedArgs;
    }


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        byte[] byteCode = classfileBuffer;
        String targetClassName = targetClass.getName();
        String finalTargetClassName = targetClassName
                .replaceAll("\\.", "/");
        if (className.equals(finalTargetClassName) && loader.equals(targetClass.getClassLoader())) {
            ClassPool cp = null;
            try {
                cp = ClassPool.getDefault();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                CtClass cc = cp.get(targetClassName);
                CtMethod m = cc.getDeclaredMethod(parsedArgs.getMethod());
                MethodInfo methodInfo = m.getMethodInfo();
                AnnotationsAttribute attr = (AnnotationsAttribute)
                        methodInfo.getAttribute(AnnotationsAttribute.visibleTag);

                ClassFile classFile = cc.getClassFile();
                ConstPool constPool = classFile.getConstPool();
                Annotation a = new Annotation("org.testng.annotations.Test", constPool);
                a.addMemberValue("invocationCount", new IntegerMemberValue(constPool, parsedArgs.getInvocationCount()));
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