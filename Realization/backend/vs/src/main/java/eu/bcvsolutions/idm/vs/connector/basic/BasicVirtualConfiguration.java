package eu.bcvsolutions.idm.vs.connector.basic;

import java.text.MessageFormat;
import java.util.Arrays;

import eu.bcvsolutions.idm.ic.api.IcConnectorConfigurationClass;
import eu.bcvsolutions.idm.ic.api.annotation.IcConfigurationClassProperty;
import eu.bcvsolutions.idm.ic.exception.IcException;

/**
 * Configuration for virtual system
 * 
 * @author svandav
 *
 */
public class BasicVirtualConfiguration implements IcConnectorConfigurationClass {

	private String[] attributes = { "firstName", "lastName" };
	private String[] implementers;
	private String[] implementerRoles;
	private boolean resetPasswordSupported = false;
	private boolean disableSupported = true;
	private boolean onlyNotification = false;
	private String[] reservedNames = { "uid", "__NAME__", "enable", "__ENABLE__", "__PASSWORD__" };

	@IcConfigurationClassProperty(order = 10, required = true, displayName = "Attributes", helpMessage = "Properties for create EAV model.")
	public String[] getAttributes() {
		return attributes;
	}

	public void setAttributes(String[] attributes) {
		this.attributes = attributes;
	}

	@IcConfigurationClassProperty(order = 20, displayName = "Implementers", helpMessage = "For this implementers will be created realization tasks. Every implementer must be dentity in CzechIdM. Value are UUIDs of identities (multivalue).")
	public String[] getImplementers() {
		return implementers;
	}

	public void setImplementers(String[] implementers) {
		this.implementers = implementers;
	}

	@IcConfigurationClassProperty(order = 30, displayName = "Roles of implementers", helpMessage = "All identity with this roles will be implementers. Every role must be role in CzechIdM. Value are UUIDs of roles (multivalue).")
	public String[] getImplementerRoles() {
		return implementerRoles;
	}

	public void setImplementerRoles(String[] realizatorRoles) {
		this.implementerRoles = realizatorRoles;
	}

	@IcConfigurationClassProperty(order = 40, displayName = "Password reset supported", helpMessage = "Not implemented yet!")
	public boolean isResetPasswordSupported() {
		return resetPasswordSupported;
	}

	public void setResetPasswordSupported(boolean changePasswordSupported) {
		this.resetPasswordSupported = changePasswordSupported;
	}

	@IcConfigurationClassProperty(order = 50, displayName = "Account disable supported", helpMessage = "Not implemented yet!")
	public boolean isDisableSupported() {
		return disableSupported;
	}

	public void setDisableSupported(boolean disableSupported) {
		this.disableSupported = disableSupported;
	}

	@IcConfigurationClassProperty(order = 60, displayName = "Only notification", helpMessage = "None implementers task will be crated. Only message will be send. - Not implemented yet!")
	public boolean isOnlyNotification() {
		return onlyNotification;
	}

	public void setOnlyNotification(boolean onlyNotification) {
		this.onlyNotification = onlyNotification;
	}

	@Override
	public void validate() {
		if (this.getAttributes() == null || this.getAttributes().length == 0) {
			throw new IcException("BasicVirtualConfiguration validation problem: Attributes cannost be null or empty!");
		}

		// Validation on reserved attribute names
		for (String name : this.reservedNames) {
			boolean reservedAttributeFound = Arrays.asList(this.getAttributes()).stream()
					.filter(attribute -> attribute.toLowerCase().equals(name)).findFirst().isPresent();

			if (reservedAttributeFound) {
				throw new IcException(MessageFormat.format(
						"BasicVirtualConfiguration validation problem: Attributes contains [{0}] attribute. This attribute name is reserved!",
						name));
			}
		}
	}

}
