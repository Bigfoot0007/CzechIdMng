package eu.bcvsolutions.idm.vs.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;
import eu.bcvsolutions.idm.vs.VirtualSystemModuleDescriptor;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;
import eu.bcvsolutions.idm.vs.event.VsRequestEvent.VsRequestEventType;
import eu.bcvsolutions.idm.vs.service.api.VsRequestService;

/**
 * Realization virtual system request
 * 
 * @author svandav
 *
 */
@Enabled(VirtualSystemModuleDescriptor.MODULE_ID)
@Component
@Description("Realization virtual system request")
public class VsRequestRealizationProcessor extends CoreEventProcessor<VsRequestDto> {
	
	public static final String PROCESSOR_NAME = "vs-request-realization-processor";
	public static final String RESULT_UID = "resultUidAttribute";
	
	private final VsRequestService service;
	
	@Autowired
	public VsRequestRealizationProcessor(VsRequestService service) {
		super(VsRequestEventType.EXCECUTE); 
		//
		Assert.notNull(service, "Service is required.");
		//
		this.service = service;
	}
	
	@Override	
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<VsRequestDto> process(EntityEvent<VsRequestDto> event) {
		VsRequestDto dto = event.getContent();
		//
		dto = service.createRequest(dto);
		IcUidAttribute uid = service.internalStart(dto);
		event.getProperties().put(VsRequestRealizationProcessor.RESULT_UID, uid);
		event.setContent(dto);
		//		
		return new DefaultEventResult<>(event, this);
	}
}
