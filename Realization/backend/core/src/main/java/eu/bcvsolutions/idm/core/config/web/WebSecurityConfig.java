package eu.bcvsolutions.idm.core.config.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import eu.bcvsolutions.idm.core.rest.BaseEntityController;
import eu.bcvsolutions.idm.security.rest.filter.OAuthAuthenticationFilter;
import eu.bcvsolutions.idm.security.service.impl.OAuthAuthenticationManager;

/**
 * Web security configuration
 * 
 * @author Radek Tomiška 
 *
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
    protected void configure(HttpSecurity http) throws Exception {
    	 http.csrf().disable(); 
    	 http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    	 http.addFilterAfter(oAuthAuthenticationFilter(), BasicAuthenticationFilter.class)
			.authorizeRequests()
			.antMatchers(HttpMethod.OPTIONS).permitAll()
			.antMatchers(BaseEntityController.BASE_PATH + "/public/**").permitAll()
			.antMatchers(BaseEntityController.BASE_PATH + "/**").fullyAuthenticated() // TODO: controllers should choose security?
			.anyRequest().permitAll(); // gui could run in application context
    }
	
	@Override
	public void configure(WebSecurity web) throws Exception {
		// public controllers
		web.ignoring().antMatchers( //
				BaseEntityController.BASE_PATH, // endpoint with supported services list
				BaseEntityController.BASE_PATH + "/authentication", // login / out
				"/error/**"
			);
	}
   
	@Bean
	public OAuthAuthenticationManager oAuthAuthenticationManager() {
		return new OAuthAuthenticationManager();
	}

	@Bean
	public OAuthAuthenticationFilter oAuthAuthenticationFilter() {
		return new OAuthAuthenticationFilter();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
