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
import java.util.concurrent.ConcurrentHashMap;

@ImportRuntimeHints(BeansApplication.CartHints.class)
@SpringBootApplication
public class BeansApplication {

    static class CartHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
            hints.reflection().registerType(LocaleCart.class, MemberCategory.values());
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
                        LocaleCart.class, () -> new LocaleCart(locale));
            });


        CartLocales.LOCALES.forEach(locale -> {
            applicationContext.registerBean("4" + locale + "ApplicationContextInitializerCart",
                    Cart.class, bd -> {
                        bd.setBeanClassName(LocaleCart.class.getName());
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

class CartBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {


        CartLocales.LOCALES.forEach(locale -> {
            System.out.println("running postProcessBeanDefinitionRegistry for locale:" + locale);
            var beanName = "2" + locale + "BeanDefinitionCart";
            if (!registry.containsBeanDefinition(beanName))
                registry.registerBeanDefinition(beanName,
                        org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition(LocaleCart.class)
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
            registry.registerBean("3" + locale + "BeanRegistrarCart", LocaleCart.class,
                    cartSpec -> cartSpec.supplier(_ -> new LocaleCart(locale)));
        }
    }
}

@Component
class LocaleAwareCartFacade implements Cart {

    private final Map<String, Cart> carts = new ConcurrentHashMap<>();
    static final ThreadLocal<String> LOCALE = new ThreadLocal<>();

    public LocaleAwareCartFacade( ) {
        CartLocales.LOCALES.forEach(locale -> carts.put(locale , new LocaleCart(locale)));
    }

    @Override
    public String getLocale() {
        var key = LOCALE.get();
        return this.carts.get(key).getLocale();
    }
}

@Component
class Lister {

    Lister(Map<String, LocaleCart> carts, LocaleAwareCartFacade cartFacade) {
        carts.forEach((k, v) -> System.out.println(k + " " + v + ":" + v.getLocale()));

        LocaleAwareCartFacade.LOCALE.set("es");
        System.out.println(cartFacade.getLocale());
        LocaleAwareCartFacade.LOCALE.set("en");
        System.out.println(cartFacade.getLocale());

    }
}

interface Cart {

    String getLocale();
}

class LocaleCart implements Cart {

    private final String locale;

    LocaleCart(String locale) {
        this.locale = locale;
    }

    @Override
    public String getLocale() {
        return locale;
    }
}