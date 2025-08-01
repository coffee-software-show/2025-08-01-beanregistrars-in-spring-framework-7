# README

* the problem: runtime iteration without a priori configuration. weve got a bunch of carts based on different locales that we want to register dynamically. 
* **java config** - nope. wont work.
* **BeanDefinitionRegistryPostProcessor**  - yah, but its super low-level. you end up having to work with it in terms of `BeanDefinition`s
* **ApplicationContextInitializer** - higher level, but it has its own lifecycle. it doesnt exist at the same time as everything else. you cant' _inject_ such a class, even though it can furnish beans itself.
* what about **graalvm**? 
* yikes! Java config works perfectly, but it's not a solution for our original problem.
* you can use the `BeanDefinitionRegistryPostProcessor`, but remember that class gets invoked _twice_ once during the creation of the ApplicationContet and another during startup so u need to check for duplicate bean registrations
* the `ApplicationContextInitializer` _almost_ works, but Spring doesn't support `Supplier<T>` constructs so you end up having to dip down into, you guessed it, `BeanDefinition` land!
* and in either case, youll need to account for the reflection happening at runtime by registering `RuntimeHintRegistrar` for the `Cart.class` 
* enter the `BeanRegistrar`: lifecycle works flawlessly, suppliers work, and it works in graalvm. 