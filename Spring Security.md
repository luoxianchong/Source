# Spring Security

### 作用及意义：

1、为什么要使用Spring Security

​	保护资源   https://erp.hupun.com/o/inventory.init.d?_calf=erp-web

​                     https://erp-web-o.hupun.com/admin.User.d?_calf=erp-web

​	一个快捷的web安全框架

2、Spring Security的同类产品(Shiro)的优劣

​	

3、Spring Security、Shiro等该类产品的作用

​      核心资源保护、访问你该访问的。

​	 能够做什么事：

​			验证用户身份(认证)

​			用户访问权限控制（授权或鉴权）

​	附加：

​			remember me

​			session 管理支持



### Spring Security 的核心/基本原理

```txt
 官方自诉：
Spring Security is a powerful and highly customizable authentication and access-control framework. It is the de-facto(事实上) standard for securing Spring-based applications.

强大和高度自定义认证访问控制框架。

Spring Security is a framework that focuses on providing both authentication and authorization to Java applications. Like all Spring projects, the real power of Spring Security is found in how easily it can be extended to meet custom requirements

spring security 优势是易于扩展以满足自定义需要

1）Comprehensive and extensible support for both Authentication and Authorization（全面和可扩展的身份验证和授权支持）

2）Protection against attacks like session fixation, clickjacking, cross site request forgery, etc（防止攻击，如会话固定，点击劫持，跨站请求伪造等）

3）Servlet API integration（Servlet API的集成）

4）Optional integration with Spring Web MVC （与Spring Web MVC的可选集成）
```



核心组件：

```
SecurityContextHolder: 提供对SecurityContext的访问

SecurityContext: 持有Authentication对象和其他可能需要的信息

AuthenticationManager: 其中可以包含多个AuthenticationProvider

ProviderManager: 对象为AuthenticationManager接口的实现类

AuthenticationProvider: 主要用来进行认证操作的类调用其中的 authenticate()方法去进行认证操作

Authentication: Spring Security方式的认证主体

GrantedAuthority: 对认证主题的应用层面的授权，含当前用户的权限信息，通常使用角色表示

UserDetails: 构建Authentication对象必须的信息，可以自定义，可能需要访问DB得到

UserDetailsService: 通过username构建UserDetails对象，
通过loadUserByUsername根据userName获取UserDetail对象
（可以在这里基于自身业务进行自定义的实现  如通过数据库，xml,缓存获取等）
```



#### Authentication(认证)



#### Authorization(授权)





### Shiro 基本原理与角色

Apache Shiro is a powerful and easy to use Java security framework that offers developers an intuitive yet comprehensive solution to authentication, authorization, cryptography, and session management.

并不局限于web

### Spring Security demo



### Spring Security 源码



初始化配置：

![config](./image/spring/spring-security-init-configure.png)





##### WebSecurityConfigurerAdapter

```java
目标(Target)角色：
    客户端所期待得到的接口，
适配器(Adapter)角色：
    适配器类是本模式的核心。适配器把源接口转换成目标接口。显然，这一角色不可以是接口，而必须是具体类。
源(Adapee)角色被适配者：
    现在需要被适配的接口
```

![Adapter模式](./image/spring/adapter.jpg)



tagert:    WebSecurityConfigurer

Adapter:  WebSecurityConfigurerAdapter

Adapee: WebSecurity



#### WebSecurity：

​	使用了建造者模式，实现了SecurityBuilder

#### HttpSecurity

```
    private final RequestMatcherConfigurer requestMatcherConfigurer;//内部类，匹配器配置类
	private List<Filter> filters = new ArrayList<>();  //filter集合
	private RequestMatcher requestMatcher = AnyRequestMatcher.INSTANCE;//请求匹配器，任何请求都匹配。
	private FilterComparator comparator = new FilterComparator();//filter 排序比较器。
```

##### RequestMatcher

​	AntPathRequestMatcher

​		1、Ant的样式路径，有三种通配符的匹配方式

​				?（匹配任何单字符）

​               *（匹配0或者任意数量的字符）

​               **（匹配0或者更多的目录）

​		2、最长匹配原则(has more characters)

​		```URL请求/web/aa/bb.jsp，现在存在两个路径匹配模式/*/.jsp和/web/aa/*.jsp，那么会根据模式/web/aa/*.jsp来匹配```

​	MvcRequestMatcher

​		1、使用Spring MVC的HandlerMappingIntrospector来匹配路径并提取变量

​			MVC中是一个HandlerMapping的List的集合。

​			HandlerMapping：List< HandlerMethod >

​	RegexRequestMatcher   根据正则模式进行匹配

例子：

```java
// http://xxxx/v1/m1 不需要认证
http.authorizeRequests().mvcMatchers(HttpMethod.GET,"m1").servletPath("/v1").access("permitAll");
//http://xxxx/v2/m2 需要USER 权限
http.authorizeRequests().mvcMatchers(HttpMethod.GET,"m2").servletPath("/v2").access("hasRole('USER')");

http.authorizeRequests().antMatchers("/**/*.html").access("permitAll");
```



HttpSecurity 构建配置。

```
headers     	//将安全标头添加到响应
cors              	//跨域资源共享配置
sessionManagement       //允许配置会话管理
rememberMe 					 	//记住我功能配置
authorizeRequests  			//请求访问控制
exceptionHandling  		//错误处理配置
csrf   									//跨站攻击配置，默认开启。
requestCache              //用户在未登录前浏览A页面，登录后，也需要返回到A页面。通过RequestCacheAwareFilter 拦截
logout                           //退出登录支持,默认路径"/logout",将清除cookies（可以指定）、Authentication、Session
httpBasic  					//配置 Http Basic 验证
formLogin                //基于表单的身份验证的配置，包括登录url、用户名和密码在html中的name属性、成功与失败跳转url
anonymous            //匿名用户配置，将建立AnonymousAuthenticationToken



openidLogin   //输入已注册的 OpenID 用户名，然后跳转到OpenID 服务网站，在OpenID 服务网站输入密码验证通过后，回到当前网站即登录成功。http://www.mossle.com/docs/auth/html/ch114-openid.html  
jee                  //配置基于容器的预认证。 在这种情况下，认证由Servlet容器管理
x509             //X509是一种基于双向加密的身份认证方式,服务端和客户端都需要使用证书
portMapper       //常用于在HTTP和HTTPS两种协议的端口之间做跳转 : 80–> 443, 8080 --> 8443 。
requiresChannel  //强制安全通道，必须要https协议的请求  
```



#### 源码

​	①org.springframework.web.filter.DelegatingFilterProxy 过滤器

```java
FilterChainProxy
 			SecurityFilterChain
						WebAsyncManagerIntegrationFilter
						SecurityContextPersistenceFilter
                        HeaderWriterFilter									//将指定的头部信息写入响应对象
                        CsrfFilter														//跨站攻击拦截器
                        LogoutFilter										
                        UsernamePasswordAuthenticationFilter  //用户登录认证
                        DefaultLoginPageGeneratingFilter
                        DefaultLogoutPageGeneratingFilter
                        BasicAuthenticationFilter
                        RequestCacheAwareFilter
                        SecurityContextHolderAwareRequestFilter
                        RememberMeAuthenticationFilter
                        AnonymousAuthenticationFilter   //创建一个anonymousUser，权限（ROLE_ANONYMOUS）
                        SessionManagementFilter   //SessionAuthenticationStrategy
                        ExceptionTranslationFilter
                        FilterSecurityInterceptor
```



##### SecurityContextPersistenceFilter

```java
	SecurityContext 持久化

​	执行chain前加载，执行后保存；

​	未登录：SecurityContext 为空

​	登录：能从SecurityContextRepository获取SecurityContext
```



##### UsernamePasswordAuthenticationFilter

```java
	AuthenticationManager  实现类 ProviderManager

​		List<AuthenticationProvider> providers

​				provider 认证 filter中包装的 authentication 对象

​	SessionAuthenticationStrategy  //session认证策略

​			CompositeSessionAuthenticationStrategy 组合使用多个SessionAuthenticationStrategy

​			CsrfAuthenticationStrategy      //根据token认证

​			SessionFixationProtectionStrategy 

​				过期原来的session，创建一个新的session，把原来session的属性拷贝到新的session中

​			ConcurrentSessionControlAuthenticationStrategy  

​				允许用户同时在线数，有一个maximumSessions属性，默认是1，session存储在内存中

​			ChangeSessionIdAuthenticationStrategy   每当用户认证过后，就会重新生成一个新的session

RememberMeServices

​	PersistentTokenBasedRememberMeServices   //持久话token remember me service

​	TokenBasedRememberMeServices   //基于cookie 存储token 默认过期为2周。

AuthenticationSuccessHandler

​	SavedRequestAwareAuthenticationSuccessHandler

AuthenticationFailureHandler

​	SimpleUrlAuthenticationFailureHandler
```



##### RequestCacheAwareFilter

```
RequestCache
		HttpSessionRequestCache
		
		该filter从session中获取以保存的请求，并检查是否正确。
		然后移除
		
		
何时缓存？
ExceptionTranslationFilter 中认证异常时缓存。

protected void sendStartAuthentication(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain,
			AuthenticationException reason) throws ServletException, IOException {
		SecurityContextHolder.getContext().setAuthentication(null);
		requestCache.saveRequest(request, response);
		logger.debug("Calling Authentication entry point.");
		authenticationEntryPoint.commence(request, response, reason);
}


以下三种请求不缓存（@link RequestCacheConfigurer.createDefaultSavedRequestMatcher）

请求地址以/favicon.*结尾
header中的content-type值为application/json、multipart/form-data
header中的X-Requested-With值为XMLHttpRequest  //ajax 请求
```



##### AnonymousAuthenticationFilter

```java
    如果SecurityContextHolder.getContext 中Authentication 为空,则创建一个AnonymousAuthenticationToken 以便后期认证。

匿名认证：

​	ExceptionTranslationFilter 捕获 AccessDeniedException 时 并且使用AuthenticationTrustResolver判断是匿名用户。
```



##### RememberMeAuthenticationFilter

![remeber-me](./image/spring/remeber-me.png)

```
rememberMeServices   获取token  并组装一个Authentication（RememberMeAuthenticationToken）
authenticationManager 认证授权
```



##### BasicAuthenticationFilter

```
Authorization: Basic Y2h5aW5ncDoxMjM0NTY=

Base64.getEncoder().encode(new String("userName:password").getBytes());
```



##### ExceptionTranslationFilter

​	捕获鉴权失败后的异常

​	1、AuthenticationException

​			AuthenticationEntryPoint  认证入口点，开启特定认证方案的认证流程

```java
Http403ForbiddenEntryPoint
		设置响应状态字为403,并非触发一个真正的认证流程。通常在一个预验证(pre-authenticated authentication)已经得出结论需要拒绝用户请求的情况被用于拒绝用户请求。

HttpStatusEntryPoint 设置特定的响应状态字，并非触发一个真正的认证流程。

LoginUrlAuthenticationEntryPoint 重定向到登录页面从而开始一个认证流程。

BasicAuthenticationEntryPoint
对应标准Http Basic认证流程的触发动作，向响应写入状态字401和头部WWW-Authenticate:"Basic realm="xxx"触发标准Http Basic认证流程。

DigestAuthenticationEntryPoint
对应标准Http Digest认证流程的触发动作，向响应写入状态字401和头部WWW-Authenticate:"Digest realm="xxx"触发标准Http Digest认证流程。
```


​    2、AccessDeniedException

​	3、AccessDeniedHandler





##### AbstractAuthenticationProcessingFilter(`主要处理登录`)

// 登录认证操作  其子类一般UsernamePasswordAuthenticationFilter

##### AbstractSecurityInterceptor (`主要处理鉴权`)  

​		实现类：FilterSecurityInterceptor和MethodSecurityInterceptor

```java
FilterSecurityInterceptor持有【AccessDecisionManager和AuthenticationManager】。
1、从SecurityContextHolder获取Authentication，
2、然后通过SecurityMetadataSource可以得知当前请求是否在请求受保护的资源。
3、对于请求那些受保护的资源，如果Authentication.isAuthenticated()返回false或者AbstractSecurityInterceptor的alwaysReauthenticate属性为true，那么将会使用其引用的AuthenticationManager再认证一次，认证之后再使用认证后的Authentication替换SecurityContextHolder中拥有的那个。然后就是利用AccessDecisionManager进行权限的检查。

AccessDecisionManager

​		AffirmativeBased  一票通过

​		ConsensusBased 通过大于反对

​		UnanimousBased 一票否决通过

SecurityAccessDecisionVoter

​	RoleVoter  //ConfigAttribute中包含以“ROLE_”开头的，则使用RoleVoter投票
    
​	WebExpressionVoter
    
​	AuthenticatedVoter 

​		AuthenticatedVoter可以处理ConfigAttribute 中包含：

​				IS_AUTHENTICATED_FULLY

​				IS_AUTHENTICATED_REMEMBERED

​				IS_AUTHENTICATED_ANONYMOUSLY	
```



```java
public void invoke(FilterInvocation fi) throws IOException, ServletException {
		if ((fi.getRequest() != null)
				&& (fi.getRequest().getAttribute(FILTER_APPLIED) != null)
				&& observeOncePerRequest) {
			// filter already applied to this request and user wants us to observe
			// once-per-request handling, so don't re-do security checking
			fi.getChain().doFilter(fi.getRequest(), fi.getResponse());
		}
		else {
			// first time this request being called, so perform security checking
			if (fi.getRequest() != null && observeOncePerRequest) {
				fi.getRequest().setAttribute(FILTER_APPLIED, Boolean.TRUE);
			}

			InterceptorStatusToken token = super.beforeInvocation(fi); //1、before 

			try {
				fi.getChain().doFilter(fi.getRequest(), fi.getResponse());//访问目标资源
			}
			finally {
				super.finallyInvocation(token);//2、必要的
			}

			super.afterInvocation(token, null);//3、after
		}
	}
```



MethodSecurityInterceptor



2、权限配置



​	@EnableGlobalMethodSecurity(prePostEnabled=true,securedEnabled=true,jsr250Enabled=true)

分别对应方法权限注解开启： 

​	prePostEnabled=true 表示

- @PreAuthorize  调用前进行权限检查
- @PreFilter   对集合类型的参数进行过滤
- @PostAuthorize 调用后进行权限检查
- @PostFilter  对集合类型返回值进行过滤

注解对Java方法级进行访问控制和处理



securedEnabled = true  表示

@Secured("IS_AUTHENTICATED_ANONYMOUSLY") 其中表达式即验证规则



#### 3、Spring Security 对OAuth2.0的支持

​		OAuth 2.0 是一个标准协议，根据

[RFC6749]: https://tools.ietf.org/html/rfc6749	""""

 [RFC(Request For Comments)]草案、网络标准文档规范的一个三方应用程序获得对HTTP服务上资源访问权限的授权框架。



OAuth 2 标准中的角色：

- 资源所有者（Resource Owner）
- 资源服务器（Resource Server）
- 授权服务器（Authorization Server）
- 客户端（Client）

协议流程

```
     +--------+                               									 +----------------+
     |       		 |--(A)- Authorization Request ->  	 |   Resource    |
     |       		 |                              									 |     Owner       |
     |        		 |<-(B)-- Authorization Grant ---	   |                          |
     |      		 |                               									+----------------+
     |       		 |
     |       		 |                               									+-------------------+
     |       		 |--(C)-- Authorization Grant -----> | Authorization |
     | Client |                               									  |     Server            |
     |        		|<-(D)----- Access Token ------------ |                               |
     |       		 |                              									 +------------------+
     |       		 |
     |       		 |                               									+-----------------+
     |       		 |--(E)----- Access Token -----------> |    Resource    |
     |       		 |                             									    |     Server         |
     |       		 |<-(F)--- Protected Resource ------|                            |
     +--------+                               									+-----------------+
```



**OAuth2.0四种授权模式**

授权码模式（authorization code）常用、安全性最高

隐式模式（implicit）

密码模式（resource owner password credentials）

客户端模式（client credentials）

![img](./image/spring/oAuth2-flow.png)

授权码模式：

![oAuth2-example](./image/spring/oAuth2-example.png)







### Spring Security 在登录中的使用



### jdk中的SecurityManager