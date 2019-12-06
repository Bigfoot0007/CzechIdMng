package eu.bcvsolutions.idm.core.eav.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Form definition attributes values
 * 
 * TODO: abstract read controller - move to resources there ...
 *
 * @author Roman Kučera
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/form-values")
@Api(
		value = IdmFormValueController.TAG,
		description = "Operations with form attribute values",
		tags = {IdmFormValueController.TAG},
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE
)
public class IdmFormValueController extends AbstractReadDtoController<IdmFormValueDto, IdmFormValueFilter<?>> {

	protected static final String TAG = "Form attributes values";
	//
	@Autowired private FormService formService;
	
	public IdmFormValueController() {
		// service is not needed
		super(null);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@ApiOperation(
			value = "Search form values (/search/quick alias)", 
			nickname = "searchFormValues",
			tags = { IdmFormValueController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = IdmGroupPermission.APP_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = IdmGroupPermission.APP_ADMIN, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return toResources(find(toFilter(parameters), pageable, IdmBasePermission.READ), IdmFormValueDto.class);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@ApiOperation(
			value = "Search form values", 
			nickname = "searchQuickFormValues", 
			tags = { IdmFormValueController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = IdmGroupPermission.APP_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = IdmGroupPermission.APP_ADMIN, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return find(parameters, pageable);
	}
	
	@Override
	public Page<IdmFormValueDto> find(IdmFormValueFilter<?> filter, Pageable pageable, BasePermission permission) {
		return formService.findValues(filter, pageable, permission);
	}
	
	@Override
	@SuppressWarnings({ "rawtypes" })
	protected IdmFormValueFilter<?> toFilter(MultiValueMap<String, Object> parameters) {	
		IdmFormValueFilter filter = getParameterConverter().toFilter(parameters, IdmFormValueFilter.class);
		//
		filter.setAttributeId(getParameterConverter().toUuid(parameters, "attributeId"));
		filter.setDefinitionId(getParameterConverter().toUuid(parameters, "definitionId"));
		filter.setPersistentType(getParameterConverter().toEnum(parameters, "persistentType", PersistentType.class));
		//
		return filter;
	}
}
