package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractRoleProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Run provisioning after role was saved.
 * 
 * @author Svanda
 *
 */
@Component("accRoleSaveProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioning after role is saved.")
public class RoleSaveProcessor extends AbstractRoleProcessor {

	public static final String PROCESSOR_NAME = "role-save-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RoleSaveProcessor.class);
	private ProvisioningService provisioningService;
	private final ApplicationContext applicationContext;

	@Autowired
	public RoleSaveProcessor(ApplicationContext applicationContext) {
		super(RoleEventType.CREATE, RoleEventType.UPDATE, CoreEventType.EAV_SAVE);
		//
		Assert.notNull(applicationContext);
		//
		this.applicationContext = applicationContext;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		Object breakProvisioning = event.getProperties().get(ProvisioningService.SKIP_PROVISIONING);
		
		if(breakProvisioning instanceof Boolean && (Boolean)breakProvisioning){
			return new DefaultEventResult<>(event, this);
		}
		doProvisioning(event.getContent());
		return new DefaultEventResult<>(event, this);
	}

	private void doProvisioning(IdmRoleDto role) {
		LOG.debug("Call account managment (create accounts for all systems) for role [{}]", role.getCode());
		getProvisioningService().createAccountsForAllSystems(role);
		LOG.debug("Call provisioning for role [{}]", role.getCode());
		getProvisioningService().doProvisioning(role);
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}

	/**
	 * provisioningService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private ProvisioningService getProvisioningService() {
		if (provisioningService == null) {
			provisioningService = applicationContext.getBean(ProvisioningService.class);
		}
		return provisioningService;
	}

}