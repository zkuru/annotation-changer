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
import java.util.Optional;

public class TestAnnotationTransformer implements ClassFileTransformer {
    private static final String INVOCATION_COUNT = "invocationCount";

    private final Class<?> targetClass;
    private final ParsedArgs parsedArgs;

    public TestAnnotationTransformer(Class<?> targetClass, ParsedArgs parsedArgs) {
        this.targetClass = targetClass;
        this.parsedArgs = parsedArgs;
    }

    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        return isTargetClass(classLoader, className) ? transformClass() : null;
    }

    private boolean isTargetClass(ClassLoader loader, String className) {
        return className.equals(targetClass.getName().replaceAll("\\.", "/"))
                && loader.equals(targetClass.getClassLoader());
    }

    private byte[] transformClass() {
        Optional<CtClass> ctClassOptional = getCtClass();
        if (ctClassOptional.isPresent()) {
            CtClass ctClass = ctClassOptional.get();
            ClassFile classFile = ctClass.getClassFile();
            if (parsedArgs.isMethodSelected())
                changeInvocationCountOfMethod(ctClass, classFile);
            else
                changeInvocationCountOfAllTestMethods(ctClass, classFile);
            return getClassByteCode(ctClass);
        }
        return null;
    }

    private Optional<CtClass> getCtClass() {
        ClassPool classPool = ClassPool.getDefault();
        try {
            return Optional.of(classPool.get(targetClass.getName()));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    private void changeInvocationCountOfMethod(CtClass ctClass, ClassFile classFile) {
        try {
            changeInvocationCountOfMethod(ctClass.getDeclaredMethod(parsedArgs.getMethod()), classFile);
        } catch (NotFoundException ignored) {}
    }

    private void changeInvocationCountOfAllTestMethods(CtClass ctClass, ClassFile classFile) {
        for (CtMethod m : ctClass.getDeclaredMethods())
            if (m.hasAnnotation(Test.class))
                changeInvocationCountOfMethod(m, classFile);
    }

    private void changeInvocationCountOfMethod(CtMethod method, ClassFile classFile) {
        MethodInfo methodInfo = method.getMethodInfo();
        AnnotationsAttribute attribute = (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag);
        ConstPool constPool = classFile.getConstPool();
        Annotation annotation = new Annotation(Test.class.getTypeName(), constPool);
        annotation.addMemberValue(INVOCATION_COUNT, new IntegerMemberValue(constPool, parsedArgs.getInvocationCount()));
        attribute.addAnnotation(annotation);
        classFile.addAttribute(attribute);
    }

    private static byte[] getClassByteCode(CtClass ctClass) {
        try {
            return ctClass.toBytecode();
        } catch (IOException | CannotCompileException e) {
            return null;
        }
    }
}