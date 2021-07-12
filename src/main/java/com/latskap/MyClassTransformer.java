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
    private Class<?> targetClass;
    private ClassLoader targetClassLoader;
    private String methodName;
    private Integer invocationCount;

    public MyClassTransformer(Class<?> targetClass, ClassLoader targetClassLoader, String methodName,
                              Integer invocationCount) {
        this.targetClass = targetClass;
        this.targetClassLoader = targetClassLoader;
        this.methodName = methodName;
        this.invocationCount = invocationCount;
    }

    public MyClassTransformer() {}

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] byteCode = classfileBuffer;
        String targetClassName = targetClass.getName();
        String finalTargetClassName = targetClassName
                .replaceAll("\\.", "/");
        if (className.equals(finalTargetClassName) && loader.equals(targetClassLoader)) {
            System.out.println("Agent in MyClassTransformer.");
            System.out.println("A1");
            ClassPool cp = null;
            try {
                cp = ClassPool.getDefault();
                System.out.println("A2");
            } catch (Exception e) {
                e.printStackTrace();
            }
//            cp.appendClassPath(new LoaderClassPath(loader));
//            System.out.println("A2");
            try {
                CtClass cc = cp.get(targetClassName);
                CtMethod m = cc.getDeclaredMethod(methodName);
                MethodInfo methodInfo = m.getMethodInfo();
                AnnotationsAttribute attr = (AnnotationsAttribute)
                        methodInfo.getAttribute(AnnotationsAttribute.visibleTag);
                System.out.println(attr.getAnnotations().length);
                System.out.println(Arrays.toString(attr.getAnnotations()));

//                Annotation an = attr.getAnnotation("org.testng.annotations.Test");
//                IntegerMemberValue memberValue = (IntegerMemberValue) an.getMemberValue("invocationCount");
//                memberValue.setValue(100);
//
//                System.out.println(((IntegerMemberValue) attr.getAnnotation("org.testng.annotations.Test")
//                        .getMemberValue("invocationCount")).getValue());


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
