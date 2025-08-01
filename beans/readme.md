# README

* the problem: runtime iteration without a priori configuration
* **java config** - nope. wont work.
* **BeanDefinitionRegistryPostProcessor**  - yah, but its super low-level. you end up having to work with it in terms of `BeanDefinition`s
* **ApplicationContextInitializer** - higher level, but it has its own lifecycle. it doesnt exist at the same time as everything else. you cant' _inject_ such a class, even though it can furnish beans itself.
* what about graalvm? 
* yikes! Java config works perfectly, but its not a solution for our original problem.
* you can use the `BeanDefinitionRegistryPostProcessor`, but remember that class gets invoked _twice_ once during the creation of the ApplicationContet and another during startup 

