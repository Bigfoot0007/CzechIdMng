package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemEntityFilter;
import eu.bcvsolutions.idm.acc.repository.AccAccountRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningArchiveRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;

/**
 * Delete system - ensures referential integrity
 * 
 * @author svandav
 *
 */
@Component
@Description("Deletes identity - ensures core referential integrity.")
public class SystemDeleteProcessor extends CoreEventProcessor<SysSystemDto> {

	public static final String PROCESSOR_NAME = "system-delete-processor";
	private final SysSystemService service;
	private final SysProvisioningOperationRepository provisioningOperationRepository;
	private final SysProvisioningArchiveRepository provisioningArchiveRepository;
	private final SysSchemaObjectClassService objectClassService;
	private final SysSyncConfigService synchronizationConfigService;
	private final AccAccountRepository accountRepository;
	private final SysSystemEntityService systemEntityService;

	@Autowired
	public SystemDeleteProcessor(SysSystemService service,
			SysProvisioningOperationRepository provisioningOperationRepository,
			SysProvisioningArchiveRepository provisioningArchiveRepository,
			SysSchemaObjectClassService objectClassService,
			SysSyncConfigService synchronizationConfigService, AccAccountRepository accountRepository,
			SysSystemEntityService systemEntityService) {
		super(IdentityEventType.DELETE);
		//
		Assert.notNull(service);
		Assert.notNull(provisioningOperationRepository);
		Assert.notNull(provisioningArchiveRepository);
		Assert.notNull(objectClassService);
		Assert.notNull(synchronizationConfigService);
		Assert.notNull(accountRepository);
		Assert.notNull(systemEntityService);
		//
		this.service = service;
		this.objectClassService = objectClassService;
		this.accountRepository = accountRepository;
		this.synchronizationConfigService = synchronizationConfigService;
		this.provisioningOperationRepository = provisioningOperationRepository;
		this.provisioningArchiveRepository = provisioningArchiveRepository;
		this.systemEntityService = systemEntityService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemDto> process(EntityEvent<SysSystemDto> event) {
		SysSystemDto system = event.getContent();
		Assert.notNull(system);
		//
		// if exists provisioning operations, then is not possible to delete
		// system
		SysProvisioningOperationFilter operationFilter = new SysProvisioningOperationFilter();
		operationFilter.setSystemId(system.getId());
		if (provisioningOperationRepository.find(operationFilter, null).getTotalElements() > 0) {
			throw new ResultCodeException(AccResultCode.SYSTEM_DELETE_FAILED_HAS_OPERATIONS,
					ImmutableMap.of("system", system.getName()));
		}
		if (accountRepository.countBySystem_Id(system.getId()) > 0) {
			throw new ResultCodeException(AccResultCode.SYSTEM_DELETE_FAILED_HAS_ACCOUNTS,
					ImmutableMap.of("system", system.getName()));
		}
		// delete system entities
		SysSystemEntityFilter systemEntityFilter = new SysSystemEntityFilter();
		systemEntityFilter.setSystemId(system.getId());
		systemEntityService.find(systemEntityFilter, null).forEach(systemEntity -> {
			systemEntityService.delete(systemEntity);
		});
		// delete synchronization configs
		SysSyncConfigFilter synchronizationConfigFilter = new SysSyncConfigFilter();
		synchronizationConfigFilter.setSystemId(system.getId());
		synchronizationConfigService.find(synchronizationConfigFilter, null).forEach(config -> {
			synchronizationConfigService.delete(config);
		});

		// delete schema
		SysSchemaObjectClassFilter filter = new SysSchemaObjectClassFilter();
		filter.setSystemId(system.getId());
		objectClassService.find(filter, null).forEach(schemaObjectClass -> {
			objectClassService.delete(schemaObjectClass);
		});
		// delete archived provisioning operations
		provisioningArchiveRepository.deleteBySystem_Id(system.getId());
		//
		// deletes identity
		service.deleteInternal(system);
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean isDisableable() {
		return false;
	}

}
