import React, { PropTypes } from 'react';
import _ from 'lodash';
import Joi from 'joi';
import { connect } from 'react-redux';
//
import * as Basic from 'app/components/basic';
import { IdentityManager } from 'core/redux';
import ApiOperationTypeEnum from 'core/enums/ApiOperationTypeEnum';

const identityManager = new IdentityManager();

class IdentityDetail extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      showLoading: false,
      showLoadingIdentityTrimmed: false,
      setDataToForm: false
    };
  }

  getContentKey() {
    return 'content.user.profile';
  }

  componentDidMount() {
    const { identity } = this.props;
    this.refs.form.setData(identity);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.identity) {
      if (nextProps.identity._trimmed) {
        this.setState({showLoadingIdentityTrimmed: true});
      } else {
        this.setState({showLoadingIdentityTrimmed: false});
      }
      if (nextProps.identity !== this.props.identity) {
        // after receive new Identity we will hide showLoading on form
        this.setState({showLoading: false, setDataToForm: true});
      }
    }
  }

  componentDidUpdate() {
    if (this.props.identity && !this.props.identity._trimmed && this.state.setDataToForm) {
      // We have to set data to form after is rendered
      this.transformData(this.props.identity, null, ApiOperationTypeEnum.GET);
    }
  }

  onSave(event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const json = this.refs.form.getData();
    this.saveIdentity(json);
  }

  saveIdentity(json) {
    this.setState({
      showLoading: true,
      setDataToForm: false // Form will not be set new data (we are waiting to saved data)
    });
    const { userID } = this.props;
    const result = _.merge({}, json);

    identityManager.getService().patchById(userID, result)
    .then(() => {
      this.context.store.dispatch(identityManager.fetchEntity(userID));
      this.addMessage({ level: 'success', key: 'form-success', message: this.i18n('messages.saved', { username: userID }) });
    }).catch(ex => {
      this.transformData(null, ex, ApiOperationTypeEnum.UPDATE);
      this.setState({
        showLoading: false
      });
    });
  }

  transformData(json, error, operationType) {
    this.refs.form.setData(json, error, operationType);
  }

  render() {
    const { userContext, identity, userID, readOnly } = this.props;
    const { showLoading, showLoadingIdentityTrimmed } = this.state;
    const canEditMap = identityManager.canEditMap(userContext, identity);
    const deactiveDisabled = !userContext || userID === userContext.username || !canEditMap.get('isSaveEnabled');

    return (
      <div>
        <form onSubmit={this.onSave.bind(this)}>
          <Basic.Row>
            <Basic.Panel className="col-lg-7 no-border last" showLoading={showLoadingIdentityTrimmed || showLoading}>
              <Basic.PanelHeader text={this.i18n('header')}/>
              <Basic.AbstractForm ref="form" className="form-horizontal" readOnly={!canEditMap.get('isSaveEnabled') || readOnly}>
                <Basic.TextField ref="username" readOnly label={this.i18n('content.user.profile.username')} required validation={Joi.string().min(3).max(30)}/>
                <Basic.TextField ref="lastName" label={this.i18n('content.user.profile.lastName')} required/>
                <Basic.TextField ref="firstName" label={this.i18n('content.user.profile.firstName')}/>
                <Basic.TextField ref="titleBefore" label={this.i18n('entity.Identity.titleBefore')}/>
                <Basic.TextField ref="titleAfter" label={this.i18n('entity.Identity.titleAfter')}/>
                <Basic.TextField ref="email" label={this.i18n('content.user.profile.email.label')} placeholder={this.i18n('email.placeholder')} hidden={false} validation={Joi.string().email()}/>
                <Basic.TextField
                  ref="phone"
                  label={this.i18n('content.user.profile.phone.label')}
                  placeholder={this.i18n('phone.placeholder')} />
                <Basic.TextArea
                  ref="description"
                  label={this.i18n('content.user.profile.description.label')}
                  placeholder={this.i18n('description.placeholder')}
                  rows={4}/>
                <Basic.Checkbox
                  ref="disabled"
                  label={this.i18n('entity.Identity.disabled')}
                  readOnly={deactiveDisabled || !identity}
                  title={deactiveDisabled ? this.i18n('messages.deactiveDisabled') : ''}>
                </Basic.Checkbox>
              </Basic.AbstractForm>

              <Basic.PanelFooter>
                <Basic.Button type="button" level="link" onClick={this.context.router.goBack} showLoading={showLoading}>{this.i18n('button.back')}</Basic.Button>
                <Basic.Button type="submit" level="success" showLoading={showLoading} rendered={canEditMap.get('isSaveEnabled')} hidden={readOnly}>{this.i18n('button.save')}</Basic.Button>
              </Basic.PanelFooter>
            </Basic.Panel>
          </Basic.Row>
        </form>
      </div>
    );
  }
}


IdentityDetail.propTypes = {
  identity: PropTypes.object,
  userID: PropTypes.string.isRequired,
  readOnly: PropTypes.bool,
  userContext: PropTypes.object
};
IdentityDetail.defaultProps = {
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}
export default connect(select)(IdentityDetail);
