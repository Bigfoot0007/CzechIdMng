package eu.bcvsolutions.idm.acc.bulk.action.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.impl.TestAccountExceptionProcessor;
import eu.bcvsolutions.idm.acc.service.impl.TestProvisioningExceptionProcessor;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link RoleAcmBulkAction}
 *
 * @author svandav
 * @author Radek Tomiška
 */

public class RoleAccountManagementBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private TestHelper helper;
	@Autowired 
	private TestProvisioningExceptionProcessor testProvisioningExceptionProcessor;
	@Autowired 
	private TestAccountExceptionProcessor testAccountExceptionProcessor;
	@Autowired 
	private IdmLongRunningTaskService longRunningTaskService;
	
	@Before
	public void init() {
		loginAsAdmin(InitApplicationData.ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testProcessBulkAction() {
		
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.UPDATE, IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
		// Delete all data
		helper.deleteAllResourceData();
		
		List<IdmRoleDto> roles = this.createRoles(1);
		IdmRoleDto role = roles.get(0);
		// Assign identity to role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, role);
		// Create system and assign him to the role
		SysSystemDto system = helper.createTestResourceSystem(true);
		helper.createRoleSystem(role, system);
		// ACM was not run, so account cannot be exists
		assertNull(helper.findResource(identity.getUsername()));
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleAccountManagementBulkAction.NAME);

		bulkAction.setIdentifiers(this.getIdFromList(roles));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 1l, null, null);
		
		// ACM was run, so account must exists
		assertNotNull(helper.findResource(identity.getUsername()));
	}
	
	@Test
	public void testProcessBulkActionWithExceptionInProvisionning() {
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		
		testProvisioningExceptionProcessor.setDisabled(false);
		testProvisioningExceptionProcessor.setFailOnEntityIdentifier(identityOne.getId());
		try {			
			// Delete all data
			helper.deleteAllResourceData();
			
			List<IdmRoleDto> roles = this.createRoles(1);
			IdmRoleDto role = roles.get(0);
			// Assign identity to role
			getHelper().createIdentityRole(identityOne, role);
			getHelper().createIdentityRole(identityTwo, role);
			// Create system and assign him to the role
			SysSystemDto system = helper.createTestResourceSystem(true);
			helper.createRoleSystem(role, system);
			// ACM was not run, so account cannot be exists
			assertNull(helper.findResource(identityOne.getUsername()));
			assertNull(helper.findResource(identityTwo.getUsername()));
			
			IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleAccountManagementBulkAction.NAME);
	
			bulkAction.setIdentifiers(this.getIdFromList(roles));
			IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
			
			checkResultLrt(processAction, 1l, null, null);
			
			// ACM was run, so account must exists on the identity without exception
			assertNull(helper.findResource(identityOne.getUsername()));
			assertNotNull(helper.findResource(identityTwo.getUsername()));
		} finally {
			testProvisioningExceptionProcessor.setDisabled(true);
			testProvisioningExceptionProcessor.setFailOnEntityIdentifier(null);
		}
	}
	
	@Test
	public void testProcessBulkActionWithExceptionBeforeProvisioning() {
		IdmIdentityDto identityOne = getHelper().createIdentity((GuardedString) null);
		IdmIdentityDto identityTwo = getHelper().createIdentity((GuardedString) null);
		
		testAccountExceptionProcessor.setDisabled(false);
		testAccountExceptionProcessor.setFailOnUid(identityOne.getUsername());
		try {
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, true);
			
			// Delete all data
			helper.deleteAllResourceData();
			
			List<IdmRoleDto> roles = this.createRoles(1);
			IdmRoleDto role = roles.get(0);
			// Assign identity to role
			getHelper().createIdentityRole(identityOne, role);
			getHelper().createIdentityRole(identityTwo, role);
			// Create system and assign him to the role
			SysSystemDto system = helper.createTestResourceSystem(true);
			helper.createRoleSystem(role, system);
			// ACM was not run, so account cannot be exists
			assertNull(helper.findResource(identityOne.getUsername()));
			assertNull(helper.findResource(identityTwo.getUsername()));
			
			IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleAccountManagementBulkAction.NAME);
	
			bulkAction.setIdentifiers(this.getIdFromList(roles));
			IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
			
			getHelper().waitForResult(res -> {
				return longRunningTaskService.get(processAction.getLongRunningTaskId()).getResultState().isRunnable();
			});
			
			checkResultLrt(processAction, null, 1l, null);
			
			// ACM was run, so account must exists on the identity without exception
			assertNull(helper.findResource(identityOne.getUsername()));
			assertNotNull(helper.findResource(identityTwo.getUsername()));
		} finally {
			getHelper().setConfigurationValue(SchedulerConfiguration.PROPERTY_TASK_ASYNCHRONOUS_ENABLED, false);
			testAccountExceptionProcessor.setDisabled(true);
			testAccountExceptionProcessor.setFailOnUid(null);
		}
	}
	
	@Test
	public void testProcessBulkActionWithoutRoleIsAssigned() {
		List<IdmRoleDto> roles = this.createRoles(1);
		IdmRoleDto role = roles.get(0);

		// Create system and assign him to the role
		SysSystemDto system = helper.createTestResourceSystem(true);
		helper.createRoleSystem(role, system);
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleAccountManagementBulkAction.NAME);

		bulkAction.setIdentifiers(this.getIdFromList(roles));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 1L, 0L, 0L);
		
		Assert.assertTrue(StringUtils.isEmpty(longRunningTaskService.get(processAction.getLongRunningTaskId()).getResult().getCause()));
	}
	
	@Test
	public void testProcessBulkActionWithoutPermissions() {
		
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
		// Delete all data
		helper.deleteAllResourceData();
		
		List<IdmRoleDto> roles = this.createRoles(1);
		IdmRoleDto role = roles.get(0);
		// Assign identity to role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, role);
		// Create system and assign him to the role
		SysSystemDto system = helper.createTestResourceSystem(true);
		helper.createRoleSystem(role, system);
		// ACM was not run, so account cannot be exists
		assertNull(helper.findResource(identity.getUsername()));
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleAccountManagementBulkAction.NAME);

		bulkAction.setIdentifiers(this.getIdFromList(roles));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, null, 0l, 1l);
		
		// ACM was run, but without premissions for update the role
		assertNull(helper.findResource(identity.getUsername()));
	}
	
	@Test
	public void testProcessBulkActionWithPermissions() {
		
		// Read permission for all
		IdmIdentityDto user = getHelper().createIdentity();
		IdmRoleDto permissionRole = getHelper().createRole();
		getHelper().createBasePolicy(permissionRole.getId(), IdmBasePermission.READ);
		getHelper().createBasePolicy(permissionRole.getId(), CoreGroupPermission.ROLE, IdmRole.class, IdmBasePermission.UPDATE);
		getHelper().createIdentityRole(user, permissionRole);
		loginAsNoAdmin(user.getUsername());
		// Delete all data
		helper.deleteAllResourceData();
		
		List<IdmRoleDto> roles = this.createRoles(1);
		IdmRoleDto role = roles.get(0);
		// Assign identity to role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, role);
		// Create system and assign him to the role
		SysSystemDto system = helper.createTestResourceSystem(true);
		helper.createRoleSystem(role, system);
		// ACM was not run, so account cannot be exists
		assertNull(helper.findResource(identity.getUsername()));
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleAccountManagementBulkAction.NAME);

		bulkAction.setIdentifiers(this.getIdFromList(roles));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 1l, null, null);
		
		// ACM was run, so account must exists
		assertNotNull(helper.findResource(identity.getUsername()));
	}
	
	@Test
	public void testPrevalidationBulkAction() {
		IdmRoleDto role = getHelper().createRole();
		
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleAccountManagementBulkAction.NAME);
		bulkAction.getIdentifiers().add(role.getId());
		
		// Warning message, role hasn't identity
		ResultModels resultModels = bulkActionManager.prevalidate(bulkAction);
		assertEquals(1, resultModels.getInfos().size());
		assertEquals(AccResultCode.ROLE_ACM_BULK_ACTION_NONE_IDENTITIES.getCode(), resultModels.getInfos().get(0).getStatusEnum());
		// Assign identity to role
		IdmIdentityDto identity = getHelper().createIdentity();
		getHelper().createIdentityRole(identity, role);
		
		// Info message, role has identity
		resultModels = bulkActionManager.prevalidate(bulkAction);
		assertEquals(1, resultModels.getInfos().size());
		assertEquals(AccResultCode.ROLE_ACM_BULK_ACTION_NUMBER_OF_IDENTITIES.getCode(), resultModels.getInfos().get(0).getStatusEnum());
	}
	
	
}
