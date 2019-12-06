package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.time.ZonedDateTime;
import java.time.LocalDate;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;

/**
 * Entity for test contract slice table resource
 * 
 * @author svandav
 *
 */
@Entity
@Table(name = TestContractSliceResource.TABLE_NAME)
public class TestContractSliceResource {

	public static final String TABLE_NAME = "test_contract_slice_resource";

	@Id
	@Column(name = "ID", length = DefaultFieldLengths.NAME)
	private String id;
	@Column(name = "NAME", length = DefaultFieldLengths.NAME)
	private String name;
	@Column(name = "STATE", length = DefaultFieldLengths.NAME)
	private String state;
	@Column(name = "DISABLED", length = DefaultFieldLengths.NAME)
	private String disabled;
	@Column(name = "DESCRIPTION", length = DefaultFieldLengths.NAME)
	private String description;
	@Column(name = "VALIDFROM")
	private LocalDate validFrom;
	@Column(name = "VALIDTILL")
	private LocalDate validTill;
	@Column(name = "LEADER", length = DefaultFieldLengths.NAME)
	private String leader;
	@Column(name = "MAIN", length = DefaultFieldLengths.NAME)
	private String main;
	@Column(name = "OWNER", length = DefaultFieldLengths.NAME)
	private String owner;
	@Column(name = "WORKPOSITION", length = DefaultFieldLengths.NAME)
	private String workposition;
	@Column(name = "MODIFIED")
	private ZonedDateTime modified;
	@Column(name = "VALIDFROM_SLICE")
	private LocalDate validFromSlice;
	@Column(name = "CONTRACT_CODE", length = DefaultFieldLengths.NAME)
	private String contractCode;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getDisabled() {
		return disabled;
	}

	public void setDisabled(String disabled) {
		this.disabled = disabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTill) {
		this.validTill = validTill;
	}

	public String getLeader() {
		return leader;
	}

	public void setLeader(String leader) {
		this.leader = leader;
	}

	public String getMain() {
		return main;
	}

	public void setMain(String main) {
		this.main = main;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public ZonedDateTime getModified() {
		return modified;
	}

	public void setModified(ZonedDateTime modified) {
		this.modified = modified;
	}

	public String getWorkposition() {
		return workposition;
	}

	public void setWorkposition(String workposition) {
		this.workposition = workposition;
	}

	public LocalDate getValidFromSlice() {
		return validFromSlice;
	}

	public void setValidFromSlice(LocalDate validFromSlice) {
		this.validFromSlice = validFromSlice;
	}

	public String getContractCode() {
		return contractCode;
	}

	public void setContractCode(String contractCode) {
		this.contractCode = contractCode;
	}
}
