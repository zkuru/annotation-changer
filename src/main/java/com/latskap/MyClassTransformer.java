package com.latskap;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.IntegerMemberValue;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class MyClassTransformer implements ClassFileTransformer {
    private static final String TEST_ANNOTATION = "org.testng.annotations.Test";
    private static final String INVOCATION_COUNT = "invocationCount";

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
        String finalTargetClassName = targetClassName.replaceAll("\\.", "/");
        if (className.equals(finalTargetClassName) && loader.equals(targetClass.getClassLoader())) {
            ClassPool cp = null;
            try {
                cp = ClassPool.getDefault();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                CtClass cc = cp.get(targetClassName);
                ClassFile classFile = cc.getClassFile();

                if (parsedArgs.isMethodSelected())
                    changeInvocationCountOfMethod(cc.getDeclaredMethod(parsedArgs.getMethod()), classFile);
                else
                    for (CtMethod m : cc.getDeclaredMethods())
                        changeInvocationCountOfMethod(m, classFile);

                byteCode = cc.toBytecode();
                cc.detach();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return byteCode;
    }

    private void changeInvocationCountOfMethod(CtMethod method, ClassFile classFile) {
        MethodInfo methodInfo = method.getMethodInfo();
        AnnotationsAttribute attr = (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag);
        ConstPool constPool = classFile.getConstPool();
        Annotation a = new Annotation(TEST_ANNOTATION, constPool);
        a.addMemberValue(INVOCATION_COUNT, new IntegerMemberValue(constPool, parsedArgs.getInvocationCount()));
        attr.setAnnotation(a);
        classFile.addAttribute(attr);
    }
}