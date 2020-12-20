# Spring bean 的生命周期

Bean有初始化和销毁两个过程

1.singleton作用域的Bean，Spring容器能准确的知道该Bean实例的创建、初始化、销毁。

2.prototype作用域的Bean，Spring容器只负责创建，原因Spring容器不知道创建了多少该Bean也不知道何时销毁。

![img](https://images0.cnblogs.com/i/580631/201405/181453414212066.png)

![img](https://images0.cnblogs.com/i/580631/201405/181454040628981.png)



# 上图有误

```java
BeanDefinitionRegistryPostProcessor


注入bean


@Import 注解 将导入（自动注入） ImportBeanDefinitionRegistrar  或者 ImportSelector  的实现类
```



#### Bean 生命周期方法调用时序图（来自源码4.3.16）

org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#createBean 

​	org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#resolveBeforeInstantiation

​		org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsBeforeInstantiation 

org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#doCreateBean

​	org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#createBeanInstance

​		org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#instantiateBean

​	org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#populateBean

​		org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor#postProcessAfterInstantiation

​		org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor#postProcessPropertyValues

​		org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#applyPropertyValues

​	org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#initializeBean

​		org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#invokeAwareMethods

​			org.springframework.beans.factory.BeanNameAware#setBeanName

​			org.springframework.beans.factory.BeanClassLoaderAware#setBeanClassLoader

​			org.springframework.beans.factory.BeanFactoryAware#setBeanFactory

​		org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsBeforeInitialization

​		org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#invokeInitMethods

​			org.springframework.beans.factory.InitializingBean#afterPropertiesSet

​			org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#invokeCustomInitMethod

​		org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsAfterInitialization