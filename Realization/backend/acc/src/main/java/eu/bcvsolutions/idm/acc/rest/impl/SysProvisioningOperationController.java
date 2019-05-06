package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.CancelProvisioningQueueTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Active provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/provisioning-operations")
@Api(
		value = SysProvisioningOperationController.TAG, 
		tags = SysProvisioningOperationController.TAG, 
		description = "Active provisioning operations in queue",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysProvisioningOperationController
		extends AbstractReadDtoController<SysProvisioningOperationDto, SysProvisioningOperationFilter> {

	protected static final String TAG = "Provisioning - queue";
	//
	private final SysProvisioningOperationService service;
	//
	@Autowired private ProvisioningExecutor provisioningExecutor;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	@Autowired private BulkActionManager bulkActionManager;
	

	@Autowired
	public SysProvisioningOperationController(SysProvisioningOperationService service) {
		super(service);
		//
		this.service = service;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@ApiOperation(
			value = "Search provisioning operations (/search/quick alias)", 
			nickname = "searchProvisioningOperations",
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@Override
	public Page<SysProvisioningOperationDto> find(SysProvisioningOperationFilter filter, Pageable pageable,
			BasePermission permission) {
		Page<SysProvisioningOperationDto> results = super.find(filter, pageable, permission);
		// fill entity embedded for FE
		Map<UUID, BaseDto> loadedDtos = new HashMap<>();
		results.getContent().forEach(operation -> {
			if (!loadedDtos.containsKey(operation.getEntityIdentifier())) {
				loadedDtos.put(operation.getEntityIdentifier(), getLookupService().lookupDto(operation.getEntityType().getEntityType(), operation.getEntityIdentifier()));
			}
			operation.getEmbedded().put("entity", loadedDtos.get(operation.getEntityIdentifier()));
		});
		return results;
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search provisioning operations", 
			nickname = "searchQuickProvisioningOperations",
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Provisioning operation detail", 
			nickname = "getProvisioningOperation", 
			response = SysProvisioningOperation.class, 
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Provisioning operation's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@RequestMapping(value = "/{backendId}/retry", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Retry provisioning operation", 
			nickname = "retryProvisioningOperation", 
			response = SysProvisioningOperation.class, 
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				})
	public ResponseEntity<?> retry(
			@ApiParam(value = "Provisioning operation's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		SysProvisioningOperationDto provisioningOperation = getDto(backendId);
		if (provisioningOperation == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		provisioningOperation = provisioningExecutor.executeSync(provisioningOperation);
		return new ResponseEntity<>(toResource(provisioningOperation), HttpStatus.OK);
	}

	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@RequestMapping(value = "/{backendId}/cancel", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Cancel provisioning operation", 
			nickname = "cancelProvisioningOperation", 
			response = SysProvisioningOperation.class, 
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				})
	public ResponseEntity<?> cancel(
			@ApiParam(value = "Provisioning operation's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		SysProvisioningOperationDto provisioningOperation = getDto(backendId);
		if (provisioningOperation == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		provisioningOperation = provisioningExecutor.cancel(provisioningOperation);
		return new ResponseEntity<>(toResource(provisioningOperation), HttpStatus.OK);
	}

	@Deprecated // @since 9.5.2 (bulk action are used)
	@ResponseBody
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@RequestMapping(value = "/action/bulk/cancel", method = RequestMethod.PUT)
	@ApiOperation(
			value = "Cancel provisioning queue", 
			nickname = "cancelAllProvisioningQueue",
			tags = { SysProvisioningOperationController.TAG },
			notes = "Cancel all operations from provisioning queue by given filter")
	public ResponseEntity<?> cancelAll(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		SysProvisioningOperationFilter filter = toFilter(parameters);
		//filter.setSystemId(getParameterConverter().toEntityUuid(parameters, "systemId", SysSystem.class));
		//
		CancelProvisioningQueueTaskExecutor lrt = new CancelProvisioningQueueTaskExecutor();
		lrt.setFilter(filter);
		//
		LongRunningFutureTask<Boolean> futureTask = longRunningTaskManager.execute(lrt);
		//
		return new ResponseEntity<Object>(longRunningTaskManager.getLongRunningTask(futureTask), HttpStatus.ACCEPTED);
	}
	
	@ResponseBody
	@PreAuthorize("hasAuthority('" + IdmGroupPermission.APP_ADMIN + "')")
	@RequestMapping(value = "/action/bulk/delete", method = RequestMethod.DELETE)
	@ApiOperation(
			value = "Delete provisioning queue", 
			nickname = "deleteAllProvisioningQueue",
			tags = { SysProvisioningOperationController.TAG },
			notes = "Delete all operations from provisioning queue. When is given systemId delete operation will be process only for this system.")
	public ResponseEntity<?> deleteAll(@RequestParam(value = "system", required=false) String system) {
		//
		if (StringUtils.isEmpty(system)) {
			service.deleteAllOperations();
		} else {
			UUID systemId = getParameterConverter().toEntityUuid(system, SysSystemDto.class);
			service.deleteOperations(systemId);
		}
		//
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Process bulk action for provisioning operation
	 *
	 * @param bulkAction
	 * @return
	 */
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@ApiOperation(
			value = "Process bulk action for provisioning operation", 
			nickname = "bulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "")})
				})
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		initBulkAction(bulkAction);
		return new ResponseEntity<IdmBulkActionDto>(bulkActionManager.processAction(bulkAction), HttpStatus.CREATED);
	}

	/**
	 * Get available bulk actions for provisioning operation
	 *
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@ApiOperation(
			value = "Get available bulk actions", 
			nickname = "availableBulkAction", 
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEM_ADMIN, description = "") })
				})
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		// Provisioning operation controller isn't read write,
		// we must get available bulk action by bulk action manager
		return bulkActionManager.getAvailableActions(getService().getEntityClass());
	}

	/**
	 * Prevalidate bulk action for provisioning operation
	 *
	 * @param bulkAction
	 * @return
	 */
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_ADMIN + "')")
	@ApiOperation(
			value = "Prevalidate bulk action for provisioning operation", 
			nickname = "prevalidateBulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { SysProvisioningOperationController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.IDENTITY_READ, description = "")})
				})
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		initBulkAction(bulkAction);
		ResultModels result = bulkActionManager.prevalidate(bulkAction);
		if(result == null) {
			return new ResponseEntity<ResultModels>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<ResultModels>(result, HttpStatus.OK);
	}

	/**
	 * Init bulk action
	 * TODO: same method is in {@link AbstractReadWriteDtoController}. But {@link SysProvisioningOperationController}
	 * is only read controller and isn't possible use features from read write controller
	 *
	 * @param bulkAction
	 */
	@SuppressWarnings("unchecked")
	private void initBulkAction(IdmBulkActionDto bulkAction) {
		// TODO: use MultiValueMap in object if is possible?
		if (bulkAction.getFilter() != null) {
			MultiValueMap<String, Object> multivaluedMap = new LinkedMultiValueMap<>();
			Map<String, Object> properties = bulkAction.getFilter();
			
			for (Entry<String, Object> entry : properties.entrySet()) {
				Object value = entry.getValue();
				if(value instanceof List<?>) {
					multivaluedMap.put(entry.getKey(), (List<Object>) value);
				}else {
					multivaluedMap.add(entry.getKey(), entry.getValue());
				}
			}
			SysProvisioningOperationFilter filter = this.toFilter(multivaluedMap);
			bulkAction.setTransformedFilter(filter);
		}
		bulkAction.setEntityClass(getService().getEntityClass().getName());
		bulkAction.setFilterClass(this.getFilterClass().getName());
	}

	@Override
	protected SysProvisioningOperationFilter toFilter(MultiValueMap<String, Object> parameters) {
		SysProvisioningOperationFilter filter = super.toFilter(parameters);
		if (filter == null) {
			return new SysProvisioningOperationFilter();
		}
		return filter;
	}
}
