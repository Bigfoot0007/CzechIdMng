package eu.bcvsolutions.idm.core.config;

import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultEntityEventManager;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.impl.DefaultLongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.service.impl.DefaultAuthorizationManager;

/**
 * Overridable core services initialization
 * 
 * TODO: move all @Service annotated bean here
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Configuration
public class IdmServiceConfiguration {
	
	//
	// Environment
	@Autowired
	private ApplicationContext context;
	@Autowired
	private ApplicationEventPublisher publisher;
	//
	// Own beans - TODO: move to @Bean init here
	@Autowired
	private EnabledEvaluator enabledEvaluator;
	
	/**
	 * Event manager for entity event publishing.
	 * 
	 * @param context
	 * @param publisher
	 * @param enabledEvaluator
	 * @return
	 */
	@Bean
	public EntityEventManager entityEventManager() {
		return new DefaultEntityEventManager(context, publisher, enabledEvaluator);
	}
	
	/**
	 * Automatic role service
	 * 
	 * @param repository
	 * @return
	 */
	@Bean
	public IdmRoleTreeNodeService roleTreeNodeService(IdmRoleTreeNodeRepository repository,
			IdmTreeNodeRepository treeNodeRepository, IdmRoleRequestService roleRequestService,
			IdmConceptRoleRequestService conceptRoleRequestService) {
		return new DefaultIdmRoleTreeNodeService(repository, treeNodeRepository, entityEventManager(), roleRequestService, conceptRoleRequestService);
	}
	
	/**
	 * Long running task manager
	 * 
	 * @param service
	 * @param executor
	 * @param configurationService
	 * @param securityService
	 * @return
	 */
	@Bean
	public LongRunningTaskManager longRunningTaskManager(
			IdmLongRunningTaskService service,
			Executor executor,
			ConfigurationService configurationService,
			SecurityService securityService) {
		return new DefaultLongRunningTaskManager(service, executor, configurationService, securityService);
	}
	
	/**
	 * Service for assigning authorization evaluators to roles.
	 * 
	 * @param repository
	 * @return
	 */
	@Bean
	public IdmAuthorizationPolicyService authorizationPolicyService(IdmAuthorizationPolicyRepository repository) {
		return new DefaultIdmAuthorizationPolicyService(repository);
	}
	
	/**
	 * Authorization manager
	 * 
	 * @param service
	 * @param evaluators
	 * @return
	 */
	@Bean
	public AuthorizationManager authorizationManager(IdmAuthorizationPolicyRepository repository, SecurityService securityService) {
		return new DefaultAuthorizationManager(context, authorizationPolicyService(repository), securityService);
	}
}
