## WebFlux 静态资源处理

静态资源：主要指 css、js、图片、html、font（字体）等文件。

WebFlux默认静态资源路径 src/main/resources/public

如果自定义目录，比如，src/main/resources/static。WebFlux 将无法进行自动映射，需要同步配置来修改。

一、指定 spring.resources.static-locations 和 spring.webflux.static-path-pattern 两个属性值即可

二、RouterFunctions.resources 来指定和映射静态资源

```java
@Configuration
public class WebFluxRouteConfigure {
    @Bean
    RouterFunction<ServerResponse> staticResourceRouter(){
        return RouterFunctions.resources("/static/**",new ClassPathResource("static/"));
    }
    
    @Bean
    public RouterFunction<ServerResponse> indexRouter(
        @Value("classpath:/static/index.html") final Resource indexHtml) {
        return route(GET("/index"), request -> ok().contentType(MediaType.TEXT_HTML)
                     .syncBody(indexHtml));
    }
}
```



三、通过配置模板引擎

​	引入thymeleaf相关的jar包

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

​	代码则和spring MVC一样

```
@GetMapping("index")
public String index(){
    return "index.html";
}
```

