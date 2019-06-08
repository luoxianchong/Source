import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.ten.btp.config.ApplicationConfig;

/**
 * Created by ing on 2019-04-29.
 */
public class SpringApplication {

    public static void main(String[] args) {
        ApplicationContext context=new ClassPathXmlApplicationContext("spring-better.xml");
        ApplicationConfig config = (ApplicationConfig) context.getBean(ApplicationConfig.class);
    }

}
