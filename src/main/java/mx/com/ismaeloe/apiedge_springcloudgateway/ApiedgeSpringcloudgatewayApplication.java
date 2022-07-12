package mx.com.ismaeloe.apiedge_springcloudgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

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
            // Add a simple re-route from: /get to: http://httpbin.org:80
            // Add a simple "Hello:World" HTTP Header
            .route(p -> p.path("/get") // intercept calls to the /get path
            			.filters(f -> f.addRequestHeader("Hello", "World")) // add header
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
            .route( p -> p.path("/products")
            				.filters( filter -> filter.hystrix( config -> config.setName("mycmd")
            																	.setFallbackUri("forward:/fallbackMethod")
            													))
            				.uri("lb://product-service") )  // lb://product-service from Eureka Registry Server
            .build();
    }

	//Uses Mono
	@RequestMapping("/fallbackMethod")
	public Mono<String> fallback() {
	  return Mono.just("filter.hystrix.setFallbackUri( forward:/fallbackMethod /products");
	}	
	
}
