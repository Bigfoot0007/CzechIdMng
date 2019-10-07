package eu.bcvsolutions.idm.acc.event.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.AccContractAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccContractAccountService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;

/**
 * Before contract delete - deletes all contract account relations
 * 
 * @author svandav
 *
 */
@Component("accContractDeleteProcessor")
@Description("Ensures referential integrity. Cannot be disabled.")
public class IdentityContractDeleteProcessor
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {
	
	private static final Logger LOG = LoggerFactory.getLogger(IdentityContractDeleteProcessor.class);
	
	public static final String PROCESSOR_NAME = "contract-delete-processor";
	private final AccContractAccountService entityAccountService;
	
	@Autowired
	public IdentityContractDeleteProcessor(
			AccContractAccountService entityAccountService) {
		super(IdentityContractEventType.DELETE);
		//
		Assert.notNull(entityAccountService, "Service is required.");
		//
		this.entityAccountService = entityAccountService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {

		// delete relations on account (includes delete of account	)
		AccContractAccountFilter filter = new AccContractAccountFilter();
		filter.setEntityId(event.getContent().getId());
		entityAccountService.find(filter, null).forEach(entityAccount -> {
			LOG.debug("Remove contract-account for account [{}]", entityAccount.getId());
			entityAccountService.delete(entityAccount);
		});
		
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// right now before role delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}