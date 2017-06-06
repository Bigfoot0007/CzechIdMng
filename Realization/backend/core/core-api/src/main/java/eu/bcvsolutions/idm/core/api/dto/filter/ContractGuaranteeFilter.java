package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

/**
 * Filter for identity contract's guarantees
 * 
 * @author Radek Tomiška
 *
 */
public class ContractGuaranteeFilter implements BaseFilter {

	private UUID identityContractId;
	private UUID guaranteeId;

	public UUID getIdentityContractId() {
		return identityContractId;
	}
	
	public void setIdentityContractId(UUID identityContractId) {
		this.identityContractId = identityContractId;
	}
	
	public UUID getGuaranteeId() {
		return guaranteeId;
	}
	
	public void setGuaranteeId(UUID guaranteeId) {
		this.guaranteeId = guaranteeId;
	}
}
