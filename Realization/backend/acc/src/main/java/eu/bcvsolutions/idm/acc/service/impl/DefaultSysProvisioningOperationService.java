package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ResultState;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningBatch;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningRequest;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningResult;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningOperationRepository;
import eu.bcvsolutions.idm.acc.repository.SysProvisioningRequestRepository;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningBatchService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.security.api.domain.ConfidentialString;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Persists provisioning operations
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultSysProvisioningOperationService
		extends AbstractReadWriteEntityService<SysProvisioningOperation, EmptyFilter> implements SysProvisioningOperationService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSysProvisioningOperationService.class);
	private static final String CONFIDENTIAL_KEY_PATTERN = "%s:%s:%d";
	private static final String ACCOUNT_OBJECT_PROPERTY_PREFIX = "sys:account:";
	private static final String CONNECTOR_OBJECT_PROPERTY_PREFIX = "sys:connector:";
	private final SysProvisioningRequestRepository provisioningRequestRepository;
	private final SysProvisioningArchiveService provisioningArchiveService;
	private final SysProvisioningBatchService batchService;
	private final NotificationManager notificationManager;
	private final ConfidentialStorage confidentialStorage;

	@Autowired
	public DefaultSysProvisioningOperationService(
			SysProvisioningOperationRepository repository,
			SysProvisioningRequestRepository provisioningRequestRepository,
			SysProvisioningArchiveService provisioningArchiveService,
			SysProvisioningBatchService batchService,
			NotificationManager notificationManager,
			ConfidentialStorage confidentialStorage) {
		super(repository);
		//
		Assert.notNull(provisioningRequestRepository);
		Assert.notNull(provisioningArchiveService);
		Assert.notNull(batchService);
		Assert.notNull(notificationManager);
		Assert.notNull(confidentialStorage);
		//
		this.provisioningRequestRepository = provisioningRequestRepository;
		this.provisioningArchiveService = provisioningArchiveService;
		this.batchService = batchService;
		this.notificationManager = notificationManager;
		this.confidentialStorage = confidentialStorage;
	}
	
	@Override
	@Transactional
	public SysProvisioningOperation save(SysProvisioningOperation entity) {
		// replace guarded strings to confidential strings (save to persist)
		Map<String, Serializable> confidentialValues = replaceGuardedStrings(entity.getProvisioningContext());
		//
		entity = super.save(entity);
		// save prepared guarded strings into confidential storage 
		for(Entry<String, Serializable> entry : confidentialValues.entrySet()) {
			confidentialStorage.save(entity, entry.getKey(), entry.getValue());
		}
		//
		return entity;
	}

	@Override
	@Transactional
	public void delete(SysProvisioningOperation provisioningOperation) {
		Assert.notNull(provisioningOperation);
		//
		// delete persisted confidential storage values
		deleteConfidentialStrings(provisioningOperation);
		//
		// create archived operation
		provisioningArchiveService.archive(provisioningOperation);	
		//
		// delete request and empty batch
		SysProvisioningBatch batch = provisioningOperation.getRequest().getBatch();
		if (batch.getRequests().size() <= 1) {
			batchService.delete(batch);
		}
		provisioningRequestRepository.deleteByOperation(provisioningOperation);
		provisioningOperation.setRequest(null);
		//
		super.delete(provisioningOperation);
	}
	
	/**
	 * Returns fully loaded AccountObject with guarded strings.
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	@Override
	public Map<UUID, Object> getFullAccountObject(SysProvisioningOperation provisioningOperation) {
		if (provisioningOperation == null 
				|| provisioningOperation.getProvisioningContext() == null 
				|| provisioningOperation.getProvisioningContext().getAccountObject() == null) {
			return null;
		}
		//
		Map<UUID, Object> fullAccountObject = new HashMap<>();
		Map<UUID, Object> accountObject = provisioningOperation.getProvisioningContext().getAccountObject();
		for (Entry<UUID, Object> entry : accountObject.entrySet()) {
			if (entry.getValue() == null) {
				fullAccountObject.put(entry.getKey(), entry.getValue());
				continue;
			}
			Object idmValue = entry.getValue();
			// single value
			if (idmValue instanceof ConfidentialString) {
				fullAccountObject.put(entry.getKey(), confidentialStorage.getGuardedString(provisioningOperation, ((ConfidentialString)idmValue).getKey()));
				continue;
			}
			// array
			if(idmValue.getClass().isArray()) {
				Object[] idmValues = (Object[]) idmValue;
				List<GuardedString> processedValues = new ArrayList<>();
				for(int j = 0; j < idmValues.length; j++) {
					Object singleValue = idmValues[j];
					if (singleValue != null && (singleValue instanceof ConfidentialString)) {
						processedValues.add(confidentialStorage.getGuardedString(provisioningOperation, ((ConfidentialString)singleValue).getKey()));
					}
				}
				if (!processedValues.isEmpty()) {
					fullAccountObject.put(entry.getKey(), processedValues.toArray(new GuardedString[processedValues.size()]));
					continue;
				}
			}
			// collection
			if (idmValue instanceof Collection) {
				Collection<?> idmValues = (Collection<?>) idmValue;
				List<GuardedString> processedValues = new ArrayList<>();
				idmValues.forEach(singleValue -> {
					if (singleValue != null && (singleValue instanceof ConfidentialString)) {													
						processedValues.add(confidentialStorage.getGuardedString(provisioningOperation, ((ConfidentialString)singleValue).getKey()));
					}
				});
				if (!processedValues.isEmpty()) {
					fullAccountObject.put(entry.getKey(), processedValues);
					continue;
				}
			}
			// copy value
			fullAccountObject.put(entry.getKey(), entry.getValue());
		}	
		return fullAccountObject;
	}
	
	/**
	 * Returns fully loaded ConnectorObject with guarded strings.
	 * 
	 * TODO: don't update connectorObject in provisioningOperation (needs attribute defensive clone)
	 * 
	 * @param provisioningOperation
	 * @return
	 */
	@Override
	public IcConnectorObject getFullConnectorObject(SysProvisioningOperation provisioningOperation) {
		if (provisioningOperation == null 
				|| provisioningOperation.getProvisioningContext() == null 
				|| provisioningOperation.getProvisioningContext().getConnectorObject() == null) {
			return null;
		}
		IcConnectorObject connectorObject = provisioningOperation.getProvisioningContext().getConnectorObject();		
		connectorObject.getAttributes().forEach(attribute -> {
			for(int j = 0; j < attribute.getValues().size(); j++) {
				Object attributeValue = attribute.getValues().get(j);
				if (attributeValue != null && (attributeValue instanceof ConfidentialString)) {						
					attribute.getValues().set(j, confidentialStorage.getGuardedString(provisioningOperation, ((ConfidentialString) attributeValue).getKey()));
				}
			}
		});
		return connectorObject;
	}
	
	@Override
	@Transactional
	public void handleFailed(SysProvisioningOperation operation, Exception ex) {
		ResultModel resultModel = new DefaultResultModel(AccResultCode.PROVISIONING_FAILED, 
				ImmutableMap.of(
						"name", operation.getSystemEntityUid(), 
						"system", operation.getSystem().getName(),
						"operationType", operation.getOperationType(),
						"objectClass", operation.getProvisioningContext().getConnectorObject().getObjectClass().getType()));			
		LOG.error(resultModel.toString(), ex);
		//
		SysProvisioningRequest request = operation.getRequest();
		request.increaseAttempt();
		request.setMaxAttempts(6); // TODO: from configuration
		operation.getRequest().setResult(
				new SysProvisioningResult.Builder(ResultState.EXCEPTION).setModel(resultModel).setCause(ex).build());
		//
		save(operation);
		//
		// calculate next attempt
		SysProvisioningBatch batch = request.getBatch();
		if (batch.getFirstRequest().equals(request)) {
			batch.setNextAttempt(batchService.calculateNextAttempt(request));
			batchService.save(batch);
		}
		//
		notificationManager.send(
				AccModuleDescriptor.TOPIC_PROVISIONING, 
				new IdmMessage.Builder().setModel(resultModel).build());
	}
	
	@Override
	@Transactional
	public void handleSuccessful(SysProvisioningOperation operation) {
		ResultModel resultModel = new DefaultResultModel(
				AccResultCode.PROVISIONING_SUCCEED, 
				ImmutableMap.of(
						"name", operation.getSystemEntityUid(), 
						"system", operation.getSystem().getName(),
						"operationType", operation.getOperationType(),
						"objectClass", operation.getProvisioningContext().getConnectorObject().getObjectClass().getType()));
		operation.getRequest().setResult(new SysProvisioningResult.Builder(ResultState.EXECUTED).setModel(resultModel).build());
		save(operation);
		//
		LOG.debug(resultModel.toString());
		notificationManager.send(AccModuleDescriptor.TOPIC_PROVISIONING, new IdmMessage.Builder().setModel(resultModel).build());
	}
	
	/**
	 * Replaces GuardedStrings as ConfidentialStrings in given {@link ProvisioningContext}. 
	 *
	 * @param context
	 * @return Returns values (key / value) to store in confidential storage. 
	 */
	protected Map<String, Serializable> replaceGuardedStrings(ProvisioningContext context) {
		Map<String, Serializable> confidentialValues = new HashMap<>();
		if (context == null) {
			return confidentialValues;
		}
		//
		Map<UUID, Object> accountObject = context.getAccountObject();
		if (accountObject != null) {
			for (Entry<UUID, Object> entry : accountObject.entrySet()) {
				if (entry.getValue() == null) {
					continue;
				}
				Object idmValue = entry.getValue();
				// single value
				if (idmValue instanceof GuardedString) {
					GuardedString guardedString = (GuardedString) entry.getValue();
					// save value into confidential storage
					String confidentialStorageKey = createAccountObjectPropertyKey(entry.getKey(), 0);
					confidentialValues.put(confidentialStorageKey, guardedString.asString());
					accountObject.put(entry.getKey(), new ConfidentialString(confidentialStorageKey));
				}
				// array
				else if(idmValue.getClass().isArray()) {
					Object[] idmValues = (Object[]) idmValue;
					List<ConfidentialString> processedValues = new ArrayList<>();
					for(int j = 0; j < idmValues.length; j++) {
						Object singleValue = idmValues[j];
						if (singleValue != null && (singleValue instanceof GuardedString)) {
							GuardedString guardedString = (GuardedString) singleValue;
							// save value into confidential storage
							String confidentialStorageKey = createAccountObjectPropertyKey(entry.getKey(), j);
							confidentialValues.put(confidentialStorageKey, guardedString.asString());
							processedValues.add(new ConfidentialString(confidentialStorageKey));
						}
					}
					if (!processedValues.isEmpty()) {
						accountObject.put(entry.getKey(), processedValues.toArray(new ConfidentialString[processedValues.size()]));
					}
				}
				// collection
				else if (idmValue instanceof Collection) {
					Collection<?> idmValues = (Collection<?>) idmValue;
					List<ConfidentialString> processedValues = new ArrayList<>();
					idmValues.forEach(singleValue -> {
						if (singleValue != null && (singleValue instanceof GuardedString)) {
							GuardedString guardedString = (GuardedString) singleValue;
							// save value into confidential storage
							String confidentialStorageKey = createAccountObjectPropertyKey(entry.getKey(), processedValues.size());
							confidentialValues.put(confidentialStorageKey, guardedString.asString());							
							processedValues.add(new ConfidentialString(confidentialStorageKey));
						}
					});
					if (!processedValues.isEmpty()) {
						accountObject.put(entry.getKey(), processedValues);
					}
				}
				
			}
		}
		//
		IcConnectorObject connectorObject = context.getConnectorObject();
		if (connectorObject != null) {
			for(IcAttribute attribute : connectorObject.getAttributes()) {
				for(int j = 0; j < attribute.getValues().size(); j++) {
					Object attributeValue = attribute.getValues().get(j);
					if (attributeValue != null && (attributeValue instanceof GuardedString)) {
						GuardedString guardedString = (GuardedString) attributeValue;
						String confidentialStorageKey = createConnectorObjectPropertyKey(attribute, j);
						confidentialValues.put(confidentialStorageKey, guardedString.asString());
						attribute.getValues().set(j, new ConfidentialString(confidentialStorageKey));
					}
				}
			}
		}
		//
		return confidentialValues;
	}
	
	/**
	 * Creates account object property key into confidential storage
	 * 
	 * @param property
	 * @param index
	 * @return
	 */
	protected String createAccountObjectPropertyKey(UUID property, int index) {
		return String.format(CONFIDENTIAL_KEY_PATTERN, ACCOUNT_OBJECT_PROPERTY_PREFIX, property.toString(), index);
	}
	
	/**
	 * Creates connector object property key into confidential storage
	 * 
	 * @param property
	 * @param index
	 * @return
	 */
	protected String createConnectorObjectPropertyKey(IcAttribute property, int index) {
		return String.format(CONFIDENTIAL_KEY_PATTERN, CONNECTOR_OBJECT_PROPERTY_PREFIX, property.getName(), index);
	}
	
	/**
	 * Deletes persisted confidential storage values
	 * 
	 * @param context
	 */
	protected void deleteConfidentialStrings(SysProvisioningOperation provisioningOperation) {
		Assert.notNull(provisioningOperation);
		//
		ProvisioningContext context = provisioningOperation.getProvisioningContext();
		if (context == null) {
			return;
		}
		
		Map<UUID, Object> accountObject = context.getAccountObject();
		if (accountObject != null) {
			for (Entry<UUID, Object> entry : accountObject.entrySet()) {
				Object idmValue = entry.getValue();
				if (idmValue == null) {
					continue;
				}
				// single value
				if (idmValue instanceof ConfidentialString) {
					confidentialStorage.delete(provisioningOperation, ((ConfidentialString)entry.getValue()).getKey());
				}
				// array
				else if(idmValue.getClass().isArray()) {
					Object[] idmValues = (Object[]) idmValue;
					for(int j = 0; j < idmValues.length; j++) {
						Object singleValue = idmValues[j];
						if (singleValue != null && (singleValue instanceof ConfidentialString)) {
							confidentialStorage.delete(provisioningOperation, ((ConfidentialString)singleValue).getKey());
						}
					}
				}
				// collection
				else if (idmValue instanceof Collection) {
					Collection<?> idmValues = (Collection<?>) idmValue;
					idmValues.forEach(singleValue -> {
						if (singleValue != null && (singleValue instanceof ConfidentialString)) {
							confidentialStorage.delete(provisioningOperation, ((ConfidentialString)singleValue).getKey());
						}
					});
				}		
			}
		}
		//
		IcConnectorObject connectorObject = context.getConnectorObject();
		if (connectorObject != null) {
			connectorObject.getAttributes().forEach(attribute -> {
				attribute.getValues().forEach(attributeValue -> {
					if (attributeValue != null && (attributeValue instanceof ConfidentialString)) {
						confidentialStorage.delete(provisioningOperation, ((ConfidentialString)attributeValue).getKey());
					}
				});	
			});
		}
	}
}
