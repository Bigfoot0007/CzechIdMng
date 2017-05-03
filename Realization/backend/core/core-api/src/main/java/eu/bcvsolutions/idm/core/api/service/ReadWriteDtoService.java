package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Interface for generic CRUD operations on a repository for a specific DTO type.
 * 
 * @param <DTO> {@link BaseDto} type
 * @param <E> {@link BaseEntity} type
 * @param <F> {@link BaseFilter} type
 * @author Svanda
 * @author Radek Tomiška
 */
public interface ReadWriteDtoService<DTO extends BaseDto, E extends BaseEntity, F extends BaseFilter> extends ReadDtoService<DTO, E, F> {
	
	/**
	 * Persists a given DTO to repository.
	 * 
	 * @param entity
	 * @return the saved DTO
	 */
	DTO saveInternal(DTO dto);
	
	/**
	 * Saves a given DTO. Event could be published instead persisting dto directly. Authorization policies are evaluated.
	 * 
	 * @param dto
	 * @param permission permissions to evaluate
	 * @return the saved DTO
	 * @throws IllegalArgumentException in case the given DTO is {@literal null}.
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	DTO save(DTO dto, BasePermission... permission);	
	
	/**
	 * Saves all given DTO. Event could be published instead persisting directly.
	 * 
	 * @param dtos
	 * @param permission permissions to evaluate
	 * @return the saved DTOs
	 * @throws IllegalArgumentException in case the given DTO is {@literal null}.
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	Iterable<DTO> saveAll(Iterable<DTO> dtos, BasePermission... permission);
	
	/**
	 * Deletes a given DTO. Event could be published instead persisting dto directly. Authorization policies are evaluated.
	 * 
	 * @param dto
	 * @param permission permissions to evaluate
	 * @throws IllegalArgumentException in case the given DTO is {@literal null}.
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	void delete(DTO dto, BasePermission... permission);
	
	void deleteById(Serializable id, BasePermission... permission);
	
	/**
	 * Deletes a given DTO (from repository).
	 * 
	 * @param dto
	 * @throws IllegalArgumentException in case the given DTO is {@literal null}.
	 */
	void deleteInternal(DTO dto);
	
	/**
	 * Deletes a dto by given id (from repository).
	 * 
	 * @param dto id
	 * @throws IllegalArgumentException in case the given DTO id is {@literal null}.
	 */
	void deleteInternalById(Serializable id);
}
