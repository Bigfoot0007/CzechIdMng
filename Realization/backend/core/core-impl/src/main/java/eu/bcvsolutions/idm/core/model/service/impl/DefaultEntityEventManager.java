	package eu.bcvsolutions.idm.core.model.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.domain.comparator.CreatedComparator;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.filter.EntityEventProcessorFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.event.AsyncEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.EntityEventEvent.EntityEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventContext;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.exception.EventContentDeletedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.repository.IdmEntityEventRepository;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Entity processing based on event publishing.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultEntityEventManager implements EntityEventManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultEntityEventManager.class);
	private final ApplicationContext context;
	private final ApplicationEventPublisher publisher;
	private final EnabledEvaluator enabledEvaluator;
	private final LookupService lookupService;
	private static final ConcurrentHashMap<UUID, UUID> runningOwnerEvents = new ConcurrentHashMap<>();
	//
	@Autowired private IdmEntityEventService entityEventService;
	@Autowired private IdmEntityEventRepository entityEventRepository;
	@Autowired private IdmEntityStateService entityStateService;
	@Autowired private ConfigurationService configurationService;
	@Autowired private SecurityService securityService;
	@Autowired private NotificationManager notificationManager;
	@Autowired private EventConfiguration eventConfiguration;
	
	public DefaultEntityEventManager(
			ApplicationContext context, 	
			ApplicationEventPublisher publisher,
			EnabledEvaluator enabledEvaluator,
			LookupService lookupService) {
		Assert.notNull(context, "Spring context is required");
		Assert.notNull(publisher, "Event publisher is required");
		Assert.notNull(enabledEvaluator, "Enabled evaluator is required");
		Assert.notNull(lookupService, "LookupService is required");
		//
		this.context = context;
		this.publisher = publisher;
		this.enabledEvaluator = enabledEvaluator;
		this.lookupService = lookupService;
	}
	
	/**
	 * Cancel all previously ran events
	 */
	@Override
	public void init() {
		LOG.info("Cancel unprocessed events - event was interrupt during instance restart");
		//
		String instanceId = configurationService.getInstanceId();
		entityEventService.findByState(instanceId, OperationState.RUNNING).forEach(event -> {
			LOG.info("Cancel unprocessed event [{}] - event was interrupt during instance [{}] restart", event.getId(), instanceId);
			//
			// cancel event
			ResultModel resultModel = new DefaultResultModel(
					CoreResultCode.EVENT_CANCELED_BY_RESTART, 
					ImmutableMap.of(
							"eventId", event.getId(), 
							"eventType", event.getEventType(),
							"ownerId", String.valueOf(event.getOwnerId()),
							"instanceId", event.getInstanceId()));		
			OperationResultDto result = new OperationResultDto.Builder(OperationState.CANCELED).setModel(resultModel).build();
			event.setResult(result);
			entityEventService.saveInternal(event);
			//
			// cancel event states		
			IdmEntityStateFilter filter = new IdmEntityStateFilter();
			filter.setEventId(event.getId());
			entityStateService.find(filter, null)
				.getContent()
				.stream()
				.filter(state -> {
					return OperationState.RUNNING == state.getResult().getState();
				})
				.forEach(state -> {		
					event.setResult(result);
					entityStateService.save(state);
				});
		});
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <E extends Serializable> EventContext<E> process(EntityEvent<E> event) {
		Assert.notNull(event);
		Serializable content = event.getContent();
		//
		LOG.info("Publishing event [{}]", event);
		//
		// continue suspended event
		event.getContext().setSuspended(false);
		//
		// read previous (original) dto source - usable in "check modification" processors
		if (event.getOriginalSource() == null && (content instanceof AbstractDto)) { // original source could be set externally
			AbstractDto contentDto = (AbstractDto) content;
			// works only for dto modification
			if (contentDto.getId() != null && lookupService.getDtoLookup(contentDto.getClass()) != null) {
				event.setOriginalSource((E) lookupService.lookupDto(contentDto.getClass(), contentDto.getId()));
			}
		}
		//
		publisher.publishEvent(event); 
		LOG.info("Event [{}] is completed", event);
		//
		return event.getContext();
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public List<EntityEventProcessorDto> find(EntityEventProcessorFilter filter) {
		List<EntityEventProcessorDto> dtos = new ArrayList<>();
		Map<String, EntityEventProcessor> processors = context.getBeansOfType(EntityEventProcessor.class);
		for(Entry<String, EntityEventProcessor> entry : processors.entrySet()) {
			EntityEventProcessor<?> processor = entry.getValue();
			// entity event processor depends on module - we could not call any processor method
			if (!enabledEvaluator.isEnabled(processor)) {
				continue;
			}
			EntityEventProcessorDto dto = toDto(processor);
			//
			if (passFilter(dto, filter)) {
				dtos.add(dto);
			}

		}
		LOG.debug("Returning [{}] registered entity event processors", dtos.size());
		return dtos;
	}

	@Override
	public void publishEvent(Object event) {
		publisher.publishEvent(event);
	}
	
	@Override
	public <E extends Identifiable> void changedEntity(E owner) {
		changedEntity(owner, null);
	}
	
	@Override
	public <E extends Identifiable> void changedEntity(E owner, EntityEvent<? extends Identifiable> originalEvent) {
		Assert.notNull(owner);
		//
		changedEntity(owner.getClass(), getOwnerId(owner), originalEvent);
	}
	
	@Override
	public void changedEntity(Class<? extends Identifiable> ownerType, UUID ownerId) {
		changedEntity(ownerType, ownerId, null);
	}
	
	@Override
	public void changedEntity(
			Class<? extends Identifiable> ownerType, 
			UUID ownerId, 
			EntityEvent<? extends Identifiable> originalEvent) {
		IdmEntityEventDto event = createEvent(ownerType, ownerId, originalEvent);
		event.setEventType(CoreEventType.NOTIFY.name());
		//
		putToQueue(event);
	}
	
	/**
	 * Spring schedule new task after previous task ended (don't run concurrently)
	 */
	@Scheduled(fixedDelayString = "${" + SchedulerConfiguration.PROPERTY_EVENT_QUEUE_PROCESS + ":" + SchedulerConfiguration.DEFAULT_EVENT_QUEUE_PROCESS + "}")
	public void scheduleProcessCreated() {
		if (!eventConfiguration.isAsynchronous()) {
			// asynchronous processing is disabled
			// prevent to debug some messages into log - usable for devs
			return;
		}
		// run as system - called from scheduler internally
		securityService.setSystemAuthentication();
		//
		// calculate events to process
		String instanceId = configurationService.getInstanceId();
		List<IdmEntityEventDto> events = getCreatedEvents(instanceId);
		LOG.trace("Events to process [{}] on instance [{}].", events.size(), instanceId);
		for (IdmEntityEventDto event : events) {
			// @Transactional
			context.getBean(this.getClass()).executeEvent(event);;
		}
	}
	
	@Override
	public IdmEntityEventDto getEvent(EntityEvent<? extends Serializable> event) {
		Assert.notNull(event);
		//
		UUID changeId = getEventId(event);
		if (changeId == null) {
			// event doesn't contain entity change - event is not based on entity change
			return null;
		}
		return entityEventService.get(changeId);
	}
	
	@Override
	public UUID getEventId(EntityEvent<? extends Serializable> event) {
		Assert.notNull(event);
		//
		return EntityUtils.toUuid(event.getProperties().get(EVENT_PROPERTY_EVENT_ID));
	}
	
	@Override
	public String getOwnerType(Class<? extends Identifiable> ownerType) {
		Assert.notNull(ownerType);
		//
		// dto class was given
		Class<? extends AbstractEntity> ownerEntityType = getOwnerClass(ownerType);
		if (ownerEntityType == null) {
			throw new IllegalArgumentException(String.format("Owner type [%s] has to generalize [AbstractEntity]", ownerType));
		}
		return ownerEntityType.getCanonicalName();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public AbstractDto findOwner(IdmEntityEventDto change) {
		try {
			Class<?> ownerType = Class.forName(change.getOwnerType());
			if (!AbstractEntity.class.isAssignableFrom(ownerType)) {
				throw new IllegalArgumentException(String.format("Owner type [%s] has to generalize [AbstractEntity]", ownerType));
			}
			//
			return (AbstractDto) lookupService.lookupDto((Class<? extends AbstractEntity>) ownerType, change.getOwnerId());
		} catch (ClassNotFoundException ex) {
			LOG.error("Class [{}] for entity change [{}] not found, module or type was uninstalled, returning null",
					change.getOwnerType(), change.getId());
			return null;
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public AbstractDto findOwner(String ownerType, Serializable ownerId) {
		try {
			Class<?> ownerTypeClass = Class.forName(ownerType);
			if (!AbstractEntity.class.isAssignableFrom(ownerTypeClass)) {
				throw new IllegalArgumentException(String.format("Owner type [%s] has to generalize [AbstractEntity]", ownerType));
			}
			//
			return (AbstractDto) lookupService.lookupDto((Class<? extends AbstractEntity>) ownerTypeClass, ownerId);
		} catch (ClassNotFoundException ex) {
			LOG.error("Class [{}] for entity change [{}] not found, module or type was uninstalled, returning null",
					ownerType, ownerId);
			return null;
		}
	}
	
	@Override
	@Transactional
	public void executeEvent(IdmEntityEventDto event) {
		Assert.notNull(event);
		Assert.notNull(event.getOwnerId());
		if (!eventConfiguration.isAsynchronous()) {
			// synchronous processing
			// we don't persist events and their states
			process(new CoreEvent<>(EntityEventType.EXECUTE, event));
			return;
		}
		if (event.getPriority() == PriorityType.IMMEDIATE) {
			// synchronous processing
			// we don't persist events and their states
			// TODO: what about running event with the same owner? And events in queue for the same owner
			process(new CoreEvent<>(EntityEventType.EXECUTE, event));
			return;
		}
		//
		if (runningOwnerEvents.putIfAbsent(event.getOwnerId(), event.getId()) != null) {
			LOG.debug("Previous event [{}] for owner with id [{}] is currently processed.", 
					runningOwnerEvents.get(event.getOwnerId()), event.getOwnerId());
			// event will be processed in another scheduling
			return;
		}			
		// execute event in new thread asynchronously
		try {
			eventConfiguration.getExecutor().execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						process(new CoreEvent<>(EntityEventType.EXECUTE, event));
					} catch (Exception ex) {
						// exception handling only ... all processor should persist their own entity state (see AbstractEntityEventProcessor)
						ResultModel resultModel;
						if (ex instanceof ResultCodeException) {
							resultModel = ((ResultCodeException) ex).getError().getError();
						} else {
							resultModel = new DefaultResultModel(
									CoreResultCode.EVENT_EXECUTE_FAILED, 
									ImmutableMap.of(
											"eventId", event.getId(), 
											"eventType", String.valueOf(event.getEventType()),
											"ownerId", String.valueOf(event.getOwnerId()),
											"instanceId", String.valueOf(event.getInstanceId())));
						}		
						context.getBean(DefaultEntityEventManager.this.getClass()).saveResult(event.getId(), new OperationResultDto
										.Builder(OperationState.EXCEPTION)
										.setCause(ex)
										.setModel(resultModel)
										.build());
						
						LOG.error(resultModel.toString(), ex);
					} finally {
						LOG.trace("Event [{}] ends for owner with id [{}].", event.getId(), event.getOwnerId());
						runningOwnerEvents.remove(event.getOwnerId());
					}
				}
			});
			//
			LOG.trace("Running event [{}] for owner with id [{}].", event.getId(), event.getOwnerId());
		} catch (RejectedExecutionException ex) {
			// thread pool is full - wait for another try
			// TODO: Thread.wait(300) ?
			runningOwnerEvents.remove(event.getOwnerId());
		}
	}
	
	/**
	 * TODO: Will be this method useful?
	 * 
	 * @param event
	 */
	@SuppressWarnings("unused")
	private void runOnBackground(EntityEvent<? extends Identifiable> event) {
		Assert.notNull(event);
		//
		putToQueue(createEvent(event.getContent(), event));
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public IdmEntityEventDto saveResult(UUID eventId, OperationResultDto result) {
		Assert.notNull(eventId);
		Assert.notNull(result);
		IdmEntityEventDto entityEvent = entityEventService.get(eventId);
		Assert.notNull(entityEvent);
		//
		entityEvent.setResult(result);
		return entityEventService.save(entityEvent);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public <E extends Serializable> List<IdmEntityStateDto> saveStates(
			EntityEvent<E> event, 
			List<IdmEntityStateDto> previousStates,
			EventResult<E> result) {
		IdmEntityEventDto entityEvent = getEvent(event);
		List<IdmEntityStateDto> results = new ArrayList<>();
		if (entityEvent == null) {
			return results;
		}
		// simple drop - we don't need to find and update results, we'll create new ones
		if (previousStates != null && !previousStates.isEmpty()) {
			previousStates.forEach(state -> {
				entityStateService.delete(state);
			});
		}
		//
		if (result == null) {
			IdmEntityStateDto state = new IdmEntityStateDto(entityEvent);
			// default result without model
			state.setResult(new OperationResultDto
					.Builder(OperationState.EXECUTED)
					.build());
			results.add(entityStateService.save(state));
			return results;
		}
		if (result.getResults().isEmpty()) {
			results.add(entityStateService.save(createState(entityEvent, result, new OperationResultDto.Builder(OperationState.EXECUTED).build())));
			return results;
		}
		result.getResults().forEach(opeartionResult -> {
			results.add(entityStateService.save(createState(entityEvent, result, opeartionResult.toDto())));
		});
		//
		return results;
	}
	
	@Override
	public EntityEvent<Identifiable> toEvent(IdmEntityEventDto entityEvent) {
		Identifiable content = null;
		// try to use persisted event content
		// only if type and id is the same as owner can be used
		if (entityEvent.getContent() != null 
				&& entityEvent.getContent().getClass().getCanonicalName().equals(entityEvent.getOwnerType())
				&& entityEvent.getContent().getId().equals(entityEvent.getOwnerId())) {
			content = entityEvent.getContent();
		}
		if (content == null) {
			// content is not persisted - try to find actual entity
			content = findOwner(entityEvent);
		}
		if (content == null) {
			throw new EventContentDeletedException(entityEvent);
		}
		//
		Map<String, Serializable> eventProperties = entityEvent.getProperties().toMap();
		eventProperties.put(EVENT_PROPERTY_EVENT_ID, entityEvent.getId());
		eventProperties.put(EVENT_PROPERTY_PRIORITY, entityEvent.getPriority());
		eventProperties.put(EVENT_PROPERTY_EXECUTE_DATE, entityEvent.getExecuteDate());
		eventProperties.put(EVENT_PROPERTY_PARENT_EVENT_TYPE, entityEvent.getParentEventType());
		final String type = entityEvent.getEventType();
		DefaultEventContext<Identifiable> initContext = new DefaultEventContext<>();
		initContext.setProcessedOrder(entityEvent.getProcessedOrder());
		EventType eventType = (EventType) () -> type;
		EntityEvent<Identifiable> resurectedEvent = new CoreEvent<>(eventType, content, eventProperties, initContext);
		resurectedEvent.setOriginalSource(entityEvent.getOriginalSource());
		//
		return resurectedEvent;
	}
	
	/**
	 * Convert processor to dto.
	 * 
	 * @param processor
	 * @return
	 */
	private EntityEventProcessorDto toDto(EntityEventProcessor<?> processor) {
		EntityEventProcessorDto dto = new EntityEventProcessorDto();
		dto.setId(processor.getId());
		dto.setName(processor.getName());
		dto.setModule(processor.getModule());
		dto.setContentClass(processor.getEntityClass());
		dto.setEntityType(processor.getEntityClass().getSimpleName());
		dto.setEventTypes(Lists.newArrayList(processor.getEventTypes()));
		dto.setClosable(processor.isClosable());
		dto.setDisabled(processor.isDisabled());
		dto.setDisableable(processor.isDisableable());
		dto.setOrder(processor.getOrder());
		// resolve documentation
		dto.setDescription(processor.getDescription());
		dto.setConfigurationProperties(processor.getConfigurationMap());
		//
		return dto;
	}
	
	@SuppressWarnings({"unchecked", "rawtypes" })
	private void putToQueue(IdmEntityEventDto entityEvent) {
		if (entityEvent.getPriority() == PriorityType.IMMEDIATE) {
			LOG.trace("Event type [{}] for owner with id [{}] will be executed synchronously.", 
					entityEvent.getEventType(), entityEvent.getOwnerId());	
			executeEvent(entityEvent);
			return;
		}
		if (!eventConfiguration.isAsynchronous()) {
			LOG.trace("Event type [{}] for owner with id [{}] will be executed synchronously, asynchronous event processing [{}] is disabled.", 
					entityEvent.getEventType(), entityEvent.getOwnerId(), EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED);	
			executeEvent(entityEvent);
			return;
		}
		//
		// get enabled processors
		final EntityEvent<Identifiable> event = toEvent(entityEvent);
		List<EntityEventProcessor> registeredProcessors = context
			.getBeansOfType(EntityEventProcessor.class)
			.values()
			.stream()
			.filter(enabledEvaluator::isEnabled)
			.filter(processor -> !processor.isDisabled())
			.filter(processor -> processor.supports(event))
			.filter(processor -> processor.conditional(event))
			.sorted(new AnnotationAwareOrderComparator())
			.collect(Collectors.toList());
		if (registeredProcessors.isEmpty()) {
			LOG.debug("Event type [{}] for owner with id [{}] will not be executed, no enabled processor is registered.", 
					entityEvent.getEventType(), entityEvent.getOwnerId());	
			return;
		}
		//
		// evaluate event priority by registered processors
		PriorityType priority = evaluatePriority(event, registeredProcessors);
		if (priority != null && priority.getPriority() < entityEvent.getPriority().getPriority()) {
			entityEvent.setPriority(priority);
		}
		//
		// registered processors voted about event will be processed synchronously
		if (entityEvent.getPriority() == PriorityType.IMMEDIATE) {
			LOG.trace("Event type [{}] for owner with id [{}] will be executed synchronously.", 
					entityEvent.getEventType(), entityEvent.getOwnerId());	
			executeEvent(entityEvent);
			return;
		}
		//
		// notification - info about registered (asynchronous) processors
		Map<String, Object> parameters = new LinkedHashMap<>();
		parameters.put("eventType", entityEvent.getEventType());
		parameters.put("ownerId", entityEvent.getOwnerId());
		parameters.put("instanceId", entityEvent.getInstanceId());
		parameters.put("processors", registeredProcessors
				.stream()
				.map(DefaultEntityEventManager.this::toDto)
				.collect(Collectors.toList()));
		notificationManager.send(
				CoreModuleDescriptor.TOPIC_EVENT, 
				new IdmMessageDto
					.Builder()
					.setLevel(NotificationLevel.INFO)
					.setModel(new DefaultResultModel(CoreResultCode.EVENT_ACCEPTED, parameters))
					.build());
		//
		// persist event - asynchronous processing
		entityEventService.save(entityEvent);
	}
	
	/**
	 * Evaluate event priority by registered processors
	 * 
	 * @param event
	 * @param registeredProcessors
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected PriorityType evaluatePriority(EntityEvent<Identifiable> event, List<EntityEventProcessor> registeredProcessors) {
		PriorityType priority = null;
		for (EntityEventProcessor processor : registeredProcessors) {
			if (!(processor instanceof AsyncEntityEventProcessor)) {
				continue;
			}
			AsyncEntityEventProcessor asyncProcessor = (AsyncEntityEventProcessor) processor; 
			PriorityType processorPriority = asyncProcessor.getPriority(event);
			if (processorPriority == null) {
				// processor doesn't vote about priority - preserve original event priority. 
				continue;
			}
			if (priority == null || processorPriority.getPriority() < priority.getPriority()) {
				priority = processorPriority;
			}
			if (priority == PriorityType.IMMEDIATE) {
				// nothing is higher
				break;
			}
		}
		//
		return priority;
	}
	
	/**
	 * Called from scheduler - concurrency is prevented.
	 * Returns events to process sorted by priority 7 / 3 (high / normal). 
	 * Immediate priority is executed synchronously.
	 * Cancel duplicate events (same type, owner and props) - last event is returned
	 * 
	 * @param instanceId
	 * @return
	 */
	protected List<IdmEntityEventDto> getCreatedEvents(String instanceId) {
		Assert.notNull(instanceId);
		//
		// load created events - high priority
		DateTime executeDate = new DateTime();
		Page<IdmEntityEventDto> highEvents = entityEventService.findToExecute(
				instanceId,
				executeDate,
				PriorityType.HIGH,
				new PageRequest(0, 100, new Sort(Direction.ASC, Auditable.PROPERTY_CREATED)));
		// load created events - low priority
		Page<IdmEntityEventDto> normalEvents = entityEventService.findToExecute(
				instanceId,
				executeDate,
				PriorityType.NORMAL,
				new PageRequest(0, 100, new Sort(Direction.ASC, Auditable.PROPERTY_CREATED)));
		// merge events
		List<IdmEntityEventDto> events = new ArrayList<>();
		events.addAll(highEvents.getContent());
		events.addAll(normalEvents.getContent());
		// sort by created date
		events.sort(new CreatedComparator());
		//
		// cancel duplicates - by owner => properties has to be the same
		// execute the first event for each owner only - preserve events order
		Map<UUID, IdmEntityEventDto> distinctEvents = new LinkedHashMap<>();	
		events.forEach(event -> {
			if (!distinctEvents.containsKey(event.getOwnerId())) {
				// the first event
				distinctEvents.put(event.getOwnerId(), event);
			} else {
				// cancel duplicate older event 
				IdmEntityEventDto olderEvent = distinctEvents.get(event.getOwnerId());
				if (Objects.equal(olderEvent.getEventType(), event.getEventType())
						&& Objects.equal(olderEvent.getProperties(), event.getProperties())) {
					// try to set higher priority
					if (olderEvent.getPriority() == PriorityType.HIGH) {
						event.setPriority(PriorityType.HIGH);
					}
					distinctEvents.put(event.getOwnerId(), event);
					//
					LOG.debug(new DefaultResultModel(
							CoreResultCode.EVENT_DUPLICATE_CANCELED, 
							ImmutableMap.of(
									"eventId", olderEvent.getId(), 
									"eventType", String.valueOf(olderEvent.getEventType()),
									"ownerId", String.valueOf(olderEvent.getOwnerId()),
									"instanceId", String.valueOf(olderEvent.getInstanceId()),
									"neverEventId", event.getId())).toString());
					//
					if (entityEventRepository.countByParentId(olderEvent.getId()) == 0) {
						entityEventService.delete(olderEvent);
					}
				}
			}
		});
		// 
		// sort by priority
		events = distinctEvents
				.values()
				.stream()
				.sorted((o1, o2) -> {
					return Integer.compare(o1.getPriority().getPriority(), o2.getPriority().getPriority());
				})
				.collect(Collectors.toList());
		int normalCount = events.stream().filter(e -> e.getPriority() == PriorityType.NORMAL).collect(Collectors.toList()).size();
		int highMaximum = normalCount > 30 ? 70 : (100 - normalCount);
		// evaluate priority => high 70 / low 30
		int highCounter = 0;
		List<IdmEntityEventDto> prioritizedEvents = new ArrayList<>();
		for (IdmEntityEventDto event : events) {
			if (event.getPriority() == PriorityType.HIGH) {
				if (highCounter < highMaximum) {
					prioritizedEvents.add(event);
					highCounter++;
				}
			} else {
				// normal priority remains only
				if (prioritizedEvents.size() >= 100) {
					break;
				}
				prioritizedEvents.add(event);
			}
		}
		//
		return prioritizedEvents;
	}
	
	private <E extends Serializable> IdmEntityStateDto createState(
			IdmEntityEventDto entityEvent, 
			EventResult<E> eventResult, 
			OperationResultDto operationResult) {
		IdmEntityStateDto state = new IdmEntityStateDto(entityEvent);
		//
		state.setClosed(eventResult.isClosed());
		state.setSuspended(eventResult.isSuspended());
		state.setProcessedOrder(eventResult.getProcessedOrder());
		state.setProcessorId(eventResult.getProcessor().getId());
		state.setProcessorModule(eventResult.getProcessor().getModule());
		state.setProcessorName(eventResult.getProcessor().getName());
		state.setResult(operationResult);
		//
		return state;
	}
	
	/**
	 * Returns true, when given processor pass given filter
	 * 
	 * @param processor
	 * @param filter
	 * @return
	 */
	private boolean passFilter(EntityEventProcessorDto processor, EntityEventProcessorFilter filter) {
		if (filter == null) {
			// empty filter
			return true;
		}
		// id - not supported
		if (filter.getId() != null) {
			throw new UnsupportedOperationException("Filtering event processors by [id] is not supported.");
		}
		// text - lowercase like in name, description, content class - canonical name
		if (StringUtils.isNotEmpty(filter.getText())) {
			if (!processor.getName().toLowerCase().contains(filter.getText().toLowerCase())
					&& (processor.getDescription() == null || !processor.getDescription().toLowerCase().contains(filter.getText().toLowerCase()))
					&& !processor.getContentClass().getCanonicalName().toLowerCase().contains(filter.getText().toLowerCase())) {
				return false;
			}
		}
		// processors name
		if (StringUtils.isNotEmpty(filter.getName()) && !processor.getName().equals(filter.getName())) {
			return false; 
		}
		// content ~ entity type - dto type
		if (filter.getContentClass() != null && !filter.getContentClass().isAssignableFrom(processor.getContentClass())) {
			return false;
		}
		// module id
		if (StringUtils.isNotEmpty(filter.getModule()) && !filter.getModule().equals(processor.getModule())) {
			return false;
		}
		// description - like
		if (StringUtils.isNotEmpty(filter.getDescription()) 
				&& StringUtils.isNotEmpty(processor.getDescription()) 
				&& !processor.getDescription().contains(filter.getDescription())) {
			return false;
		}
		// entity ~ content type - simple name
		if (StringUtils.isNotEmpty(filter.getEntityType()) && !processor.getEntityType().equals(filter.getEntityType())) {
			return false;
		}
		// event types
		if (!filter.getEventTypes().isEmpty() && !processor.getEventTypes().containsAll(filter.getEventTypes())) {
			return false;
		}
		//
		return true;
	}
	
	/**
	 * Creates entity event
	 * 
	 * @param identifiable
	 * @param originalEvent
	 * @return
	 */
	protected IdmEntityEventDto createEvent(Identifiable identifiable, EntityEvent<? extends Identifiable> originalEvent) {
		Assert.notNull(identifiable);
		Assert.notNull(identifiable.getId(), "Change can be published after entity id is assigned at least.");
		//
		return createEvent(identifiable.getClass(), getOwnerId(identifiable), originalEvent);
	}
	
	private IdmEntityEventDto createEvent(Class<? extends Identifiable> ownerType, UUID ownerId, EntityEvent<? extends Identifiable> originalEvent) {
		Assert.notNull(ownerType);
		Assert.notNull(ownerId, "Change can be published after entity id is assigned at least.");
		//
		IdmEntityEventDto savedEvent = new IdmEntityEventDto();
		savedEvent.setOwnerId(ownerId);
		savedEvent.setOwnerType(getOwnerType(ownerType));
		savedEvent.setResult(new OperationResultDto.Builder(OperationState.CREATED).build());
		savedEvent.setInstanceId(eventConfiguration.getAsynchronousInstanceId());
		//
		if (originalEvent != null) {
			savedEvent.setEventType(originalEvent.getType().name());
			savedEvent.getProperties().putAll(originalEvent.getProperties());
			savedEvent.setParent(EntityUtils.toUuid(originalEvent.getProperties().get(EVENT_PROPERTY_EVENT_ID)));
			savedEvent.setExecuteDate((DateTime) originalEvent.getProperties().get(EVENT_PROPERTY_EXECUTE_DATE));
			savedEvent.setPriority((PriorityType) originalEvent.getProperties().get(EVENT_PROPERTY_PRIORITY));
			savedEvent.setParentEventType(originalEvent.getType().name());
			savedEvent.setContent(originalEvent.getContent());
			savedEvent.setOriginalSource(originalEvent.getOriginalSource());
			savedEvent.setClosed(originalEvent.isClosed());
			if (savedEvent.isClosed()) {
				savedEvent.setResult(new OperationResultDto
						.Builder(OperationState.EXECUTED)
						.setModel(new DefaultResultModel(CoreResultCode.EVENT_ALREADY_CLOSED))
						.build());
			}
			savedEvent.setSuspended(originalEvent.isSuspended());
		} else {
			// notify as default event type
			savedEvent.setEventType(CoreEventType.NOTIFY.name());
		}
		if (savedEvent.getPriority() == null) {
			savedEvent.setPriority(PriorityType.NORMAL);
		}
		//
		return savedEvent;
	}
	
	/**
	 * UUID identifier from given owner.
	 * 
	 * @param owner
	 * @return
	 */
	private UUID getOwnerId(Identifiable owner) {
		Assert.notNull(owner);
		if (owner.getId() == null) {
			return null;
		}		
		Assert.isInstanceOf(UUID.class, owner.getId(), "Entity with UUID identifier is supported as owner for entity changes.");
		//
		return (UUID) owner.getId();
	}
	
	/**
	 * Returns {@link AbstractEntity}. Owner type has to be entity class - dto class can be given.
	 * 
	 * @param ownerType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Class<? extends AbstractEntity> getOwnerClass(Class<? extends Identifiable> ownerType) {
		Assert.notNull(ownerType, "Owner type is required!");
		// formable entity class was given
		if (AbstractEntity.class.isAssignableFrom(ownerType)) {
			return (Class<? extends AbstractEntity>) ownerType;
		}
		// dto class was given
		Class<?> ownerEntityType = lookupService.getEntityClass(ownerType);
		if (AbstractEntity.class.isAssignableFrom(ownerEntityType)) {
			return (Class<? extends AbstractEntity>) ownerEntityType;
		}
		return null;
	}
}
