package org.ten.btp.config.spring.schemas;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.lang.Nullable;
import org.ten.btp.config.AbstractConfig;
import org.ten.btp.config.ApplicationConfig;
import org.w3c.dom.Element;

/**
 * Created by ing on 2019-04-28.
 */
public class BtpBeanDefinitionParser implements BeanDefinitionParser {

    private final Class<?> configClass;
    private final boolean required;

    public BtpBeanDefinitionParser(Class<?> configClass, boolean required) {
        this.configClass = configClass;
        this.required = required;
    }

    @Nullable
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition bdf = new RootBeanDefinition();
        bdf.setBeanClass(configClass);
        bdf.setLazyInit(false);
        String appID=element.getAttribute("id");
        String appName=element.getAttribute("name");
        MutablePropertyValues pp=new MutablePropertyValues();
        pp.addPropertyValue("id",appID);
        pp.addPropertyValue("name",appName);
        bdf.setPropertyValues(pp);
        bdf.setScope("singleton");
        parserContext.getRegistry().registerBeanDefinition(appID,bdf);
        return bdf;
    }
}
