package com.yanglinkui.grass.spring;

import com.yanglinkui.grass.client.ClientBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;

public class ClientFactoryBean<T> extends ClientBuilder<T> implements FactoryBean<Object>, InitializingBean, ApplicationContextAware {

    private Class<T> type;

    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public T getObject() throws Exception {
        this.setInterface(this.type);
        return this.build();
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Class<?> getType() {
        return this.type;
    }

    public void setType(Class<T> type) {
        this.type = type;
    }

    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientFactoryBean that = (ClientFactoryBean) o;
        return Objects.equals(this.applicationContext, that.applicationContext)
                && Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.applicationContext, this.type);
    }

    @Override
    public String toString() {
        return new StringBuilder("ClientFactoryBean{").append("type=")
                .append(this.type).append(", ")
                .append("', ").append("applicationContext=")
                .append(this.applicationContext).append(", ").toString();
    }
}
