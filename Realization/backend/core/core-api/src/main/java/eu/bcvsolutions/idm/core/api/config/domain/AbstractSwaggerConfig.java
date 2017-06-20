package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.Arrays;

import org.apache.commons.collections.map.MultiValueMap;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.http.ResponseEntity;

import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import io.swagger.annotations.Api;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Modular swagger simple configuration
 * - expose all api endpoints in given basePackage
 * 
 * @see #api(String)
 * @author Radek Tomiška
 *
 */
public abstract class AbstractSwaggerConfig implements SwaggerConfig {
	
	/**
	 * Module for this swagger configuration
	 * 
	 * @return
	 */
	protected abstract ModuleDescriptor getModuleDescriptor();
	
	/**
	 * Docket initialization by module conventions.
	 * 
	 * @see ModuleDescriptor
	 * @param basePackage
	 * @return
	 */
	protected Docket api(String basePackage) {
		return new Docket(DocumentationType.SWAGGER_2)
				// common
				.forCodeGeneration(true)
				.genericModelSubstitutes(ResponseEntity.class)
				.securitySchemes(Arrays.asList(
						new BasicAuth(AUTHENTICATION_BASIC),
						apiKey()
						))
				.ignoredParameterTypes(Pageable.class, MultiValueMap.class, PersistentEntityResourceAssembler.class)
				// module
				.groupName(getModuleDescriptor().getId())
				.select()
					.apis(Predicates.and(
							RequestHandlerSelectors.withClassAnnotation(Api.class), 
							Predicates.or(
									RequestHandlerSelectors.basePackage(basePackage),
									RequestHandlerSelectors.basePackage("eu.bcvsolutions.idm.core.security") // security endpoint will be in all docs
									)))
					.paths(PathSelectors.ant(BaseController.BASE_PATH + "/**"))
				.build()
				.apiInfo(metaData());
	}
	
	/**
	 * CIDMST token authentication
	 * 
	 * @return
	 */
	protected ApiKey apiKey() {
		return new ApiKey(AUTHENTICATION_CIDMST, AUTHENTICATION_CIDMST_TOKEN, "header");
	}	
	
	/**
	 * TODO: license to properties (maven license plugin or simple pom props?)
	 * 
	 * @return
	 */
	protected ApiInfo metaData() {
        ApiInfo apiInfo = new ApiInfo(
                getModuleDescriptor().getName() + "- RESTfull API",
                getModuleDescriptor().getDescription(),
                getModuleDescriptor().getVersion(),
                "Terms of service",
                new Contact(getModuleDescriptor().getVendor(), getModuleDescriptor().getVendorUrl(), getModuleDescriptor().getVendorEmail()),
               "MIT",
                "https://github.com/bcvsolutions/CzechIdMng/blob/develop/LICENSE",
                Lists.newArrayList());
        return apiInfo;
    }
}
