package eu.bcvsolutions.idm.acc.repository;

import java.util.UUID;

import eu.bcvsolutions.idm.acc.entity.SysProvisioningArchive;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * Provisioning log
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningArchiveRepository extends AbstractEntityRepository<SysProvisioningArchive> {
	
	/**
	 * Delete all archived provisioning logs
	 * 
	 * @param system
	 * @return
	 * @deprecated use {@link #deleteBySystem_Id(UUID)}
	 */
	@Deprecated
	int deleteBySystem(SysSystem system);
	
	/**
	 * Delete all archived provisioning logs
	 * 
	 * @param systemId
	 * @return
	 */
	int deleteBySystem_Id(UUID systemId);
}
