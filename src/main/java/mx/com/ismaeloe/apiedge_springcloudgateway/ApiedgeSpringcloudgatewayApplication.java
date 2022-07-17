package mx.com.ismaeloe.apiedge_springcloudgateway;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
//import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

//import org.springframework.cloud.netflix.hystrix.EnableHystrix;

//import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import mx.com.ismaeloe.apiedge_springcloudgateway.filter.AuthFilter;
import mx.com.ismaeloe.apiedge_springcloudgateway.filter.AuthFilter.Config;
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
	
 * RESUMEN:
 * 1 HIGHEST_PRECEDENCE Tiene Prioridad
 * 2 Se ejecuta primero el GLOBAL FILTER y al Validar Authorization Si Regresa el 401 NOT AUTHORIZED
 * 3 Si Ejecutamos primero el AUTHFilter con Resilience4J, el 401 NOT AUTHORIZED lo cambia por 404 NOT FOUND
 * 4 lb://product-service <== donde lb=Load Balancer from Eureka Registry Server
 * 5 We Don't need @Bean @LoadBalanced public WebClient.Builder
 *   porque utilizamos lb://  
 * TODO
 * 	//ApplicationListener<ApplicationEvent>
 * 
 *  ignoreExceptions:
       - com.resilience4j.exception.BusinessException
       - feign.FeignException
      recordExceptions:
        - java.net.SocketTimeoutException
        - java.net.ConnectException
        https://github.com/resilience4j/resilience4j/issues/568
        Use Flux.error(RuntimeException("fail"), true) to not trigger the error on subscribe but on request
        https://stackoverflow.com/questions/49276946/resilience4j-how-to-handle-errors-when-using-a-circuit-breaker-in-a-spring-webfl
 */
@EnableEurekaClient
//@EnableHystrix
@RestController		//to HystrixCall
@SpringBootApplication
public class ApiedgeSpringcloudgatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiedgeSpringcloudgatewayApplication.class, args);
	}

	/*
	 * Example taken from https://www.youtube.com/watch?v=iuH_B1FutRo&t=1033s
	 * But It's No necessary because we are using .uri("lb://product-service")
	 */
	@Bean
	@LoadBalanced
	public WebClient.Builder loadBalancedRestTemplateWebClientBuilder() {
		return WebClient.builder();
	}
	
	@Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder ,AuthFilter authFilter) {
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
    
            /* PERFECT REDIRECTION */
            .route( p -> p.path("/lapi")
            				.and()
            				.method( HttpMethod.GET)
            				.filters( filter ->
            						  filter.redirect( HttpStatus.MOVED_PERMANENTLY.value() , "https://lapi.com.mx") )	//200 = IllegalArgumentException: status must be a 3xx code, but was 200
            				.uri("https://lapi.com.mx")
            				 ) //rewrite.backend.uri:https://lapi.com.mx/
            
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
            
            //PERFECT Route Mapping WITHOUT FILTER. Latest Version
            .route( p -> p.path("/productsv2")
            				.filters( filter ->
            						  filter.rewritePath( "/productsv2", "/products")//.filter(AuthFilter.Config)
            						  /*OK
            						  		.hystrix( config ->
            						  				  config.setName("mycmd")
            												.setFallbackUri("forward:/hystrixFallbackMethod")
            													)
            							*/
            						 )
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
            .route( predicate ->
            		predicate.path("/products")	///productsv1
            				 //.or()
            				 //.path("/productsresilience4j")
            				 .filters(	filter ->
            				 			//filter.rewritePath( "/productsv1", "/products")
            				 				  //.rewritePath( "/productsresilience4j", "/products")
            							filter.filter(authFilter.apply( new AuthFilter.Config("My Custom AUTH" ,true, true) ))
            								  .circuitBreaker(	cb ->
            													//cb.setRouteId("resilience4jCircuitBreaker")
            													  cb.setName("resilience4jCircuitBreaker")//.setRouteId( "resilience4jCircuitBreaker" )
            										  			  .setFallbackUri("resilience4jfallback").setRouteId("resilience4jCircuitBreaker")  )

            								    .addResponseHeader("newHeader", "newHeaderValue") )
            				.uri("lb://product-service") )  
            .build();
    }

	//Uses Mono
	@RequestMapping("/hystrixFallbackMethod")
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

	@GetMapping("/resilience4j-fallbackXX")
	Flux<Void> getFallback() {
	//Mono<String> getFallback() {
		//return Mono.justOrEmpty("return resilience4j-fallback");
		return Flux.empty();
	}
	
	@RequestMapping("/resilience4jfallback")
    public Mono<ResponseEntity<String>> inCaseOfFailureUseThis() {
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("return resilience4j-fallback body for service failure case"));
	}
}

//@RestController
class FallbackController {

	@GetMapping("/resilience4j-fallbackX")
	Flux<Void> getFallbackX() {
	//Mono<String> getFallback() {
		//return Mono.justOrEmpty("return resilience4j-fallback");
		return Flux.empty();
	}	
}