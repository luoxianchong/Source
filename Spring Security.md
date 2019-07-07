### Spring Security

#### 1、filter

如下为其主要过滤器  

```java
    WebAsyncManagerIntegrationFilter 
    SecurityContextPersistenceFilter 
    HeaderWriterFilter 
    CorsFilter 
    LogoutFilter
    RequestCacheAwareFilter
    SecurityContextHolderAwareRequestFilter
    AnonymousAuthenticationFilter
    SessionManagementFilter
    ExceptionTranslationFilter
    FilterSecurityInterceptor
    UsernamePasswordAuthenticationFilter
    BasicAuthenticationFilter
```

#### 2、httpSecurity



#### 3、框架的核心组件

```java
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

   

#### 4、webFlux  使用Spring security

##### 1、ServerHttpSecurity

```java

package org.springframework.security.config.web.server;

public class ServerHttpSecurity {
    private ServerWebExchangeMatcher securityMatcher 
    = ServerWebExchangeMatchers.anyExchange();
    
    private ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchange;
    
    private ServerHttpSecurity.HttpsRedirectSpec httpsRedirectSpec;
    
    private ServerHttpSecurity.HeaderSpec headers 
    = new ServerHttpSecurity.HeaderSpec();
    
    private ServerHttpSecurity.CsrfSpec csrf = new ServerHttpSecurity.CsrfSpec();
    
    private ServerHttpSecurity.CorsSpec cors = new ServerHttpSecurity.CorsSpec();
    
    private ServerHttpSecurity.ExceptionHandlingSpec exceptionHandling 
    = new ServerHttpSecurity.ExceptionHandlingSpec();
    
    private ServerHttpSecurity.HttpBasicSpec httpBasic;
    
    private final ServerHttpSecurity.RequestCacheSpec requestCache 
    = new ServerHttpSecurity.RequestCacheSpec();
    
    private ServerHttpSecurity.FormLoginSpec formLogin;
    
    private ServerHttpSecurity.OAuth2LoginSpec oauth2Login;
    
    private ServerHttpSecurity.OAuth2ResourceServerSpec resourceServer;
    
    private ServerHttpSecurity.OAuth2ClientSpec client;
    
    private ServerHttpSecurity.LogoutSpec logout 
    = new ServerHttpSecurity.LogoutSpec();
    
    private ServerHttpSecurity.LoginPageSpec loginPage
    = new ServerHttpSecurity.LoginPageSpec();
    
    private ReactiveAuthenticationManager authenticationManager;
    
    private ServerSecurityContextRepository securityContextRepository 
    = new WebSessionServerSecurityContextRepository();
    
    private ServerAuthenticationEntryPoint authenticationEntryPoint;
    
    private List<DelegateEntry> defaultEntryPoints = new ArrayList();
    
    private ServerAccessDeniedHandler accessDeniedHandler;
    
    private List<org.springframework.security.web.server
    .authorization.ServerWebExchangeDelegatingServerAccessDeniedHandler
    .DelegateEntry> defaultAccessDeniedHandlers = new ArrayList();
    
    private List<WebFilter> webFilters = new ArrayList();
    
    private ApplicationContext context;
    
    private Throwable built;

    public ServerHttpSecurity securityMatcher(ServerWebExchangeMatcher matcher) {
        Assert.notNull(matcher, "matcher cannot be null");
        this.securityMatcher = matcher;
        return this;
    }

    public ServerHttpSecurity addFilterAt(WebFilter webFilter, SecurityWebFiltersOrder order) {
        this.webFilters.add(new ServerHttpSecurity.OrderedWebFilter(webFilter, order.getOrder()));
        return this;
    }

    private ServerWebExchangeMatcher getSecurityMatcher() {
        return this.securityMatcher;
    }

    public ServerHttpSecurity securityContextRepository(ServerSecurityContextRepository securityContextRepository) {
        Assert.notNull(securityContextRepository, "securityContextRepository cannot be null");
        this.securityContextRepository = securityContextRepository;
        return this;
    }

    public ServerHttpSecurity.HttpsRedirectSpec redirectToHttps() {
        this.httpsRedirectSpec = new ServerHttpSecurity.HttpsRedirectSpec();
        return this.httpsRedirectSpec;
    }

    public ServerHttpSecurity.CsrfSpec csrf() {
        if(this.csrf == null) {
            this.csrf = new ServerHttpSecurity.CsrfSpec();
        }

        return this.csrf;
    }

    public ServerHttpSecurity.CorsSpec cors() {
        if(this.cors == null) {
            this.cors = new ServerHttpSecurity.CorsSpec();
        }

        return this.cors;
    }

    public ServerHttpSecurity.HttpBasicSpec httpBasic() {
        if(this.httpBasic == null) {
            this.httpBasic = new ServerHttpSecurity.HttpBasicSpec();
        }

        return this.httpBasic;
    }

    public ServerHttpSecurity.FormLoginSpec formLogin() {
        if(this.formLogin == null) {
            this.formLogin = new ServerHttpSecurity.FormLoginSpec();
        }

        return this.formLogin;
    }

    public ServerHttpSecurity.OAuth2LoginSpec oauth2Login() {
        if(this.oauth2Login == null) {
            this.oauth2Login = new ServerHttpSecurity.OAuth2LoginSpec();
        }

        return this.oauth2Login;
    }

    public ServerHttpSecurity.OAuth2ClientSpec oauth2Client() {
        if(this.client == null) {
            this.client = new ServerHttpSecurity.OAuth2ClientSpec();
        }

        return this.client;
    }

    public ServerHttpSecurity.OAuth2ResourceServerSpec oauth2ResourceServer() {
        if(this.resourceServer == null) {
            this.resourceServer = new ServerHttpSecurity.OAuth2ResourceServerSpec();
        }

        return this.resourceServer;
    }

    public ServerHttpSecurity.HeaderSpec headers() {
        if(this.headers == null) {
            this.headers = new ServerHttpSecurity.HeaderSpec();
        }

        return this.headers;
    }

    public ServerHttpSecurity.ExceptionHandlingSpec exceptionHandling() {
        if(this.exceptionHandling == null) {
            this.exceptionHandling = new ServerHttpSecurity.ExceptionHandlingSpec();
        }

        return this.exceptionHandling;
    }

    public ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchange() {
        if(this.authorizeExchange == null) {
            this.authorizeExchange = new ServerHttpSecurity.AuthorizeExchangeSpec();
        }

        return this.authorizeExchange;
    }

    public ServerHttpSecurity.LogoutSpec logout() {
        if(this.logout == null) {
            this.logout = new ServerHttpSecurity.LogoutSpec();
        }

        return this.logout;
    }

    public ServerHttpSecurity.RequestCacheSpec requestCache() {
        return this.requestCache;
    }

    public ServerHttpSecurity authenticationManager(ReactiveAuthenticationManager manager) {
        this.authenticationManager = manager;
        return this;
    }

    public SecurityWebFilterChain build() {
        if(this.built != null) {
            throw new IllegalStateException("This has already been built with the following stacktrace. " + this.buildToString());
        } else {
            this.built = (new RuntimeException("First Build Invocation")).fillInStackTrace();
            if(this.headers != null) {
                this.headers.configure(this);
            }

            WebFilter securityContextRepositoryWebFilter = this.securityContextRepositoryWebFilter();
            if(securityContextRepositoryWebFilter != null) {
                this.webFilters.add(securityContextRepositoryWebFilter);
            }

            if(this.httpsRedirectSpec != null) {
                this.httpsRedirectSpec.configure(this);
            }

            if(this.csrf != null) {
                this.csrf.configure(this);
            }

            if(this.cors != null) {
                this.cors.configure(this);
            }

            if(this.httpBasic != null) {
                this.httpBasic.authenticationManager(this.authenticationManager);
                this.httpBasic.configure(this);
            }

            if(this.formLogin != null) {
                this.formLogin.authenticationManager(this.authenticationManager);
                if(this.securityContextRepository != null) {
                    this.formLogin.securityContextRepository(this.securityContextRepository);
                }

                this.formLogin.configure(this);
            }

            if(this.oauth2Login != null) {
                this.oauth2Login.configure(this);
            }

            if(this.resourceServer != null) {
                this.resourceServer.configure(this);
            }

            if(this.client != null) {
                this.client.configure(this);
            }

            this.loginPage.configure(this);
            if(this.logout != null) {
                this.logout.configure(this);
            }

            this.requestCache.configure(this);
            this.addFilterAt(new SecurityContextServerWebExchangeWebFilter(), SecurityWebFiltersOrder.SECURITY_CONTEXT_SERVER_WEB_EXCHANGE);
            if(this.authorizeExchange != null) {
                ServerAuthenticationEntryPoint authenticationEntryPoint = this.getAuthenticationEntryPoint();
                ExceptionTranslationWebFilter exceptionTranslationWebFilter = new ExceptionTranslationWebFilter();
                if(authenticationEntryPoint != null) {
                    exceptionTranslationWebFilter.setAuthenticationEntryPoint(authenticationEntryPoint);
                }

                ServerAccessDeniedHandler accessDeniedHandler = this.getAccessDeniedHandler();
                if(accessDeniedHandler != null) {
                    exceptionTranslationWebFilter.setAccessDeniedHandler(accessDeniedHandler);
                }

                this.addFilterAt(exceptionTranslationWebFilter, SecurityWebFiltersOrder.EXCEPTION_TRANSLATION);
                this.authorizeExchange.configure(this);
            }

            AnnotationAwareOrderComparator.sort(this.webFilters);
            List<WebFilter> sortedWebFilters = new ArrayList();
            this.webFilters.forEach((f) -> {
                if(f instanceof ServerHttpSecurity.OrderedWebFilter) {
                    f = ((ServerHttpSecurity.OrderedWebFilter)f).webFilter;
                }

                sortedWebFilters.add(f);
            });
            sortedWebFilters.add(0, new ServerHttpSecurity.ServerWebExchangeReactorContextWebFilter());
            return new MatcherSecurityWebFilterChain(this.getSecurityMatcher(), sortedWebFilters);
        }
    }

    private String buildToString() {
        try {
            StringWriter writer = new StringWriter();
            Throwable var2 = null;

            Object var5;
            try {
                PrintWriter printer = new PrintWriter(writer);
                Throwable var4 = null;

                try {
                    printer.println();
                    printer.println();
                    this.built.printStackTrace(printer);
                    printer.println();
                    printer.println();
                    var5 = writer.toString();
                } catch (Throwable var30) {
                    var5 = var30;
                    var4 = var30;
                    throw var30;
                } finally {
                    if(printer != null) {
                        if(var4 != null) {
                            try {
                                printer.close();
                            } catch (Throwable var29) {
                                var4.addSuppressed(var29);
                            }
                        } else {
                            printer.close();
                        }
                    }

                }
            } catch (Throwable var32) {
                var2 = var32;
                throw var32;
            } finally {
                if(writer != null) {
                    if(var2 != null) {
                        try {
                            writer.close();
                        } catch (Throwable var28) {
                            var2.addSuppressed(var28);
                        }
                    } else {
                        writer.close();
                    }
                }

            }

            return (String)var5;
        } catch (IOException var34) {
            throw new RuntimeException(var34);
        }
    }

    private ServerAuthenticationEntryPoint getAuthenticationEntryPoint() {
        if(this.authenticationEntryPoint == null && !this.defaultEntryPoints.isEmpty()) {
            if(this.defaultEntryPoints.size() == 1) {
                return ((DelegateEntry)this.defaultEntryPoints.get(0)).getEntryPoint();
            } else {
                DelegatingServerAuthenticationEntryPoint result = new DelegatingServerAuthenticationEntryPoint(this.defaultEntryPoints);
                result.setDefaultEntryPoint(((DelegateEntry)this.defaultEntryPoints.get(this.defaultEntryPoints.size() - 1)).getEntryPoint());
                return result;
            }
        } else {
            return this.authenticationEntryPoint;
        }
    }

    private ServerAccessDeniedHandler getAccessDeniedHandler() {
        if(this.accessDeniedHandler == null && !this.defaultAccessDeniedHandlers.isEmpty()) {
            if(this.defaultAccessDeniedHandlers.size() == 1) {
                return ((org.springframework.security.web.server.authorization.ServerWebExchangeDelegatingServerAccessDeniedHandler.DelegateEntry)this.defaultAccessDeniedHandlers.get(0)).getAccessDeniedHandler();
            } else {
                ServerWebExchangeDelegatingServerAccessDeniedHandler result = new ServerWebExchangeDelegatingServerAccessDeniedHandler(this.defaultAccessDeniedHandlers);
                result.setDefaultAccessDeniedHandler(((org.springframework.security.web.server.authorization.ServerWebExchangeDelegatingServerAccessDeniedHandler.DelegateEntry)this.defaultAccessDeniedHandlers.get(this.defaultAccessDeniedHandlers.size() - 1)).getAccessDeniedHandler());
                return result;
            }
        } else {
            return this.accessDeniedHandler;
        }
    }

    public static ServerHttpSecurity http() {
        return new ServerHttpSecurity();
    }

    private WebFilter securityContextRepositoryWebFilter() {
        ServerSecurityContextRepository repository = this.securityContextRepository;
        if(repository == null) {
            return null;
        } else {
            WebFilter result = new ReactorContextWebFilter(repository);
            return new ServerHttpSecurity.OrderedWebFilter(result, SecurityWebFiltersOrder.REACTOR_CONTEXT.getOrder());
        }
    }

    protected ServerHttpSecurity() {
    }

    private <T> T getBean(Class<T> beanClass) {
        return this.context == null?null:this.context.getBean(beanClass);
    }

    private <T> T getBeanOrNull(Class<T> beanClass) {
        return this.getBeanOrNull(ResolvableType.forClass(beanClass));
    }

    private <T> T getBeanOrNull(ResolvableType type) {
        if(this.context == null) {
            return null;
        } else {
            String[] names = this.context.getBeanNamesForType(type);
            return names.length == 1?this.context.getBean(names[0]):null;
        }
    }

    protected void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    static class ServerWebExchangeReactorContextWebFilter implements WebFilter {
        ServerWebExchangeReactorContextWebFilter() {
        }

        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            return chain.filter(exchange).subscriberContext(Context.of(ServerWebExchange.class, exchange));
        }
    }

    private static class OrderedWebFilter implements WebFilter, Ordered {
        private final WebFilter webFilter;
        private final int order;

        public OrderedWebFilter(WebFilter webFilter, int order) {
            this.webFilter = webFilter;
            this.order = order;
        }

        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            return this.webFilter.filter(exchange, chain);
        }

        public int getOrder() {
            return this.order;
        }

        public String toString() {
            return "OrderedWebFilter{webFilter=" + this.webFilter + ", order=" + this.order + '}';
        }
    }

    public final class LogoutSpec {
        private LogoutWebFilter logoutWebFilter;
        private List<ServerLogoutHandler> logoutHandlers;

        public ServerHttpSecurity.LogoutSpec logoutHandler(ServerLogoutHandler logoutHandler) {
            Assert.notNull(logoutHandler, "logoutHandler cannot be null");
            this.logoutHandlers.clear();
            return this.addLogoutHandler(logoutHandler);
        }

        private ServerHttpSecurity.LogoutSpec addLogoutHandler(ServerLogoutHandler logoutHandler) {
            Assert.notNull(logoutHandler, "logoutHandler cannot be null");
            this.logoutHandlers.add(logoutHandler);
            return this;
        }

        public ServerHttpSecurity.LogoutSpec logoutUrl(String logoutUrl) {
            Assert.notNull(logoutUrl, "logoutUrl must not be null");
            ServerWebExchangeMatcher requiresLogout = ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, new String[]{logoutUrl});
            return this.requiresLogout(requiresLogout);
        }

        public ServerHttpSecurity.LogoutSpec requiresLogout(ServerWebExchangeMatcher requiresLogout) {
            this.logoutWebFilter.setRequiresLogoutMatcher(requiresLogout);
            return this;
        }

        public ServerHttpSecurity.LogoutSpec logoutSuccessHandler(ServerLogoutSuccessHandler handler) {
            this.logoutWebFilter.setLogoutSuccessHandler(handler);
            return this;
        }

        public ServerHttpSecurity and() {
            return ServerHttpSecurity.this;
        }

        public ServerHttpSecurity disable() {
            ServerHttpSecurity.this.logout = null;
            return this.and();
        }

        private Optional<ServerLogoutHandler> createLogoutHandler() {
            return this.logoutHandlers.isEmpty()?Optional.empty():(this.logoutHandlers.size() == 1?Optional.of(this.logoutHandlers.get(0)):Optional.of(new DelegatingServerLogoutHandler(this.logoutHandlers)));
        }

        protected void configure(ServerHttpSecurity http) {
            Optional var10000 = this.createLogoutHandler();
            LogoutWebFilter var10001 = this.logoutWebFilter;
            this.logoutWebFilter.getClass();
            var10000.ifPresent(var10001::setLogoutHandler);
            http.addFilterAt(this.logoutWebFilter, SecurityWebFiltersOrder.LOGOUT);
        }

        private LogoutSpec() {
            this.logoutWebFilter = new LogoutWebFilter();
            this.logoutHandlers = new ArrayList(Arrays.asList(new SecurityContextServerLogoutHandler[]{new SecurityContextServerLogoutHandler()}));
        }
    }

    
    public class HeaderSpec {
        private final List<ServerHttpHeadersWriter> writers;
        private CacheControlServerHttpHeadersWriter cacheControl;
        private ContentTypeOptionsServerHttpHeadersWriter contentTypeOptions;
        private StrictTransportSecurityServerHttpHeadersWriter hsts;
        private XFrameOptionsServerHttpHeadersWriter frameOptions;
        private XXssProtectionServerHttpHeadersWriter xss;
        private FeaturePolicyServerHttpHeadersWriter featurePolicy;
        private ContentSecurityPolicyServerHttpHeadersWriter contentSecurityPolicy;
        private ReferrerPolicyServerHttpHeadersWriter referrerPolicy;

        public ServerHttpSecurity and() {
            return ServerHttpSecurity.this;
        }

        public ServerHttpSecurity disable() {
            ServerHttpSecurity.this.headers = null;
            return ServerHttpSecurity.this;
        }

        public ServerHttpSecurity.HeaderSpec.CacheSpec cache() {
            return new ServerHttpSecurity.HeaderSpec.CacheSpec();
        }

        public ServerHttpSecurity.HeaderSpec.ContentTypeOptionsSpec contentTypeOptions() {
            return new ServerHttpSecurity.HeaderSpec.ContentTypeOptionsSpec();
        }

        public ServerHttpSecurity.HeaderSpec.FrameOptionsSpec frameOptions() {
            return new ServerHttpSecurity.HeaderSpec.FrameOptionsSpec();
        }

        public ServerHttpSecurity.HeaderSpec.HstsSpec hsts() {
            return new ServerHttpSecurity.HeaderSpec.HstsSpec();
        }

        protected void configure(ServerHttpSecurity http) {
            ServerHttpHeadersWriter writer = new CompositeServerHttpHeadersWriter(this.writers);
            HttpHeaderWriterWebFilter result = new HttpHeaderWriterWebFilter(writer);
            http.addFilterAt(result, SecurityWebFiltersOrder.HTTP_HEADERS_WRITER);
        }

        public ServerHttpSecurity.HeaderSpec.XssProtectionSpec xssProtection() {
            return new ServerHttpSecurity.HeaderSpec.XssProtectionSpec();
        }

        public ServerHttpSecurity.HeaderSpec.ContentSecurityPolicySpec contentSecurityPolicy(String policyDirectives) {
            return new ServerHttpSecurity.HeaderSpec.ContentSecurityPolicySpec(policyDirectives);
        }

        public ServerHttpSecurity.HeaderSpec.FeaturePolicySpec featurePolicy(String policyDirectives) {
            return new ServerHttpSecurity.HeaderSpec.FeaturePolicySpec(policyDirectives);
        }

        public ServerHttpSecurity.HeaderSpec.ReferrerPolicySpec referrerPolicy(ReferrerPolicy referrerPolicy) {
            return new ServerHttpSecurity.HeaderSpec.ReferrerPolicySpec(referrerPolicy);
        }

        public ServerHttpSecurity.HeaderSpec.ReferrerPolicySpec referrerPolicy() {
            return new ServerHttpSecurity.HeaderSpec.ReferrerPolicySpec();
        }

        private HeaderSpec() {
            this.cacheControl = new CacheControlServerHttpHeadersWriter();
            this.contentTypeOptions = new ContentTypeOptionsServerHttpHeadersWriter();
            this.hsts = new StrictTransportSecurityServerHttpHeadersWriter();
            this.frameOptions = new XFrameOptionsServerHttpHeadersWriter();
            this.xss = new XXssProtectionServerHttpHeadersWriter();
            this.featurePolicy = new FeaturePolicyServerHttpHeadersWriter();
            this.contentSecurityPolicy = new ContentSecurityPolicyServerHttpHeadersWriter();
            this.referrerPolicy = new ReferrerPolicyServerHttpHeadersWriter();
            this.writers = new ArrayList(Arrays.asList(new ServerHttpHeadersWriter[]{this.cacheControl, this.contentTypeOptions, this.hsts, this.frameOptions, this.xss, this.featurePolicy, this.contentSecurityPolicy, this.referrerPolicy}));
        }

        public class ReferrerPolicySpec {
            public ServerHttpSecurity.HeaderSpec and() {
                return HeaderSpec.this;
            }

            private ReferrerPolicySpec() {
            }

            private ReferrerPolicySpec(ReferrerPolicy referrerPolicy) {
                HeaderSpec.this.referrerPolicy.setPolicy(referrerPolicy);
            }
        }

        public class FeaturePolicySpec {
            public ServerHttpSecurity.HeaderSpec and() {
                return HeaderSpec.this;
            }

            private FeaturePolicySpec(String policyDirectives) {
                HeaderSpec.this.featurePolicy.setPolicyDirectives(policyDirectives);
            }
        }

        public class ContentSecurityPolicySpec {
            public ServerHttpSecurity.HeaderSpec reportOnly(boolean reportOnly) {
                HeaderSpec.this.contentSecurityPolicy.setReportOnly(reportOnly);
                return HeaderSpec.this;
            }

            public ServerHttpSecurity.HeaderSpec and() {
                return HeaderSpec.this;
            }

            private ContentSecurityPolicySpec(String policyDirectives) {
                HeaderSpec.this.contentSecurityPolicy.setPolicyDirectives(policyDirectives);
            }
        }

        public class XssProtectionSpec {
            public ServerHttpSecurity.HeaderSpec disable() {
                HeaderSpec.this.writers.remove(HeaderSpec.this.xss);
                return HeaderSpec.this;
            }

            private XssProtectionSpec() {
            }
        }

        public class HstsSpec {
            public ServerHttpSecurity.HeaderSpec.HstsSpec maxAge(Duration maxAge) {
                HeaderSpec.this.hsts.setMaxAge(maxAge);
                return this;
            }

            public ServerHttpSecurity.HeaderSpec.HstsSpec includeSubdomains(boolean includeSubDomains) {
                HeaderSpec.this.hsts.setIncludeSubDomains(includeSubDomains);
                return this;
            }

            public ServerHttpSecurity.HeaderSpec and() {
                return HeaderSpec.this;
            }

            public ServerHttpSecurity.HeaderSpec disable() {
                HeaderSpec.this.writers.remove(HeaderSpec.this.hsts);
                return HeaderSpec.this;
            }

            private HstsSpec() {
            }
        }

        public class FrameOptionsSpec {
            public ServerHttpSecurity.HeaderSpec mode(Mode mode) {
                HeaderSpec.this.frameOptions.setMode(mode);
                return this.and();
            }

            private ServerHttpSecurity.HeaderSpec and() {
                return HeaderSpec.this;
            }

            public ServerHttpSecurity.HeaderSpec disable() {
                HeaderSpec.this.writers.remove(HeaderSpec.this.frameOptions);
                return this.and();
            }

            private FrameOptionsSpec() {
            }
        }

        public class ContentTypeOptionsSpec {
            public ServerHttpSecurity.HeaderSpec disable() {
                HeaderSpec.this.writers.remove(HeaderSpec.this.contentTypeOptions);
                return HeaderSpec.this;
            }

            private ContentTypeOptionsSpec() {
            }
        }

        public class CacheSpec {
            public ServerHttpSecurity.HeaderSpec disable() {
                HeaderSpec.this.writers.remove(HeaderSpec.this.cacheControl);
                return HeaderSpec.this;
            }

            private CacheSpec() {
            }
        }
    }

    private class LoginPageSpec {
        protected void configure(ServerHttpSecurity http) {
            if(http.authenticationEntryPoint == null) {
                if(http.formLogin == null || !http.formLogin.isEntryPointExplicit) {
                    LoginPageGeneratingWebFilter loginPage = null;
                    if(http.formLogin != null && !http.formLogin.isEntryPointExplicit) {
                        loginPage = new LoginPageGeneratingWebFilter();
                        loginPage.setFormLoginEnabled(true);
                    }

                    if(http.oauth2Login != null) {
                        Map<String, String> urlToText = http.oauth2Login.getLinks();
                        if(loginPage == null) {
                            loginPage = new LoginPageGeneratingWebFilter();
                        }

                        loginPage.setOauth2AuthenticationUrlToClientName(urlToText);
                    }

                    if(loginPage != null) {
                        http.addFilterAt(loginPage, SecurityWebFiltersOrder.LOGIN_PAGE_GENERATING);
                        http.addFilterAt(new LogoutPageGeneratingWebFilter(), SecurityWebFiltersOrder.LOGOUT_PAGE_GENERATING);
                    }

                }
            }
        }

        private LoginPageSpec() {
        }
    }

    public class FormLoginSpec {
        private final RedirectServerAuthenticationSuccessHandler defaultSuccessHandler;
        private RedirectServerAuthenticationEntryPoint defaultEntryPoint;
        private ReactiveAuthenticationManager authenticationManager;
        private ServerSecurityContextRepository securityContextRepository;
        private ServerAuthenticationEntryPoint authenticationEntryPoint;
        private boolean isEntryPointExplicit;
        private ServerWebExchangeMatcher requiresAuthenticationMatcher;
        private ServerAuthenticationFailureHandler authenticationFailureHandler;
        private ServerAuthenticationSuccessHandler authenticationSuccessHandler;

        public ServerHttpSecurity.FormLoginSpec authenticationManager(ReactiveAuthenticationManager authenticationManager) {
            this.authenticationManager = authenticationManager;
            return this;
        }

        public ServerHttpSecurity.FormLoginSpec authenticationSuccessHandler(ServerAuthenticationSuccessHandler authenticationSuccessHandler) {
            Assert.notNull(authenticationSuccessHandler, "authenticationSuccessHandler cannot be null");
            this.authenticationSuccessHandler = authenticationSuccessHandler;
            return this;
        }

        public ServerHttpSecurity.FormLoginSpec loginPage(String loginPage) {
            this.defaultEntryPoint = new RedirectServerAuthenticationEntryPoint(loginPage);
            this.authenticationEntryPoint = this.defaultEntryPoint;
            this.requiresAuthenticationMatcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, new String[]{loginPage});
            this.authenticationFailureHandler = new RedirectServerAuthenticationFailureHandler(loginPage + "?error");
            return this;
        }

        public ServerHttpSecurity.FormLoginSpec authenticationEntryPoint(ServerAuthenticationEntryPoint authenticationEntryPoint) {
            this.authenticationEntryPoint = authenticationEntryPoint;
            return this;
        }

        public ServerHttpSecurity.FormLoginSpec requiresAuthenticationMatcher(ServerWebExchangeMatcher requiresAuthenticationMatcher) {
            this.requiresAuthenticationMatcher = requiresAuthenticationMatcher;
            return this;
        }

        public ServerHttpSecurity.FormLoginSpec authenticationFailureHandler(ServerAuthenticationFailureHandler authenticationFailureHandler) {
            this.authenticationFailureHandler = authenticationFailureHandler;
            return this;
        }

        public ServerHttpSecurity.FormLoginSpec securityContextRepository(ServerSecurityContextRepository securityContextRepository) {
            this.securityContextRepository = securityContextRepository;
            return this;
        }

        public ServerHttpSecurity and() {
            return ServerHttpSecurity.this;
        }

        public ServerHttpSecurity disable() {
            ServerHttpSecurity.this.formLogin = null;
            return ServerHttpSecurity.this;
        }

        protected void configure(ServerHttpSecurity http) {
            if(this.authenticationEntryPoint == null) {
                this.isEntryPointExplicit = false;
                this.loginPage("/login");
            } else {
                this.isEntryPointExplicit = true;
            }

            if(http.requestCache != null) {
                ServerRequestCache requestCache = http.requestCache.requestCache;
                this.defaultSuccessHandler.setRequestCache(requestCache);
                if(this.defaultEntryPoint != null) {
                    this.defaultEntryPoint.setRequestCache(requestCache);
                }
            }

            MediaTypeServerWebExchangeMatcher htmlMatcher = new MediaTypeServerWebExchangeMatcher(new MediaType[]{MediaType.TEXT_HTML});
            htmlMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
            ServerHttpSecurity.this.defaultEntryPoints.add(0, new DelegateEntry(htmlMatcher, this.authenticationEntryPoint));
            AuthenticationWebFilter authenticationFilter = new AuthenticationWebFilter(this.authenticationManager);
            authenticationFilter.setRequiresAuthenticationMatcher(this.requiresAuthenticationMatcher);
            authenticationFilter.setAuthenticationFailureHandler(this.authenticationFailureHandler);
            authenticationFilter.setAuthenticationConverter(new ServerFormLoginAuthenticationConverter());
            authenticationFilter.setAuthenticationSuccessHandler(this.authenticationSuccessHandler);
            authenticationFilter.setSecurityContextRepository(this.securityContextRepository);
            http.addFilterAt(authenticationFilter, SecurityWebFiltersOrder.FORM_LOGIN);
        }

        private FormLoginSpec() {
            this.defaultSuccessHandler = new RedirectServerAuthenticationSuccessHandler("/");
            this.securityContextRepository = new WebSessionServerSecurityContextRepository();
            this.authenticationSuccessHandler = this.defaultSuccessHandler;
        }
    }

    public class HttpBasicSpec {
        private ReactiveAuthenticationManager authenticationManager;
        private ServerSecurityContextRepository securityContextRepository;
        private ServerAuthenticationEntryPoint entryPoint;

        public ServerHttpSecurity.HttpBasicSpec authenticationManager(ReactiveAuthenticationManager authenticationManager) {
            this.authenticationManager = authenticationManager;
            return this;
        }

        public ServerHttpSecurity.HttpBasicSpec securityContextRepository(ServerSecurityContextRepository securityContextRepository) {
            this.securityContextRepository = securityContextRepository;
            return this;
        }

        public ServerHttpSecurity and() {
            return ServerHttpSecurity.this;
        }

        public ServerHttpSecurity disable() {
            ServerHttpSecurity.this.httpBasic = null;
            return ServerHttpSecurity.this;
        }

        protected void configure(ServerHttpSecurity http) {
            MediaTypeServerWebExchangeMatcher restMatcher = new MediaTypeServerWebExchangeMatcher(new MediaType[]{MediaType.APPLICATION_ATOM_XML, MediaType.APPLICATION_FORM_URLENCODED, MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM, MediaType.APPLICATION_XML, MediaType.MULTIPART_FORM_DATA, MediaType.TEXT_XML});
            restMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
            ServerHttpSecurity.this.defaultEntryPoints.add(new DelegateEntry(restMatcher, this.entryPoint));
            AuthenticationWebFilter authenticationFilter = new AuthenticationWebFilter(this.authenticationManager);
            authenticationFilter.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(this.entryPoint));
            authenticationFilter.setAuthenticationConverter(new ServerHttpBasicAuthenticationConverter());
            if(this.securityContextRepository != null) {
                authenticationFilter.setSecurityContextRepository(this.securityContextRepository);
            }

            http.addFilterAt(authenticationFilter, SecurityWebFiltersOrder.HTTP_BASIC);
        }

        private HttpBasicSpec() {
            this.securityContextRepository = NoOpServerSecurityContextRepository.getInstance();
            this.entryPoint = new HttpBasicServerAuthenticationEntryPoint();
        }
    }

    public class RequestCacheSpec {
        private ServerRequestCache requestCache;

        public ServerHttpSecurity.RequestCacheSpec requestCache(ServerRequestCache requestCache) {
            Assert.notNull(requestCache, "requestCache cannot be null");
            this.requestCache = requestCache;
            return this;
        }

        protected void configure(ServerHttpSecurity http) {
            ServerRequestCacheWebFilter filter = new ServerRequestCacheWebFilter();
            filter.setRequestCache(this.requestCache);
            http.addFilterAt(filter, SecurityWebFiltersOrder.SERVER_REQUEST_CACHE);
        }

        public ServerHttpSecurity and() {
            return ServerHttpSecurity.this;
        }

        public ServerHttpSecurity disable() {
            this.requestCache = NoOpServerRequestCache.getInstance();
            return this.and();
        }

        private RequestCacheSpec() {
            this.requestCache = new WebSessionServerRequestCache();
        }
    }

    public class ExceptionHandlingSpec {
        public ServerHttpSecurity.ExceptionHandlingSpec authenticationEntryPoint(ServerAuthenticationEntryPoint authenticationEntryPoint) {
            ServerHttpSecurity.this.authenticationEntryPoint = authenticationEntryPoint;
            return this;
        }

        public ServerHttpSecurity.ExceptionHandlingSpec accessDeniedHandler(ServerAccessDeniedHandler accessDeniedHandler) {
            ServerHttpSecurity.this.accessDeniedHandler = accessDeniedHandler;
            return this;
        }

        public ServerHttpSecurity and() {
            return ServerHttpSecurity.this;
        }

        private ExceptionHandlingSpec() {
        }
    }

    public class CsrfSpec {
        private CsrfWebFilter filter;
        private ServerCsrfTokenRepository csrfTokenRepository;
        private boolean specifiedRequireCsrfProtectionMatcher;

        public ServerHttpSecurity.CsrfSpec accessDeniedHandler(ServerAccessDeniedHandler accessDeniedHandler) {
            this.filter.setAccessDeniedHandler(accessDeniedHandler);
            return this;
        }

        public ServerHttpSecurity.CsrfSpec csrfTokenRepository(ServerCsrfTokenRepository csrfTokenRepository) {
            this.csrfTokenRepository = csrfTokenRepository;
            return this;
        }

        public ServerHttpSecurity.CsrfSpec requireCsrfProtectionMatcher(ServerWebExchangeMatcher requireCsrfProtectionMatcher) {
            this.filter.setRequireCsrfProtectionMatcher(requireCsrfProtectionMatcher);
            this.specifiedRequireCsrfProtectionMatcher = true;
            return this;
        }

        public ServerHttpSecurity and() {
            return ServerHttpSecurity.this;
        }

        public ServerHttpSecurity disable() {
            ServerHttpSecurity.this.csrf = null;
            return ServerHttpSecurity.this;
        }

        protected void configure(ServerHttpSecurity http) {
            Optional.ofNullable(this.csrfTokenRepository).ifPresent((serverCsrfTokenRepository) -> {
                this.filter.setCsrfTokenRepository(serverCsrfTokenRepository);
                http.logout().addLogoutHandler(new CsrfServerLogoutHandler(serverCsrfTokenRepository));
            });
            http.addFilterAt(this.filter, SecurityWebFiltersOrder.CSRF);
        }

        private CsrfSpec() {
            this.filter = new CsrfWebFilter();
            this.csrfTokenRepository = new WebSessionServerCsrfTokenRepository();
        }
    }

    public class HttpsRedirectSpec {
        private ServerWebExchangeMatcher serverWebExchangeMatcher;
        private PortMapper portMapper;

        public HttpsRedirectSpec() {
        }

        public ServerHttpSecurity.HttpsRedirectSpec httpsRedirectWhen(ServerWebExchangeMatcher... matchers) {
            this.serverWebExchangeMatcher = new OrServerWebExchangeMatcher(matchers);
            return this;
        }

        public ServerHttpSecurity.HttpsRedirectSpec httpsRedirectWhen(Function<ServerWebExchange, Boolean> when) {
            ServerWebExchangeMatcher matcher = (e) -> {
                return ((Boolean)when.apply(e)).booleanValue()?MatchResult.match():MatchResult.notMatch();
            };
            return this.httpsRedirectWhen(new ServerWebExchangeMatcher[]{matcher});
        }

        public ServerHttpSecurity.HttpsRedirectSpec portMapper(PortMapper portMapper) {
            this.portMapper = portMapper;
            return this;
        }

        protected void configure(ServerHttpSecurity http) {
            HttpsRedirectWebFilter httpsRedirectWebFilter = new HttpsRedirectWebFilter();
            if(this.serverWebExchangeMatcher != null) {
                httpsRedirectWebFilter.setRequiresHttpsRedirectMatcher(this.serverWebExchangeMatcher);
            }

            if(this.portMapper != null) {
                httpsRedirectWebFilter.setPortMapper(this.portMapper);
            }

            http.addFilterAt(httpsRedirectWebFilter, SecurityWebFiltersOrder.HTTPS_REDIRECT);
        }

        public ServerHttpSecurity and() {
            return ServerHttpSecurity.this;
        }
    }

    public class AuthorizeExchangeSpec extends AbstractServerWebExchangeMatcherRegistry<ServerHttpSecurity.AuthorizeExchangeSpec.Access> {
        private Builder managerBldr = DelegatingReactiveAuthorizationManager.builder();
        private ServerWebExchangeMatcher matcher;
        private boolean anyExchangeRegistered;

        public AuthorizeExchangeSpec() {
        }

        public ServerHttpSecurity and() {
            return ServerHttpSecurity.this;
        }

        public ServerHttpSecurity.AuthorizeExchangeSpec.Access anyExchange() {
            ServerHttpSecurity.AuthorizeExchangeSpec.Access result = (ServerHttpSecurity.AuthorizeExchangeSpec.Access)super.anyExchange();
            this.anyExchangeRegistered = true;
            return result;
        }

        protected ServerHttpSecurity.AuthorizeExchangeSpec.Access registerMatcher(ServerWebExchangeMatcher matcher) {
            if(this.anyExchangeRegistered) {
                throw new IllegalStateException("Cannot register " + matcher + " which would be unreachable because anyExchange() has already been registered.");
            } else if(this.matcher != null) {
                throw new IllegalStateException("The matcher " + matcher + " does not have an access rule defined");
            } else {
                this.matcher = matcher;
                return new ServerHttpSecurity.AuthorizeExchangeSpec.Access();
            }
        }

        protected void configure(ServerHttpSecurity http) {
            if(this.matcher != null) {
                throw new IllegalStateException("The matcher " + this.matcher + " does not have an access rule defined");
            } else {
                AuthorizationWebFilter result = new AuthorizationWebFilter(this.managerBldr.build());
                http.addFilterAt(result, SecurityWebFiltersOrder.AUTHORIZATION);
            }
        }

        public final class Access {
            public Access() {
            }

            public ServerHttpSecurity.AuthorizeExchangeSpec permitAll() {
                return this.access((a, e) -> {
                    return Mono.just(new AuthorizationDecision(true));
                });
            }

            public ServerHttpSecurity.AuthorizeExchangeSpec denyAll() {
                return this.access((a, e) -> {
                    return Mono.just(new AuthorizationDecision(false));
                });
            }

            public ServerHttpSecurity.AuthorizeExchangeSpec hasRole(String role) {
                return this.access(AuthorityReactiveAuthorizationManager.hasRole(role));
            }

            public ServerHttpSecurity.AuthorizeExchangeSpec hasAuthority(String authority) {
                return this.access(AuthorityReactiveAuthorizationManager.hasAuthority(authority));
            }

            public ServerHttpSecurity.AuthorizeExchangeSpec authenticated() {
                return this.access(AuthenticatedReactiveAuthorizationManager.authenticated());
            }

            public ServerHttpSecurity.AuthorizeExchangeSpec access(ReactiveAuthorizationManager<AuthorizationContext> manager) {
                AuthorizeExchangeSpec.this.managerBldr.add(new ServerWebExchangeMatcherEntry(AuthorizeExchangeSpec.this.matcher, manager));
                AuthorizeExchangeSpec.this.matcher = null;
                return AuthorizeExchangeSpec.this;
            }
        }
    }

    public class OAuth2ResourceServerSpec {
        private BearerTokenServerAuthenticationEntryPoint entryPoint = new BearerTokenServerAuthenticationEntryPoint();
        private BearerTokenServerAccessDeniedHandler accessDeniedHandler = new BearerTokenServerAccessDeniedHandler();
        private ServerAuthenticationConverter bearerTokenConverter = new ServerBearerTokenAuthenticationConverter();
        private ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec jwt;

        public OAuth2ResourceServerSpec() {
        }

        public ServerHttpSecurity.OAuth2ResourceServerSpec bearerTokenConverter(ServerAuthenticationConverter bearerTokenConverter) {
            Assert.notNull(bearerTokenConverter, "bearerTokenConverter cannot be null");
            this.bearerTokenConverter = bearerTokenConverter;
            return this;
        }

        public ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec jwt() {
            if(this.jwt == null) {
                this.jwt = new ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec();
            }

            return this.jwt;
        }

        protected void configure(ServerHttpSecurity http) {
            if(this.jwt != null) {
                this.jwt.configure(http);
            }

        }

        public ServerHttpSecurity and() {
            return ServerHttpSecurity.this;
        }

        public class JwtSpec {
            private ReactiveAuthenticationManager authenticationManager;
            private ReactiveJwtDecoder jwtDecoder;
            private Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter = new ReactiveJwtAuthenticationConverterAdapter(new JwtAuthenticationConverter());
            private ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec.BearerTokenServerWebExchangeMatcher bearerTokenServerWebExchangeMatcher = new ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec.BearerTokenServerWebExchangeMatcher();

            public JwtSpec() {
            }

            public ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec authenticationManager(ReactiveAuthenticationManager authenticationManager) {
                Assert.notNull(authenticationManager, "authenticationManager cannot be null");
                this.authenticationManager = authenticationManager;
                return this;
            }

            public ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec jwtAuthenticationConverter(Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter) {
                Assert.notNull(jwtAuthenticationConverter, "jwtAuthenticationConverter cannot be null");
                this.jwtAuthenticationConverter = jwtAuthenticationConverter;
                return this;
            }

            public ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec jwtDecoder(ReactiveJwtDecoder jwtDecoder) {
                this.jwtDecoder = jwtDecoder;
                return this;
            }

            public ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec publicKey(RSAPublicKey publicKey) {
                this.jwtDecoder = new NimbusReactiveJwtDecoder(publicKey);
                return this;
            }

            public ServerHttpSecurity.OAuth2ResourceServerSpec.JwtSpec jwkSetUri(String jwkSetUri) {
                this.jwtDecoder = new NimbusReactiveJwtDecoder(jwkSetUri);
                return this;
            }

            public ServerHttpSecurity.OAuth2ResourceServerSpec and() {
                return OAuth2ResourceServerSpec.this;
            }

            protected void configure(ServerHttpSecurity http) {
                this.bearerTokenServerWebExchangeMatcher.setBearerTokenConverter(OAuth2ResourceServerSpec.this.bearerTokenConverter);
                this.registerDefaultAccessDeniedHandler(http);
                this.registerDefaultAuthenticationEntryPoint(http);
                this.registerDefaultCsrfOverride(http);
                ReactiveAuthenticationManager authenticationManager = this.getAuthenticationManager();
                AuthenticationWebFilter oauth2 = new AuthenticationWebFilter(authenticationManager);
                oauth2.setServerAuthenticationConverter(OAuth2ResourceServerSpec.this.bearerTokenConverter);
                oauth2.setAuthenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(OAuth2ResourceServerSpec.this.entryPoint));
                http.addFilterAt(oauth2, SecurityWebFiltersOrder.AUTHENTICATION);
            }

            protected ReactiveJwtDecoder getJwtDecoder() {
                return this.jwtDecoder == null?(ReactiveJwtDecoder)ServerHttpSecurity.this.getBean(ReactiveJwtDecoder.class):this.jwtDecoder;
            }

            protected Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> getJwtAuthenticationConverter() {
                return this.jwtAuthenticationConverter;
            }

            private ReactiveAuthenticationManager getAuthenticationManager() {
                if(this.authenticationManager != null) {
                    return this.authenticationManager;
                } else {
                    ReactiveJwtDecoder jwtDecoder = this.getJwtDecoder();
                    Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter = this.getJwtAuthenticationConverter();
                    JwtReactiveAuthenticationManager authenticationManager = new JwtReactiveAuthenticationManager(jwtDecoder);
                    authenticationManager.setJwtAuthenticationConverter(jwtAuthenticationConverter);
                    return authenticationManager;
                }
            }

            private void registerDefaultAccessDeniedHandler(ServerHttpSecurity http) {
                if(http.exceptionHandling != null) {
                    http.defaultAccessDeniedHandlers.add(new org.springframework.security.web.server.authorization.ServerWebExchangeDelegatingServerAccessDeniedHandler.DelegateEntry(this.bearerTokenServerWebExchangeMatcher, new BearerTokenServerAccessDeniedHandler()));
                }

            }

            private void registerDefaultAuthenticationEntryPoint(ServerHttpSecurity http) {
                if(http.exceptionHandling != null) {
                    http.defaultEntryPoints.add(new DelegateEntry(this.bearerTokenServerWebExchangeMatcher, new BearerTokenServerAuthenticationEntryPoint()));
                }

            }

            private void registerDefaultCsrfOverride(ServerHttpSecurity http) {
                if(http.csrf != null && !http.csrf.specifiedRequireCsrfProtectionMatcher) {
                    http.csrf().requireCsrfProtectionMatcher(new AndServerWebExchangeMatcher(new ServerWebExchangeMatcher[]{CsrfWebFilter.DEFAULT_CSRF_MATCHER, new NegatedServerWebExchangeMatcher(this.bearerTokenServerWebExchangeMatcher)}));
                }

            }

            private class BearerTokenServerWebExchangeMatcher implements ServerWebExchangeMatcher {
                ServerAuthenticationConverter bearerTokenConverter;

                private BearerTokenServerWebExchangeMatcher() {
                }

                public Mono<MatchResult> matches(ServerWebExchange exchange) {
                    return this.bearerTokenConverter.convert(exchange).flatMap(this::nullAuthentication).onErrorResume((e) -> {
                        return MatchResult.notMatch();
                    });
                }

                public void setBearerTokenConverter(ServerAuthenticationConverter bearerTokenConverter) {
                    Assert.notNull(bearerTokenConverter, "bearerTokenConverter cannot be null");
                    this.bearerTokenConverter = bearerTokenConverter;
                }

                private Mono<MatchResult> nullAuthentication(Authentication authentication) {
                    return authentication == null?MatchResult.notMatch():MatchResult.match();
                }
            }
        }
    }

    public class OAuth2ClientSpec {
        private ReactiveClientRegistrationRepository clientRegistrationRepository;
        private ServerAuthenticationConverter authenticationConverter;
        private ServerOAuth2AuthorizedClientRepository authorizedClientRepository;
        private ReactiveAuthenticationManager authenticationManager;

        public ServerHttpSecurity.OAuth2ClientSpec authenticationConverter(ServerAuthenticationConverter authenticationConverter) {
            this.authenticationConverter = authenticationConverter;
            return this;
        }

        private ServerAuthenticationConverter getAuthenticationConverter() {
            if(this.authenticationConverter == null) {
                this.authenticationConverter = new ServerOAuth2AuthorizationCodeAuthenticationTokenConverter(this.getClientRegistrationRepository());
            }

            return this.authenticationConverter;
        }

        public ServerHttpSecurity.OAuth2ClientSpec authenticationManager(ReactiveAuthenticationManager authenticationManager) {
            this.authenticationManager = authenticationManager;
            return this;
        }

        private ReactiveAuthenticationManager getAuthenticationManager() {
            if(this.authenticationManager == null) {
                this.authenticationManager = new OAuth2AuthorizationCodeReactiveAuthenticationManager(new WebClientReactiveAuthorizationCodeTokenResponseClient());
            }

            return this.authenticationManager;
        }

        public ServerHttpSecurity.OAuth2ClientSpec clientRegistrationRepository(ReactiveClientRegistrationRepository clientRegistrationRepository) {
            this.clientRegistrationRepository = clientRegistrationRepository;
            return this;
        }

        public ServerHttpSecurity.OAuth2ClientSpec authorizedClientRepository(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
            this.authorizedClientRepository = authorizedClientRepository;
            return this;
        }

        public ServerHttpSecurity and() {
            return ServerHttpSecurity.this;
        }

        protected void configure(ServerHttpSecurity http) {
            ReactiveClientRegistrationRepository clientRegistrationRepository = this.getClientRegistrationRepository();
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository = this.getAuthorizedClientRepository();
            ServerAuthenticationConverter authenticationConverter = this.getAuthenticationConverter();
            ReactiveAuthenticationManager authenticationManager = this.getAuthenticationManager();
            OAuth2AuthorizationCodeGrantWebFilter codeGrantWebFilter = new OAuth2AuthorizationCodeGrantWebFilter(authenticationManager, authenticationConverter, authorizedClientRepository);
            OAuth2AuthorizationRequestRedirectWebFilter oauthRedirectFilter = new OAuth2AuthorizationRequestRedirectWebFilter(clientRegistrationRepository);
            http.addFilterAt(codeGrantWebFilter, SecurityWebFiltersOrder.OAUTH2_AUTHORIZATION_CODE);
            http.addFilterAt(oauthRedirectFilter, SecurityWebFiltersOrder.HTTP_BASIC);
        }

        private ReactiveClientRegistrationRepository getClientRegistrationRepository() {
            return this.clientRegistrationRepository != null?this.clientRegistrationRepository:(ReactiveClientRegistrationRepository)ServerHttpSecurity.this.getBeanOrNull(ReactiveClientRegistrationRepository.class);
        }

        private ServerOAuth2AuthorizedClientRepository getAuthorizedClientRepository() {
            if(this.authorizedClientRepository != null) {
                return this.authorizedClientRepository;
            } else {
                ServerOAuth2AuthorizedClientRepository result = (ServerOAuth2AuthorizedClientRepository)ServerHttpSecurity.this.getBeanOrNull(ServerOAuth2AuthorizedClientRepository.class);
                if(result == null) {
                    ReactiveOAuth2AuthorizedClientService authorizedClientService = this.getAuthorizedClientService();
                    if(authorizedClientService != null) {
                        result = new AuthenticatedPrincipalServerOAuth2AuthorizedClientRepository(authorizedClientService);
                    }
                }

                return (ServerOAuth2AuthorizedClientRepository)result;
            }
        }

        private ReactiveOAuth2AuthorizedClientService getAuthorizedClientService() {
            ReactiveOAuth2AuthorizedClientService service = (ReactiveOAuth2AuthorizedClientService)ServerHttpSecurity.this.getBeanOrNull(ReactiveOAuth2AuthorizedClientService.class);
            if(service == null) {
                service = new InMemoryReactiveOAuth2AuthorizedClientService(this.getClientRegistrationRepository());
            }

            return (ReactiveOAuth2AuthorizedClientService)service;
        }

        private OAuth2ClientSpec() {
        }
    }

    public class OAuth2LoginSpec {
        private ReactiveClientRegistrationRepository clientRegistrationRepository;
        private ServerOAuth2AuthorizedClientRepository authorizedClientRepository;
        private ReactiveAuthenticationManager authenticationManager;
        private ServerAuthenticationConverter authenticationConverter;

        public ServerHttpSecurity.OAuth2LoginSpec authenticationManager(ReactiveAuthenticationManager authenticationManager) {
            this.authenticationManager = authenticationManager;
            return this;
        }

        private ReactiveAuthenticationManager getAuthenticationManager() {
            if(this.authenticationManager == null) {
                this.authenticationManager = this.createDefault();
            }

            return this.authenticationManager;
        }

        private ReactiveAuthenticationManager createDefault() {
            WebClientReactiveAuthorizationCodeTokenResponseClient client = new WebClientReactiveAuthorizationCodeTokenResponseClient();
            ReactiveAuthenticationManager result = new OAuth2LoginReactiveAuthenticationManager(client, this.getOauth2UserService());
            boolean oidcAuthenticationProviderEnabled = ClassUtils.isPresent("org.springframework.security.oauth2.jwt.JwtDecoder", this.getClass().getClassLoader());
            if(oidcAuthenticationProviderEnabled) {
                OidcAuthorizationCodeReactiveAuthenticationManager oidc = new OidcAuthorizationCodeReactiveAuthenticationManager(client, this.getOidcUserService());
                result = new DelegatingReactiveAuthenticationManager(new ReactiveAuthenticationManager[]{oidc, (ReactiveAuthenticationManager)result});
            }

            return (ReactiveAuthenticationManager)result;
        }

        public ServerHttpSecurity.OAuth2LoginSpec authenticationConverter(ServerAuthenticationConverter authenticationConverter) {
            this.authenticationConverter = authenticationConverter;
            return this;
        }

        private ServerAuthenticationConverter getAuthenticationConverter(ReactiveClientRegistrationRepository clientRegistrationRepository) {
            if(this.authenticationConverter == null) {
                this.authenticationConverter = new ServerOAuth2AuthorizationCodeAuthenticationTokenConverter(clientRegistrationRepository);
            }

            return this.authenticationConverter;
        }

        public ServerHttpSecurity.OAuth2LoginSpec clientRegistrationRepository(ReactiveClientRegistrationRepository clientRegistrationRepository) {
            this.clientRegistrationRepository = clientRegistrationRepository;
            return this;
        }

        public ServerHttpSecurity.OAuth2LoginSpec authorizedClientService(ReactiveOAuth2AuthorizedClientService authorizedClientService) {
            this.authorizedClientRepository = new AuthenticatedPrincipalServerOAuth2AuthorizedClientRepository(authorizedClientService);
            return this;
        }

        public ServerHttpSecurity.OAuth2LoginSpec authorizedClientRepository(ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {
            this.authorizedClientRepository = authorizedClientRepository;
            return this;
        }

        public ServerHttpSecurity and() {
            return ServerHttpSecurity.this;
        }

        protected void configure(ServerHttpSecurity http) {
            ReactiveClientRegistrationRepository clientRegistrationRepository = this.getClientRegistrationRepository();
            ServerOAuth2AuthorizedClientRepository authorizedClientRepository = this.getAuthorizedClientRepository();
            OAuth2AuthorizationRequestRedirectWebFilter oauthRedirectFilter = new OAuth2AuthorizationRequestRedirectWebFilter(clientRegistrationRepository);
            ReactiveAuthenticationManager manager = this.getAuthenticationManager();
            AuthenticationWebFilter authenticationFilter = new OAuth2LoginAuthenticationWebFilter(manager, authorizedClientRepository);
            authenticationFilter.setRequiresAuthenticationMatcher(this.createAttemptAuthenticationRequestMatcher());
            authenticationFilter.setServerAuthenticationConverter(this.getAuthenticationConverter(clientRegistrationRepository));
            RedirectServerAuthenticationSuccessHandler redirectHandler = new RedirectServerAuthenticationSuccessHandler();
            authenticationFilter.setAuthenticationSuccessHandler(redirectHandler);
            authenticationFilter.setAuthenticationFailureHandler(new ServerAuthenticationFailureHandler() {
                public Mono<Void> onAuthenticationFailure(WebFilterExchange webFilterExchange, AuthenticationException exception) {
                    return Mono.error(exception);
                }
            });
            authenticationFilter.setSecurityContextRepository(new WebSessionServerSecurityContextRepository());
            MediaTypeServerWebExchangeMatcher htmlMatcher = new MediaTypeServerWebExchangeMatcher(new MediaType[]{MediaType.TEXT_HTML});
            htmlMatcher.setIgnoredMediaTypes(Collections.singleton(MediaType.ALL));
            Map<String, String> urlToText = http.oauth2Login.getLinks();
            if(urlToText.size() == 1) {
                http.defaultEntryPoints.add(new DelegateEntry(htmlMatcher, new RedirectServerAuthenticationEntryPoint((String)urlToText.keySet().iterator().next())));
            } else {
                http.defaultEntryPoints.add(new DelegateEntry(htmlMatcher, new RedirectServerAuthenticationEntryPoint("/login")));
            }

            http.addFilterAt(oauthRedirectFilter, SecurityWebFiltersOrder.HTTP_BASIC);
            http.addFilterAt(authenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION);
        }

        private ServerWebExchangeMatcher createAttemptAuthenticationRequestMatcher() {
            PathPatternParserServerWebExchangeMatcher loginPathMatcher = new PathPatternParserServerWebExchangeMatcher("/login/oauth2/code/{registrationId}");
            ServerWebExchangeMatcher notAuthenticatedMatcher = (e) -> {
                return ReactiveSecurityContextHolder.getContext().flatMap((p) -> {
                    return MatchResult.notMatch();
                }).switchIfEmpty(MatchResult.match());
            };
            return new AndServerWebExchangeMatcher(new ServerWebExchangeMatcher[]{loginPathMatcher, notAuthenticatedMatcher});
        }

        private ReactiveOAuth2UserService<OidcUserRequest, OidcUser> getOidcUserService() {
            ResolvableType type = ResolvableType.forClassWithGenerics(ReactiveOAuth2UserService.class, new Class[]{OidcUserRequest.class, OidcUser.class});
            ReactiveOAuth2UserService<OidcUserRequest, OidcUser> bean = (ReactiveOAuth2UserService)ServerHttpSecurity.this.getBeanOrNull(type);
            return (ReactiveOAuth2UserService)(bean == null?new OidcReactiveOAuth2UserService():bean);
        }

        private ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> getOauth2UserService() {
            ResolvableType type = ResolvableType.forClassWithGenerics(ReactiveOAuth2UserService.class, new Class[]{OAuth2UserRequest.class, OAuth2User.class});
            ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> bean = (ReactiveOAuth2UserService)ServerHttpSecurity.this.getBeanOrNull(type);
            return (ReactiveOAuth2UserService)(bean == null?new DefaultReactiveOAuth2UserService():bean);
        }

        private Map<String, String> getLinks() {
            Iterable<ClientRegistration> registrations = (Iterable)ServerHttpSecurity.this.getBeanOrNull(ResolvableType.forClassWithGenerics(Iterable.class, new Class[]{ClientRegistration.class}));
            if(registrations == null) {
                return Collections.emptyMap();
            } else {
                Map<String, String> result = new HashMap();
                registrations.iterator().forEachRemaining((r) -> {
                    result.put("/oauth2/authorization/" + r.getRegistrationId(), r.getClientName());
                });
                return result;
            }
        }

        private ReactiveClientRegistrationRepository getClientRegistrationRepository() {
            if(this.clientRegistrationRepository == null) {
                this.clientRegistrationRepository = (ReactiveClientRegistrationRepository)ServerHttpSecurity.this.getBeanOrNull(ReactiveClientRegistrationRepository.class);
            }

            return this.clientRegistrationRepository;
        }

        private ServerOAuth2AuthorizedClientRepository 
            getAuthorizedClientRepository() {
            ServerOAuth2AuthorizedClientRepository 
                result = this.authorizedClientRepository;
            if(result == null) {
                result = (ServerOAuth2AuthorizedClientRepository)ServerHttpSecurity
                    .this.getBeanOrNull(ServerOAuth2AuthorizedClientRepository.class);
            }

            if(result == null) {
                ReactiveOAuth2AuthorizedClientService 
                    authorizedClientService = this.getAuthorizedClientService();
                if(authorizedClientService != null) {
                    result = new AuthenticatedPrincipalServerOAuth2AuthorizedClientRepository(authorizedClientService);
                }
            }

            return (ServerOAuth2AuthorizedClientRepository)result;
        }

        private ReactiveOAuth2AuthorizedClientService getAuthorizedClientService() {
            ReactiveOAuth2AuthorizedClientService 
                service = (ReactiveOAuth2AuthorizedClientService)ServerHttpSecurity
                .this.getBeanOrNull(ReactiveOAuth2AuthorizedClientService.class);
            
            if(service == null) {
                service = new InMemoryReactiveOAuth2AuthorizedClientService(
                    this.getClientRegistrationRepository());
            }
            return (ReactiveOAuth2AuthorizedClientService)service;
        }

        private OAuth2LoginSpec() {
        }
    }

    public class CorsSpec {
        private CorsWebFilter corsFilter;

        public ServerHttpSecurity.CorsSpec configurationSource(
            CorsConfigurationSource source) {
            this.corsFilter = new CorsWebFilter(source);
            return this;
        }

        public ServerHttpSecurity disable() {
            ServerHttpSecurity.this.cors = null;
            return ServerHttpSecurity.this;
        }

        public ServerHttpSecurity and() {  return ServerHttpSecurity.this; }

        protected void configure(ServerHttpSecurity http) {
            CorsWebFilter corsFilter = this.getCorsFilter();
            if(corsFilter != null) {
                http.addFilterAt(this.corsFilter, SecurityWebFiltersOrder.CORS);
            }

        }

        private CorsWebFilter getCorsFilter() {
            if(this.corsFilter != null) { return this.corsFilter; } else {
                CorsConfigurationSource 
                    source = (CorsConfigurationSource)ServerHttpSecurity
                    .this.getBeanOrNull(CorsConfigurationSource.class);
                
                if(source == null) { return null; } else {
                    CorsProcessor processor = (CorsProcessor)ServerHttpSecurity.
                        this.getBeanOrNull(CorsProcessor.class);
                    
                    if(processor == null) { processor = new DefaultCorsProcessor();}

                    this.corsFilter = 
                        new CorsWebFilter(source, (CorsProcessor)processor);
                    return this.corsFilter;
                }
            }
        }

        private CorsSpec() {}
    }
}

```



#### 5、webFlux 默认Filter

##### 	1、ServerHttpSecurity$ServerWebExchangeReactorContextWebFilter

```java
static class ServerWebExchangeReactorContextWebFilter implements WebFilter {
        ServerWebExchangeReactorContextWebFilter() {
        }

        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            return chain.filter(exchange).subscriberContext(Context.of(ServerWebExchange.class, exchange));
        }
    }
```

#####   2、HttpHeaderWriterWebFilter

```java
public class HttpHeaderWriterWebFilter implements WebFilter {
    private final ServerHttpHeadersWriter writer;
    public HttpHeaderWriterWebFilter(ServerHttpHeadersWriter writer) {
        this.writer = writer;
    }
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        exchange.getResponse().beforeCommit(() -> {
            return this.writer.writeHttpHeaders(exchange);
        });
        return chain.filter(exchange);
    }
}
```

#####   3、ReactorContextWebFilter

```java

public class ReactorContextWebFilter implements WebFilter {
    private final ServerSecurityContextRepository repository;
    public ReactorContextWebFilter(ServerSecurityContextRepository repository) {
        Assert.notNull(repository, "repository cannot be null");
        this.repository = repository;
    }
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange).subscriberContext((c) -> {
            return 
                c.hasKey(SecurityContext.class)?c:this.withSecurityContext(c, exchange);
        });
    }

    private Context withSecurityContext(Context mainContext, 
    ServerWebExchange exchange) {
        return mainContext.putAll((Context)this.repository.load(exchange)
                                  	.as(ReactiveSecurityContextHolder::withSecurityContext));
    }
}
```

##### 4、org.springframework.security.web.server.authentication.AuthenticationWebFilter

```java
public class AuthenticationWebFilter implements WebFilter {
    private final ReactiveAuthenticationManager authenticationManager;
    
    private ServerAuthenticationSuccessHandler authenticationSuccessHandler 
    = new WebFilterChainServerAuthenticationSuccessHandler();
    
    private ServerAuthenticationConverter authenticationConverter 
    = new ServerHttpBasicAuthenticationConverter();
    
    private ServerAuthenticationFailureHandler authenticationFailureHandler 
    = new ServerAuthenticationEntryPointFailureHandler(
    new HttpBasicServerAuthenticationEntryPoint());
    
    private ServerSecurityContextRepository securityContextRepository 
    = NoOpServerSecurityContextRepository.getInstance();
    
    private ServerWebExchangeMatcher requiresAuthenticationMatcher
    = ServerWebExchangeMatchers.anyExchange();

    public AuthenticationWebFilter(ReactiveAuthenticationManager authenticationManager) {
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        this.authenticationManager = authenticationManager;
    }

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return this.requiresAuthenticationMatcher
        .matches(exchange).filter((matchResult) -> {
            return matchResult.isMatch();
        }).flatMap((matchResult) -> {
            return this.authenticationConverter.convert(exchange);
        }).switchIfEmpty(chain.filter(exchange).then(Mono.empty())).flatMap((token) -> {
            return this.authenticate(exchange, chain, token);
        });
    }

    private Mono<Void> authenticate(ServerWebExchange exchange,
    					WebFilterChain chain, Authentication token) {
        WebFilterExchange webFilterExchange = new WebFilterExchange(exchange, chain);
        return this.authenticationManager.authenticate(token)
        .switchIfEmpty(Mono.defer(() -> {
            return Mono.error(new IllegalStateException("No provider found for " + token.getClass()));
        })).flatMap((authentication) -> {
            return this.onAuthenticationSuccess(authentication, webFilterExchange);
        }).onErrorResume(AuthenticationException.class, (e) -> {
            return this.authenticationFailureHandler
            .onAuthenticationFailure(webFilterExchange, e);
        });
    }

    protected Mono<Void> onAuthenticationSuccess(Authentication authentication, WebFilterExchange webFilterExchange) {
        ServerWebExchange exchange = webFilterExchange.getExchange();
        SecurityContextImpl securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        return this.securityContextRepository
        .save(exchange,securityContext).then(this.authenticationSuccessHandler
        .onAuthenticationSuccess(webFilterExchange, authentication)).subscriberContext(ReactiveSecurityContextHolder
        .withSecurityContext(Mono.just(securityContext)));
    }

    public void setSecurityContextRepository(ServerSecurityContextRepository securityContextRepository) {
        Assert.notNull(securityContextRepository, "securityContextRepository cannot be null");
        this.securityContextRepository = securityContextRepository;
    }

    public void setAuthenticationSuccessHandler(ServerAuthenticationSuccessHandler authenticationSuccessHandler) {
        Assert.notNull(authenticationSuccessHandler, "authenticationSuccessHandler cannot be null");
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    public void setServerAuthenticationConverter(ServerAuthenticationConverter authenticationConverter) {
        Assert.notNull(authenticationConverter, 
        "authenticationConverter cannot be null");
        this.authenticationConverter = authenticationConverter;
    }

    public void setAuthenticationFailureHandler(ServerAuthenticationFailureHandler authenticationFailureHandler) {
        Assert.notNull(authenticationFailureHandler,
        "authenticationFailureHandler cannot be null");
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    public void setRequiresAuthenticationMatcher(ServerWebExchangeMatcher requiresAuthenticationMatcher) {
        Assert.notNull(requiresAuthenticationMatcher,
        "requiresAuthenticationMatcher cannot be null");
        this.requiresAuthenticationMatcher = requiresAuthenticationMatcher;
    }
}

```

##### 5、org.springframework.security.web.server.context.

##### SecurityContextServerWebExchangeWebFilter

```java

public class SecurityContextServerWebExchangeWebFilter implements WebFilter {
    public SecurityContextServerWebExchangeWebFilter() {
    }

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(new SecurityContextServerWebExchange(exchange, ReactiveSecurityContextHolder.getContext()));
    }
}
```

##### 6、org.springframework.security.web.server.savedrequest.ServerRequestCacheWebFilter

```java

public class ServerRequestCacheWebFilter implements WebFilter {
    private ServerRequestCache requestCache = new WebSessionServerRequestCache();

    public ServerRequestCacheWebFilter() {
    }

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return this.requestCache.removeMatchingRequest(exchange).map((r) -> {
            return exchange.mutate().request(r).build();
        }).defaultIfEmpty(exchange).flatMap((e) -> {
            return chain.filter(e);
        });
    }

    public void setRequestCache(ServerRequestCache requestCache) {
        Assert.notNull(requestCache, "requestCache cannot be null");
        this.requestCache = requestCache;
    }
}

```

##### 7、org.springframework.security.web.server.authentication.logout.LogoutWebFilter

```java

public class LogoutWebFilter implements WebFilter {
    private AnonymousAuthenticationToken anonymousAuthenticationToken 
    = new AnonymousAuthenticationToken("key", "anonymous", AuthorityUtils
    .createAuthorityList(new String[]{"ROLE_ANONYMOUS"}));
    
    private ServerLogoutHandler logoutHandler 
    = new SecurityContextServerLogoutHandler();
    
    private ServerLogoutSuccessHandler logoutSuccessHandler 
    = new RedirectServerLogoutSuccessHandler();
    
    private ServerWebExchangeMatcher requiresLogout;

    public LogoutWebFilter() {
        this.requiresLogout = ServerWebExchangeMatchers
        .pathMatchers(HttpMethod.POST, new String[]{"/logout"});
    }

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return this.requiresLogout.matches(exchange).filter((result) -> {
            return result.isMatch();
        }).switchIfEmpty(chain.filter(exchange)
        .then(Mono.empty())).map((result) -> {
            return exchange;
        }).flatMap(this::flatMapAuthentication)
        .flatMap((authentication) -> {
            WebFilterExchange webFilterExchange 
            = new WebFilterExchange(exchange, chain);
            return this.logout(webFilterExchange, authentication);
        });
    }

    private Mono<Authentication> flatMapAuthentication(ServerWebExchange exchange) {
        return exchange.getPrincipal().cast(Authentication.class)
        .defaultIfEmpty(this.anonymousAuthenticationToken);
    }

    private Mono<Void> logout(WebFilterExchange webFilterExchange,
    Authentication authentication) {
        return this.logoutHandler
        .logout(webFilterExchange, authentication)
        .then(this.logoutSuccessHandler
        .onLogoutSuccess(webFilterExchange, authentication))
        .subscriberContext(ReactiveSecurityContextHolder.clearContext());
    }

    public void setLogoutSuccessHandler(ServerLogoutSuccessHandler logoutSuccessHandler) {
        Assert.notNull(logoutSuccessHandler, "logoutSuccessHandler cannot be null");
        this.logoutSuccessHandler = logoutSuccessHandler;
    }

    public void setLogoutHandler(ServerLogoutHandler logoutHandler) {
        Assert.notNull(logoutHandler, "logoutHandler must not be null");
        this.logoutHandler = logoutHandler;
    }

    public void setRequiresLogoutMatcher(ServerWebExchangeMatcher requiresLogoutMatcher) {
        Assert.notNull(requiresLogoutMatcher, "requiresLogoutMatcher must not be null");
        this.requiresLogout = requiresLogoutMatcher;
    }
}

```

##### 8、org.springframework.security.web.server.authorization.ExceptionTranslationWebFilter

```java
public class ExceptionTranslationWebFilter implements WebFilter {
    private ServerAuthenticationEntryPoint authenticationEntryPoint 
    = new HttpBasicServerAuthenticationEntryPoint();
    
    private ServerAccessDeniedHandler accessDeniedHandler;

    public ExceptionTranslationWebFilter() {
        this.accessDeniedHandler
        = new HttpStatusServerAccessDeniedHandler(HttpStatus.FORBIDDEN);
    }

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
        .onErrorResume(AccessDeniedException.class, (denied) -> {
            return exchange.getPrincipal()
            .switchIfEmpty(this.commenceAuthentication(exchange, denied))
            .flatMap((principal) -> {
                return this.accessDeniedHandler.handle(exchange, denied);
            });
        });
    }

    public void setAccessDeniedHandler(ServerAccessDeniedHandler accessDeniedHandler) {
        Assert.notNull(accessDeniedHandler, "accessDeniedHandler cannot be null");
        this.accessDeniedHandler = accessDeniedHandler;
    }

    public void setAuthenticationEntryPoint(
   					 ServerAuthenticationEntryPoint authenticationEntryPoint) {
        
        Assert.notNull(authenticationEntryPoint,
        "authenticationEntryPoint cannot be null");
        
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    private <T> Mono<T> commenceAuthentication(ServerWebExchange exchange, AccessDeniedException denied) {
        return this.authenticationEntryPoint
        .commence(exchange, 
        new AuthenticationCredentialsNotFoundException("Not Authenticated", denied))
        .then(Mono.empty());
    }
}

```

##### 9、org.springframework.security.web.server.authorization.AuthorizationWebFilter

```java
public class AuthorizationWebFilter implements WebFilter {
    private ReactiveAuthorizationManager<? super ServerWebExchange> accessDecisionManager;

    public AuthorizationWebFilter(
    ReactiveAuthorizationManager<? super ServerWebExchange> accessDecisionManager) {
        this.accessDecisionManager = accessDecisionManager;
    }

    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ((Mono)ReactiveSecurityContextHolder.getContext()
        .filter((c) -> {
            return c.getAuthentication() != null;
        }).map(SecurityContext::getAuthentication)
        .as((authentication) -> {
            return this.accessDecisionManager.verify(authentication, exchange);
        })).switchIfEmpty(chain.filter(exchange));
    }
}

```



![1562509319909](E:\201320180110\source\image\webflux-spring-security.png)



