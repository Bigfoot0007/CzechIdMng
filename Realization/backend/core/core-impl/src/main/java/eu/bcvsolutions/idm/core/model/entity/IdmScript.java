package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import javax.validation.constraints.NotEmpty;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Default entity for Script
 * * Name
 * * Category
 * * Groovy script (string)
 * * description
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "idm_script", indexes = {
		@Index(name = "ux_script_code", columnList = "code", unique = true),
		@Index(name = "ux_script_category", columnList = "category") 
		})
public class IdmScript extends AbstractEntity implements Codeable {

	private static final long serialVersionUID = -3827618803196757060L;

	@Audited
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = true)
	private String name;
	
	@Audited
	@NotEmpty
	@Size(min = 0, max = DefaultFieldLengths.NAME)
	@Column(name = "code", length = DefaultFieldLengths.NAME, nullable = false)
	private String code;
	
	@Audited
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "script")
	private String script;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false)
	private IdmScriptCategory category = IdmScriptCategory.DEFAULT;
	
	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public IdmScriptCategory getCategory() {
		return category;
	}

	public void setCategory(IdmScriptCategory category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
