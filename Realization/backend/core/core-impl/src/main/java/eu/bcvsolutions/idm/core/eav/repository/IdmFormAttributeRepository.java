package eu.bcvsolutions.idm.core.eav.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;

/**
 * Form attribute definition repository
 * 
 * @author Radek Tomiška 
 *
 */
public interface IdmFormAttributeRepository extends AbstractEntityRepository<IdmFormAttribute, IdmFormAttributeFilter> {
	
	/**
	 * @deprecated Use IdmFormAttributeService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmFormAttribute> find(IdmFormAttributeFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmFormAttributeService (uses criteria api)");
	}
	
	
	/**
	 * Attribute definition name is unique in one form definition
	 * 
	 * @param formDefinition
	 * @param name
	 * @return
	 */
	IdmFormAttribute findOneByFormDefinitionAndCode(@Param("formDefinition") IdmFormDefinition formDefinition, @Param("code") String code);
	
	/**
	 * Finds one attribute from given definition by given name
	 * 
	 * @param definitionType
	 * @param definitionName
	 * @param name
	 * @return
	 */
	IdmFormAttribute findOneByFormDefinition_typeAndFormDefinition_codeAndCode(String definitionType, String definitionCode, String code);
	
	/**
	 * Returns all form attributes by given definition ordered by seq
	 * 
	 * @param formDefinition
	 * @return
	 */
	List<IdmFormAttribute> findByFormDefinitionOrderBySeq(@Param("formDefinition") IdmFormDefinition formDefinition);
	
	/**
	 * Returns all form attributes by given definition ordered by seq
	 * 
	 * @param formDefinition
	 * @return
	 */
	List<IdmFormAttribute> findByFormDefinition_IdOrderBySeq(@Param("formDefinitionId") UUID formDefinitionId);
}
