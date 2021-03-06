package com.jcrud.utils;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class SpringXmlUtils {

	public static <T> T getBeanFromSpringContext(String xmlContext, Class<T> beanClass) {

		GenericXmlApplicationContext springContext = getSpringContextFromXml(xmlContext);

		T myBean = springContext.getBean(beanClass);

		return myBean;
	}

	public static GenericXmlApplicationContext getSpringContextFromXml(String xml) {

		String wrapperSpringContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:context=\"http://www.springframework.org/schema/context\" xmlns:util=\"http://www.springframework.org/schema/util\" xmlns:tx=\"http://www.springframework.org/schema/tx\" xmlns:jee=\"http://www.springframework.org/schema/jee\" xmlns:aop=\"http://www.springframework.org/schema/aop\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.2.xsd http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.2.xsd http://www.springframework.org/schema/util http://www.springframework.org/schema/util/spring-util-3.2.xsd http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx.xsd http://www.springframework.org/schema/jee http://www.springframework.org/schema/jee/spring-jee-3.2.xsd http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop-3.2.xsd\"> %s </beans>";

		String contextXML = String.format(wrapperSpringContent, xml);

		Resource resource = new ByteArrayResource(contextXML.getBytes());
		GenericXmlApplicationContext springContext = new GenericXmlApplicationContext();
		springContext.load(resource);

		return springContext;
	}

	// Test method
	public static void main(String[] args) throws Exception {

		// String contextXML =
		// "<bean class=\"com.apimock.model.impl.MockRepositoryManagerStubImpl\"/>";

		// MockRepositoryManagerStubImpl impl =
		// getBeanFromSpringContext(contextXML,
		// MockRepositoryManagerStubImpl.class);
		//
		// System.out.println(impl);
	}
}
