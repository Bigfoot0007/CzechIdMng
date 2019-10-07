package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Identity's password provisioning
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Identity's and all selected systems password provisioning.")
public class IdentityPasswordProvisioningProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "identity-password-provisioning-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordProvisioningProcessor.class);
	private final ProvisioningService provisioningService;
	
	@Autowired
	public IdentityPasswordProvisioningProcessor(ProvisioningService provisioningService) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(provisioningService, "Service is required.");
		//
		this.provisioningService = provisioningService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(passwordChangeDto, "Password change dto is required.");
		//
		LOG.debug("Call provisioning for identity password [{}]", event.getContent().getUsername());
		List<OperationResult> results = provisioningService.changePassword(identity, passwordChangeDto);
		//
		return new DefaultEventResult.Builder<>(event, this).setResults(results).build();
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}