package eu.bcvsolutions.idm.core.model.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.dto.filter.RoleFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Roles repository
 * 
 * @author Radek Tomiška 
 *
 */
public interface IdmRoleRepository extends AbstractEntityRepository<IdmRole, RoleFilter> {
	
	/**
	 * @deprecated use IdmRoleService (uses criteria api)
	 */
	@Override
	@Deprecated
	@Query(value = "select e from #{#entityName} e")
	default Page<IdmRole> find(RoleFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Use IdmRoleService (uses criteria api)");
	}
	
	/**
	 * @deprecated use {@link #findOneByCode(String)}
	 */
	@Deprecated
	IdmRole findOneByName(@Param("name") String name);
	
	@Query(value = "select e from #{#entityName} e where e.name = :code")
	IdmRole findOneByCode(@Param("code") String code);
	
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
	@Query(value = "select e from #{#entityName} e where e = :role")
	IdmRole getPersistedRole(@Param("role") IdmRole role);

	@Query("select s.sub from #{#entityName} e join e.subRoles s where e.id = :roleId")
	List<IdmRole> getSubroles(@Param("roleId") UUID roleId);
	
}
