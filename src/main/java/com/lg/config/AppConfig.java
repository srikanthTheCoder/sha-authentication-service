package com.lg.config;

import java.net.MalformedURLException;

import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.apache.tomcat.util.http.SameSiteCookies;
import org.ektorp.CouchDbConnector;
import org.ektorp.CouchDbInstance;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/* Configuration to connect the couch db */

@Configuration
public class AppConfig {

	private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

	@Value("${authenticationservice.url}")
	String url;

	@Value("${authenticationservice.username}")
	String username;

	@Value("${authenticationservice.password}")
	String password;

	@Value("${authenticationservice.database}")
	String database;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	/* To connect the connect the couch db with the help of dbInstance */
	@Bean
	public CouchDbConnector couchDbConnector() throws MalformedURLException {
		CouchDbConnector connector = dbInstance().createConnector(database, true);
		log.debug("Couch DB connetion created");
		return connector;
	}

	/* To create the Instance for the couch db based on the given credentials */
	@Bean
	public CouchDbInstance dbInstance() throws MalformedURLException {
		HttpClient httpClient = new StdHttpClient.Builder().url(url).username(username).password(password).build();
		CouchDbInstance dbInstance = new StdCouchDbInstance(httpClient);
		log.debug("Couch DB instance created");
		return dbInstance;
	}
	
	/* To set the sameSite in the header cookie */
	@Bean
	public TomcatContextCustomizer sameSiteCookiesConfig() {
		return context -> {
			final Rfc6265CookieProcessor cookieProcessor = new Rfc6265CookieProcessor();
			cookieProcessor.setSameSiteCookies(SameSiteCookies.LAX.getValue());
			context.setCookieProcessor(cookieProcessor);
		};
	}
}
