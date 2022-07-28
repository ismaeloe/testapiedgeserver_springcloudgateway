package mx.com.ismaeloe.apiedge_springcloudgateway.filter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import mx.com.ismaeloe.apiedge_springcloudgateway.dto.UserDto;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import org.springframework.core.Ordered;

@Component
//public class AuthFilter extends AbstractGatewayFilterFactory<CustomFilter.Config> {
public class AuthFilter extends AbstractGatewayFilterFactory<AuthFilter.Config> implements Ordered {

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}
	
	final Logger LOG = LoggerFactory.getLogger(AuthFilter.class);
	
	private final WebClient.Builder webClientBuilder;
	
	public AuthFilter( WebClient.Builder _webClientBuilder)
	{
		super(Config.class); //AuthFilter.Config
		this.webClientBuilder = _webClientBuilder;
	}

	@Override
	public GatewayFilter apply(Config config)
	{
		//Custom Pre Filter. Suppose we can extract JWT and perform Authentication
		return (exchange, chain) -> {
			
			//if (Config.isPre)
			System.err.println("FIRST AuthFilter PRE FILTER =" + exchange.getRequest().getPath());
			LOG.warn("FIRST AuthFilter PRE FILTER ={}" , exchange.getRequest().getPath());
	
			if (exchange.getRequest().getHeaders().getAcceptLanguage().isEmpty() ) {
				//add Accept-Language Header
			}
	
			/*** IMPORTANTE: En este Filtro al regresar Any Exception, Return 404 NOt Foud **/
			
			//SiNo llega Authorization
			if ( ! exchange.getRequest().getHeaders().containsKey( HttpHeaders.AUTHORIZATION) )
			{
				System.err.println("User Not Authenticated");
				LOG.error("User Not Authenticated");
					
				//throw new RuntimeException("User Not Authenticated");
				//throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Please Login");
			}else {

			//Parse Authorization
			String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
			
			String[] parts = authHeader.split(" ");
			if ( (parts.length != 2) || !"Bearer".equals( parts[0]) )
			{
				System.err.println("Incorrect Authenticated Structure");
				LOG.error("Incorrect Authenticated Structure");	
				//throw new RuntimeException("Incorrect Authenticated Structure"); 
			}
		
			System.err.println("AuthFilter " + HttpHeaders.AUTHORIZATION + " =" + authHeader);
			LOG.warn("HttpHeaders.AUTHORIZATION ={}", authHeader);
			}
	
			/*HASTA AQUI BIEN
			HttpClient httpClient = HttpClient.create()
					  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
					  .responseTimeout(Duration.ofMillis(5000))
					  .doOnConnected(conn -> 
					    conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
					      .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));
	
			WebClient webClient = this.webClientBuilder
										.clientConnector(new ReactorClientHttpConnector(httpClient))
										.baseUrl("http://localhost:8091")
										.build();				
			
			//OK Mono<String> monoString = webClient.get()
			 return webClient.get()
					.uri("/customers/health")
					//.exchangeToMono( response -> {
					.retrieve().bodyToMono(Void.class); //UserDto.class
			 */
	
			  //Custom Post Filter.Suppose we can call error response handler based on error code.
			return chain.filter(exchange)
						.then(Mono.fromRunnable( () -> {
				
				System.err.println("FIRST AuthFilter POST FILTER");
				LOG.warn( "FIST AuthFilter POST FILTER" );
			}));
				/*.map( string -> {
					.flatMap {
						exchange.getRequest()
						.mutate()
						.header("x-auth-user-id", "UserDto is NULL OR UserDto is Not NULL"); 
					};
			 
						System.err.println(" /customers/health = OK" );
						LOG.warn(" /customers/health = OK");
				
						if (response.statusCode().equals(HttpStatus.OK) ) {
							return response.bodyToMono(String.class);
						}else if ( response.statusCode().is4xxClientError() ) {
							return Mono.just("Error 4xx Client Error");	
						}else {
							return response.createException().flatMap(Mono::error);
						}
						
			// https://howtodoinjava.com/spring-webflux/webclient-get-post-example/
			
			return webClient.post().uri("http://service-users/users/validateToken?token=" + parts[1])  .accept( MediaType.APPLICATION_JSON)
									.get()
									.uri("lb://customers-service/customers/health?token=" + parts[1])
									.exchangeToMono( response -> {
									}
									);
									//.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).
									//.accept(MediaType.APPLICATION_JSON_TYPE)
									//.retrieve().bodyToMono(String.class); //UserDto.class
									.map( userDto -> {
													exchange.getRequest()
															.mutate()
															.header("x-auth-user-id", (userDto == null ? "UserDto is NULL": "UserDto is Not NULL") );
									})
								*/
										
		}; //return
	} //apply

	public static class Config {
		// Put the configuration properties
		 private String baseMessage;
		 private boolean preLogger;
		 private boolean postLogger;
		 
		public Config(String baseMessage, boolean preLogger, boolean postLogger) {
			super();
			this.baseMessage = baseMessage;
			this.preLogger = preLogger;
			this.postLogger = postLogger;
			
			System.err.println("Constructor Config.baseMessage =" + this.baseMessage);
		}
		
		public String getBaseMessage() {
			return baseMessage;
		}
		public void setBaseMessage(String baseMessage) {
			this.baseMessage = baseMessage;
		}
		public boolean isPreLogger() {
			return preLogger;
		}
		public void setPreLogger(boolean preLogger) {
			this.preLogger = preLogger;
		}
		public boolean isPostLogger() {
			return postLogger;
		}
		public void setPostLogger(boolean postLogger) {
			this.postLogger = postLogger;
		}	 
	}
}
