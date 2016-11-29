package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.api.service.IdentifiableByNameEntityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.eav.domain.PersistentType;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;

/**
 * Target system configuration service 
 * 
 * @author Radek Tomiška
 *
 */
public interface SysSystemService extends ReadWriteEntityService<SysSystem, QuickFilter>, IdentifiableByNameEntityService<SysSystem> {

	/**
	 * Generate and persist schema to system. 
	 * Use connector info and connector configuration stored in system.
	 * If system contains any schema, then will be every object compare and only same will be regenerated
	 * @param system
	 */
	void generateSchema(SysSystem system);
	
	/**
	 * Returns connector configuration for given system
	 * 
	 * @param system
	 * @return
	 */
	IcfConnectorConfiguration getConnectorConfiguration(SysSystem system);
	
	/**
	 * Returns form definition to given connector key. If no definition for connector type is found, then new definition is created by connector properties.
	 * 
	 * @param connectorKey
	 * @return
	 */
	IdmFormDefinition getConnectorFormDefinition(IcfConnectorKey connectorKey);
	
	
	
	// TODO: move to test after FE form implementation
	@Deprecated
	IcfConnectorKey getTestConnectorKey();
	@Deprecated
	SysSystem createTestSystem();

	PersistentType convertPropertyType(String connectorPropertyType);
}
