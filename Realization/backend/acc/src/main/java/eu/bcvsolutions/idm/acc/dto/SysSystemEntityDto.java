package eu.bcvsolutions.idm.acc.dto;

import java.util.Map;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.core.api.domain.Contextable;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Relation(collectionRelation = "systemEntities")
public class SysSystemEntityDto extends AbstractDto implements Contextable {

	private static final long serialVersionUID = -5087700187793325363L;

	private String uid;
	private SystemEntityType entityType;
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	private boolean wish = true;
	@JsonIgnore
	private Map<String, Object> context = null;

	public SysSystemEntityDto() {
	}
	
	public SysSystemEntityDto(String uid, SystemEntityType entityType) {
		this.uid = uid;
		this.entityType = entityType;
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public SystemEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}

	public UUID getSystem() {
		return system;
	}

	public void setSystem(UUID system) {
		this.system = system;
	}

	public boolean isWish() {
		return wish;
	}

	public void setWish(boolean wish) {
		this.wish = wish;
	}
	
	@JsonIgnore
	public Map<String, Object> getContext() {
		return context;
	}

	@JsonIgnore
	public void setContext(Map<String, Object> context) {
		this.context = context;
	}
}
