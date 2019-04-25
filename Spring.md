# Spring

```java
AnnotationConfigApplicationContext impl AnnotationConfigRegistry
	GenericApplicationContext impl BeanDefinitionRegistry
		AbstractApplicationContext  impl ConfigurableApplicationContext
			DefaultResourceLoader impl  ResourceLoader
```

### refresh()

###### 1、prepareRefresh  开始refresh的前期准备，

​	关闭为false，激活为true

​	初始化 propertySource

​	校验property

​	设置监听

###### 2、obtainFreshBeanFactory  获取BeanFactory

​	设置 context （BeanFactory）的id，

###### 3、prepareBeanFactory  准备BeanFactory

​	1、设置Bean的ClassLoader （AppClassLoader）

​	2、设置表达式解析器

​	3、设置property注册器

​	4、设置BeanPostProcessor 后置处理器 并存储到：ignoredDependencyInterfaces 的set容器、resolvableDependencies （classType,context）的Map容器

​	5、设置一些意识感知器（ResourceLoaderAware、ApplicationContextAware、ApplicationEventPublisherAware 等）添加到ignoreDependencyInterfaces这个HashSet容器中

​	6、设置BeanFactory、ResourceLoader、ApplicationEventPublisher、ApplicationContext  添加到resolvableDependencies的concurrentHashMap中

​	7、注册environment、systemProperties、systemEnvironment

###### 4、invokeBeanFactoryPostProcessors  调用BeanFactory的后置处理器

​	1、首先判断该BeanFactory是不是一个BeanDef的注册器

​	如果是：

​		1、先调用实现了PriorityOrdered的BeanDefinitionRegistryPostProcessors ，即先找到、创建、排序、调用

​			```其中 ConfigurationClassPostProcessor就在此时调用，它实现了PriorityOrdered、BeanDefinitionRegistryPostProcessors。而ConfigurationClassPostProcessor就是扫描包路径、加载被注解的bean、并注册到BeanFactory中```

​		2、下一步 实现了Ordered的BeanDefinitionRegistryPostProcessors ，查找、创建、排序、调用

​		3、最后是其他没有发表的 BeanDefinitionRegistryPostProcessors

​		4、调用BeanPostProcessors的后置处理方法

​	如果不是：

​		1、调用其BeanFactoryPostProcessor 的postProcessBeanFactory实现方法

​	2、下一步是实现BeanFactoryPostProcessors的后置处理器。

​	3、先调用实现PriorityOrdered的BeanFactoryPostProcessor，如果前面已被调用则跳过

​	4、然后调用实现Ordered 的BeanFactoryPostProcessor ，最后调用没普通的

​	5、beanFactory清理MetadataCache（即mergedBeanDefinitions、allBeanNamesByType、singletonBeanNamesByType容器）

###### 5、registerBeanPostProcessors（）//注册BeanPostProcessor

​	1、根据BeanPostProcessor类型获取其实现类名、

​	2、创建一个BeanPostProcessorCheck 它也是一个BeanPostProcessor

​	3、先加载实现了PriorityOrdered的bean、然后加载实现Ordered的bean、最后加载普通bean

​	4、排序实现了PriorityOrdered的bean，并注册到BeanFactory中

​	5、排序实现了Ordered的bean，并注册到BeanFactory中

​	6、注册普通的Bean并注册到BeanFactory中

​	7、上述4、5、6都会判断是否是一个internalPostProcessors（内部的postPorcessor）,最后注册internalPostProcessors到BeanFactory中

​	8、最后创建一个ApplicationContextDetector 的探测器并持有ApplicationContext注册到BeanFactory中

​	```其中注册到BeanFactory中是保存，在一个beanPostProcessors的CopyOnWriteArrayList（写时复制）```

###### 6、initMessageSource（）//初始化message资源，比如国际化资源（i18n）

​	1、是否已经注册了messageSource的Bean：

​		如果没有创建一个DelegatingMessageSource并注册到BeanFactory中；

​		如果有获取该bean设置ApplicationContext。

###### 7、initApplicationEventMulticaster()//初始化ApplicationEvent

​	1、 是否已有applicationEventMulticaster的bean

​		如果没有创建一个SimpleApplicationEventMulticaster并注册到BeanFactory中；

###### 8、onRefresh（）//context子类实现、初始化指定的Bean

###### 9、registerListeners()//注册listener的bean

​	1、获取早期的listener并注册到ApplicationEventMulticaster的applicationListenerBeans中

​	2、查找实现ApplicationListener的监听器

​	3、获取早期Event,并广播事件。

###### 10、finishBeanFactoryInitialization()//初始化非懒加载的所有单例bean

​	1、conversionService 包含该bean，并且是ConversionService的子类。则注入beanFactory中，具体做什么？

​	2、注入配置文件解析器、实例化LoadTimeWeaverAware的子类

​	3、清空临时类加载器。

​	4、



###### 11、finishRefresh() //完成刷新,

### doCreateBean()

​	1、如果bean的类中没有重写方法（即没有继承或实现），则使用反射创建bean对应，否则使用Cglib

