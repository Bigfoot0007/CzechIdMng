package eu.bcvsolutions.idm.notification;

import static org.junit.Assert.assertEquals;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.notification.entity.IdmMessage;
import eu.bcvsolutions.idm.notification.repository.IdmConsoleLogRepository;
import eu.bcvsolutions.idm.notification.repository.IdmEmailLogRepository;
import eu.bcvsolutions.idm.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.notification.service.api.EmailService;
import eu.bcvsolutions.idm.notification.service.api.NotificationService;
import eu.bcvsolutions.idm.security.service.impl.DefaultSecurityService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for {@link DefaultSecurityService}
 * 
 * @author Radek Tomiška 
 *
 */
public class DefaultNotificationServiceTest extends AbstractIntegrationTest {

	@Autowired
	private NotificationService notificationService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private IdmNotificationLogRepository idmNotificationRepository;
	
	@Autowired
	private IdmIdentityRepository identityRepository;
	
	@Autowired
	private IdmEmailLogRepository emailLogRepository;	
	
	@Autowired
	private IdmConsoleLogRepository consoleLogRepository;
	
	@Before
	public void clear() {
		loginAsAdmin("admin");
		emailLogRepository.deleteAll();
		consoleLogRepository.deleteAll();
		idmNotificationRepository.deleteAll();
	}
	
	@After
	@Override
	public void logout() {
		super.logout();
	}
	
	@Test
	@Transactional
	public void testSendSimple() {
		assertEquals(0, idmNotificationRepository.count());
		
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		notificationService.send(new IdmMessage("subject", "Idm notification"),  identity);
		
		assertEquals(1, idmNotificationRepository.count());
		assertEquals(1, emailLogRepository.count());
	}
	
	@Test
	public void testFilterByDate() {
		assertEquals(0, idmNotificationRepository.count());
		
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		
		DateTime start = new DateTime();
		notificationService.send(new IdmMessage("subject", "Idm notification"),  identity);		
		notificationService.send(new IdmMessage("subject2", "Idm notification2"),  identity);	
		
		NotificationFilter filter = new NotificationFilter();
		assertEquals(2, idmNotificationRepository.find(filter, null).getTotalElements());
		
		filter.setFrom(start);
		assertEquals(2, idmNotificationRepository.find(filter, null).getTotalElements());
		
		filter.setFrom(null);
		filter.setTill(start);
		assertEquals(0, idmNotificationRepository.find(filter, null).getTotalElements());
	}
	
	@Test
	@Transactional
	public void testEmailFilterBySender() {
		NotificationFilter filter = new NotificationFilter();
		
		filter.setSender(InitTestData.TEST_USER_2);
		assertEquals(0, emailLogRepository.find(filter, null).getTotalElements());
		filter.setSender(InitTestData.TEST_USER_1);
		assertEquals(0, emailLogRepository.find(filter, null).getTotalElements());
		
		// send some email
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		IdmIdentity identity2 = identityRepository.findOneByUsername(InitTestData.TEST_USER_2);
		emailService.send(new IdmMessage("subject", "Idm notification"),  identity);
		
		filter.setSender(null);
		assertEquals(1, emailLogRepository.find(filter, null).getTotalElements());
		filter.setSender(identity2.getUsername());
		assertEquals(0, emailLogRepository.find(filter, null).getTotalElements());
		filter.setSender(null);
		filter.setRecipient(identity.getUsername());
		assertEquals(1, emailLogRepository.find(filter, null).getTotalElements());
	}
	
	@Test
	@Transactional
	public void testEmailFilterBySent() {
		IdmIdentity identity = identityRepository.findOneByUsername(InitTestData.TEST_USER_1);
		NotificationFilter filter = new NotificationFilter();
		
		emailService.send(new IdmMessage("subject", "Idm notification"),  identity);
		filter.setSent(true);
		assertEquals(0, emailLogRepository.find(filter, null).getTotalElements());
		
		emailService.send(new IdmMessage("subject2", "Idm notification2"),  identity);
		filter.setSent(false);
		assertEquals(2, emailLogRepository.find(filter, null).getTotalElements());
	}
	
	
}
