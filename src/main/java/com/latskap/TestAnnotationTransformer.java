package com.latskap;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.IntegerMemberValue;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class TestAnnotationTransformer implements ClassFileTransformer {
    private static final String TEST_ANNOTATION = "org.testng.annotations.Test";
    private static final String INVOCATION_COUNT = "invocationCount";

    private final Class<?> targetClass;
    private final ParsedArgs parsedArgs;

    public TestAnnotationTransformer(Class<?> targetClass, ParsedArgs parsedArgs) {
        this.targetClass = targetClass;
        this.parsedArgs = parsedArgs;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        byte[] byteCode = classfileBuffer;
        String targetClassName = targetClass.getName();
        String finalTargetClassName = targetClassName.replaceAll("\\.", "/");
        if (className.equals(finalTargetClassName) && loader.equals(targetClass.getClassLoader())) {
            ClassPool cp = ClassPool.getDefault();
            CtClass cc;
            ClassFile classFile;
            try {
                cc = cp.get(targetClassName);
            } catch (NotFoundException e) {
                return byteCode;
            }

            classFile = cc.getClassFile();
            if (parsedArgs.isMethodSelected())
                changeInvocationCountOfMethod(cc, classFile);
            else
                changeInvocationCountOfAllTestMethods(cc, classFile);

            byteCode = getClassByteCode(cc, byteCode);
            cc.detach();
        }
        return byteCode;
    }

    private void changeInvocationCountOfMethod(CtClass cc, ClassFile classFile) {
        try {
            changeInvocationCountOfMethod(cc.getDeclaredMethod(parsedArgs.getMethod()), classFile);
        } catch (NotFoundException ignored) {}
    }

    private void changeInvocationCountOfAllTestMethods(CtClass cc, ClassFile classFile) {
        for (CtMethod m : cc.getDeclaredMethods())
            if (m.hasAnnotation(Test.class))
                changeInvocationCountOfMethod(m, classFile);
    }

    private void changeInvocationCountOfMethod(CtMethod method, ClassFile classFile) {
        MethodInfo methodInfo = method.getMethodInfo();
        AnnotationsAttribute attribute = (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag);
        ConstPool constPool = classFile.getConstPool();
        Annotation annotation = new Annotation(TEST_ANNOTATION, constPool);
        annotation.addMemberValue(INVOCATION_COUNT, new IntegerMemberValue(constPool, parsedArgs.getInvocationCount()));
        attribute.addAnnotation(annotation);
        classFile.addAttribute(attribute);
    }

    private static byte[] getClassByteCode(CtClass cc, byte[] classfileBuffer) {
        try {
            return cc.toBytecode();
        } catch (IOException | CannotCompileException e) {
            return classfileBuffer;
        }
    }
}