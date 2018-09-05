package eu.bcvsolutions.idm.core.api.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemChangesDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Manager for automatic role request
 * 
 * @author svandav
 * 
 */
public interface RequestManager<R extends Requestable> extends RequestService<IdmRequestDto> {

	R post(Serializable requestId, R dto, BasePermission... permission);

	R delete(Serializable requestId, R dto, BasePermission... permission);

	R get(UUID requestId, UUID dtoId, Class<R> dtoClass, BasePermission... permission);

	IdmRequestDto createRequest(R dto, BasePermission... permission);

	Page<R> find(Class<? extends R> dtoClass, Serializable requestId, BaseFilter filter, Pageable pageable,
			IdmBasePermission permission);

	IdmFormInstanceDto saveFormInstance(UUID requestId, R owner, IdmFormDefinitionDto formDefinition,
			List<IdmFormValueDto> newValues, BasePermission... permission);

	IdmFormInstanceDto getFormInstance(UUID fromString, R owner, IdmFormDefinitionDto formDefinition,
			BasePermission... permission);

	IdmRequestItemChangesDto getChanges(IdmRequestItemDto item, BasePermission... permission);

	/**
	 * Returns confidential storage key for given request item
	 * 
	 * @param itemId
	 * @return
	 */
	public static String getConfidentialStorageKey(UUID itemId) {
		Assert.notNull(itemId);
		//
		return FormValueService.CONFIDENTIAL_STORAGE_VALUE_PREFIX + ":" + itemId;
	}

	/**
	 * Cancel requests and request items using that deleting DTO
	 * 
	 * @param requestable
	 */
	void onDeleteRequestable(R requestable);

	/**
	 * Delete given requestable DTO. Creates and executes request.
	 * 
	 * @param dto
	 * @param executeImmediately
	 */
	void deleteRequestable(R dto, boolean executeImmediately);

	
	/**
	 * Get DTO from the request item. Place for additional conversion (EAV attribute
	 * for example)
	 *
	 * @param item
	 * @param type
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	R convertItemToDto(IdmRequestItemDto item, Class<? extends R> type)
			throws JsonParseException, JsonMappingException, IOException, ClassNotFoundException;

}
