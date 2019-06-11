import { connect } from 'react-redux';
//
import * as Advanced from '../../../components/advanced';
import { IdentityManager, ConfigurationManager } from '../../../redux';

const identityManager = new IdentityManager();

/**
 * Quick button to change identity password.
 *
 * @author Radek Tomiška
 * @since 9.6.0
 */
class PasswordChangeDashboardButton extends Advanced.AbstractIdentityDashboardButton {

  constructor(props, context) {
    super(props, context);
  }

  getIcon() {
    return 'component:password';
  }

  isRendered() {
    const { passwordChangeType, permissions } = this.props;
    //
    return identityManager.canChangePassword(passwordChangeType, permissions);
  }

  getLabel() {
    return this.i18n('content.password.change.header');
  }

  onClick() {
    this.context.router.push(`/identity/${encodeURIComponent(this.getIdentityIdentifier())}/password/change`);
  }
}

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady'), // required
    userContext: state.security.userContext, // required
    passwordChangeType: ConfigurationManager.getPublicValue(state, 'idm.pub.core.identity.passwordChange')
  };
}

export default connect(select)(PasswordChangeDashboardButton);
