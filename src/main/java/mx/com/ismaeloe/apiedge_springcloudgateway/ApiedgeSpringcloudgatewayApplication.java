package mx.com.ismaeloe.apiedge_springcloudgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

@EnableEurekaClient
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
            	//.uri("http://www.lapi.com.mx"))
            .route( p -> p.path("/guides").uri("https://spring.io") ) //lb:customers/ =LoadBalancer from Custormer Services without DNS
            .route( p -> p.path("/lapi").and().method( HttpMethod.GET).uri("rewrite.backend.uri:https://lapi.com.mx/") )
            .build();
    }
	
}
