## Spring MVC

### DispatcherServlet

##### 1、DispatcherServlet 

![img](E:\201320180110\source\image\Dispatch.png)



DispatcherServlet 继承了FrameworkServlet继承了HttpServletBean继承了HttpServlet继承了GenericServlet实现了Servlet接口

```java
Servlet接口抽象方法

void init(ServletConfig var1) throws ServletException;

ServletConfig getServletConfig();

void service(ServletRequest var1, ServletResponse var2) throws ServletException, IOException;

String getServletInfo();

void destroy();
```

`DispatcherServlet` 改`Servlet`的init方法由其父类`HttpServletBean`实现。此父类提功能一个initServletBean。init->initServletBean->initWebApplicationContext->onRefresh->initStrategies.

以上流水最后到DispatcherServlet 的initStrategies方法初始化各种策略。

而`DispatcherServlet` 该`Servlet`的service方法由`HttpServlet`实现。HttpServlet会区分好post、get、put、delete、head、trace、option等类型请求。但是都被FrameworkServlet 重写并转到processRequest 方法上。从而调用doService方法。所以DispatcherServlet 重写doService方法即可处理大部分请求。这也是DispatcherServlet 的入口方法。

##### 2、DispatcherServlet初始化工作

```
protected void initStrategies(ApplicationContext context) {
		initMultipartResolver(context);
		initLocaleResolver(context);
		initThemeResolver(context);
		initHandlerMappings(context);
		initHandlerAdapters(context);
		initHandlerExceptionResolvers(context);
		initRequestToViewNameTranslator(context);
		initViewResolvers(context);
		initFlashMapManager(context);
}
```



##### 3、doService

设置request的Attribute ，然后调用doDispatch

##### 4、doDispatch

①、检查是否为文件上传请求。

②、获取请求映射的HandlerExecutionChain

③、根据 HandlerExecutionChain中的Handler获取请求映射的HandlerAdapter

④、调用HandlerExecutionChain中拦截器的preHandler方法执行早期拦截，返回值为false，则请求直接返回，后面步骤不处理。

⑤、HandlerAdapter 的handle方法调用、执行请求映射的方法并返回ModelAndView

⑥、当modelAndView没有返回视图时，设置默认的view视图

⑦、调用HandlerExecutionChain中拦截器的postHandle方法执行后置拦截。

⑧、请求时以上结果的处理，对modelAndView的处理（其中没有异常，则会执行拦截器的afterCompletion）。

⑨、如果⑧中执行拦截器afterCompletion方法之前抛出异常，则会执行拦截器afterCompletion



### 深入doDispatch

##### 1、怎样判断为文件上传。

​	由MultipartResolver的isMultipart判断。判断依据是请求method为post请求、请求头(Header)中

Content-Type值为 `multipart/`

MultipartResolver 是多文件上传的一个接口类、SpringFrameWork中默认实现类时CommonsMultipartResolver、StandardServletMultipartResolver

##### 2、HandlerExecutionChain的获取

从HandlerMapping的List中遍历获取请求路径映射的HandlerMapping.

疑问：HandlerMapping为什么用list容器存储。

从HandlerMapping中维护了一个MappingRegistry，此registry中又维护了一个LinkedMultiValueMap。该map中存储了URL与controller中方法的映射，包装成HandlerMethod 返回。

根据HandlerMethod和HandlerInterceptor 创建一个HandlerExecutionChain。其中HandlerExecutionChain的Handler实际为HandlerMethod

##### 3、HandlerAdapter的获取

遍历HandlerAdapter列表，支持HandlerExecutionChain中的Handler即返回HandlerAdapter

普通的get和post请求获取的都是RequestMappingHandlerAdapter。

##### 4、HandlerAdapter的Handler

RequestMappingHandlerAdapter中做实事的是invokeHandlerMethod方法。

```java
protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
			HttpServletResponse response, HandlerMethod handlerMethod) {

	ServletWebRequest webRequest = new ServletWebRequest(request, response);
	try {
		WebDataBinderFactory binderFactory = getDataBinderFactory(handlerMethod);
		ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);

		ServletInvocableHandlerMethod invocableMethod 
            = createInvocableHandlerMethod(handlerMethod);
        
		if (this.argumentResolvers != null) {    
            invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
		}
		if (this.returnValueHandlers != null) {	
      invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
		}
		invocableMethod.setDataBinderFactory(binderFactory);
        
		invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

		ModelAndViewContainer mavc = new ModelAndViewContainer();
		mavc.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
		modelFactory.initModel(webRequest, mavc, invocableMethod);
		mavc.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);

		AsyncWebRequest asyncWebRequest 
            = WebAsyncUtils.createAsyncWebRequest(request, response);
        
		asyncWebRequest.setTimeout(this.asyncRequestTimeout);

		WebAsyncManager manager = WebAsyncUtils.getAsyncManager(request);
		manager.setTaskExecutor(this.taskExecutor);
		manager.setAsyncWebRequest(asyncWebRequest);
		manager.registerCallableInterceptors(this.callableInterceptors);
				            manager.registerDeferredResultInterceptors(this.deferredResultInterceptors);

		if (manager.hasConcurrentResult()) {
			Object result = manager.getConcurrentResult();
			mavc = (ModelAndViewContainer) manager.getConcurrentResultContext()[0];
			
            manager.clearConcurrentResult();
            
			invocableMethod = invocableMethod.wrapConcurrentResult(result);
		}

		invocableMethod.invokeAndHandle(webRequest, mavc);
		
		if (manager.isConcurrentHandlingStarted()) { return null; }
		
		return getModelAndView(mavc, modelFactory, webRequest);
	}
	finally { webRequest.requestCompleted(); }
}
```

1、根据HandlerMethod获取WebDataBinderFactory、ModelFactory

2、包装HandlerMethod成ServletInvocableHandlerMethod 一个能被调用的HandlerMethod

3、设置参数解析器、返回值解析器（比如：@ResponseBody和@RequestBody,即被这两个解析器解析的。如果出入参是JSON，则是此二解析器中HttpMessageConventer转化的）。

4、初始化一个ModelAndViewContainer

5、初始化一个WebAsyncManager

6、invokeAndHandle方法调用。执行入参解析装换、Controller方法调用、返回值参数解析装换

7、根据ModelAndViewContainer和Request生成ModelAndView并返回。



##### 5、视图渲染（结果处理）

其实在调用HandlerAdapter的Handler方法是已经把响应结果报保存了。

此时主要是视图的渲染。



### HandlerMapping

### HandlerExecutionChain

### HandlerAdapter



### HandlerInterceptor

1、HandlerInterceptor 一个拦截器接口

```java
public interface HandlerInterceptor {

    /** 
     * preHandle方法是进行处理器拦截用的，该方法将在Controller处理之前进行调用，
     * SpringMVC中的Interceptor拦截器是链式的，可以同时存在 
     * 多个Interceptor，然后SpringMVC会根据声明的前后顺序一个接一个的执行，
     * 而且所有的Interceptor中的preHandle方法都会在 
     * Controller方法调用之前调用。
     * SpringMVC的这种Interceptor链式结构也是可以进行中断的，这种中断方式是令preHandle的返 
     * 回值为false，当preHandle的返回值为false的时候整个请求就结束了。  
     */
	default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		return true;
	}
	
     /** 
     * 这个方法只会在当前这个Interceptor的preHandle方法返回值为true的时候才会执行。
     * postHandle是进行处理器拦截用的，它的执行时间是在处理器进行处理之后，
     * 也就是在Controller的方法调用之后执行，但是它会在DispatcherServlet进行视图的渲染之前执行，
     * 也就是说在这个方法中你可以对ModelAndView进行操 
     * 作。这个方法的链式结构跟正常访问的方向是相反的，
     * 也就是说先声明的Interceptor拦截器该方法反而会后调用，
     * 这跟Struts2里面的拦截器的执行过程有点像， 
     * 只是Struts2里面的intercept方法中要手动的调用ActionInvocation的invoke方法，
     * Struts2中调用ActionInvocation的invoke方法就是调用下一个Interceptor 
     * 或者是调用action，然后要在Interceptor之前调用的内容都写在调用invoke之前，
     * 要在Interceptor之后调用的内容都写在调用invoke方法之后。 
     */ 
	default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
	}

    /** 
     * 该方法也是需要当前对应的Interceptor的preHandle方法的返回值为true时才会执行。
     * 该方法将在整个请求完成之后，也就是DispatcherServlet渲染了视图执行， 
     * 这个方法的主要作用是用于清理资源的，
     * 当然这个方法也只能在当前这个Interceptor的preHandle方法的返回值为true时才会执行。 
     */  
	default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
	}
}
```



### 