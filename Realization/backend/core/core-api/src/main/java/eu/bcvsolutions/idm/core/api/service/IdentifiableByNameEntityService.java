package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Entity is identifiable by id and name
 * 
 * @author Radek Tomiška
 *
 * @param <E>
 * @deprecated Codeable will be used
 */
@Deprecated
public interface IdentifiableByNameEntityService<E extends BaseEntity> {

	/**
	 * Returns entity by given id
	 * 
	 * @param id
	 * @return
	 */
	E get(Serializable id);
	
	/**
	 * Return entity by given name
	 * 
	 * @param name
	 * @return
	 */
	E getByName(String name);
	
}
