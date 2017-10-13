package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Single entity event processor
 * <p>
 * Types could be {@literal null}, then processor supports all event types
 * <p>
 * TODO: move @Autowire to @Configuration bean post processor
 * 
 * @param <E> {@link BaseEntity}, {@link BaseDto} or any other {@link Serializable} content type
 * @author Radek Tomiška
 */
public abstract class AbstractEntityEventProcessor<E extends Serializable> 
		implements EntityEventProcessor<E>, ApplicationListener<AbstractEntityEvent<E>> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractEntityEventProcessor.class);
	public static final String PROPERTY_EVENT_TYPES = "eventTypes";
	private final Class<E> entityClass;
	private final Set<String> types = new HashSet<>();
	
	@Autowired(required = false)
	private EnabledEvaluator enabledEvaluator; // optional internal dependency - checks for module is enabled
	
	@Autowired(required = false)
	private ConfigurationService configurationService; // optional internal dependency - checks for processor is enabled
	
	@SuppressWarnings({"unchecked"})
	public AbstractEntityEventProcessor(EventType... types) {
		this.entityClass = (Class<E>)GenericTypeResolver.resolveTypeArgument(getClass(), EntityEventProcessor.class);
		if (types != null) {
			for(EventType type : types) {
				this.types.add(type.name());
			}
		}
	}
	
	public AbstractEntityEventProcessor(EnabledEvaluator enabledEvaluator, ConfigurationService configurationService, EventType... types) {
		this(types);
		this.enabledEvaluator = enabledEvaluator;
		this.configurationService = configurationService;
	}
	
	@Override
	public Class<E> getEntityClass() {
		return entityClass;
	}

	@Override
	public String[] getEventTypes() {
		final Set<String> configTypes = getEventTypesFromConfiguration();
		// Default event types can be overwritten using config property
		final Set<String> eventTypesToUse = configTypes == null ? types : configTypes;
		//
		return eventTypesToUse.toArray(new String[eventTypesToUse.size()]);
	}

	/**
	 * Method returns {@link Collection} of event types for this processor.
	 *
	 * @return Collection of event types configured in app config. Null if configurationService is not defined, or if
	 * config property is not defined.
	 */
	private Set<String> getEventTypesFromConfiguration() {
		if (getConfigurationService() == null) {
			return null;
		}
		//
		final String configValue = getConfigurationService().getValue(
			getConfigurationPrefix()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ PROPERTY_EVENT_TYPES);
		//
		return configValue == null ? null : Arrays.stream(configValue.split(ConfigurationService.PROPERTY_MULTIVALUED_SEPARATOR))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.collect(Collectors.toSet());
	}

	@Override
	public boolean supports(EntityEvent<?> entityEvent) {
		Assert.notNull(entityEvent);
		Assert.notNull(entityEvent.getContent(), "Entity event does not contain content, content is required!");
		//
		final List<String> supportedEventTypes = Arrays.asList(getEventTypes());
		return entityClass.isAssignableFrom(entityEvent.getContent().getClass())
				&& (supportedEventTypes.isEmpty() || supportedEventTypes.contains(entityEvent.getType().name()));
	}
	
	/* 
	 * (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(java.lang.Object)
	 */
	@Override
	public void onApplicationEvent(AbstractEntityEvent<E> event) {
		if (!supports(event)) {
			// event is not supported with this processor
			// its on the start to prevent debug logging
			LOG.trace("Skipping processor [{}] for [{}]. Processor don't support given event. ", getName(), event);
			return;
		}
		// check for module is enabled, if evaluator is given
		if (enabledEvaluator != null && !enabledEvaluator.isEnabled(this.getClass())) {
			LOG.debug("Skipping processor [{}] for [{}]. Module [{}] is disabled. ", getName(), event, getModule());
			return;
		}
		// check for processor is enabled
		if (isDisabled()) {
			LOG.debug("Skipping processor [{}] for [{}]. Module [{}] is disabled.", getName(), event, getModule());
			return;
		}
		if (event.isClosed()) {	
			// event is completely processed 
			LOG.debug("Skipping processor [{}]. [{}] is completely processed.", getName(), event);
			return;
		}
		if (event.isSuspended()) {	
			// event is suspended
			LOG.debug("Skipping processor [{}]. [{}] is suspended.", getName(), event);
			return;
		}
		//
		EventContext<E> context = event.getContext();
		//
		Integer processedOrder = context.getProcessedOrder();
		if (processedOrder != null) {	
			// event was processed with this processor
			if (processedOrder > this.getOrder()) {
				LOG.debug("Skipping processor [{}]. [{}] was already processed by this processor with order [{}].", getName(), event, getOrder());
				return;
			}
			// the same order - only different processor instance can process event
			if (processedOrder == this.getOrder()) {
				if (context.getResults().isEmpty()) {
					// if event was started in the middle manually => results are empty, event could continue with processors with higher order only
					LOG.debug("Skipping processor [{}]. Processed context for [{}] is empty. Processor's order [{}] is the same as event start.", getName(), event, getOrder());
					return;
				}
				for(EventResult<E> result : Lists.reverse(context.getResults())) {
					if (result.getProcessedOrder() != this.getOrder()) {
						// only same order is interesting
						break;
					}
					EntityEventProcessor<E> resultProcessor = result.getProcessor();
					if (resultProcessor != null && resultProcessor.equals(this)) {
						// event was already processed by this processor
						LOG.debug("Skipping processor [{}]. [{}] was already processed by this processor with order [{}].", getName(), event, getOrder());
						return;
					}	
				}
			}
		}
		LOG.info("Processor [{}] start for [{}] with order [{}].", getName(), event, getOrder());
		// prepare order ... in processing
		context.setProcessedOrder(this.getOrder());
		// process event
		EventResult<E> result = process(event);
		// add result to history
		context.addResult(result);
		//
		LOG.info("Processor [{}] end for [{}] with order [{}].", getName(), event, getOrder());
	}
	
	@Override
	public boolean isClosable() {
		return false;
	}
	
	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
	
	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}
}
