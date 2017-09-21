package eu.bcvsolutions.idm.vs.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.vs.repository.filter.VsSystemImplementerFilter;
import eu.bcvsolutions.idm.vs.service.api.dto.VsSystemImplementerDto;

/**
 * Service for system-implementer in virtual system
 * 
 * @author Svanda
 *
 */
public interface VsSystemImplementerService extends 
		ReadWriteDtoService<VsSystemImplementerDto, VsSystemImplementerFilter>, AuthorizableService<VsSystemImplementerDto> {

	/**
	 * Find all implementers for this system. Merge all identities and identities from all roles.
	 * @param vsSystemId
	 * @return
	 */
	List<IdmIdentityDto> findRequestImplementers(UUID vsSystemId);

}
