package com.latskap;

import org.junit.Test;

import static org.junit.Assert.*;

public class ParsedArgsTest {
    @Test
    public void parsesArgs_ifClassAndMethodAndCountPassed() {
        ParsedArgs parsedArgs = new ParsedArgs("class=com.bar.MyClass,method=test,count=9");
        assertEquals("com.bar.MyClass", parsedArgs.getTargetClass());
        assertEquals("test", parsedArgs.getMethod());
        assertEquals(9, parsedArgs.getInvocationCount().intValue());
    }

    @Test
    public void parsingArgs_trimsArgsEntries() {
        ParsedArgs parsedArgs = new ParsedArgs(" class=org.foo.SomeClassCreator, method=testing_smt , count=1098 ");
        assertEquals("org.foo.SomeClassCreator", parsedArgs.getTargetClass());
        assertEquals("testing_smt", parsedArgs.getMethod());
        assertEquals(1098, parsedArgs.getInvocationCount().intValue());
    }

    @Test
    public void parsingArgs_trimsArgsValues() {
        ParsedArgs parsedArgs = new ParsedArgs("class= org.bar.TransformerImpl, method= returns_if_when , count= 78 ");
        assertEquals("org.bar.TransformerImpl", parsedArgs.getTargetClass());
        assertEquals("returns_if_when", parsedArgs.getMethod());
        assertEquals(78, parsedArgs.getInvocationCount().intValue());
    }

    @Test
    public void parsingArgs_trimsArgsKeys() {
        ParsedArgs parsedArgs = new ParsedArgs("class =org.bar.TransformerImpl, method =returns_if_when, count=78");
        assertEquals("org.bar.TransformerImpl", parsedArgs.getTargetClass());
        assertEquals("returns_if_when", parsedArgs.getMethod());
        assertEquals(78, parsedArgs.getInvocationCount().intValue());
    }

    @Test
    public void parsingArgs_definesWhetherMethodSelected() {
        assertTrue(new ParsedArgs("class =Foo, method=test_test, count=78").isMethodSelected());
        assertFalse(new ParsedArgs("class =Foo, method=, count=78").isMethodSelected());
        assertFalse(new ParsedArgs("class =Foo, method = , count=78").isMethodSelected());
        assertFalse(new ParsedArgs("class =Foo,count=78").isMethodSelected());
    }

    // todo can be empty params?

    // todo to pass package
}