package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperation;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.filter.ProvisioningOperationFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Archived provisioning operation
 * 
 * @author Radek Tomiška
 *
 */
public interface SysProvisioningArchiveService extends ReadWriteDtoService<SysProvisioningArchiveDto, ProvisioningOperationFilter> {

	/**
	 * Archives provisioning operation
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	SysProvisioningArchiveDto archive(ProvisioningOperation provisioningOperation);
}
