package eu.bcvsolutions.idm.core.model.service.api;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;

/**
 * Subordinates criteria builder.
 * 
 * Override in custom module for changing subordinates evaluation.
 * 
 * TODO: split to two builders, better api, dynamic filters registration
 * 
 * @author Radek Tomiška
 *
 */
public interface SubordinatesCriteriaBuilder {

	/**
	 * Predicate for subordinates
	 * 
	 * @param root
	 * @param query
	 * @param builder
	 * @param subordinatesFor
	 * @param byTreeType
	 * @return
	 */
	Predicate getSubordinatesPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, 
			String subordinatesFor, IdmTreeType byTreeType);
	
	/**
	 * Predicate for manager
	 * 
	 * @param root
	 * @param query
	 * @param builder
	 * @param managersFor
	 * @param byTreeType
	 * @return
	 */
	Predicate getManagersPredicate(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, 
			String managersFor, IdmTreeType byTreeType);
	
	/**
	 * Returns given identity's managers
	 * 
	 * @param managersFor
	 * @param byTreeType
	 * @param pageable
	 * @return
	 */
	Page<IdmIdentity> getManagers(String managersFor, IdmTreeType byTreeType , Pageable pageable);
	
}
