package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.AbstractEntityEvent;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/**
 * Events for identity roles
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityRoleEvent extends AbstractEntityEvent<IdmIdentityRole> {

	public IdentityRoleEvent(IdentityRoleEventType operation, IdmIdentityRole content) {
		super(operation, content);
	}
	
	public IdentityRoleEvent(IdentityRoleEventType operation, IdmIdentityRole content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}