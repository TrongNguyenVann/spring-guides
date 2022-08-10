package trongnv.restful_service;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// In Spring’s approach to building RESTful web services, HTTP requests are handled by a controller.
// These components are identified by the @RestController annotation
// In our project, this controller will handle GET request for /greeting by return a new instance of Greeting class

// @RestController mark the class as a controller, every method returns a domain object instead of a view
// It is shorthand for including both @Controller and @ResponseBody
@RestController
public class GreetingController {

    private final AtomicInteger counter = new AtomicInteger();

    // @GetMapping annotation ensures that HTTP GET requests to /greeting are mapped to the greeting() method
    // There are companion annotations for other HTTP verbs (e.g. @PostMapping for POST).
    // There is also a @RequestMapping annotation that they all derive from, and can serve as a synonym (e.g. @RequestMapping(method=GET)).
    @GetMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {  // @RequestParam bind value of query string name to parameter name of grerting()
        return new Greeting(counter.incrementAndGet(), String.format("Hello, %s!", name));
    }

}