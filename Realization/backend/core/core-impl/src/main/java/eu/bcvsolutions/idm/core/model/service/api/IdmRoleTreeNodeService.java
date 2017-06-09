package eu.bcvsolutions.idm.core.model.service.api;

import java.util.Set;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.RoleTreeNodeFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Automatic role service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmRoleTreeNodeService extends 
		ReadWriteDtoService<IdmRoleTreeNodeDto, RoleTreeNodeFilter>,
		AuthorizableService<IdmRoleTreeNodeDto> {
	
	/**
	 * Returns all automatic role for given work position. 
	 * 
	 * @param workPosition
	 * @return
	 */
	Set<IdmRoleTreeNodeDto> getAutomaticRolesByTreeNode(UUID workPosition);
	
	/**
	 * Prepare request to assign automatic roles by standard role request.
	 * Start of process is defined by parameter .
	 * 
	 * @param contract
	 * @param automaticRoles
	 * @return
	 */
	IdmRoleRequestDto prepareAssignAutomaticRoles(IdmIdentityContractDto contract, Set<IdmRoleTreeNodeDto> automaticRoles);
	
	/**
	 * Assign automatic roles by standard role request - with internal start
	 * Start of process is defined by parameter .
	 * 
	 * @param contract
	 * @param automaticRoles
	 * @return
	 */
	IdmRoleRequestDto assignAutomaticRoles(IdmIdentityContractDto contract, Set<IdmRoleTreeNodeDto> automaticRoles);
	
	/**
	 * Prepare role request for delete automatic roles by standard role request.
	 * 
	 * @param identityRole
	 * @param automaticRoles
	 * @return
	 */
	IdmRoleRequestDto prepareRemoveAutomaticRoles(IdmIdentityRoleDto identityRole, Set<IdmRoleTreeNodeDto> automaticRoles);
}
