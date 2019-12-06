package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccRoleAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.EntityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.CorrelationFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.ic.api.IcAttribute;

@Component
public class RoleSynchronizationExecutor extends AbstractSynchronizationExecutor<IdmRoleDto>
		implements SynchronizationEntityExecutor {

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private AccRoleAccountService roleAccoutnService;
	public final static String ROLE_TYPE_FIELD = "roleType";

	/**
	 * Call provisioning for given account
	 * 
	 * @param entity
	 * @param entityType
	 * @param logItem
	 */
	@Override
	protected void callProvisioningForEntity(IdmRoleDto entity, SystemEntityType entityType, SysSyncItemLogDto logItem) {
		addToItemLog(logItem,
				MessageFormat.format(
						"Call provisioning (process RoleEventType.SAVE) for role ({0}) with username ({1}).",
						entity.getId(), entity.getCode()));
		entityEventManager.process(new RoleEvent(RoleEventType.UPDATE, entity)).getContent();
	}


	/**
	 * Operation remove RoleAccount relations and linked roles
	 * 
	 * @param account
	 * @param removeRoleRole
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doUnlink(AccAccountDto account, boolean removeRoleRole, SysSyncLogDto log, SysSyncItemLogDto logItem,
			List<SysSyncActionLogDto> actionLogs) {

		EntityAccountFilter roleAccountFilter = new AccRoleAccountFilter();
		roleAccountFilter.setAccountId(account.getId());
		List<AccRoleAccountDto> roleAccounts = roleAccoutnService
				.find((AccRoleAccountFilter) roleAccountFilter, null).getContent();
		if (roleAccounts.isEmpty()) {
			addToItemLog(logItem, "Warning! - Role account relation was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		addToItemLog(logItem, MessageFormat.format("Role-account relations to delete {0}", roleAccounts));

		roleAccounts.stream().forEach(roleAccount -> {
			// We will remove role account, but without delete connected
			// account
			roleAccoutnService.delete(roleAccount, false);
			addToItemLog(logItem,
					MessageFormat.format(
							"Role-account relation deleted (without call delete provisioning) (username: {0}, id: {1})",
							roleAccount.getRole(), roleAccount.getId()));

		});
		return;
	}
	
	@Override
	protected Object getValueByMappedAttribute(AttributeMapping attribute, List<IcAttribute> icAttributes, SynchronizationContext context) {
		Object transformedValue =  super.getValueByMappedAttribute(attribute, icAttributes, context);
		// Transform role type enumeration from string
		if (transformedValue instanceof String && attribute.isEntityAttribute() && ROLE_TYPE_FIELD.equals(attribute.getIdmPropertyName())) {
			transformedValue = RoleType.valueOf((String) transformedValue);
		}
		return transformedValue;
	}
	
	/**
	 * Save entity
	 * @param entity
	 * @param skipProvisioning
	 * @return
	 */
	@Override
	protected IdmRoleDto save(IdmRoleDto entity, boolean skipProvisioning, SynchronizationContext context) {		
		// Content will be set in service (we need do transform entity to DTO). 
		// Here we set only dummy dto (null content is not allowed)
		EntityEvent<IdmRoleDto> event = new RoleEvent(
				roleService.isNew(entity) ? RoleEventType.CREATE : RoleEventType.UPDATE, 
				entity, 
				ImmutableMap.of(ProvisioningService.SKIP_PROVISIONING, skipProvisioning));
		
		return roleService.publish(event).getContent();
	}

	@Override
	protected EntityAccountFilter createEntityAccountFilter() {
		return new AccRoleAccountFilter();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected EntityAccountService<EntityAccountDto, EntityAccountFilter> getEntityAccountService() {
		return (EntityAccountService)roleAccoutnService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccRoleAccountDto();
	}

	@Override
	protected IdmRoleService getService() {
		return roleService;
	}
	
	@Override
	protected CorrelationFilter getEntityFilter(SynchronizationContext context) {
		return new IdmRoleFilter();
	}

	@Override
	protected IdmRoleDto createEntityDto() {
		return new IdmRoleDto();
	}
}
