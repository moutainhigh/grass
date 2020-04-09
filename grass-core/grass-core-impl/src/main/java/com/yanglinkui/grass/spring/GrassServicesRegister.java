package com.yanglinkui.grass.spring;

import com.yanglinkui.grass.DefaultGrassContext;
import com.yanglinkui.grass.GrassContext;
import com.yanglinkui.grass.annotation.GrassClient;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GrassServicesRegister implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    private static final String DEFAULT_GRASS_CONTEXT_BEAN_NAME = "grassContext";

    private ResourceLoader resourceLoader;

    private Environment environment;

    private String defaultProtocol;

    private String defaultSerializer;

    private String defaultRegistry;

    public GrassServicesRegister() {}


    static void validateFallback(final Class clazz) {
        Assert.isTrue(!clazz.isInterface(),
                "Fallback class must implement the interface annotated by @GrassClient");
    }

    static void validateFallbackFactory(final Class clazz) {
        Assert.isTrue(!clazz.isInterface(), "Fallback factory must produce instances "
                + "of fallback classes that implement the interface annotated by @GrassClient");
    }

    private void validate(Map<String, Object> attributes) {
        AnnotationAttributes annotation = AnnotationAttributes.fromMap(attributes);
        validateFallback(annotation.getClass("fallback"));
        validateFallbackFactory(annotation.getClass("fallbackFactory"));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        initDefaultValues(metadata, registry);

        //扫描package，是否有RemoteClient注释的接口
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);

        AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(
                GrassClient.class);

        scanner.addIncludeFilter(annotationTypeFilter);
        Set<String> basePackages = getBasePackages(metadata);

        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner
                    .findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    //验证注解的是一个接口
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    Assert.isTrue(annotationMetadata.isInterface(),
                            "@GrassClient can only be specified on an interface");

                    Map<String, Object> attributes = annotationMetadata
                            .getAnnotationAttributes(
                                    GrassClient.class.getCanonicalName());

                    //注册Bean
                    registerClient(registry, annotationMetadata, attributes);
                }
            }
        }

    }

   private void initDefaultValues(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        //初始化Context
        Map<String, Object> defaultValues = metadata
                .getAnnotationAttributes(EnableGrassServices.class.getCanonicalName());

        this.defaultProtocol = (String) defaultValues.get("protocol");
        this.defaultSerializer = (String) defaultValues.get("serializer");
        Assert.isTrue(this.defaultProtocol.length() != 0, "The default protocol must be a valida value in @EnableGrassServices.");
        Assert.isTrue(this.defaultSerializer.length() != 0, "The default serializer must be a valida value in @EnableGrassServices.");

       BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(DefaultGrassContext.class);
       definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
       definition.addPropertyValue("defaultProtocol", this.defaultProtocol);
       definition.addPropertyValue("defaultSerializer", this.defaultSerializer);
       definition.setPrimary(true);

       BeanDefinitionHolder holder = new BeanDefinitionHolder(definition.getBeanDefinition(), GrassContext.class.getCanonicalName(), new String[] {"grassContext"});
       BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
   }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(
                    AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }

    protected Set<String> getBasePackages(AnnotationMetadata metadata) {
        Map<String, Object> attributes = metadata
                .getAnnotationAttributes(EnableGrassServices.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        for (String pkg : (String[]) attributes.get("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            basePackages.add(
                    ClassUtils.getPackageName(metadata.getClassName()));
        }
        return basePackages;
    }


    private void registerClient(BeanDefinitionRegistry registry,
                                     AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        String className = annotationMetadata.getClassName();
        BeanDefinitionBuilder definition = BeanDefinitionBuilder
                .genericBeanDefinition(ClientFactoryBean.class);
        validate(attributes);

        definition.addPropertyValue("type", className);

        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

        boolean primary = (Boolean) attributes.get("primary");
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        beanDefinition.setPrimary(primary);

        String alias = getQualifier(attributes);
        if (!StringUtils.hasText(alias)) {
            alias = null;
        }

        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className,
                alias == null ? null : new String[] { alias });
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }

    private String getValue(Map<String, Object> attributes, String key, String defaultValue) {
        String value = (String) attributes.get(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        } else {
            return value;
        }
    }

    private String resolve(String value) {
        if (StringUtils.hasText(value)) {
            return this.environment.resolvePlaceholders(value);
        }
        return value;
    }


    private String getQualifier(Map<String, Object> client) {
        if (client == null) {
            return null;
        }
        String qualifier = (String) client.get("qualifier");
        if (StringUtils.hasText(qualifier)) {
            return qualifier;
        }
        return null;
    }
}
