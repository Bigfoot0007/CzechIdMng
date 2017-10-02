import React, { PropTypes } from 'react';
import classnames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';
import Icon from '../Icon/Icon';

/**
 * Label box
 */
class Label extends AbstractComponent {

  constructor(props) {
    super(props);
  }

  _onClose(event) {
    const { onClose } = this.props;
    if (!onClose) {
      return;
    }
    onClose(event);
  }

  render() {
    const { level, title, text, value, className, icon, rendered, showLoading, ...others } = this.props;
    const _text = text || value;
    if (!rendered || !_text) {
      return null;
    }
    const classNames = classnames(
      'label',
      'label-' + (level === 'error' ? 'danger' : level),
      className
    );
    return (
      <span className={classNames} title={title} {...others}>
        {
          showLoading
          ?
          <Icon type="fa" icon="refresh" showLoading/>
          :
          _text
        }
      </span>
    );
  }
}

Label.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Label level / css / class
   */
  level: PropTypes.oneOf(['default', 'success', 'warning', 'info', 'danger', 'error', 'primary']),
  /**
   * Label text content
   */
  text: PropTypes.string,
  /**
   * Label text content (text alias - text has higher priority)
   */
  value: PropTypes.string
};

Label.defaultProps = {
  ...AbstractComponent.defaultProps,
  level: 'default'
};

export default Label;
