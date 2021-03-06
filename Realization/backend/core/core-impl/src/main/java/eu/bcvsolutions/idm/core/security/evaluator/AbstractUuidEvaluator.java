package eu.bcvsolutions.idm.core.security.evaluator;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Share entity with uuid
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractUuidEvaluator<T extends Identifiable> extends AbstractAuthorizationEvaluator<T> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractUuidEvaluator.class);
	public static final String PARAMETER_UUID = "uuid";
	
	@Override
	public Predicate getPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasPermission(policy, permission)) {
			return null;
		}
		//
		UUID uuid = getUuid(policy);		
		if (uuid == null) { 
			return null;
		}
		return builder.equal(root.get(AbstractEntity_.id.getName()), uuid);
	}
	
	@Override
	public Set<String> getPermissions(T entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);
		if (entity == null) {
			return permissions;
		}	
		UUID uuid = getUuid(policy);
		if (uuid != null && uuid.equals(entity.getId())) {
			permissions.addAll(policy.getPermissions());
		}
		return permissions;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_UUID);
		return parameters;
	}
	
	private UUID getUuid(AuthorizationPolicy policy) {
		try {
			return policy.getEvaluatorProperties().getUuid(PARAMETER_UUID);
		} catch (ClassCastException ex) {
			LOG.warn("Wrong uuid for authorization evaluator - skipping.", ex);
			return null;
		}
	}
}
