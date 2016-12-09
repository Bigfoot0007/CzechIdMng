package eu.bcvsolutions.idm.eav.rest.projection;

import java.util.List;

import org.springframework.data.rest.core.config.Projection;

import eu.bcvsolutions.idm.core.api.rest.projection.AbstractDtoProjection;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;

/**
 * Form definition trimmed projection
 * 
 * @author Radek Tomiška
 *
 */
@Projection(name = "excerpt", types = IdmFormDefinition.class)
public interface IdmFormDefinitionExcerpt extends AbstractDtoProjection {

	String getType();
	
	String getName();
	
	List<IdmFormAttribute> getFormAttributes();
}
