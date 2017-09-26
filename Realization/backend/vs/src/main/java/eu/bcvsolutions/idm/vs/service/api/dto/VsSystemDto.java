package eu.bcvsolutions.idm.vs.service.api.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO for create new virtual system
 * 
 * @author Svanda
 *
 */
@Relation(collectionRelation = "vs-systems")
@ApiModel(description = "Dto for create new virtual system")
public class VsSystemDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Name of vs system")
	private String name;
	@ApiModelProperty(required = false, notes = "Identities in IdM. Will be implementers for this system.")
	private List<UUID> implementers;
	@ApiModelProperty(required = false, notes = "Roles where his identities will be implementers for this system.")
	private List<UUID> implementerRoles;
	@ApiModelProperty(required = false, notes = "Attributes of systems, if is null then will be used default attributes")
	private List<String> attributes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<UUID> getImplementers() {
		if (implementers == null) {
			implementers = new ArrayList<>();
		}
		return implementers;
	}

	public void setImplementers(List<UUID> implementers) {
		this.implementers = implementers;
	}

	public List<UUID> getImplementerRoles() {
		if (implementerRoles == null) {
			implementerRoles = new ArrayList<>();
		}
		return implementerRoles;
	}

	public void setImplementerRoles(List<UUID> implementerRoles) {
		this.implementerRoles = implementerRoles;
	}

	public List<String> getAttributes() {
		if(attributes == null){
			attributes = new ArrayList<>();
		}
		return attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
}
