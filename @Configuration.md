# @Configuration



被@Configuration注解的类

在org.springframework.context.annotation.AnnotatedBeanDefinitionReader#registerBean(java.lang.Class<?>, java.lang.String, java.lang.Class<? extends java.lang.annotation.Annotation>...) 方法中被注册到IOC容器中 ，此时只是一个beanDefinition





org.springframework.context.support.AbstractApplicationContext#refresh

​	org.springframework.context.support.AbstractApplicationContext#invokeBeanFactoryPostProcessors

​		org.springframework.context.support.PostProcessorRegistrationDelegate#invokeBeanFactoryPostProcessors

​			org.springframework.context.support.PostProcessorRegistrationDelegate#invokeBeanDefinitionRegistryPostProcessors

​				org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry

​					org.springframework.context.annotation.ConfigurationClassPostProcessor#processConfigBeanDefinitions  //读取早期的config beandef

​						org.springframework.context.annotation.ConfigurationClassParser#parse  //解析注解内容

​							org.springframework.context.annotation.ConfigurationClassParser#processConfigurationClass

​								org.springframework.context.annotation.ConfigurationClassParser#processPropertySource

​							org.springframework.context.annotation.ConfigurationClassParser#processDeferredImportSelectors

​						org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitions

​							org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsForConfigurationClass

​								org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsFromImportedResources

​								org.springframework.context.annotation.ConfigurationClassBeanDefinitionReader#loadBeanDefinitionsFromRegistrars











ImportSelector  与  ImportBeanDefinitionRegistrar  区别： 



```
public interface ImportSelector {

	/**
	 * Select and return the names of which class(es) should be imported based on
	 * the {@link AnnotationMetadata} of the importing @{@link Configuration} class.
	 */
	String[] selectImports(AnnotationMetadata importingClassMetadata);

}


public interface ImportBeanDefinitionRegistrar {

	/**
	 * Register bean definitions as necessary based on the given annotation metadata of
	 * the importing {@code @Configuration} class.
	 * <p>Note that {@link BeanDefinitionRegistryPostProcessor} types may <em>not</em> be
	 * registered here, due to lifecycle constraints related to {@code @Configuration}
	 * class processing.
	 * @param importingClassMetadata annotation metadata of the importing class
	 * @param registry current bean definition registry
	 */
	public void registerBeanDefinitions(
			AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry);

}

1、两个接口参数不一致，regist 带有 registry注册器
2、返回值不同selector 放回一个class 全类名数组
```