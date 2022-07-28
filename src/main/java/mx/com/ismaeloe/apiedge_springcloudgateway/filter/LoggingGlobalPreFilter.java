package mx.com.ismaeloe.apiedge_springcloudgateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class LoggingGlobalPreFilter implements GlobalFilter ,Ordered {

    final Logger logger = LoggerFactory.getLogger(LoggingGlobalPreFilter.class);

    @Override
    public Mono<Void> filter( ServerWebExchange exchange,GatewayFilterChain chain)
    {
    		System.err.println( "GLOBAL PRE FILTER executed =" + exchange.getRequest().getPath());
        	logger.warn("GLOBAL PRE FILTER executed ={}" , exchange.getRequest().getPath());
 
			if (exchange.getRequest().getHeaders().getAcceptLanguage().isEmpty() ) {
				//add Accept-Language Header
			}
			
			//SiNo llega Authorization
			if ( ! exchange.getRequest().getHeaders().containsKey( HttpHeaders.AUTHORIZATION) )
			{
				System.err.println("GLOBAL User Not Authenticated");
				logger.error("GLOBAl User Not Authenticated");
					
				//throw new RuntimeException("User Not Authenticated");
				throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Please Login");
			}
			
			//Parse Authorization
			String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
			
			String[] parts = authHeader.split(" ");
			if ( ! authHeader.startsWith("Bearer ") || (parts.length != 2) || !"Bearer".equals( parts[0]) )
			{
				System.err.println("Incorrect Authorization Header");
				logger.warn("Incorrect Authorization Header");
				
				//throw new RuntimeException("Incorrect Authenticated Structure");
				throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Missing Authorization Header");
			}

			System.err.println(HttpHeaders.AUTHORIZATION + " =" + authHeader);
			logger.warn("HttpHeaders.AUTHORIZATION ={}", authHeader);
			
			//TEST Add api-key
			exchange.getRequest().mutate().header("api-key", parts[1]);
        	return chain.filter(exchange);
    	}

    /* Higher values are interpreted as lower priority. As a consequence,
	 * the object with the lowest value has the highest priority (somewhat
	 * analogous to Servlet {@code load-on-startup} values).
	 */
	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE ;
	}
}
