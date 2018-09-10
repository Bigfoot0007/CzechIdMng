package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractPositionFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;

/**
 * Automatic roles recount while identity contract is saved, updated or deleted / disabled.
 * 
 * @author Radek Tomiška
 * @author Ondřej Kopr
 *
 */
@Component
@Description("Automatic roles recount while identity contract is updated, disabled or enabled.")
public class IdentityContractUpdateByAutomaticRoleProcessor
		extends CoreEventProcessor<IdmIdentityContractDto> 
		implements IdentityContractProcessor {
	
	public static final String PROCESSOR_NAME = "identity-contract-update-by-automatic-role-processor";
	//
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmContractPositionService contractPositionService;
	
	public IdentityContractUpdateByAutomaticRoleProcessor() {
		super(IdentityContractEventType.NOTIFY);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public boolean conditional(EntityEvent<IdmIdentityContractDto> event) {
		return super.conditional(event)
				&& IdentityContractEventType.UPDATE.name().equals(event.getParentType());
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto contract = event.getContent();
		//
		IdmIdentityContractDto previous = event.getOriginalSource();
		UUID previousPosition = previous.getWorkPosition();
		UUID newPosition = contract.getWorkPosition();
		//
		// check if new and old work position are same
		// check automatic roles - if position or disabled was changed
		boolean validityChangedToValid = contract.isValidNowOrInFuture() && previous.isValidNowOrInFuture() != contract.isValidNowOrInFuture();
		if (!Objects.equals(newPosition, previousPosition) || validityChangedToValid) {
			// work positions has some difference or validity changes
			List<IdmIdentityRoleDto> assignedRoles = identityRoleService.findAllByContract(contract.getId());
			//
			// remove all automatic roles by attribute
			if (!assignedRoles.isEmpty()) {
				assignedRoles = assignedRoles.stream().filter(autoRole -> {
					AbstractIdmAutomaticRoleDto automaticRoleDto = DtoUtils.getEmbedded(autoRole, IdmIdentityRole_.automaticRole, (AbstractIdmAutomaticRoleDto) null);
					if (automaticRoleDto instanceof IdmRoleTreeNodeDto) {
						return true;
					}
					return false;
				}).collect(Collectors.toList());
			}
			//
			Set<UUID> previousAutomaticRoles = assignedRoles.stream()
					.filter(identityRole -> {
						return identityRole.getAutomaticRole() != null;
					})
					.map(identityRole -> {
						return identityRole.getAutomaticRole();
					})
					.collect(Collectors.toSet());
			Set<IdmRoleTreeNodeDto> addedAutomaticRoles = new HashSet<>();
			if (newPosition != null && contract.isValidNowOrInFuture()) {
				addedAutomaticRoles = roleTreeNodeService.getAutomaticRolesByTreeNode(newPosition);
			}
			// prevent to remove newly added or still exists roles
			Set<UUID> removedAutomaticRoles = new HashSet<>(previousAutomaticRoles);
			removedAutomaticRoles.removeAll(addedAutomaticRoles
					.stream()
					.map(IdmRoleTreeNodeDto::getId)
					.collect(Collectors.toList())
					);
			addedAutomaticRoles.removeIf(a -> {
				return previousAutomaticRoles.contains(a.getId());
			});
			//
			for(UUID removedAutomaticRole : removedAutomaticRoles) {
				Iterator<IdmIdentityRoleDto> iter = assignedRoles.iterator();
				while (iter.hasNext()){
					IdmIdentityRoleDto identityRole = iter.next();				
					if (Objects.equals(identityRole.getAutomaticRole(), removedAutomaticRole)) {					
						// check, if role will be added by new automatic roles and prevent removing
						IdmRoleTreeNodeDto addedAutomaticRole = getByRole(identityRole.getRole(), addedAutomaticRoles);
						if (addedAutomaticRole == null) {
							// remove assigned role
							roleTreeNodeService.removeAutomaticRoles(identityRole, null);
							iter.remove();
						} else {
							// change relation only
							identityRole.setAutomaticRole(addedAutomaticRole.getId());
							updateIdentityRole(identityRole);
							//
							// new automatic role is not needed
							addedAutomaticRoles.remove(addedAutomaticRole);
						}
					}
			    }
			}
			//
			// change date - for unchanged assigned roles only
			if (EntityUtils.validableChanged(previous, contract)) {
				changeValidable(contract, assignedRoles);
			}
			//
			// add identity roles
			roleTreeNodeService.addAutomaticRoles(contract, addedAutomaticRoles);	
			//
			// contract is enabled => process all contract positions
			if (validityChangedToValid) {
				IdmContractPositionFilter filter = new IdmContractPositionFilter();
				filter.setIdentityContractId(contract.getId());
				contractPositionService
					.find(filter, null)
					.getContent()
					.forEach(position -> {
						CoreEvent<IdmContractPositionDto> positionEvent = new CoreEvent<>(CoreEventType.NOTIFY, position);
						// positionEvent.setParentType(CoreEventType.UPDATE.name());
						// recount automatic roles for given position
						contractPositionService.publish(positionEvent, event);
					});
			}
		}
		//
		// process validable change
		else if (EntityUtils.validableChanged(previous, contract)) {
			changeValidable(contract, identityRoleService.findAllByContract(contract.getId()));
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	private IdmRoleTreeNodeDto getByRole(UUID roleId, Set<IdmRoleTreeNodeDto> automaticRoles) {
		for (IdmRoleTreeNodeDto automaticRole : automaticRoles) {
			if (automaticRole.getRole().equals(roleId)) {
				return automaticRole;
			}
		}
		return null;
	}
	
	/**
	 * Change dates for roles assigned by given contract (roles assigned by contract positions are included)
	 * 
	 * @param contract
	 * @param assignedRoles
	 */
	private void changeValidable(IdmIdentityContractDto contract, List<IdmIdentityRoleDto> assignedRoles) {
		if (assignedRoles.isEmpty()) {
			return;
		}
		//
		assignedRoles
			.stream()
			.filter(identityRole -> {
				// automatic roles only
				return identityRole.getAutomaticRole() != null;
			})
			.forEach(identityRole -> {				
				identityRole.setValidFrom(contract.getValidFrom());
				identityRole.setValidTill(contract.getValidTill());				
				updateIdentityRole(identityRole);
			});
	}
	
	/**
	 * Saves identity role by event - skip authorities check is needed (optimalizations)
	 * 
	 * @param identityRole
	 */
	private void updateIdentityRole(IdmIdentityRoleDto identityRole) {
		// skip check granted authorities
		IdentityRoleEvent event = new IdentityRoleEvent(IdentityRoleEventType.UPDATE, identityRole);
		event.getProperties().put(IdmIdentityRoleService.SKIP_CHECK_AUTHORITIES, Boolean.TRUE);
		identityRoleService.publish(event);
	}
	
	/**
	 * after save
	 */
	@Override
	public int getOrder() {
		return super.getOrder() + 500;
	}

}
