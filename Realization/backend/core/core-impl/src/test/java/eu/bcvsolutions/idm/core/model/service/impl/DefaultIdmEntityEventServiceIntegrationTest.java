package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.DeleteExecutedEventTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Entity events integration tests
 * - referential integrity
 * - delete all (with states, which is owning by event)
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmEntityEventServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	@Autowired private IdmEntityStateService entityStateService;
	@Autowired private EntityEventManager entityEventManager;
	@Autowired private IdmIdentityService identityService;
	@Autowired private LongRunningTaskManager longRunningTaskManager;
	//
	private DefaultIdmEntityEventService entityEventService;

	@Before
	public void init() {
		entityEventService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmEntityEventService.class);
	}
	
	@Test
	@Transactional
	public void testReferentialIntegrity() {
		IdmEntityEventDto entityEvent = new IdmEntityEventDto();
		entityEvent.setOwnerType("empty");
		entityEvent.setEventType("empty");
		entityEvent.setOwnerId(UUID.randomUUID());
		entityEvent.setInstanceId("empty");
		entityEvent.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEvent.setPriority(PriorityType.NORMAL);
		entityEvent = entityEventService.save(entityEvent);
		//
		Assert.assertNotNull(entityEvent.getId());
		//
		IdmEntityStateDto entityState = new IdmEntityStateDto(entityEvent);
		entityState.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityState = entityStateService.save(entityState);
		//
		Assert.assertNotNull(entityState.getId());
		//
		entityEventService.delete(entityEvent);
		//
		Assert.assertNull(entityEventService.get(entityEvent));
		Assert.assertNull(entityStateService.get(entityState));
	}
	
	@Test
	@Ignore
	public void testReferentialIntegrityOwnerIsDeleted() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		//
		IdmEntityEventDto entityEvent = new IdmEntityEventDto();
		entityEvent.setOwnerType(entityEventManager.getOwnerType(identity.getClass()));
		entityEvent.setEventType("empty");
		entityEvent.setOwnerId(identity.getId());
		entityEvent.setInstanceId("empty");
		entityEvent.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEvent.setPriority(PriorityType.NORMAL);
		entityEvent = entityEventService.save(entityEvent);
		//
		Assert.assertNotNull(entityEvent.getId());
		//
		IdmEntityStateDto entityState = new IdmEntityStateDto(entityEvent);
		entityState.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityState = entityStateService.save(entityState);
		//
		Assert.assertNotNull(entityState.getId());
		//
		identityService.delete(identityTwo);
		//
		Assert.assertNotNull(identityService.get(identity));
		Assert.assertNotNull(entityEventService.get(entityEvent));
		Assert.assertNotNull(entityStateService.get(entityState));
		//
		identityService.delete(identity);
		//
		Assert.assertNull(identityService.get(identity));
		Assert.assertNull(entityEventService.get(entityEvent));
		Assert.assertNull(entityStateService.get(entityState));
	}
	
	@Test
	@Transactional
	public void testReferentialIntegrityParentIsDeleted() {
		IdmEntityEventDto parentEvent = new IdmEntityEventDto();
		parentEvent.setOwnerType("empty");
		parentEvent.setEventType("empty");
		parentEvent.setOwnerId(UUID.randomUUID());
		parentEvent.setInstanceId("empty");
		parentEvent.setResult(new OperationResultDto(OperationState.BLOCKED));
		parentEvent.setPriority(PriorityType.NORMAL);
		parentEvent = entityEventService.save(parentEvent);
		//
		IdmEntityEventDto entityEvent = new IdmEntityEventDto();
		entityEvent.setOwnerType("empty");
		entityEvent.setEventType("empty");
		entityEvent.setOwnerId(UUID.randomUUID());
		entityEvent.setInstanceId("empty");
		entityEvent.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEvent.setPriority(PriorityType.NORMAL);
		entityEvent.setParent(parentEvent.getId());
		entityEvent = entityEventService.save(entityEvent);
		//
		Assert.assertNotNull(entityEvent.getId());
		//
		entityEventService.delete(parentEvent);
		//
		Assert.assertNull(entityEventService.get(parentEvent.getId()));
		Assert.assertNull(entityEventService.get(entityEvent.getId()));
	}	
	
	@Test
	public void testReferentialIntegrityParentIsDeletedByLrt() {
		IdmEntityEventDto parentEvent = new IdmEntityEventDto();
		parentEvent.setOwnerType("empty");
		parentEvent.setEventType("empty");
		parentEvent.setOwnerId(UUID.randomUUID());
		parentEvent.setInstanceId("empty");
		parentEvent.setResult(new OperationResultDto(OperationState.EXECUTED));
		parentEvent.setPriority(PriorityType.NORMAL);
		parentEvent = entityEventService.save(parentEvent);
		//
		IdmEntityEventDto entityEventOne = new IdmEntityEventDto();
		entityEventOne.setOwnerType("empty");
		entityEventOne.setEventType("empty");
		entityEventOne.setOwnerId(UUID.randomUUID());
		entityEventOne.setInstanceId("empty");
		entityEventOne.setResult(new OperationResultDto(OperationState.EXECUTED));
		entityEventOne.setPriority(PriorityType.NORMAL);
		entityEventOne.setParent(parentEvent.getId());
		entityEventOne.setRootId(parentEvent.getId());
		entityEventOne = entityEventService.save(entityEventOne);
		//
		IdmEntityEventDto entityEventTwo = new IdmEntityEventDto();
		entityEventTwo.setOwnerType("empty");
		entityEventTwo.setEventType("empty");
		entityEventTwo.setOwnerId(UUID.randomUUID());
		entityEventTwo.setInstanceId("empty");
		entityEventTwo.setResult(new OperationResultDto(OperationState.EXECUTED));
		entityEventTwo.setPriority(PriorityType.NORMAL);
		entityEventTwo.setParent(parentEvent.getId());
		entityEventTwo.setRootId(parentEvent.getId());
		entityEventTwo = entityEventService.save(entityEventTwo);
		//
		Assert.assertNotNull(parentEvent.getId());
		Assert.assertNotNull(entityEventOne.getId());
		Assert.assertNotNull(entityEventTwo.getId());
		//
		longRunningTaskManager.execute(new DeleteExecutedEventTaskExecutor());
		//
		Assert.assertNull(entityEventService.get(parentEvent.getId()));
		Assert.assertNull(entityEventService.get(entityEventOne.getId()));
		Assert.assertNull(entityEventService.get(entityEventTwo.getId()));
	}
	
	@Test
	@Transactional
	public void testReferentialIntegrityRootIsDeleted() {
		IdmEntityEventDto parentEvent = new IdmEntityEventDto();
		parentEvent.setId(UUID.randomUUID());
		parentEvent.setRootId(parentEvent.getId());
		parentEvent.setOwnerType("empty");
		parentEvent.setEventType("empty");
		parentEvent.setOwnerId(UUID.randomUUID());
		parentEvent.setInstanceId("empty");
		parentEvent.setResult(new OperationResultDto(OperationState.EXECUTED));
		parentEvent.setPriority(PriorityType.NORMAL);
		
		parentEvent = entityEventService.save(parentEvent);
		//
		IdmEntityEventDto entityEventOne = new IdmEntityEventDto();
		entityEventOne.setRootId(parentEvent.getId());
		entityEventOne.setOwnerType("empty");
		entityEventOne.setEventType("empty");
		entityEventOne.setOwnerId(UUID.randomUUID());
		entityEventOne.setInstanceId("empty");
		entityEventOne.setResult(new OperationResultDto(OperationState.EXECUTED));
		entityEventOne.setPriority(PriorityType.NORMAL);
		entityEventOne.setParent(parentEvent.getId());
		entityEventOne.setRootId(parentEvent.getId());
		entityEventOne = entityEventService.save(entityEventOne);
		//
		IdmEntityEventDto entityEventTwo = new IdmEntityEventDto();
		entityEventTwo.setRootId(parentEvent.getId());
		entityEventTwo.setOwnerType("empty");
		entityEventTwo.setEventType("empty");
		entityEventTwo.setOwnerId(UUID.randomUUID());
		entityEventTwo.setInstanceId("empty");
		entityEventTwo.setResult(new OperationResultDto(OperationState.EXECUTED));
		entityEventTwo.setPriority(PriorityType.NORMAL);
		entityEventTwo.setParent(parentEvent.getId());
		entityEventTwo.setRootId(parentEvent.getId());
		entityEventTwo = entityEventService.save(entityEventTwo);
		//
		Assert.assertNotNull(parentEvent.getId());
		Assert.assertNotNull(entityEventOne.getId());
		Assert.assertNotNull(entityEventTwo.getId());
		//
		entityEventService.delete(parentEvent);
		//
		Assert.assertNull(entityEventService.get(parentEvent.getId()));
		Assert.assertNull(entityEventService.get(entityEventOne.getId()));
		Assert.assertNull(entityEventService.get(entityEventTwo.getId()));
	}
	
	@Test
	@Transactional
	public void testDeleteAll() {
		String mockOwnerType = getHelper().createName();
		//
		IdmEntityEventDto entityEvent = new IdmEntityEventDto();
		entityEvent.setOwnerType(mockOwnerType);
		entityEvent.setEventType("empty");
		entityEvent.setOwnerId(UUID.randomUUID());
		entityEvent.setInstanceId("empty");
		entityEvent.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityEvent.setPriority(PriorityType.NORMAL);
		entityEvent = entityEventService.save(entityEvent);
		//
		Assert.assertNotNull(entityEvent.getId());
		//
		IdmEntityStateDto entityState = new IdmEntityStateDto(entityEvent);
		entityState.setResult(new OperationResultDto(OperationState.BLOCKED));
		entityState = entityStateService.save(entityState);
		IdmEntityStateDto otherState = new IdmEntityStateDto();
		otherState.setInstanceId("mock");
		otherState.setOwnerId(UUID.randomUUID());
		otherState.setOwnerType(mockOwnerType);
		otherState.setResult(new OperationResultDto(OperationState.BLOCKED));
		otherState = entityStateService.save(otherState);
		//
		Assert.assertNotNull(entityState.getId());
		Assert.assertNotNull(otherState.getId());
		//
		//
		IdmEntityEventFilter eventFilter = new IdmEntityEventFilter();
		eventFilter.setOwnerType(mockOwnerType);
		Assert.assertEquals(1, entityEventService.find(eventFilter, null).getTotalElements());
		IdmEntityStateFilter stateFilter = new IdmEntityStateFilter();
		stateFilter.setOwnerType(mockOwnerType);
		Assert.assertEquals(2, entityStateService.find(stateFilter, null).getTotalElements());
		//
		entityEventService.deleteAll();
		Assert.assertEquals(0, entityEventService.find(eventFilter, null).getTotalElements());
		List<IdmEntityStateDto> states = entityStateService.find(stateFilter, null).getContent();
		Assert.assertEquals(1, states.size());
		Assert.assertEquals(otherState.getId(), states.get(0).getId());
	}
	
	@Test
	@Transactional
	public void testExceptOwner() {
		String mockOwnerType = getHelper().createName();
		String instanceId = getHelper().createName();
		//
		UUID ownerOne = UUID.randomUUID();
		IdmEntityEventDto entityEventOne = new IdmEntityEventDto();
		entityEventOne.setOwnerType(mockOwnerType);
		entityEventOne.setEventType("empty");
		entityEventOne.setOwnerId(ownerOne);
		entityEventOne.setInstanceId(instanceId);
		entityEventOne.setResult(new OperationResultDto(OperationState.CREATED));
		entityEventOne.setPriority(PriorityType.NORMAL);
		entityEventOne = entityEventService.save(entityEventOne);
		//
		UUID ownerTwo = UUID.randomUUID();
		IdmEntityEventDto entityEventTwo = new IdmEntityEventDto();
		entityEventTwo.setOwnerType(mockOwnerType);
		entityEventTwo.setEventType("empty");
		entityEventTwo.setOwnerId(ownerTwo);
		entityEventTwo.setInstanceId(instanceId);
		entityEventTwo.setResult(new OperationResultDto(OperationState.CREATED));
		entityEventTwo.setPriority(PriorityType.NORMAL);
		entityEventTwo = entityEventService.save(entityEventTwo);
		//
		List<IdmEntityEventDto> events = entityEventService.findToExecute(instanceId, null, PriorityType.NORMAL, null, null).getContent();
		//
		Assert.assertEquals(2, events.size());
		Assert.assertTrue(events.stream().anyMatch(e -> e.getOwnerId().equals(ownerOne)));
		Assert.assertTrue(events.stream().anyMatch(e -> e.getOwnerId().equals(ownerTwo)));
		//
		events = entityEventService.findToExecute(instanceId, null, PriorityType.NORMAL, Lists.newArrayList(ownerTwo), null).getContent();
		//
		Assert.assertEquals(1, events.size());
		Assert.assertTrue(events.stream().anyMatch(e -> e.getOwnerId().equals(ownerOne)));
	}
}
