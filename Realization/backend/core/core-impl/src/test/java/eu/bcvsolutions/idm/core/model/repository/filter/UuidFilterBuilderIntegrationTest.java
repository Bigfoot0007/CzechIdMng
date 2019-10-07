package eu.bcvsolutions.idm.core.model.repository.filter;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * UuidFilterBuilder test
 * 
 * @author Radek Tomiška
 *
 */
public class UuidFilterBuilderIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private IdmIdentityRepository repository;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testFindIdentityByUuid() {
		// prepare data
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto roleOne = getHelper().createRole();
		UuidFilter<IdmIdentity> identityFilter = new FindableUuidFilter<>(repository); 
		//
		DataFilter dataFilter = new DataFilter(IdmIdentityDto.class);
		dataFilter.setId(identityOne.getId());
		List<IdmIdentity> identities = identityFilter.find(dataFilter, null).getContent();
		//
		assertEquals(1, identities.size());
		assertEquals(identityOne.getId(), identities.get(0).getId());
		//
		dataFilter.setId(identityTwo.getId());
		identities = identityFilter.find(dataFilter, null).getContent();
		assertEquals(1, identities.size());
		assertEquals(identityTwo.getId(), identities.get(0).getId());
		//
		dataFilter.setId(roleOne.getId());
		assertEquals(0, identityFilter.find(dataFilter, null).getTotalElements());
	}
	
	private class FindableUuidFilter<E extends AbstractEntity> extends UuidFilter<E> {
		
		private final AbstractEntityRepository<E> repository;
		
		public FindableUuidFilter(AbstractEntityRepository<E> repository) {
			this.repository = repository;
		}
		
		@Override
		public Page<E> find(DataFilter filter, Pageable pageable) {
			// transform filter to criteria
			Specification<E> criteria = new Specification<E>() {
				
				private static final long serialVersionUID = 1L;

				public Predicate toPredicate(Root<E> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
					Predicate predicate = FindableUuidFilter.this.getPredicate(root, query, builder, filter);
					return query.where(predicate).getRestriction();
				}
			};
			if (pageable == null) {
				pageable = PageRequest.of(0, Integer.MAX_VALUE);
			}
			return repository.findAll(criteria, pageable);
		}
		
	}

}
