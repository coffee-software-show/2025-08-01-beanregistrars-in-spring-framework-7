package com.example.beans;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


@SpringBootApplication
public class BeansApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeansApplication.class, args);
        /*
        var ac = new SpringApplicationBuilder(BeansApplication.class)
                .initializers(new CartApplicationContextInitializer())
                .build()
                .run(args);

         */
    }


    //    @Bean
    static CartBeanDefinitionRegistryPostProcessor cartBeanDefinitionRegistryPostProcessor() {
        return new CartBeanDefinitionRegistryPostProcessor();
    }
}

class CartApplicationContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

    @Override
    public void initialize(GenericApplicationContext applicationContext) {
        CartLocales.LOCALES.forEach(locale -> {
            applicationContext.registerBean(locale.getLanguage() + "Cart",
                    Cart.class, () -> new Cart(locale));
        });
    }
}

class CartLocales {

    static Collection<Locale> LOCALES = Set.of("fr", "es", "en", "zh", "hi")
            .stream()
            .map(Locale::forLanguageTag)
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
            registry.registerBeanDefinition(locale.getLanguage() + "Cart",
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
            registry.registerBean(locale.getLanguage() + "Cart", Cart.class,
                    cartSpec -> cartSpec.supplier(_ -> new Cart(locale)));
        }
    }
}

class Cart {

    private final Locale locale;

    Cart(Locale locale) {
        this.locale = locale;
    }

    Locale getLocale() {
        return locale;
    }
}