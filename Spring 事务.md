# Spring 事务

Spring支持编程式事务管理以及声明式事务管理两种方式。

### 1. 编程式事务管理

编程式事务管理是侵入性事务管理，使用TransactionTemplate或者直接使用PlatformTransactionManager，对于编程式事务管理，Spring推荐使用TransactionTemplate。

### 2. 声明式事务管理

声明式事务管理建立在AOP之上，其本质是对方法前后进行拦截，然后在目标方法开始之前创建或者加入一个事务，执行完目标方法之后根据执行的情况提交或者回滚。
编程式事务每次实现都要单独实现，但业务量大功能复杂时，使用编程式事务无疑是痛苦的，而声明式事务不同，声明式事务属于无侵入式，不会影响业务逻辑的实现，只需要在配置文件中做相关的事务规则声明或者通过注解的方式，便可以将事务规则应用到业务逻辑中。
显然声明式事务管理要优于编程式事务管理，这正是Spring倡导的非侵入式的编程方式。唯一不足的地方就是声明式事务管理的粒度是方法级别，而编程式事务管理是可以到代码块的，但是可以通过提取方法的方式完成声明式事务管理的配置。

## 事务的传播机制

事务的传播性一般用在事务嵌套的场景，比如一个事务方法里面调用了另外一个事务方法，那么两个方法是各自作为独立的方法提交还是内层的事务合并到外层的事务一起提交，这就是需要事务传播机制的配置来确定怎么样执行。
常用的事务传播机制如下：

- PROPAGATION_REQUIRED
  Spring默认的传播机制，能满足绝大部分业务需求，如果外层有事务，则当前事务加入到外层事务，一块提交，一块回滚。如果外层没有事务，新建一个事务执行
- PROPAGATION_REQUES_NEW
  该事务传播机制是每次都会新开启一个事务，同时把外层事务挂起，当当前事务执行完毕，恢复上层事务的执行。如果外层没有事务，执行当前新开启的事务即可
- PROPAGATION_SUPPORT
  如果外层有事务，则加入外层事务，如果外层没有事务，则直接使用非事务方式执行。完全依赖外层的事务
- PROPAGATION_NOT_SUPPORT
  该传播机制不支持事务，如果外层存在事务则挂起，执行完当前代码，则恢复外层事务，无论是否异常都不会回滚当前的代码
- PROPAGATION_NEVER
  该传播机制不支持外层事务，即如果外层有事务就抛出异常
- PROPAGATION_MANDATORY
  与NEVER相反，如果外层没有事务，则抛出异常
- PROPAGATION_NESTED
  该传播机制的特点是可以保存状态保存点，当前事务回滚到某一个点，从而避免所有的嵌套事务都回滚，即各自回滚各自的，如果子事务没有把异常吃掉，基本还是会引起全部回滚的。



数据访问技术及实现

| 数据访问技术 | 实现类                       |
| ------------ | ---------------------------- |
| JDBC         | DataSourceTransactionManager |
| JPA          | JpaTransactionManager        |
| Hibernate    | HibernateTransactionManager  |
| JDO          | JdoTransactionManager        |
| 分布式事务   | JtaTransactionManager        |



### 源码:

@EnableTransactionManagement 注解导入了 TransactionManagementConfigurationSelector   选择器。

TransactionManagementConfigurationSelector    此选择器注入了  AutoProxyRegistrar 和  ProxyTransactionManagementConfiguration 这两个bean



1、AutoProxyRegistrar  将把 InfrastructureAdvisorAutoProxyCreator 注入到IOC容器中。而    InfrastructureAdvisorAutoProxyCreator  是一个 BeanPostProcessor 的子类，

所以需要关注其父类的 org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessAfterInitialization

​			org.springframework.transaction.config.internalTransactionalEventListenerFactory（待定）



2、ProxyTransactionManagementConfiguration 该配置类注入时 调用父类 setImportMetadata  判断是否开启事务

3、ProxyTransactionManagementConfiguration  类中配置了  AnnotationTransactionAttributeSource、TransactionInterceptor、BeanFactoryTransactionAttributeSourceAdvisor

三个事务的重要组件。它们将被在加载改配置类时，分别被创建。

4、BeanFactoryTransactionAttributeSourceAdvisor  这是一个AOP的增强器，在创建 时注入AnnotationTransactionAttributeSource 和TransactionInterceptor 属性，因此这两个数量的创建在BeanFactoryTransactionAttributeSourceAdvisor 此之前。

5、以上是注册配置时必须加载的类。



### InfrastructureAdvisorAutoProxyCreator  一切事务代理的开始

。。。。











### 事务失效









