package eu.bcvsolutions.idm.core.model.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.UUID;

import javax.validation.ConstraintViolationException;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.InitDemoData;
import eu.bcvsolutions.idm.InitTestData;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmContractGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.service.IdmProfileService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.contract.IdentityContractEnableProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.contract.IdentityContractEndProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic identity service operations
 * 
 * @author Radek Tomiška
 *
 */
@Transactional
public class DefaultIdmIdentityServiceIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ApplicationContext context;
	@Autowired private FormService formService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmContractGuaranteeService contractGuaranteeService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmRoleGuaranteeService roleGuaranteeService;
	@Autowired private IdmPasswordService passwordService;
	@Autowired private IdmConceptRoleRequestService conceptRequestService;
	@Autowired private IdmProfileService profileService;
	@Autowired private TokenManager tokenManager;
	//
	private DefaultIdmIdentityService identityService;

	@Before
	public void init() {
		identityService = context.getAutowireCapableBeanFactory().createBean(DefaultIdmIdentityService.class);
	}

	@Test
	public void testReferentialIntegrity() {
		IdmIdentityDto identity = getHelper().createIdentity();
		String username = identity.getUsername();
		// eav
		IdmFormDefinitionDto formDefinition = formService.getDefinition(IdmIdentity.class);
		IdmFormValueDto value1 = new IdmFormValueDto(formDefinition.getMappedAttributeByCode(InitDemoData.FORM_ATTRIBUTE_PASSWORD));
		value1.setValue("one");
		formService.saveValues(identity.getId(), IdmIdentity.class, formDefinition, Lists.newArrayList(value1));
		// role with guarantee
		IdmRoleDto role = getHelper().createRole();
		getHelper().createRoleGuarantee(role, identity);
		// contract
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity);
		// contract guarantee
		IdmIdentityContractDto contract2 = getHelper().createIdentityContact(identityService.getByUsername(InitTestData.TEST_USER_1));
		
		contractGuaranteeService.save(new IdmContractGuaranteeDto(contract2.getId(), identity.getId()));
		// assigned role
		getHelper().createIdentityRole(contract, role);
		IdmIdentityRoleFilter identityRolefilter = new IdmIdentityRoleFilter();
		identityRolefilter.setIdentityId(identity.getId());
		// profile
		getHelper().createProfile(identity);
		// token
		IdmTokenDto token = new IdmTokenDto();
		token.setToken("token");
		token.setTokenType("test");
		token = tokenManager.saveToken(identity, token);
		//
		assertNotNull(tokenManager.getToken(token.getId()));
		assertNotNull(profileService.findOneByIdentity(identity.getId()));
		assertNotNull(identityService.getByUsername(username));
		assertNotNull(passwordService.findOneByIdentity(identity.getId()));
		assertEquals(1, formService.getValues(identity).size());
		assertEquals(identity.getId(), roleGuaranteeService.findByRole(role.getId(), null).getContent().get(0).getGuarantee());
		assertEquals(1, identityRoleService.find(identityRolefilter, null).getTotalElements());
		assertEquals(2, identityContractService.findAllByIdentity(identity.getId()).size()); // + default contract is created
		IdmContractGuaranteeFilter filter = new IdmContractGuaranteeFilter();
		filter.setIdentityContractId(contract2.getId());
		List<IdmContractGuaranteeDto> guarantees = contractGuaranteeService.find(filter, null).getContent();
		assertEquals(1, guarantees.size());
		assertEquals(identity.getId(), guarantees.get(0).getGuarantee());
		//
		identityService.delete(identity);
		role = roleService.get(role.getId());
		//
		assertEquals(0L, roleGuaranteeService.findByRole(role.getId(), null).getTotalElements());
		assertNull(identityService.getByUsername(username));
		assertNull(passwordService.findOneByIdentity(identity.getId()));
		assertEquals(0, identityContractService.findAllByIdentity(identity.getId()).size());
		assertEquals(0, identityRoleService.find(identityRolefilter, null).getTotalElements());
		assertEquals(0, contractGuaranteeService.find(filter, null).getTotalElements());
		assertNull(profileService.findOneByIdentity(identity.getId()));
		assertNull(tokenManager.getToken(token.getId()));
	}
	
	@Test
	public void testReferentialRoleRequestIntegrity() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		String username = identity.getUsername();
		
		// role with guarantee
		IdmRoleDto role = getHelper().createRole();
		// assigned role
		IdmRoleRequestDto request = getHelper().assignRoles(getHelper().getPrimeContract(identity.getId()), false, role);
		IdmConceptRoleRequestFilter conceptFilter = new IdmConceptRoleRequestFilter();
		conceptFilter.setRoleRequestId(request.getId());
		
		IdmIdentityRoleFilter identityRolefilter = new IdmIdentityRoleFilter();
		identityRolefilter.setIdentityId(identity.getId());

		assertNotNull(identityService.getByUsername(username));
		assertEquals(1, identityRoleService.find(identityRolefilter, null).getTotalElements());
		assertEquals(1, conceptRequestService.find(conceptFilter, null).getTotalElements());
		
		IdmConceptRoleRequestDto concept = conceptRequestService.find(conceptFilter, null).getContent().get(0);
		concept.setWfProcessId("test_wf_" + System.currentTimeMillis());
		conceptRequestService.save(concept);
		//
		identityService.delete(identity);
		role = roleService.get(role.getId());
		//
		assertNull(identityService.getByUsername(username));
		assertNull(passwordService.findOneByIdentity(identity.getId()));
		assertEquals(0, identityContractService.findAllByIdentity(identity.getId()).size());
		assertEquals(0, identityRoleService.find(identityRolefilter, null).getTotalElements());
		assertEquals(0, conceptRequestService.find(conceptFilter, null).getTotalElements());
		roleService.delete(role);
		assertNull(roleService.get(role.getId()));
	}

	/**
	 * When identity is created, then default contract have to be created too.
	 */
	@Test
	public void testCreateDefaultContract() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "contract_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setPassword(new GuardedString("heslo")); // confidential storage
		identity.setFirstName("Test");
		identity.setLastName("Identity");
		identity = identityService.save(identity);
		//
		List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
		assertEquals(1, contracts.size());
		//
		IdmIdentityContractDto defaultContract = identityContractService.prepareMainContract(identity.getId());
		assertEquals(defaultContract.getIdentity(), contracts.get(0).getIdentity());
		assertEquals(defaultContract.getPosition(), contracts.get(0).getPosition());
		assertEquals(defaultContract.getWorkPosition(), contracts.get(0).getWorkPosition());
	}
	
	/**
	 * When identity is created with the event property
	 * IdmIdentityContractService.SKIP_CREATION_OF_DEFAULT_POSITION = TRUE, then
	 * default contract haven't to be created.
	 */
	@Test
	public void testSkipDefaultContract() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "contract_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setPassword(new GuardedString("heslo")); // confidential storage
		identity.setFirstName("Test");
		identity.setLastName("Identity");

		EntityEvent<IdmIdentityDto> event = new IdentityEvent(IdentityEventType.CREATE, identity, ImmutableMap.of(
				// In the identity sync are creation of the default contract skipped.
				IdmIdentityContractService.SKIP_CREATION_OF_DEFAULT_POSITION, Boolean.TRUE));

		identity = identityService.publish(event).getContent();
		//
		List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
		assertEquals(0, contracts.size());
	}
	
	@Test(expected = ConstraintViolationException.class)
	public void testIdentityJSR303Validations() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "validation_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity.setLastName("Identity");
		identity.setEmail("email_wrong");
		identity = identityService.save(identity);
	}
	
	@Test
	public void testSaveIdentityWithoutLastname() {
		IdmIdentityDto identity = new IdmIdentityDto();
		String username = "validation_test_" + System.currentTimeMillis();
		identity.setUsername(username);
		identity = identityService.save(identity);
		//
		Assert.assertNotNull(identity.getId());
	}
	
	@Test
	public void testModifiedAfterUpdateIdentity() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		Assert.assertNull(identity.getModified());
		//
		identity.setDescription("update");
		identity = identityService.save(identity);
		Assert.assertNotNull(identity.getModified());
	}
	
	@Test
	public void testDisableAndEnableIdentity() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		Assert.assertFalse(identity.isDisabled());
		Assert.assertEquals(IdentityState.CREATED, identity.getState());
		identity = identityService.get(identity.getId());
		// default contract is aplied
		Assert.assertEquals(IdentityState.VALID, identity.getState());
		//
		identity = identityService.disable(identity.getId());
		//
		Assert.assertTrue( identity.isDisabled());
		Assert.assertEquals(IdentityState.DISABLED_MANUALLY, identity.getState());
		//
		identity = identityService.enable(identity.getId());
		Assert.assertFalse(identity.isDisabled());
		Assert.assertEquals(IdentityState.VALID, identity.getState());
	}
	
	@Test
	public void testEnableIdentityByContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		contract.setValidFrom(new LocalDate().plusDays(1));
		identityContractService.save(contract);
		identity = identityService.get(identity.getId());
		Assert.assertTrue(identity.isDisabled());
		Assert.assertEquals(IdentityState.FUTURE_CONTRACT, identity.getState());
		//
		contract.setValidFrom(new LocalDate());
		identityContractService.save(contract);
		//
		identity = identityService.get(identity.getId());
		Assert.assertFalse(identity.isDisabled());
		Assert.assertEquals(IdentityState.VALID, identity.getState());
	}
	
	@Test
	public void testIdentityFutureContract() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		identityContractService.delete(contract);
		identity = identityService.get(identity.getId());
		Assert.assertTrue(identity.isDisabled());
		Assert.assertEquals(IdentityState.NO_CONTRACT, identity.getState());
		//
		contract = getHelper().createIdentityContact(identity, null, new LocalDate().plusDays(1), null);
		//
		identity = identityService.get(identity.getId());
		Assert.assertTrue(identity.isDisabled());
		Assert.assertEquals(IdentityState.FUTURE_CONTRACT, identity.getState());
	}
	
	@Test
	public void testFindByRole() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(identity, role);
		getHelper().createIdentityRole(identity, role);
		getHelper().createIdentityRole(identity, role, new LocalDate().minusDays(1), new LocalDate().plusDays(1));
		IdmIdentityContractDto contract = getHelper().createIdentityContact(identity);
		getHelper().createIdentityRole(contract, role);
		getHelper().createIdentityRole(contract, role);
		getHelper().createIdentityRole(contract, role, new LocalDate().minusDays(1), new LocalDate().plusDays(1));
		//
		List<IdmIdentityDto> identities = identityService.findAllByRole(role.getId());
		//
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(identity.getId(), identities.get(0).getId());
		//
		identities = identityService.findValidByRole(role.getId());
		//
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(identity.getId(), identities.get(0).getId());
	}
	
	@Test
	public void testFindValidByRole() {
		IdmIdentityDto validIdentity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(validIdentity, role, new LocalDate().plusDays(1), null);
		IdmIdentityContractDto contract = getHelper().createIdentityContact(validIdentity, null, new LocalDate().minusDays(1), null);
		getHelper().createIdentityRole(contract, role);
		getHelper().createIdentityRole(contract, role);
		getHelper().createIdentityRole(contract, role, new LocalDate().minusDays(1), new LocalDate().plusDays(1));
		//
		// disabled identity
		IdmIdentityDto identityDisabled = getHelper().createIdentity((GuardedString) null);
		identityDisabled.setState(IdentityState.DISABLED);
		identityDisabled = identityService.save(identityDisabled);
		getHelper().createIdentityRole(identityDisabled, role);
		//
		// left identity
		IdmIdentityDto identityLeft = getHelper().createIdentity((GuardedString) null);
		identityLeft.setState(IdentityState.LEFT);
		identityLeft = identityService.save(identityLeft);
		getHelper().createIdentityRole(identityLeft, role);
		//
		// disabled contract
		IdmIdentityDto identityDisabledContract = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto disabledContract = getHelper().getPrimeContract(identityDisabledContract.getId());
		disabledContract.setState(ContractState.DISABLED);
		identityContractService.save(disabledContract);
		getHelper().createIdentityRole(identityDisabledContract, role);
		//
		// expired contract
		IdmIdentityDto identityInvalidContract = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto invalidContract = getHelper().getPrimeContract(identityInvalidContract.getId());
		invalidContract.setValidFrom(new LocalDate().plusDays(1));
		identityContractService.save(invalidContract);
		getHelper().createIdentityRole(identityInvalidContract, role);
		//
		// excluded contract
		IdmIdentityDto identityExcludedContract = getHelper().createIdentity((GuardedString) null);
		IdmIdentityContractDto excludedContract = getHelper().getPrimeContract(identityExcludedContract.getId());
		excludedContract.setState(ContractState.EXCLUDED);
		identityContractService.save(excludedContract);
		getHelper().createIdentityRole(identityExcludedContract, role);
		//
		List<IdmIdentityDto> identities = identityService.findValidByRole(role.getId());
		//
		Assert.assertEquals(1, identities.size());
		Assert.assertEquals(validIdentity.getId(), identities.get(0).getId());
	}
	
	@Test
	public void testFindIdsWithPageRequest() {
		// just for sure some two identity exists
		getHelper().createIdentity((GuardedString) null);
		getHelper().createIdentity((GuardedString) null);
		//
		UUID firstIdentity = identityService.findIds(null, new PageRequest(0, 1)).getContent().get(0);
		UUID secondIdentity = identityService.findIds(null, new PageRequest(1, 1)).getContent().get(0);
		//
		Assert.assertNotEquals(firstIdentity, secondIdentity);
		Assert.assertTrue(identityService.findIds(null, new PageRequest(1, 1)).getTotalElements() > 1);
		//
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setId(firstIdentity);
		//
		Page<UUID> findIds = identityService.findIds(filter, new PageRequest(0, 1));
		//
		Assert.assertEquals(1, findIds.getTotalElements());
		Assert.assertEquals(firstIdentity, findIds.getContent().get(0));
	}
	
	@Test
	public void testFindIdsWithSort() {
		// just for sure some two identity exists
		getHelper().createIdentity((GuardedString) null);
		getHelper().createIdentity((GuardedString) null);
		//
		Page<UUID> findIds = identityService.findIds(null, new PageRequest(0, 2, new Sort(Direction.ASC, IdmIdentity_.username.getName())));
		UUID firstIdentity = findIds.getContent().get(0);
		UUID secondIdentity = findIds.getContent().get(1);
		//
		Assert.assertNotNull(firstIdentity);
		Assert.assertNotNull(secondIdentity);
		Assert.assertTrue(findIds.getTotalElements() > 1);
		//
		findIds = identityService.findIds(null, new PageRequest(0, 2, new Sort(Direction.DESC, IdmIdentity_.username.getName())));
		//
		Assert.assertNotEquals(firstIdentity, findIds.getContent().get(0));
		Assert.assertNotEquals(secondIdentity, findIds.getContent().get(1));
	}
	
	@Test
	public void testEvaluateStateAgain() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		identity.setState(null);
		//
		identity = identityService.save(identity);
		//
		Assert.assertEquals(IdentityState.VALID, identity.getState());
		Assert.assertFalse(identity.isDisabled());
		//
		try {
			getHelper().disableProcessor(IdentityContractEndProcessor.PROCESSOR_NAME);
			getHelper().disableProcessor(IdentityContractEnableProcessor.PROCESSOR_NAME);
			//
			IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
			contract.setValidTill(LocalDate.now().minusDays(1));
			identityContractService.save(contract);
			//
			identity = identityService.get(identity);
			Assert.assertEquals(IdentityState.VALID, identity.getState());
			Assert.assertFalse(identity.isDisabled());
			//
			identity.setState(null);
			identity = identityService.save(identity);
			//
			Assert.assertEquals(IdentityState.LEFT, identity.getState());
			Assert.assertTrue(identity.isDisabled());
			//
			contract.setValidTill(null);
			identityContractService.save(contract);
			//
			identity = identityService.get(identity);
			Assert.assertEquals(IdentityState.LEFT, identity.getState());
			Assert.assertTrue(identity.isDisabled());
			//
			identity.setState(null);
			identity = identityService.save(identity);
			//
			Assert.assertEquals(IdentityState.VALID, identity.getState());
			Assert.assertFalse(identity.isDisabled());
		} finally {
			getHelper().enableProcessor(IdentityContractEndProcessor.PROCESSOR_NAME);
			getHelper().enableProcessor(IdentityContractEnableProcessor.PROCESSOR_NAME);
		}
	}
}
