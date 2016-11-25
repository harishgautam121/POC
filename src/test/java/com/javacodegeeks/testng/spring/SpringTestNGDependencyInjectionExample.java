package com.javacodegeeks.testng.spring;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

@ContextConfiguration("context.xml")
public class SpringTestNGDependencyInjectionExample extends AbstractTestNGSpringContextTests {
    @Test
    public void verifyFooName() {
    	System.out.println("verifyFooName: Is foo not null? " + (foo != null));
    	assertNotNull(foo);
    	System.out.println("verifyFooName: Foo name is '" + foo.getName() + "'");
        assertEquals(foo.getName(), "TestNG Spring");        
    }
    
    @Test
    public void verifyBeanFactory() {
    	System.out.println("verifyBeanFactory: Is bean factory not null? " + (beanFactory!= null)); 
        assertNotNull(beanFactory);
    }        
    
    @Autowired
    private BeanFactory beanFactory;    
    
    @Autowired
    private Foo foo;
}