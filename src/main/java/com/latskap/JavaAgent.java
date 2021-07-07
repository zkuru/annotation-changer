package com.latskap;

import java.lang.instrument.Instrumentation;

public class JavaAgent {
    public static void premain(String args, Instrumentation inst) {
        System.out.println("com.latskap.JavaAgent");
    }
}
