package com.example.carts_bean_registrars;

import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

//@ImportRuntimeHints(CartHints.class)
@Import(CartBeanRegistrar.class)
@SpringBootApplication
public class CartsBeanRegistrarsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartsBeanRegistrarsApplication.class, args);
//
//        var ac = new SpringApplicationBuilder(CartsBeanRegistrarsApplication.class)
//                .initializers(new CartApplicationContextInitializer())
//                .run(args);

    }
}
//
//class CartHints implements RuntimeHintsRegistrar {
//
//    @Override
//    public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
//        hints.reflection().registerType(DefaultCart.class, MemberCategory.values());
//    }
//}
//
//@Configuration
//class LocalesConfiguration {
//
//    @Bean
//    static CartBeanDefinitionRegistryPostProcessor cartBeanDefinitionRegistryPostProcessor() {
//        return new CartBeanDefinitionRegistryPostProcessor();
//    }
//
//}
//
//class CartApplicationContextInitializer implements ApplicationContextInitializer<GenericApplicationContext> {
//
//    @Override
//    public void initialize(GenericApplicationContext applicationContext) {
//        Locales.LOCALES.forEach(locale -> {
//            applicationContext.registerBean("cartApplicationContextInitializerFor" + locale, DefaultCart.class, bd -> {
//                bd.setBeanClassName(DefaultCart.class.getName());
//                bd.getConstructorArgumentValues().addGenericArgumentValue(locale);
//            });
//        });
//
//    }
//}
//

/// /@Component
//class MultitenantLocaleResolvingCart implements Cart {
//
//    static final ThreadLocal<String> LOCALE = new ThreadLocal<>();
//
//    private final Map<String, Cart> carts = new ConcurrentHashMap<>();
//
//    public MultitenantLocaleResolvingCart() {
//        Locales.LOCALES.forEach(locale -> carts.put(locale, new DefaultCart(locale)));
//    }
//
//    @Override
//    public String getLocale() {
//        var key = LOCALE.get();
//        return this.carts.get(key).getLocale();
//    }
//}
//
//
//class CartBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {
//
//    @Override
//    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
//        Locales.LOCALES.forEach(locale -> {
//            var beanName = "beanDefinitionRegistryCartFor" + locale;
//            if (!registry.containsBeanDefinition(beanName)) {
//                var beanDefinition = BeanDefinitionBuilder
//                        .genericBeanDefinition(DefaultCart.class)
//                        .addConstructorArgValue(locale)
//                        .getBeanDefinition();
//                registry.registerBeanDefinition(beanName, beanDefinition);
//            }
//        });
//
//    }
//}


class CartBeanRegistrar implements BeanRegistrar {

    @Override
    public void register(BeanRegistry registry, Environment env) {

        Locales.LOCALES.forEach(locale -> registry
                .registerBean("beanRegistrarCartFor" + locale,
                        DefaultCart.class,
                        defaultCartSpec -> defaultCartSpec
                                .supplier(_ -> new DefaultCart(locale))
                                .description("a cart for the " + locale + " locale")
                ));

    }
}


@Component
class Lister {

    public Lister(Map<String, Cart> carts) {
        carts.forEach((k, v) -> System.out.println(k + " " + v.getLocale()));
    }
}

class Locales {

    static Set<String> LOCALES = Set.of("en", "es", "zh", "hi");
}

interface Cart {
    String getLocale();
}

class DefaultCart implements Cart {

    private final String locale;

    DefaultCart(String locale) {
        this.locale = locale;
    }

    @Override
    public String getLocale() {
        return this.locale;
    }
}