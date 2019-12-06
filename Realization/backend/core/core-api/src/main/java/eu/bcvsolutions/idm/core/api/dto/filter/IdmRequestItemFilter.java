package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;

/**
 * Filter for request's items
 *
 * @author svandav
 */
public class IdmRequestItemFilter extends DataFilter {

	private List<RequestState> states;
	private UUID requestId;
	private UUID ownerId;
	private String ownerType;
	private RequestOperationType operationType;

	public IdmRequestItemFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmRequestItemFilter(MultiValueMap<String, Object> data) {
		super(IdmRequestItemDto.class, data);
	}

	public List<RequestState> getStates() {
		if (states == null) {
			states = new ArrayList<>();
		}
		return states;
	}

	public void setStates(List<RequestState> states) {
		this.states = states;
	}

	public UUID getRequestId() {
		return requestId;
	}

	public void setRequestId(UUID requestId) {
		this.requestId = requestId;
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public RequestOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(RequestOperationType operationType) {
		this.operationType = operationType;
	}

}
