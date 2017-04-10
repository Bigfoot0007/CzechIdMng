package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.SysSyncActionLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

@Component
public class IdentitySynchronizationExecutor extends AbstractSynchronizationExecutor implements SynchronizationExecutor  {

	private final IdmIdentityService identityService;
	private final AccIdentityAccountService identityAccoutnService;
	private final IdmIdentityRoleService identityRoleService;
	
	@Autowired
	public IdentitySynchronizationExecutor(IcConnectorFacade connectorFacade, SysSystemService systemService,
			SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService, SysSyncLogService synchronizationLogService,
			SysSyncActionLogService syncActionLogService, AccAccountService accountService,
			SysSystemEntityService systemEntityService, ConfidentialStorage confidentialStorage,
			FormService formService, IdmIdentityService identityService,
			AccIdentityAccountService identityAccoutnService, SysSyncItemLogService syncItemLogService,
			IdmIdentityRoleService identityRoleService, EntityEventManager entityEventManager,
			GroovyScriptService groovyScriptService, WorkflowProcessInstanceService workflowProcessInstanceService,
			EntityManager entityManager) {
		super(connectorFacade, systemService, attributeHandlingService, synchronizationConfigService, synchronizationLogService,
				syncActionLogService, accountService, systemEntityService, confidentialStorage, formService, syncItemLogService,
				entityEventManager, groovyScriptService, workflowProcessInstanceService, entityManager);
		
		Assert.notNull(identityService, "Identity service is mandatory!");
		Assert.notNull(identityAccoutnService, "Identity account service is mandatory!");
		Assert.notNull(identityRoleService, "Identity role service is mandatory!");
		
		
		this.identityService = identityService;
		this.identityAccoutnService = identityAccoutnService;
		this.identityRoleService = identityRoleService;
		
	}
	
	/**
	 * Delete entity linked with given account
	 * 
	 * @param account
	 * @param entityType
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doDeleteEntity(AccAccount account, SystemEntityType entityType, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = getIdentityByAccount(account);
			if (identity == null) {
				addToItemLog(logItem, "Identity account relation (with ownership = true) was not found!");
				initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
						actionLogs);
				return;
			}
			// Delete identity
			identityService.delete(identity);
		} else if (SystemEntityType.GROUP == entityType) {
			// TODO: group
		}
	}
	
	/**
	 * Call provisioning for given account
	 * 
	 * @param account
	 * @param entityType
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doUpdateAccount(AccAccount account, SystemEntityType entityType, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = getIdentityByAccount(account);
			if (identity == null) {
				addToItemLog(logItem, "Identity account relation (with ownership = true) was not found!");
				initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
						actionLogs);
				return;
			}
			// Call provisioning for this entity
			doUpdateAccountByEntity(identity, entityType, logItem);
		}
	}
	
	/**
	 * Call provisioning for given account
	 * 
	 * @param entity
	 * @param entityType
	 * @param logItem
	 */
	protected void doUpdateAccountByEntity(AbstractEntity entity, SystemEntityType entityType, SysSyncItemLog logItem) {
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = (IdmIdentity) entity;
			addToItemLog(logItem,
					MessageFormat.format(
							"Call provisioning (process IdentityEventType.SAVE) for identity ({0}) with username ({1}).",
							identity.getId(), identity.getUsername()));
			entityEventManager.process(new IdentityEvent(IdentityEventType.UPDATE, identity)).getContent();
		}
	}
	
	/**
	 * Create and persist new entity by data from IC attributes
	 * 
	 * @param entityType
	 * @param mappedAttributes
	 * @param logItem
	 * @param uid
	 * @param icAttributes
	 * @param account
	 */
	protected void doCreateEntity(SystemEntityType entityType, List<SysSystemAttributeMapping> mappedAttributes,
			SysSyncItemLog logItem, String uid, List<IcAttribute> icAttributes, AccAccount account) {
		if (SystemEntityType.IDENTITY == entityType) {
			// We will create new Identity
			addToItemLog(logItem, "Missing entity action is CREATE_ENTITY, we will do create new identity.");
			IdmIdentity identity = new IdmIdentity();
			// Fill Identity by mapped attribute
			identity = (IdmIdentity) fillEntity(mappedAttributes, uid, icAttributes, identity);
			// Create new Identity
			identityService.save(identity);
			// Update extended attribute (entity must be persisted first)
			updateExtendedAttributes(mappedAttributes, uid, icAttributes, identity);
			// Update confidential attribute (entity must be persisted first)
			updateConfidentialAttributes(mappedAttributes, uid, icAttributes, identity);

			// Create new Identity account relation
			AccIdentityAccountDto identityAccount = new AccIdentityAccountDto();
			identityAccount.setAccount(account.getId());
			identityAccount.setIdentity(identity.getId());
			identityAccount.setOwnership(true);
			identityAccoutnService.save(identityAccount);

			// Identity Created
			addToItemLog(logItem, MessageFormat.format("Identity with id {0} was created", identity.getId()));
			if (logItem != null) {
				logItem.setDisplayName(identity.getUsername());
			}
		}
	}
	
	/**
	 * Create account and relation on him
	 * 
	 * @param uid
	 * @param callProvisioning
	 * @param entity
	 * @param systemEntity
	 * @param entityType
	 * @param system
	 * @param logItem
	 */
	protected void doCreateLink(String uid, boolean callProvisioning, AbstractEntity entity, SysSystemEntity systemEntity,
			SystemEntityType entityType, SysSystem system, SysSyncItemLog logItem) {
		AccAccount account = doCreateIdmAccount(uid, system);
		if (systemEntity != null) {
			// If SystemEntity for this account already exist, then we linked
			// him to new account
			account.setSystemEntity(systemEntity);
		}

		accountService.save(account);
		addToItemLog(logItem,
				MessageFormat.format("Account with uid {0} and id {1} was created", uid, account.getId()));
		
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = (IdmIdentity) entity;

			// Create new Identity account relation
			AccIdentityAccountDto identityAccount = new AccIdentityAccountDto();
			identityAccount.setAccount(account.getId());
			identityAccount.setIdentity(identity.getId());
			identityAccount.setOwnership(true);
			identityAccount = identityAccoutnService.save(identityAccount);

			// Identity account Created
			addToItemLog(logItem,
					MessageFormat.format(
							"Identity account relation  with id ({0}), between account ({1}) and identity ({2}) was created",
							uid, identity.getUsername(), identityAccount.getId()));
			logItem.setDisplayName(identity.getUsername());
			logItem.setType(AccIdentityAccount.class.getSimpleName());
			logItem.setIdentification(identityAccount.getId().toString());

			if (callProvisioning) {
				// Call provisioning for this identity
				doUpdateAccountByEntity(entity, entityType, logItem);
			}
		}
	}
	
	/**
	 * Fill data from IC attributes to entity (EAV and confidential storage too)
	 * 
	 * @param account
	 * @param entityType
	 * @param uid
	 * @param icAttributes
	 * @param mappedAttributes
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doUpdateEntity(AccAccount account, SystemEntityType entityType, String uid,
			List<IcAttribute> icAttributes, List<SysSystemAttributeMapping> mappedAttributes, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {
		if (SystemEntityType.IDENTITY == entityType) {
			IdmIdentity identity = null;

			identity = getIdentityByAccount(account);
			if (identity != null) {
				// Update identity
				identity = (IdmIdentity) fillEntity(mappedAttributes, uid, icAttributes, identity);
				identityService.save(identity);
				// Update extended attribute (entity must be persisted first)
				updateExtendedAttributes(mappedAttributes, uid, icAttributes, identity);
				// Update confidential attribute (entity must be persisted first)
				updateConfidentialAttributes(mappedAttributes, uid, icAttributes, identity);

				// Identity Updated
				addToItemLog(logItem, MessageFormat.format("Identity with id {0} was updated", identity.getId()));
				if (logItem != null) {
					logItem.setDisplayName(identity.getUsername());
				}

				return;
			} else {
				addToItemLog(logItem, "Identity account relation (with ownership = true) was not found!");
				initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
						actionLogs);
				return;
			}
		}
	}
	
	/**
	 * Operation remove IdentityAccount relations and linked roles
	 * 
	 * @param account
	 * @param removeIdentityRole
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doUnlink(AccAccount account, boolean removeIdentityRole, SysSyncLog log,
			SysSyncItemLog logItem, List<SysSyncActionLog> actionLogs) {

		IdentityAccountFilter identityAccountFilter = new IdentityAccountFilter();
		identityAccountFilter.setAccountId(account.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccoutnService.findDto(identityAccountFilter, null)
				.getContent();
		if (identityAccounts.isEmpty()) {
			addToItemLog(logItem, "Identity account relation was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		addToItemLog(logItem, MessageFormat.format("Identity-account relations to delete {0}", identityAccounts));

		identityAccounts.stream().forEach(identityAccount -> {
			// We will remove identity account, but without delete connected
			// account
			identityAccoutnService.delete(identityAccount, false);
			addToItemLog(logItem,
					MessageFormat.format(
							"Identity-account relation deleted (without call delete provisioning) (username: {0}, id: {1})",
							identityAccount.getIdentity(), identityAccount.getId()));
			UUID identityRole = identityAccount.getIdentityRole();

			if (removeIdentityRole && identityRole != null) {
				// We will remove connected identity role
				identityRoleService.deleteById(identityRole);
				addToItemLog(logItem, MessageFormat.format("Identity-role relation deleted (id: {0})",
						identityRole));
			}

		});
		return;
	}
	
	/**
	 * Find entity by correlation attribute
	 * 
	 * @param attribute
	 * @param entityType
	 * @param icAttributes
	 * @return
	 */
	protected AbstractEntity findEntityByCorrelationAttribute(AttributeMapping attribute, SystemEntityType entityType,
			List<IcAttribute> icAttributes) {
		Assert.notNull(attribute);
		Assert.notNull(entityType);
		Assert.notNull(icAttributes);

		Object value = getValueByMappedAttribute(attribute, icAttributes);
		if (value == null) {
			return null;
		}
		if (attribute.isEntityAttribute()) {
			if (SystemEntityType.IDENTITY == entityType) {
				IdentityFilter identityFilter = new IdentityFilter();
				identityFilter.setProperty(attribute.getIdmPropertyName());
				identityFilter.setValue(value);
				List<IdmIdentity> identities = identityService.find(identityFilter, null).getContent();
				if (CollectionUtils.isEmpty(identities)) {
					return null;
				}
				if (identities.size() > 1) {
					throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_CORRELATION_TO_MANY_RESULTS,
							ImmutableMap.of("correlationAttribute", attribute.getName(), "value", value));
				}
				if (identities.size() == 1) {
					return identities.get(0);
				}
			}
		} else if (attribute.isExtendedAttribute()) {
			// TODO: not supported now

			return null;
		}
		return null;
	}

	
	@Override
	public boolean supports(SystemEntityType delimiter) {
		return SystemEntityType.IDENTITY == delimiter;
	}

	@Override
	protected AbstractEntity findEntityById(UUID entityId, SystemEntityType entityType) {
		if (SystemEntityType.IDENTITY == entityType) {
			return identityService.get(entityId);
		} else {
			throw new UnsupportedOperationException(
					MessageFormat.format("SystemEntityType {0} is not supported!", entityType));
		}
	}

	
	/**
	 * Find identity by account
	 * 
	 * @param account
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 * @return
	 */
	private IdmIdentity getIdentityByAccount(AccAccount account) {
		IdentityAccountFilter identityAccountFilter = new IdentityAccountFilter();
		identityAccountFilter.setAccountId(account.getId());
		identityAccountFilter.setOwnership(Boolean.TRUE);
		List<AccIdentityAccount> identityAccounts = identityAccoutnService.find(identityAccountFilter, null)
				.getContent();
		if (identityAccounts.isEmpty()) {
			return null;
		} else {
			// We assume that all identity accounts
			// (mark as
			// ownership) have same identity!
			return identityAccounts.get(0).getIdentity();
		}
	}
}
