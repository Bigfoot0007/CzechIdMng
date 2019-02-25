package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import com.google.common.annotations.Beta;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Identity accounts on target system
 * 
 * @author Svanda
 *
 */
public interface AccIdentityAccountService extends 
		EntityAccountService<AccIdentityAccountDto, AccIdentityAccountFilter>, 
		ScriptEnabled,
		AuthorizableService<AccIdentityAccountDto> {

	static final String FORCE_DELETE_OF_IDENTITY_ACCOUNT_KEY = "forceDeleteOfIdentityAccount";
	static final String DELETE_TARGET_ACCOUNT_KEY = "deleteTargetAccount";
	
	/**
	 * Delete identity account
	 * @param entity
	 * @param deleteAccount  If is true, then will be deleted (call provisioning) account on target system.
	 */
	void delete(AccIdentityAccountDto entity, boolean deleteAccount, BasePermission... permission);
	
	static AccAccountDto getEmbeddedAccount(AccIdentityAccountDto identityAccount){
		return DtoUtils.getEmbedded(identityAccount, AccIdentityAccount_.account);
	}

	/**
	 * Force delete identity account. Call event as normal, but if is account in protected mode,
	 * then will be identity-account deleted (connected AccAccount will be not deleted).
	 * For situation, when we need identity-account always delete (for example during identity delete).
	 */
	@Beta
	void forceDelete(AccIdentityAccountDto dto, BasePermission... permission);

	List<AccIdentityAccountDto> findAllByIdentity(UUID identityId);
}
	