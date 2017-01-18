package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.ResultState;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningResult;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningBatchRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;

/**
 * Entry point to all provisioning operations.
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultProvisioningExecutor implements ProvisioningExecutor {

	private final EntityEventManager entityEventManager;
	private final SysProvisioningOperationService sysProvisioningOperationService;
	@Autowired
	private SysProvisioningBatchRepository batchRepository;

	@Autowired
	public DefaultProvisioningExecutor(
			SysProvisioningOperationRepository repository,
			EntityEventManager entityEventManager,
			SysProvisioningOperationService sysProvisioningOperationService) {
		Assert.notNull(entityEventManager);
		Assert.notNull(sysProvisioningOperationService);
		//
		this.entityEventManager = entityEventManager;
		this.sysProvisioningOperationService = sysProvisioningOperationService;
	}

	@Override
	public SysProvisioningOperation execute(SysProvisioningOperation provisioningOperation) {
		Assert.notNull(provisioningOperation);
		//
		if (provisioningOperation.getId() == null) {
			// save new operation to provisioning log / queue
			SysProvisioningRequest request = new SysProvisioningRequest(provisioningOperation);
			request.setResult(new SysProvisioningResult.Builder(ResultState.CREATED).build());
			SysProvisioningBatch batch = batchRepository.findBatch(provisioningOperation);
			if (batch == null) {
				batch = batchRepository.save(new SysProvisioningBatch());
			} else {
				// put to queue
			}
			request.setBatch(batch);
			provisioningOperation.setRequest(request);
			//
			provisioningOperation = sysProvisioningOperationService.save(provisioningOperation);
		}
		CoreEvent<SysProvisioningOperation> event = new CoreEvent<SysProvisioningOperation>(provisioningOperation.getOperationType(), provisioningOperation);
		EventContext<SysProvisioningOperation> context = entityEventManager.process(event);
		return context.getContent();
	}
	
	@Override
	public SysProvisioningOperation cancel(SysProvisioningOperation provisioningOperation) {
		// Cancel single request
		CoreEvent<SysProvisioningOperation> event = new CoreEvent<SysProvisioningOperation>(ProvisioningOperationType.CANCEL, provisioningOperation);
		EventContext<SysProvisioningOperation> context = entityEventManager.process(event);
		return context.getContent();
	}

}
