package mx.com.ismaeloe.apiedge_springcloudgateway;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
//import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * Using Spring Cloud CircuitBreaker Hystrix
 * youtube Spring Cloud Gateway with Hystrix example | Tech Primers
 * https://www.google.com/search?q=spring+cloud+gateway+add+hystrix&oq=spring+cloud+gateway+add+hystrix&aqs=chrome..69i57.16188j0j15&sourceid=chrome&ie=UTF-8#kpvalbx=_7x3NYt7CJr2pqtsPtfOakAs23
 *  
 * <java.version>11</java.version>
 * <version>2.3.12.RELEASE</version>
 * <spring-cloud.version>Hoxton.SR12</spring-cloud.version>
 * 
 * <dependency>
 *    <groupId>org.springframework.cloud</groupId>
 *     <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
 * </dependency
 * 
 * TODO Using Spring Cloud CircuitBreaker Resilience4J
	https://spring.io/guides/gs/gateway/
	<dependency>
    	<groupId>org.springframework.cloud</groupId>
    	<artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
	</dependency>
	
 *
 */
@EnableEurekaClient
@EnableHystrix
@RestController		//to HystrixCall
@SpringBootApplication
public class ApiedgeSpringcloudgatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiedgeSpringcloudgatewayApplication.class, args);
	}

	//ApplicationListener<ApplicationEvent>
	
	@Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
          
        	/*
        	 * Add a simple re-route from: /get to: http://httpbin.org:80
        	 * 
             * PreFilter  Add Custom RequestHeader "Hello:World" 
             */
            .route(p -> p.path("/get") // intercept calls to the /get path
            			.filters( preFilter -> preFilter.addRequestHeader("Hello", "World")) // add header
            			.uri("http://httpbin.org:80")) // forward to httpbin
    
            .route( p -> p.path("/guides").uri("https://spring.io") ) //lb:customers/ =LoadBalancer from Custormer Services without DNS
            .route( p -> p.path("/lapi").and().method( HttpMethod.GET).uri("https://lapi.com.mx") ) //rewrite.backend.uri:https://lapi.com.mx/
            
            /* Remove from gateway-service.yml
             * - id: product-service
          	 *   uri: lb://product-service
             *   predicates:
             *     - Path=/products/**
             *     
             *  TODO f.redirect( 200 , "https://lapi.com.mx/" )
             *  
             *   Sin  .setFallbackUri("forward:/fallback")  recibiremos:
             *   
             *   HTTP/1.1 504 Gateway Timeout
			 *   content-length: 0
			 *   or
			 *   status": 503 Service Temporarily Unavailable
             */
            
            //Route Mapping WITHOUT FILTER. Latest Version
            .route( p -> p.path("/products")
            				.filters( filter ->
            						  filter.hystrix( config ->
            						  				  config.setName("mycmd")
            												.setFallbackUri("forward:/fallbackMethod")
            													))
            				.uri("lb://product-service") )  // lb://product-service from Eureka Registry Server

            // OR() Example
            .route( p -> p.path("/").or().path("/default").or().path("/js/**").uri( "lb://product-service" ) )
            
            /*
             * Route Mapping WITH FILTER, Old Version Redirected to Latest
             * 
             * PostFilter Add Custom ResponseHeader "newHeader : newHeaderValue"
             * 
             * - id: productv1
             *   uri: lb://product-service    //http://localhost:8096/
             *   predicates: 
             *   - Path=/productsv1/**
             *   filters:
             *   - RewritePath=/productsv1, /products  
             */
            .route( predicate -> predicate.path("/productsv1")
            				.filters( filter -> filter.rewritePath( "/productsv1", "/products")
            						.addResponseHeader("newHeader", "newHeaderValue") )
            				.uri("lb://product-service") )  
            .build();
    }

	//Uses Mono
	@RequestMapping("/fallbackMethod")
	public Mono<String> fallbackMethod()
	{
	  return Mono.just("filter.hystrix.setFallbackUri( forward:/fallbackMethod /products");
	}	

	@RequestMapping("/fallbackMethodMap")
	public Map<String,String> fallbackMethodMap()
	{		
		Map<String,String> map = new HashMap<String,String>();
		map.put("key", "value");
	  return map;
	}	

}

@RestController
class FallbackController {

	@GetMapping("/resilience-fallback")
	Flux<Void> getFallback() {
		return Flux.empty();
	}	
}