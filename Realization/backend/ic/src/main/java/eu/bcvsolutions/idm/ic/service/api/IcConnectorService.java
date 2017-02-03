package eu.bcvsolutions.idm.ic.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorKey;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcSyncResultsHandler;
import eu.bcvsolutions.idm.ic.api.IcSyncToken;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;
import eu.bcvsolutions.idm.ic.filter.impl.IcResultsHandler;

public interface IcConnectorService {

	/**
	 * Return key defined IC implementation
	 * 
	 * @return
	 */
	String getImplementationType();

	/**
	 * Create new object in resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param attributes - Attributes for new object
	 * @return
	 */
	IcUidAttribute createObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, List<IcAttribute> attributes);
	
	/**
	 * Replace attributes in exist object in resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param uid - Identification of object in resource
	 * @param replaceAttributes - Attributes to replace in resource object
	 * @return
	 */
	IcUidAttribute updateObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid, List<IcAttribute> replaceAttributes);
	
	/**
	 * Delete object with same uid from resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param uid - Identification of object in resource
	 */
	void deleteObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid);
	
	/**
	 * Read object with same uid from resource
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param uid - Identification of object in resource
	 * @return
	 */
	IcConnectorObject readObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, IcUidAttribute uid);
	
	/**
	 * Authenticate user
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param username
	 * @param password
	 * @return
	 */
	IcUidAttribute authenticateObject(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration,
			IcObjectClass objectClass, String username, GuardedString password);

	/**
	 * Check if is connector valid
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 */
	void validate(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration);

	/**
	 * Do synchronization. For every changed item will be call given handler.
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param token - Synchronization token
	 * @param handler - Handler will be call for every synchronization item
	 * @return token
	 */
	IcSyncToken synchronization(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration, IcObjectClass objectClass,
			IcSyncToken token, IcSyncResultsHandler handler);

	/**
	 * Search by given filter. For every searched item will be call given handler.
	 * @param key - Identification of connector
	 * @param connectorConfiguration - Connector configuration
	 * @param objectClass - Type or category of connector object
	 * @param filter
	 * @param handler - Handler will be call for every searched item
	 */
	void search(IcConnectorKey key, IcConnectorConfiguration connectorConfiguration, IcObjectClass objectClass,
			IcFilter filter, IcResultsHandler handler);


}