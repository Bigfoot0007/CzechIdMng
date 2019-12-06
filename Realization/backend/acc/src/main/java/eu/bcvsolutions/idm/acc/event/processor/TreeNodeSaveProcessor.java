package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.TreeNodeEvent.TreeNodeEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Run provisioning after tree node was saved.
 * 
 * @author Svanda
 * @author Radek Tomiška
 */
@Component(TreeNodeSaveProcessor.PROCESSOR_NAME)
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioning after tree node is saved.")
public class TreeNodeSaveProcessor extends AbstractEntityEventProcessor<IdmTreeNodeDto> {

	public static final String PROCESSOR_NAME = "acc-tree-node-save-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TreeNodeSaveProcessor.class);
	private ProvisioningService provisioningService;
	private final ApplicationContext applicationContext;
	
	@Autowired
	public TreeNodeSaveProcessor(ApplicationContext applicationContext) {
		super(TreeNodeEventType.NOTIFY);
		//
		Assert.notNull(applicationContext, "Context is required.");
		//
		this.applicationContext = applicationContext;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmTreeNodeDto> event) {
		// Skip provisioning
		return !this.getBooleanProperty(ProvisioningService.SKIP_PROVISIONING, event.getProperties());
	}

	@Override
	public EventResult<IdmTreeNodeDto> process(EntityEvent<IdmTreeNodeDto> event) {
		doProvisioning(event.getContent());
		//
		return new DefaultEventResult<>(event, this);
	}
	
	private void doProvisioning(IdmTreeNodeDto node) {
		LOG.debug("Call account managment (create accounts for all systems) for tree node [{}]", node.getCode());
		getProvisioningService().accountManagement(node);
		LOG.debug("Call provisioning for tree node [{}]", node.getCode());
		getProvisioningService().doProvisioning(node);
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