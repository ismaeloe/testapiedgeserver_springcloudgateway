package mx.com.ismaeloe.apiedge_springcloudgateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class LoggingGlobalPostFiltersConfigurations {
	
	final Logger logger = LoggerFactory.getLogger( LoggingGlobalPostFiltersConfigurations.class);

    @Bean
    public GlobalFilter postGlobalFilter()
    {
        return (exchange, chain) -> {
            	return chain.filter(exchange)
            				.then(Mono.fromRunnable(() ->
            	{
            		System.err.println("GLOBAL POST FILTER executed");
                  logger.warn("GLOBAL POST FILTER executed");
                }));
        };
    }
}