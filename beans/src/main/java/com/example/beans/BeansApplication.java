package com.example.beans;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

@ImportRuntimeHints(BeansApplication.CartHints.class)
@SpringBootApplication
public class BeansApplication {

    static class CartHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
            hints.reflection().registerType(Cart.class , MemberCategory.values());
        }
    }

    public static void main(String[] args) {
//        SpringApplication.run(BeansApplication.class, args);

        var ac = new SpringApplicationBuilder(BeansApplication.class)
                .initializers(new CartApplicationContextInitializer())
                .build()
                .run(args);


    }

    @Bean
    static CartBeanDefinitionRegistryPostProcessor cartBeanDefinitionRegistryPostProcessor() {
        return new CartBeanDefinitionRegistryPostProcessor();
    }
}

class CartApplicationContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

    @Override
    public void initialize(GenericApplicationContext applicationContext) {
        if (false)
            CartLocales.LOCALES.forEach(locale -> {
                applicationContext.registerBean("1" + locale + "ApplicationContextInitializerCart",
                        Cart.class, () -> new Cart(locale));
            });


        CartLocales.LOCALES.forEach(locale -> {
            applicationContext.registerBean("4" + locale + "ApplicationContextInitializerCart",
                    Cart.class, bd -> {
                        bd.setBeanClassName(Cart.class.getName());
                        bd.getConstructorArgumentValues().addGenericArgumentValue(locale);
                    });
        });

    }
}


class CartLocales {

    static Collection<String> LOCALES = Set.of("fr", "es", "en", "zh", "hi")
            .stream()
            .toList();

}

@Component
class Lister {

    Lister(Map<String, Cart> carts) {
        carts.forEach((k, v) -> System.out.println(k + " " + v + ":" + v.getLocale()));
    }
}

class CartBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {


        CartLocales.LOCALES.forEach(locale -> {
            System.out.println("running postProcessBeanDefinitionRegistry for locale:" + locale);
            var beanName = "2" + locale + "BeanDefinitionCart";
            if (!registry.containsBeanDefinition(beanName))
                registry.registerBeanDefinition(beanName,
                        org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition(Cart.class)
                                .addConstructorArgValue(locale)
                                .getBeanDefinition());
        });
    }

}

@Configuration
@Import(CartBeanRegistrar.class)
class CartBeanRegistrarConfiguration {
}

class CartBeanRegistrar implements BeanRegistrar {

    @Override
    public void register(BeanRegistry registry, Environment env) {
        for (var locale : CartLocales.LOCALES) {
            registry.registerBean("3" + locale + "BeanRegistrarCart", Cart.class,
                    cartSpec -> cartSpec.supplier(_ -> new Cart(locale)));
        }
    }
}

class Cart {

    private final String locale;

    Cart(String locale) {
        this.locale = locale;
    }

    String getLocale() {
        return locale;
    }
}