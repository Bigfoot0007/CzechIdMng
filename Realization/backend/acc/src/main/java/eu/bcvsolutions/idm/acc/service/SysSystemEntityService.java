package eu.bcvsolutions.idm.acc.service;

import eu.bcvsolutions.idm.acc.dto.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.model.service.ReadWriteEntityService;

/**
 * Entities on target system
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemEntityService extends ReadWriteEntityService<SysSystemEntity, SystemEntityFilter> {

}
