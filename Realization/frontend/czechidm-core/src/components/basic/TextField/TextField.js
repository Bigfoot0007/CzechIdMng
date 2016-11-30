import React, { PropTypes } from 'react';
import classNames from 'classnames';
import Joi from 'joi';
//
import AbstractFormComponent from '../AbstractFormComponent/AbstractFormComponent';
import HelpIcon from '../HelpIcon/HelpIcon';
import Tooltip from '../Tooltip/Tooltip';
import Icon from '../Icon/Icon';
import Button from '../Button/Button';

class TextField extends AbstractFormComponent {

  constructor(props) {
    super(props);
    this.state = {
      ...this.state,
      confidentialState: {
        showInput: false
      }
    };
  }

  getComponentKey() {
    return 'component.basic.TextField';
  }

  getValidationDefinition(required) {
    const { min, max } = this.props;
    let validation = super.getValidationDefinition(min ? true : required);

    if (min && max) {
      validation = validation.concat(Joi.string().min(min).max(max));
    } else if (min) {
      validation = validation.concat(Joi.string().min(min));
    } else if (max) {
      if (!required) {
        // if set only max is necessary to set allow null and empty string
        validation = validation.concat(Joi.string().max(max).allow(null).allow(''));
      } else {
        // if set prop required it must not be set allow null or empty string
        validation = validation.concat(Joi.string().max(max));
      }
    }

    return validation;
  }

  getRequiredValidationSchema() {
    return Joi.string().required();
  }

  /**
   * Focus input field
   */
  focus() {
    this.refs.input.focus();
  }

  onChange(event) {
    super.onChange(event);
    this.refs.popover.show();
  }

  /**
   * Show / hide input istead confidential wrapper
   *
   * @param  {bool} showInput
   */
  toogleConfidentialState(showInput) {
    this.setState({
      value: null,
      confidentialState: {
        showInput
      }
    }, () => {
      this.focus();
    });
  }

  getValue() {
    const { confidential } = this.props;
    const { confidentialState } = this.state;
    //
    if (confidential && !confidentialState.showInput) {
      // preserve previous value
      return undefined;
    }
    return super.getValue();
  }

  clearValue() {
    this.setState({ value: null }, () => { this.validate(); });
  }

  /**
   * Return true, when confidential wrapper should be shown
   *
   * @return {bool}
   */
  _showConfidentialWrapper() {
    const { required, confidential } = this.props;
    const { value, confidentialState } = this.state;
    return confidential && !confidentialState.showInput && (!required || value);
  }

  getBody(feedback) {
    const { type, labelSpan, label, componentSpan, placeholder, style, required, help, helpBlock } = this.props;
    const { value, disabled, readOnly } = this.state;
    //
    const className = classNames('form-control');
    const labelClassName = classNames(labelSpan, 'control-label');
    let showAsterix = false;
    if (required && !feedback && !this._showConfidentialWrapper()) {
      showAsterix = true;
    }
    const validationResult = this.getValidationResult();
    const title = validationResult != null ? validationResult.message : null;
    //
    // value and readonly properties depends on confidential wrapper
    let _value = value || '';
    let _readOnly = readOnly;
    if (this._showConfidentialWrapper()) {
      if (value) {
        _value = '*****'; // asterix wil be shown, when value is filled
      } else {
        _value = null;
      }
      _readOnly = true;
    }
    // input component
    const component = (
      <input
        ref="input"
        type={type}
        className={className}
        disabled={disabled}
        placeholder={placeholder}
        onChange={this.onChange.bind(this)}
        value={_value}
        style={style}
        readOnly={_readOnly}/>
    );
    //
    // show confidential wrapper, when confidential value could be changed
    let confidentialWrapper = component;
    if (this._showConfidentialWrapper()) {
      confidentialWrapper = (
        <div className="input-group">
          { component }
          <span className="input-group-btn">
            <Button
              type="button"
              level="default"
              className="btn-sm"
              style={{ marginTop: '0px', height: '34px' }}
              onClick={this.toogleConfidentialState.bind(this, true)}
              title={this.i18n('confidential.edit')}
              titlePlacement="bottom">
              <Icon type="fa" icon="edit"/>
            </Button>
          </span>
        </div>
      );
    }

    return (
      <div className={showAsterix ? 'has-feedback' : ''}>
        {
          !label
          ||
          <label
            className={labelClassName}>
            {label}
          </label>
        }
        <div className={componentSpan} style={{ whiteSpace: 'nowrap' }}>
          <Tooltip ref="popover" placement="right" value={title}>
            <span>
              {confidentialWrapper}
              {
                (feedback || !showAsterix)
                ||
                <span className="form-control-feedback" style={{color: 'red', zIndex: 0}}>*</span>
              }
            </span>
          </Tooltip>
          <HelpIcon content={help} style={{ marginLeft: '3px' }}/>
          {
            !helpBlock
            ||
            <span className="help-block" style={{ whiteSpace: 'normal' }}>{helpBlock}</span>
          }
        </div>
      </div>
    );
  }
}

TextField.propTypes = {
  ...AbstractFormComponent.propTypes,
  type: PropTypes.string,
  placeholder: PropTypes.string,
  help: PropTypes.string,
  min: PropTypes.number,
  max: PropTypes.number,
  /**
   * Confidential text field - if it is filled, then shows asterix only and supports to add new value
   */
  confidential: PropTypes.bool
};

TextField.defaultProps = {
  ...AbstractFormComponent.defaultProps,
  type: 'text',
  confidential: false
};

export default TextField;
