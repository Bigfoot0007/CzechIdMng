package eu.bcvsolutions.idm;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.configuration.service.ConfigurationService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityWorkingPosition;
import eu.bcvsolutions.idm.core.model.entity.IdmOrganization;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityWorkingPositionRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmOrganizationRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.security.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.security.service.SecurityService;

/**
 * Initialize demo data for application
 * 
 * @author Radek Tomiška <radek.tomiska@bcvsolutions.eu>
 *
 */
@Component
@Profile("test")
@DependsOn("initApplicationData")
public class InitTestData implements ApplicationListener<ContextRefreshedEvent> {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InitTestData.class);
	private static final String PARAMETER_TEST_DATA_CREATED = "idm.sec.core.test.data";
	
	public static final String HAL_CONTENT_TYPE = "application/hal+json";
	
	public static final String TEST_ADMIN_USERNAME = InitApplicationData.ADMIN_USERNAME;
	public static final String TEST_ADMIN_PASSWORD = InitApplicationData.ADMIN_PASSWORD;
	public static final String TEST_USER_1 = "testUser1";
	public static final String TEST_USER_2 = "testUser2";

	@Autowired
	private InitApplicationData initApplicationData;
	
	@Autowired
	private IdmIdentityRepository identityRepository;

	@Autowired
	private IdmRoleRepository roleRepository;

	@Autowired
	private IdmOrganizationRepository organizationRepository;

	@Autowired
	private IdmIdentityWorkingPositionRepository identityWorkingPositionRepository;

	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private ConfigurationService configurationService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		init();
	}
	
	protected void init() {
		// we need to be ensured admin and and admin role exists.
		initApplicationData.init();
		//
		// TODO: runAs
		SecurityContextHolder.getContext().setAuthentication(
				new IdmJwtAuthentication("[SYSTEM]", null, securityService.getAvailableAuthorities()));
		try {
			// IdmRole superAdminRole = this.roleRepository.findOneByName(InitApplicationData.ADMIN_ROLE);
			// IdmIdentity identityAdmin = this.identityRepository.findOneByUsername(InitApplicationData.ADMIN_USERNAME);
			IdmOrganization rootOrganization = organizationRepository.findOneByParentIsNull();
			//
			if (!configurationService.getBooleanValue(PARAMETER_TEST_DATA_CREATED, false)) {
				log.info("Creating test data ...");				
				//
				// TODO: split test and demo data - use flyway?
				// Users for JUnit testing
				IdmIdentity testUser1 = new IdmIdentity();
				testUser1.setUsername(TEST_USER_1);
				testUser1.setPassword("heslo".getBytes());
				testUser1.setFirstName("Test");
				testUser1.setLastName("First User");
				testUser1.setEmail("test1@bscsolutions.eu");
				testUser1 = this.identityRepository.save(testUser1);
				log.info(MessageFormat.format("Identity created [id: {0}]", testUser1.getId()));
				this.identityRepository.save(testUser1);
				

				IdmIdentity testUser2 = new IdmIdentity();
				testUser2.setUsername(TEST_USER_2);
				testUser2.setPassword("heslo".getBytes());
				testUser2.setFirstName("Test");
				testUser2.setLastName("Second User");
				testUser2.setEmail("test2@bscsolutions.eu");
				testUser2 = this.identityRepository.save(testUser2);
				log.info(MessageFormat.format("Identity created [id: {0}]", testUser2.getId()));
				this.identityRepository.save(testUser2);
			
				IdmOrganization organization = new IdmOrganization();
				organization.setName("Organization Test");
				organization.setCreator("ja");
				organization.setParent(rootOrganization);
				this.organizationRepository.save(organization);
				
				IdmIdentityWorkingPosition identityWorkingPosition2 = new IdmIdentityWorkingPosition();
				identityWorkingPosition2.setIdentity(testUser1);
				identityWorkingPosition2.setPosition("vedoucí");
				identityWorkingPosition2.setManager(testUser2);
				identityWorkingPosition2.setOrganization(organization);
				identityWorkingPositionRepository.save(identityWorkingPosition2);
				//
				log.info("Test data was created.");
				//
				configurationService.setBooleanValue(PARAMETER_TEST_DATA_CREATED, true);
			}
			//
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

}
