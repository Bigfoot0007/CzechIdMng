import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
import Joi from 'joi';
//
import { Basic, Managers, Utils, Enums } from 'czechidm-core';
import { SystemManager } from '../../redux';

/**
 * Target system detail content
 */
class SystemDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.manager = new SystemManager();
    this.passwordPolicyManager = new Managers.PasswordPolicyManager();
    //
    let showConfigurationRemoteServer = false;
    if (props.entity) {
      showConfigurationRemoteServer = props.entity.remote;
    }

    this.state = {
      _showLoading: false,
      showConfigurationRemoteServer
    };
  }

  getContentKey() {
    return 'acc:content.system.detail';
  }

  componentDidMount() {
    const { entity } = this.props;
    this._initForm(entity);
  }

  /**
   * Component will receive new props, try to compare with actual,
   * then init form
   */
  componentWillReceiveProps(nextProps) {
    const { entity } = this.props;
    if (entity && entity.id !== nextProps.entity.id) {
      this._initForm(nextProps.entity);
    }
  }

  _initForm(entity) {
    let data = {
      ...entity,
    };

    if (entity && entity._embedded && entity._embedded.passwordPolicyGenerate) {
      data.passwordPolicyGenerate = entity._embedded.passwordPolicyGenerate;
    }
    if (entity && entity._embedded && entity._embedded.passwordPolicyValidate) {
      data.passwordPolicyValidate = entity._embedded.passwordPolicyValidate;
    }

    // connector is part of entity, not embedded
    if (entity && entity.connectorServer) {
      // set data for connector server
      data = {
        ...data,
        host: entity.connectorServer.host,
        port: entity.connectorServer.port,
        useSs: entity.connectorServer.useSsl,
        password: entity.connectorServer.password,
        timeout: entity.connectorServer.timeout
      };
    }

    this.refs.form.setData(data);
    this.refs.name.focus();
  }

  /**
   * In this method validate bouth forms - remote connector server and/or system detail
   * after validate save bouth details - system detail, remote connector
   * After complete previous steps, save system
   */
  save(afterAction, event) {
    if (event) {
      event.preventDefault();
    }
    const entity = this.refs.form.getData();

    if (!this.refs.form.isFormValid()) {
      return;
    }
    const { uiKey } = this.props;

    this.setState({
      _showLoading: true
    }, () => {
      // transform password policies
      if (entity.passwordPolicyGenerate) {
        entity.passwordPolicyGenerate = this.passwordPolicyManager.getSelfLink(entity.passwordPolicyGenerate);
      }
      if (entity.passwordPolicyValidate) {
        entity.passwordPolicyValidate = this.passwordPolicyManager.getSelfLink(entity.passwordPolicyValidate);
      }

      const saveEntity = {
        ...entity,
        connectorServer: {
          host: entity.host,
          password: entity.password,
          port: entity.port,
          timeout: entity.timeout,
          useSsl: entity.useSsl
        }
      };

      if (Utils.Entity.isNew(saveEntity)) {
        this.context.store.dispatch(this.manager.createEntity(saveEntity, `${uiKey}-detail`, (createdEntity, newError) => {
          this._afterSave(createdEntity, newError, afterAction);
        }));
      } else {
        this.context.store.dispatch(this.manager.patchEntity(saveEntity, `${uiKey}-detail`, (patchedEntity, newError) => {
          this._afterSave(patchedEntity, newError, afterAction);
        }));
      }
    });
  }

  /**
   * Method show form for remote server configuration
   */
  _setRemoteServer(event) {
    this.setState({
      showConfigurationRemoteServer: event.currentTarget.checked
    });
  }

  _afterSave(entity, error, afterAction = 'CLOSE') {
    this.setState({
      _showLoading: false
    }, () => {
      if (error) {
        this.addError(error);
        return;
      }

      this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
      //
      if (afterAction === 'CLOSE') {
        // reload options with remote connectors
        this.context.router.replace(`systems`);
      } else {
        this.context.router.replace(`system/${entity.id}/detail`);
      }
    });
  }

  render() {
    const { uiKey, entity } = this.props;
    const { _showLoading, showConfigurationRemoteServer } = this.state;
    return (
      <div>
        <Helmet title={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('edit.title')} />

        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel className={Utils.Entity.isNew(entity) ? '' : 'no-border last'}>
            <Basic.PanelHeader text={Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('basic')} />

            <Basic.PanelBody style={Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 }}>
              <Basic.AbstractForm ref="form" uiKey={uiKey} className="form-horizontal" readOnly={!Managers.SecurityManager.hasAuthority('SYSTEM_WRITE')} >
                <Basic.TextField
                  ref="name"
                  label={this.i18n('acc:entity.System.name')}
                  required
                  max={255}/>
                <Basic.Checkbox
                  ref="remote"
                  onChange={this._setRemoteServer.bind(this)}
                  label={this.i18n('acc:entity.System.remoteConnector.label')}
                  helpBlock={this.i18n('acc:entity.System.remoteConnector.help')}/>
                {/* definition for remote connector server */}
                <Basic.TextField
                  ref="host"
                  label={this.i18n('acc:entity.ConnectorServer.host')}
                  hidden={!showConfigurationRemoteServer}
                  required={showConfigurationRemoteServer}
                  max={255}/>
                <Basic.Checkbox
                  ref="useSsl"
                  label={this.i18n('acc:entity.ConnectorServer.useSsl')}
                  hidden={!showConfigurationRemoteServer}/>
                <Basic.TextField
                  ref="port"
                  label={this.i18n('acc:entity.ConnectorServer.port')}
                  hidden={!showConfigurationRemoteServer}/>
                <Basic.TextField
                  ref="password"
                  confidential
                  label={this.i18n('acc:entity.ConnectorServer.password')}
                  hidden={!showConfigurationRemoteServer}/>
                <Basic.TextField
                  ref="timeout"
                  label={this.i18n('acc:entity.ConnectorServer.timeout')}
                  hidden={!showConfigurationRemoteServer}/>
                {/* end for connector server definition */}
                <Basic.Checkbox
                  ref="disabled"
                  label={this.i18n('acc:entity.System.disabled')}/>
                <Basic.SelectBox
                  ref="passwordPolicyValidate"
                  label={this.i18n('acc:entity.System.passwordPolicyValidate')}
                  placeholder={this.i18n('acc:entity.System.passwordPolicyValidate')}
                  manager={this.passwordPolicyManager}
                  forceSearchParameters={this.passwordPolicyManager.getDefaultSearchParameters()
                    .setFilter('type', Enums.PasswordPolicyTypeEnum.findKeyBySymbol(Enums.PasswordPolicyTypeEnum.VALIDATE))}/>
                <Basic.SelectBox
                  ref="passwordPolicyGenerate"
                  label={this.i18n('acc:entity.System.passwordPolicyGenerate')}
                  placeholder={this.i18n('acc:entity.System.passwordPolicyGenerate')}
                  manager={this.passwordPolicyManager}
                  forceSearchParameters={this.passwordPolicyManager.getDefaultSearchParameters()
                    .setFilter('type', Enums.PasswordPolicyTypeEnum.findKeyBySymbol(Enums.PasswordPolicyTypeEnum.GENERATE))}/>
                <Basic.TextArea
                  ref="description"
                  label={this.i18n('acc:entity.System.description')}
                  max={255}/>
                <Basic.Checkbox
                  ref="virtual"
                  label={this.i18n('acc:entity.System.virtual')}
                  rendered={false}/>
                <Basic.Checkbox
                  ref="readonly"
                  label={this.i18n('acc:entity.System.readonly.label')}
                  helpBlock={this.i18n('acc:entity.System.readonly.help')}/>
                <Basic.Checkbox
                  ref="queue"
                  label={this.i18n('acc:entity.System.queue.label')}
                  helpBlock={this.i18n('acc:entity.System.queue.help')}
                  hidden/>
              </Basic.AbstractForm>
            </Basic.PanelBody>

            <Basic.PanelFooter>
              <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>

              <Basic.SplitButton
                level="success"
                title={this.i18n('button.saveAndContinue')}
                onClick={this.save.bind(this, 'CONTINUE')}
                showLoading={_showLoading}
                showLoadingIcon
                showLoadingText={this.i18n('button.saving')}
                rendered={Managers.SecurityManager.hasAuthority('SYSTEM_WRITE')}
                pullRight
                dropup>
                <Basic.MenuItem eventKey="1" onClick={this.save.bind(this, 'CLOSE')}>{this.i18n('button.saveAndClose')}</Basic.MenuItem>
              </Basic.SplitButton>
            </Basic.PanelFooter>
          </Basic.Panel>
          {/* onEnter action - is needed because SplitButton is used instead standard submit button */}
          <input type="submit" className="hidden"/>
        </form>
      </div>
    );
  }
}

SystemDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired
};
SystemDetail.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(SystemDetail);
