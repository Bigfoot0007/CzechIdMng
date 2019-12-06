package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for identity roles
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityRoleEvent extends CoreEvent<IdmIdentityRoleDto> {

	private static final long serialVersionUID = 1L;
	//
	public static final String PROPERTY_PROCESSED_ROLES = RoleEvent.PROPERTY_PROCESSED_ROLES; // event property, contains Set<UUID> of processed roles (used for role composition processing for the prevent cycles)
	public static final String PROPERTY_ASSIGNED_NEW_ROLES = "idm:assigned_new_roles"; // event property, contains List<IdmIdentityRole> of new assigned roles
	public static final String PROPERTY_ASSIGNED_REMOVED_ROLES = "idm:assigned_removed_roles"; // event property, contains List<UUID> of removed assigned roles
	public static final String PROPERTY_ASSIGNED_UPDATED_ROLES = "idm:assigned_updated_roles"; // event property, contains List<IdmIdentityRole> of updated assigned roles

	/**
	 * Supported identity events
	 *
	 */
	public enum IdentityRoleEventType implements EventType {
		CREATE, UPDATE, DELETE, NOTIFY
	}
	
	public IdentityRoleEvent(IdentityRoleEventType operation, IdmIdentityRoleDto content) {
		super(operation, content);
	}
	
	public IdentityRoleEvent(IdentityRoleEventType operation, IdmIdentityRoleDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}