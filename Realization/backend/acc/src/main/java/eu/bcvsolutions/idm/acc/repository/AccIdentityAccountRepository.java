package eu.bcvsolutions.idm.acc.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.repository.query.Param;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Identity accounts
 * 
 * @author Radek Tomiška
 *
 */
public interface AccIdentityAccountRepository extends AbstractEntityRepository<AccIdentityAccount> {
	
	List<AccIdentityAccount> findAllByAccount_Id(UUID accountId);
	
	/**
	 * Removes mapping by given account
	 * 
	 * @param account
	 * @return
	 */
	int deleteByAccount(@Param("account") AccAccount account);
	
	/**
	 * Removes mapping by given identity
	 * 
	 * @param identity
	 * @return
	 */
	int deleteByIdentity(@Param("identity") IdmIdentity identity);
}
