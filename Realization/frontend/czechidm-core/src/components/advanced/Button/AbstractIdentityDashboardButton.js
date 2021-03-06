import React from 'react';
import PropTypes from 'prop-types';
import * as Basic from '../../basic';

/**
 * Quick dashboard button supper class.
 *
 * @author Radek Tomiška
 * @since 9.6.0
 */
export default class AbstractIdentityDashboardButton extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      showLoading: false
    };
  }

  /**
   * Identity identifier - can be resolved from params (url) or from user context (global dashboard).
   *
   * @return {string} id
   */
  getIdentityIdentifier() {
    const { entityId, userContext } = this.props;
    //
    if (entityId) {
      return entityId;
    }
    if (userContext) {
      return userContext.username;
    }
    return null;
  }

  /**
   * Button icon.
   * Override button label or icon at least.
   *
   * @return {string}
   */
  getIcon() {
    return null;
  }

  /**
   * Button is rendered.
   *
   * @return {Boolean}
   */
  isRendered() {
    return true;
  }

  /**
   * Button text.
   * Override label or icon at least.
   *
   * @return {string}
   */
  getLabel() {
    return null;
  }

  /**
   * Button tooltip.
   *
   * @return {string}
   */
  getTitle() {
    return null;
  }

  /**
   * OnClick button function
   */
  onClick(/* event */) {
  }

  /**
   * Render confirm dialog if needed.
   * Confirm dialog cannot be rendered inside button ro prevent event propagation.
   *
   * @since 10.2.0
   */
  renderConfirm() {
  }

  /**
   * Render button content (~children).
   */
  renderContent() {
    return this.getLabel();
  }

  /**
   * Action runs
   * @return {Boolean}
   */
  isShowLoading() {
    return this.state.showLoading;
  }

  /**
   * Button level (~color). See Button.level property for available options.
   *
   * @return {string}
   */
  getLevel() {
    return 'default';
  }

  render() {
    const { buttonSize, style, showLoading } = this.props;
    //
    const _style = {
      marginRight: 3,
      minWidth: 150,
      ...style
    };
    if (!buttonSize) {
      _style.height = 50;
    }
    //
    return (
      <span>
        { this.renderConfirm() }

        <Basic.Button
          icon={ this.getIcon() }
          level={ this.getLevel() }
          className={ !buttonSize || buttonSize === 'default' ? null : `btn-${ buttonSize }` }
          onClick={ (event) => this.onClick(event) }
          style={ _style }
          title={ this.getTitle() }
          titlePlacement="bottom"
          rendered={ this.isRendered() === true }
          showLoading={ this.isShowLoading() }
          showLoadingIcon
          disabled={ showLoading }>
          { this.renderContent() }
        </Basic.Button>
      </span>
    );
  }
}

AbstractIdentityDashboardButton.propTypes = {
  /**
   * Selected identity (in redux state)
   *
   * @type {IdmIdentityDto}
   */
  identity: PropTypes.object.isRequired,
  /**
   * Loaded perrmissions for selected identity                                 [description]
   */
  permissions: PropTypes.oneOfType([
    PropTypes.bool,
    PropTypes.arrayOf(PropTypes.string)
  ]),
  /**
   * Security context
   */
  userContext: PropTypes.object.isRequired,
  /**
   * Button size (by bootstrap).
   *
   * @since 10.3.0
   */
  buttonSize: PropTypes.oneOf(['default', 'xs', 'sm', 'lg']),
  /**
   * Callback function.
   * Depends on button implementation. Supported e.g. in action buttons.
   *
   * @since 10.3.0
   */
  onComplete: PropTypes.func
};
