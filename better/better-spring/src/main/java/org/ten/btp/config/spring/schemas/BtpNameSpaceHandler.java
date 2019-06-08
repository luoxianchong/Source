package org.ten.btp.config.spring.schemas;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.ten.btp.config.ApplicationConfig;

/**
 * Created by ing on 2019-04-28.
 */
public class BtpNameSpaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("application", new BtpBeanDefinitionParser(ApplicationConfig.class, true));
    }
}
