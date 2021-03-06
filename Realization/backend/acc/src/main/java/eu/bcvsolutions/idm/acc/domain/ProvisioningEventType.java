package eu.bcvsolutions.idm.acc.domain;

import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Active provisioning operation type
 * 
 * @author Radek Tomiška
 *
 */
public enum ProvisioningEventType implements EventType {
	
	CREATE,
	UPDATE,
	DELETE,
	CANCEL;
}
