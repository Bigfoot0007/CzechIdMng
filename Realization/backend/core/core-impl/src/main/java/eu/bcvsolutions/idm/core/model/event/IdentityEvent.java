package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Events for identity
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityEvent extends CoreEvent<IdmIdentity> {

	/**
	 * Supported identity events
	 *
	 */
	public enum IdentityEventType implements EventType<IdmIdentity> {
		SAVE, DELETE, PASSWORD // TODO: split SAVE to UPDATE / CREATE?
	}
	
	public IdentityEvent(IdentityEventType operation, IdmIdentity content) {
		super(operation, content);
	}
	
	public IdentityEvent(IdentityEventType operation, IdmIdentity content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}