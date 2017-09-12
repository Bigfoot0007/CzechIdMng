package eu.bcvsolutions.idm.acc.service.api;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;

/**
 * Schema attribute handling service
 * 
 * @author svandav
 *
 */
public interface SysSystemAttributeMappingService extends ReadWriteDtoService<SysSystemAttributeMappingDto, SystemAttributeMappingFilter>, CloneableService<SysSystemAttributeMappingDto> {
	
	public static final String ATTRIBUTE_VALUE_KEY = "attributeValue";
	public static final String SYSTEM_KEY = "system";
	public static final String IC_ATTRIBUTES_KEY = "icAttributes";
	public static final String ENTITY_KEY = "entity";
	public static final String ACCOUNT_UID = "uid";
	
	/**
	 * All mapped attributes in given mapping
	 * 
	 * @param systemMapping
	 * @return
	 */
	List<SysSystemAttributeMappingDto> findBySystemMapping(SysSystemMappingDto systemMapping);
	
	/**
	 * Single mapped attribute in given mapping by given name
	 * 
	 * @param systemMappingId
	 * @param name
	 * @return
	 */
	SysSystemAttributeMappingDto findBySystemMappingAndName(UUID systemMappingId, String name);
	
	/**
	 * Do transformation given value to value for target system (resource)
	 * @param uid - Account identifier, can be null
	 * @param value
	 * @param attributeMapping
	 * @return transformed value
	 */
	Object transformValueToResource(String uid, Object value, AttributeMapping attributeMapping, AbstractEntity entity);
	
	/**
	 * Do transformation given value to value for IDM system
	 * @param uid - Account identifier, can be null
	 * @param value
	 * @param attributeMapping
	 * @param entity 
	 * @param icAttributes 
	 * @return transformed value
	 */
	Object transformValueFromResource(Object value, AttributeMapping attributeMapping,  List<IcAttribute> icAttributes );

	Object transformValueToResource(String uid, Object value, String script, AbstractEntity entity, SysSystemDto system);

	Object transformValueFromResource(Object value, String script, List<IcAttribute> icAttributes, SysSystemDto system);

	/**
	 * Check on exists EAV definition for given attribute. If the definition not exist, then we try create it.
	 * Update exist attribute definition is not supported.
	 * 
	 * @param attributeMapping
	 * @param entityType
	 */
	void createExtendedAttributeDefinition(AttributeMapping attributeMapping, Class<?> entityType);
	
	/**
	 * Create instance of IC attribute for given name. Given idm value will be
	 * transformed to resource.
	 * 
	 * @param schemaAttribute
	 * @param idmValue
	 * @return
	 */
	IcAttribute createIcAttribute(SysSchemaAttributeDto schemaAttribute, Object idmValue);
	
	/**
	 * Method return {@link SysSystemAttributeMappingDto} for system id, that has flag for authentication attribute.
	 * If this attribute don't exist, found attribute flagged as UID, this attribute must exists.
	 * 
	 * @param systemId
	 * @return
	 */
	SysSystemAttributeMappingDto getAuthenticationAttribute(UUID systemId);

	/**
	 * Find value for this mapped attribute by property name. Returned value can be list of objects. Returns transformed value.
	 * 
	 * @param uid - Account identifier
	 * @param entity
	 * @param attributeHandling
	 * @param idmValue
	 * @return
	 * @throws IntrospectionException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	Object getAttributeValue(String uid, AbstractEntity entity, AttributeMapping attributeHandling);

	/**
	 * Generate UID from UID attribute
	 * @param entity
	 * @param uidAttribute
	 * @return
	 */
	String generateUid(AbstractEntity entity, SysSystemAttributeMappingDto uidAttribute);

	/**
	 * Return UID attribute from list of mapped attributes
	 * @param mappedAttributes
	 * @return
	 */
	SysSystemAttributeMappingDto getUidAttribute(List<SysSystemAttributeMappingDto> mappedAttributes, SysSystemDto system);

	/**
	 * Return transformed value from resource (IC attributes) for given mapped attribute
	 * @param attribute
	 * @param icAttributes
	 * @return
	 */
	Object getValueByMappedAttribute(AttributeMapping attribute, List<IcAttribute> icAttributes);

	/**
	 * Return value of UID attribute from resource (IC attributes).
	 * First try find UID mapped attribute (if not exist -> throw exception).
	 * Second do transform from system (if value is null or is not String -> throw exception). 
	 * @param icAttributes
	 * @param mappedAttributes
	 * @param system
	 * @return
	 */
	String getUidValueFromResource(List<IcAttribute> icAttributes, List<SysSystemAttributeMappingDto> mappedAttributes,
			SysSystemDto system);
}
