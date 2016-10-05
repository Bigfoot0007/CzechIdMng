package eu.bcvsolutions.idm.security.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Group permission representation
 * 
 * @author Radek Tomiška 
 *
 */
public class GroupPermissionDto  implements Serializable {

	private static final long serialVersionUID = -7680244766930626618L;
	private String name;
	private List<String> permissions;
	
	public GroupPermissionDto() {
	}
	
	public GroupPermissionDto(String name, List<String> permissions) {
		this.name = name;
		this.permissions = permissions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getPermissions() {
		if (permissions == null) {
			permissions = new ArrayList<>();
		}
		return permissions;
	}
	
	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}
}
