package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * Basic interface for do provisioning
 * 
 * @author svandav
 *
 */
public interface ProvisioningService {
	
	public static final String PASSWORD_SCHEMA_PROPERTY_NAME = "__PASSWORD__";
	public static final String ENTITY_PROPERTY_NAME = "entity";
	/**
	 * Property in provisioning start event. If is value TRUE, then will be cancelled provisioning break during account protection.
	 * In extra cases, we do provisioning with account in protection. For example we need do first provisioning (for move account to archive) 
	 */
	public static final String CANCEL_PROVISIONING_BREAK_IN_PROTECTION = "cancel_provisioning_break_in_account_protection";
	
	/**
	 * Property in event. If is value TRUE, then will be provisioning skipped. Skip must be implemented in every processor for now!
	 */
	public static final String SKIP_PROVISIONING = "skip_provisioning";

	/**
	 * Do provisioning for given entity on all connected systems
	 * 
	 * @param entity
	 */
	void doProvisioning(AbstractEntity entity);
	
	/**
	 * Do provisioning for given account on connected system
	 * 
	 * @param account
	 */
	void doProvisioning(AccAccount account);
	
	/**
	 * Do provisioning for given account and entity
	 * Emits ProvisioningEventType.START event.
	 * 
	 * @param account
	 * @param entity
	 * @return
	 */
	void doProvisioning(AccAccount account, AbstractEntity entity);

	/**
	 * Do delete provisioning for given account on connected system
	 * 
	 * @param account
	 * @param entityType
	 * @param entityId - Id of entity connected to the account. Can be null, but provisioning archive will not have correct information.
	 * 
	 */
	void doDeleteProvisioning(AccAccount account, SystemEntityType entityType, UUID entityId);
	
	/**
	 * 
	 * Change password for selected identity accounts.
	 * @param identity
	 * @param passwordChange
	 */
	void changePassword(AbstractEntity identity, PasswordChangeDto passwordChange);
	
	/**
	 * Do provisioning only for single attribute. For example, it is needed to change password
	 * 
	 * @param systemEntity
	 * @param mappedAttribute
	 * @param value
	 * @param system
	 * @param operationType
	 * @param entity
	 */
	void doProvisioningForAttribute(SysSystemEntity systemEntity, AttributeMapping mappedAttribute, Object value,
			ProvisioningOperationType operationType, AbstractEntity entity);
	
	/**
	 * Do authenticate check for given username and password on target resource
	 * @param username
	 * @param password
	 * @param system
	 * @param entityType
	 * @return
	 */
	IcUidAttribute authenticate(String username, GuardedString password, SysSystem system, SystemEntityType entityType);

	/**
	 * Return all mapped attributes for this account (include overloaded attributes)
	 * 
	 * @param uid
	 * @param account
	 * @param identity
	 * @param system
	 * @param entityType
	 * @return
	 */
	List<AttributeMapping> resolveMappedAttributes(AccAccount account, AbstractEntity entity, SysSystem system, SystemEntityType entityType);

	/**
	 * Create final list of attributes for provisioning.
	 * 
	 * @param identityAccount
	 * @param defaultAttributes
	 * @param overloadingAttributes
	 * @return
	 */
	List<AttributeMapping> compileAttributes(List<? extends AttributeMapping> defaultAttributes,
			List<SysRoleSystemAttribute> overloadingAttributes, SystemEntityType entityType);

	/**
	 * Create accounts for given entity on all systems with provisioning mapping and same entity type.
	 * @param entity
	 * @param entityType
	 */
	void createAccountsForAllSystems(AbstractEntity entity);
	
	/**
	 * Do provisioning for given account and identity. For internal purpose without emit event.
	 * 
	 * @param account
	 * @param identity
	 * @param system
	 * @return
	 */
	void doInternalProvisioning(AccAccount account, AbstractEntity entity);

	
}
