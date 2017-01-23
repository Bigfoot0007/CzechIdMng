package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;

/**
 * Save identity role
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists identity role.")
public class IdentityRoleSaveProcessor extends CoreEventProcessor<IdmIdentityRole> {

	public static final String PROCESSOR_NAME = "identity-role-save-processor";
	private final IdmIdentityRoleRepository repository;
	
	@Autowired
	public IdentityRoleSaveProcessor(
			IdmIdentityRoleRepository repository) {
		super(IdentityRoleEventType.CREATE, IdentityRoleEventType.UPDATE);
		//
		Assert.notNull(repository);
		//
		this.repository = repository;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityRole> process(EntityEvent<IdmIdentityRole> event) {
		repository.save(event.getContent());
		//
		return new DefaultEventResult<>(event, this);
	}
}