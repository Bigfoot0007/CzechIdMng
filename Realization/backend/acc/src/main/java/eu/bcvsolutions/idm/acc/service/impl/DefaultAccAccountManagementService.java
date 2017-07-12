package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.MappingAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.AccIdentityAccountRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;

/**
 * Service for control account management
 * 
 * @author svandav
 *
 */
@Service
public class DefaultAccAccountManagementService implements AccAccountManagementService {

	private final AccAccountService accountService;
	private final SysRoleSystemService roleSystemService;
	private final AccIdentityAccountService identityAccountService;
	private final AccIdentityAccountRepository identityAccountRepository;
	private final IdmIdentityRoleRepository identityRoleRepository;
	private final SysRoleSystemAttributeService roleSystemAttributeService;
	private final SysSystemAttributeMappingService systemAttributeMappingService;
	private final SysSystemMappingService systemMappingService;

	@Autowired
	public DefaultAccAccountManagementService(SysRoleSystemService roleSystemService, AccAccountService accountService,
			AccIdentityAccountService identityAccountService, IdmIdentityRoleRepository identityRoleRepository,
			SysRoleSystemAttributeService roleSystemAttributeService,
			AccIdentityAccountRepository identityAccountRepository,
			SysSystemAttributeMappingService systemAttributeMappingService,
			SysSystemMappingService systemMappingService) {
		super();
		//
		Assert.notNull(identityAccountService);
		Assert.notNull(roleSystemService);
		Assert.notNull(accountService);
		Assert.notNull(identityRoleRepository);
		Assert.notNull(roleSystemAttributeService);
		Assert.notNull(identityAccountRepository);
		Assert.notNull(systemAttributeMappingService);
		Assert.notNull(systemMappingService);
		//
		this.roleSystemService = roleSystemService;
		this.accountService = accountService;
		this.identityAccountService = identityAccountService;
		this.identityRoleRepository = identityRoleRepository;
		this.roleSystemAttributeService = roleSystemAttributeService;
		this.identityAccountRepository = identityAccountRepository;
		this.systemAttributeMappingService = systemAttributeMappingService;
		this.systemMappingService = systemMappingService;
	}

	@Override
	public boolean resolveIdentityAccounts(IdmIdentity identity) {
		Assert.notNull(identity);

		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccountDto> identityAccountList = identityAccountService.find(filter, null).getContent();

		List<IdmIdentityRole> identityRoles = identityRoleRepository.findAllByIdentityContract_Identity_Id(identity.getId(), null);

		boolean provisioningRequired = false;

		if (CollectionUtils.isEmpty(identityRoles) && CollectionUtils.isEmpty(identityAccountList)) {
			// None roles and accounts ... we don't have any to do
			return false;
		}

		List<AccIdentityAccountDto> identityAccountsToCreate = new ArrayList<>();
		List<AccIdentityAccountDto> identityAccountsToDelete = new ArrayList<>();

		// Is role valid in this moment
		resolveIdentityAccountForCreate(identity, identityAccountList, identityRoles, identityAccountsToCreate,
				identityAccountsToDelete);

		// Is role invalid in this moment
		resolveIdentityAccountForDelete(identityAccountList, identityRoles, identityAccountsToDelete);
		
		// Delete invalid identity accounts
		provisioningRequired = !identityAccountsToDelete.isEmpty() ? true : provisioningRequired;
		identityAccountsToDelete.forEach(identityAccount -> identityAccountService.deleteById(identityAccount.getId()));

		// Create new identity accounts
		provisioningRequired = !identityAccountsToCreate.isEmpty() ? true : provisioningRequired;
		identityAccountsToCreate.forEach(identityAccount -> identityAccountService.save(identityAccount));

		return provisioningRequired;
	}

	/**
	 * Resolve identity account to delete
	 * @param identityAccountList
	 * @param identityRoles
	 * @param identityAccountsToDelete
	 */
	private void resolveIdentityAccountForDelete(List<AccIdentityAccountDto> identityAccountList,
			List<IdmIdentityRole> identityRoles, List<AccIdentityAccountDto> identityAccountsToDelete) {

		identityRoles.stream().filter(identityRole -> {
			return !identityRole.isValid();
		}).forEach(identityRole -> {
			// Search IdentityAccounts to delete
			identityAccountList.stream().filter(identityAccount -> {
				return identityRole.equals(identityAccount.getIdentityRole());
			}).forEach(identityAccount -> {
				identityAccountsToDelete.add(identityAccount);
			});
		});
	}

	/**
	 * Resolve Identity account - to create
	 * @param identity
	 * @param identityAccountList
	 * @param identityRoles
	 * @param identityAccountsToCreate
	 * @param identityAccountsToDelete
	 * @param resolvedRolesForCreate
	 */
	private void resolveIdentityAccountForCreate(IdmIdentity identity, List<AccIdentityAccountDto> identityAccountList,
			List<IdmIdentityRole> identityRoles, List<AccIdentityAccountDto> identityAccountsToCreate,
			List<AccIdentityAccountDto> identityAccountsToDelete) {
		
		// Is role valid in this moment
		identityRoles.stream().filter(identityRole -> {
			return identityRole.isValid();
		}).forEach(identityRole -> {
			
			IdmRole role = identityRole.getRole();
			RoleSystemFilter roleSystemFilter = new RoleSystemFilter();
			roleSystemFilter.setRoleId(role.getId());
			List<SysRoleSystem> roleSystems = roleSystemService.find(roleSystemFilter, null).getContent();

			roleSystems.stream().filter(roleSystem -> {
				// Filter out identity-accounts for same role-system, account (by UID)
				return !identityAccountList.stream().filter(identityAccount -> {
					if (roleSystem.getId().equals(identityAccount.getRoleSystem())) {
						
						// Has identity account same uid as account?
						String uid = generateUID(identity, roleSystem);
						AccAccountDto account = AccIdentityAccountService.getEmbeddedAccount(identityAccount);
						if (uid.equals(account.getUid())) {
							// Identity account for this role, system and uid is
							// created
							return true;
						}else{
							// We found identityAccount for same identity and roleSystem, but this identityAccount
							// is link to Account with different UID. It's probably means definition of UID (transformation)\
							// on roleSystem was changed. We have to delete this identityAccount. 
							identityAccountsToDelete.add(identityAccount);
						}
					}
					return false;
				}).findFirst().isPresent();

			}).forEach(roleSystem -> {
				// For this system we have to create new account
				UUID accountId = createAccountByRoleSystem(identity, roleSystem, identityAccountsToCreate);
				AccIdentityAccountDto identityAccount = new AccIdentityAccountDto();
				identityAccount.setAccount(accountId);
				identityAccount.setIdentity(identity.getId());
				identityAccount.setIdentityRole(identityRole.getId());
				identityAccount.setRoleSystem(roleSystem.getId());
				// TODO: Add flag ownership to SystemRole and set here.
				identityAccount.setOwnership(true);

				identityAccountsToCreate.add(identityAccount);
			});
		});
	}


	/**
	 * Return UID for this identity and roleSystem. First will be find and use
	 * transform script from roleSystem attribute. If isn't UID attribute for
	 * roleSystem defined, then will be use default UID attribute handling.
	 * 
	 * @param entity
	 * @param roleSystem
	 * @return
	 */
	@Override
	public String generateUID(AbstractEntity entity, SysRoleSystem roleSystem) {
		// Find attributes for this roleSystem
		RoleSystemAttributeFilter roleSystemAttrFilter = new RoleSystemAttributeFilter();
		roleSystemAttrFilter.setRoleSystemId(roleSystem.getId());
		List<SysRoleSystemAttribute> attributes = roleSystemAttributeService.find(roleSystemAttrFilter, null)
				.getContent();
		List<SysRoleSystemAttribute> attributesUid = attributes.stream().filter(attribute -> {
			return attribute.isUid();
		}).collect(Collectors.toList());

		if (attributesUid.size() > 1) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_ROLE_ATTRIBUTE_MORE_UID, ImmutableMap.of("role",
					roleSystem.getRole().getName(), "system", roleSystem.getSystem().getName()));
		}

		SysRoleSystemAttribute uidRoleAttribute = !attributesUid.isEmpty() ? attributesUid.get(0) : null;

		// If roleSystem UID attribute found, then we use his transformation
		// script.
		if (uidRoleAttribute != null) {
			// We have to create own dto and set up all values
			// (overloaded and default)
			AttributeMapping overloadedAttribute = new MappingAttributeDto();
			// Default values (values from schema attribute handling)
			overloadedAttribute.setSchemaAttribute(uidRoleAttribute.getSystemAttributeMapping().getSchemaAttribute());
			overloadedAttribute
					.setTransformFromResourceScript(uidRoleAttribute.getSystemAttributeMapping().getTransformFromResourceScript());
			// Overloaded values
			roleSystemAttributeService.fillOverloadedAttribute(uidRoleAttribute, overloadedAttribute);
			Object uid = systemAttributeMappingService.getAttributeValue(null, entity, overloadedAttribute);
			if(uid == null) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_GENERATED_UID_IS_NULL,
						ImmutableMap.of("system", roleSystem.getSystem().getName()));
			}
			if (!(uid instanceof String)) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_UID_IS_NOT_STRING,
						ImmutableMap.of("uid", uid));
			}
			return (String) uid;
		}

		SysSystemMapping mapping = roleSystem.getSystemMapping();
		// If roleSystem UID was not found, then we use default UID schema
		// attribute handling
	
		SysSystem system = mapping.getSystem();
		SystemAttributeMappingFilter systeAttributeMappingFilter = new SystemAttributeMappingFilter();
		systeAttributeMappingFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMapping> schemaHandlingAttributes = systemAttributeMappingService
				.find(systeAttributeMappingFilter, null).getContent();
		SysSystemAttributeMapping uidAttribute = systemAttributeMappingService.getUidAttribute(schemaHandlingAttributes, system);
		return systemAttributeMappingService.generateUid(entity, uidAttribute);
	}

	@Override
	public void deleteIdentityAccount(IdmIdentityRoleDto entity) {
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityRoleId(entity.getId());
		Page<AccIdentityAccountDto> identityAccounts = identityAccountService.find(filter, null);
		List<AccIdentityAccountDto> identityAccountList = identityAccounts.getContent();

		identityAccountList.forEach(identityAccount -> {
			identityAccountService.delete(identityAccount);
		});
	}
	
	/**
	 * Create Account by given roleSystem
	 * @param identity
	 * @param roleSystem
	 * @param identityAccountsToCreate
	 * @return
	 */
	private UUID createAccountByRoleSystem(IdmIdentity identity, SysRoleSystem roleSystem,
			List<AccIdentityAccountDto> identityAccountsToCreate) {
		String uid = generateUID(identity, roleSystem);
		
		// We try find account for same uid on same system
		// First we try search same account in list for create new accounts
		Optional<AccIdentityAccountDto> sameAccountOptional = identityAccountsToCreate.stream().filter(ia -> {
			AccAccount account = accountService.get(ia.getAccount());
			return account.getUid().equals(uid);
		}).findFirst();
		
		UUID accountId = null;
		if(sameAccountOptional.isPresent()){
			accountId = sameAccountOptional.get().getAccount();
		}else{
			// If account is not in list accounts to create, then we will search in database
			AccountFilter accountFilter = new AccountFilter();
			accountFilter.setUid(uid);
			accountFilter.setSystemId(roleSystem.getSystem().getId());
			List<AccAccount> sameAccounts = accountService.find(accountFilter, null).getContent();
			if (CollectionUtils.isEmpty(sameAccounts)) {
				// Create and persist new account
				accountId = createAccount(uid, roleSystem);
			} else {
				// We use existed account
				accountId = sameAccounts.get(0).getId();
			}
		}
		return accountId;
	}
	
	private UUID createAccount(String uid, SysRoleSystem roleSystem) {
		UUID accountId;
		AccAccount account = new AccAccount();
		account.setUid(uid);
		account.setAccountType(AccountType.PERSONAL);
		account.setSystem(roleSystem.getSystem());
		account = accountService.save(account);
		accountId = account.getId();
		return accountId;
	}
	
}
