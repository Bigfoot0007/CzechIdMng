package eu.bcvsolutions.idm.core.security.api.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.bcvsolutions.idm.core.api.service.Configurable;

/**
 * An interface common to all authentication filters in IdM.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 * 
 */
public interface IdmAuthenticationFilter extends Configurable {
	
	String AUTHORIZATION_HEADER_NAME = "Authorization";
	String AUTHENTICATION_TOKEN_NAME = "cidmst";
	
	@Override
	default String getConfigurableType() {
		return "authentication-filter";
	}

	/**
	 * Authenticate user based on authorization token in HTTP header.
	 * Request and response parameters are just additional elements
	 * that enable the filter to gain more control over the request.
	 * 
	 * If the filter needs to break the filter chain, it is necessary
	 * to commit the response.
	 * 
	 * @param token
	 * @param request
	 * @param response
	 * @return Whether the authorization was successful
	 */
	boolean authorize(String token, HttpServletRequest request, HttpServletResponse response);
	
	/**
	 * Authenticate user based on request - most general method than above, token is not resolved from request automatically.
	 * 
	 * @param request
	 * @param response
	 * @return Whether the authorization was successful
	 * 
	 */
	default boolean authorize(HttpServletRequest request, HttpServletResponse response) {
		return false;
	}
	
	/**
	 * Return the name of HTTP header carrying the Authorization token.
	 * Default is "Authorization".
	 * 
	 * @return Authorization HTTP header name
	 */
	default String getAuthorizationHeaderName() {
		return AUTHORIZATION_HEADER_NAME;
	}
	
	/**
	 * Return the name of url parameter (GET) carrying the Authorization token.
	 * Default is "cidmst".
	 * 
	 * @return token parameter name
	 * @since 7.6.0
	 */
	default String getTokenParameterName() {
		return AUTHENTICATION_TOKEN_NAME;
	}
	
	/**
	 * Return the string prefix of the token in Authorization HTTP
	 * header. For example in Basic auth. scheme, the header value
	 * is prefix with 'Basic ' string.
	 *   
	 * @return Authorization token value prefix
	 */
	default String getAuthorizationHeaderPrefix() {
		return "";
	}
	
}
