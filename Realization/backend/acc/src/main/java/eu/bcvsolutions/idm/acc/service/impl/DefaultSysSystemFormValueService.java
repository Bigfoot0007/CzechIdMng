package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemFormValue;
import eu.bcvsolutions.idm.acc.repository.SysSystemFormValueRepository;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.core.eav.service.impl.AbstractFormValueService;

/**
 * Form values for system entity
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysSystemFormValueService extends AbstractFormValueService<SysSystem, SysSystemFormValue> {

	private final SysSystemFormValueRepository systemFormValueRepository;
	
	@Autowired
	public DefaultSysSystemFormValueService(
			ConfidentialStorage confidentialStorage,
			SysSystemFormValueRepository systemFormValueRepository) {
		super(confidentialStorage);
		//
		Assert.notNull(systemFormValueRepository);
		//
		this.systemFormValueRepository = systemFormValueRepository;
	}
	
	@Override
	protected AbstractFormValueRepository<SysSystem, SysSystemFormValue> getRepository() {
		return systemFormValueRepository;
	}
}
