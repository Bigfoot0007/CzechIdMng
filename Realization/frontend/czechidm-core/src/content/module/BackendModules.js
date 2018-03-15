import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { BackendModuleManager, DataManager, SecurityManager } from '../../redux';
import * as Utils from '../../utils';
import ConfigLoader from '../../utils/ConfigLoader';

/**
 * BE modules administration
 *
 * @author Radek Tomiška
 */
class BackendModules extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      detail: {
        show: false
      }
    };
    this.backendModuleManager = new BackendModuleManager();
  }

  getContentKey() {
    return 'content.system.be-modules';
  }

  componentDidMount() {
    this.selectNavigationItems(['system', 'modules', 'be-modules']);
    this.context.store.dispatch(this.backendModuleManager.fetchInstalledModules());
  }

  onEnable(entity, enable, event) {
    if (event) {
      event.preventDefault();
    }
    this.refs[`confirm-${enable ? '' : 'de'}activate`].show(
      this.i18n(`action.${enable ? '' : 'de'}activate.message`, { count: 1, record: entity.name }),
      this.i18n(`action.${enable ? '' : 'de'}activate.header`, { count: 1 })
    ).then(() => {
      this.context.store.
      dispatch(this.backendModuleManager.setEnabled(entity.id, enable, (patchedEntity, error) => {
        if (!error) {
          this.addMessage({ message: this.i18n(`action.${enable ? '' : 'de'}activate.success`, { count: 1, record: entity.name }) });
          // reload is needed - rotes could be disabled too
          window.location.reload();
        } else {
          this.addError(error);
        }
      }));
    }, () => {
      // Rejected
    });
  }

  /**
  * Show modal window
  */
  showResultCodes(moduleId) {
    this.context.store.dispatch(this.backendModuleManager.getResultCodes(moduleId, (error) => {
      if (error) {
        this.addError(error);
      }
    }));
    const { detail } = this.state;
    detail.show = true;
    this.setState({
      ...detail
    });
  }

  _closeModal(event) {
    if (event) {
      event.preventDefault();
    }
    const { detail } = this.state;
    detail.show = false;
    this.setState({
      ...detail
    });
  }

  render() {
    const { installedModules, showLoading, resultCodes } = this.props;
    const { detail } = this.state;

    const _installedModules = [];
    if (installedModules) {
      installedModules.forEach(moduleDescriptor => {
        _installedModules.push(moduleDescriptor);
      });
    }
    _installedModules.sort((one, two) => {
      return one.id > two.id;
    });
    // TODO add filter, make modal separete
    const _resultCodes = [];
    if (resultCodes) {
      resultCodes.forEach(resultCode => {
        _resultCodes.push(resultCode);
      });
    }
    _resultCodes.sort((one, two) => {
      return parseInt(one.statusCode, 10) - parseInt(two.statusCode, 10);
    });
    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-deactivate" level="warning"/>
        <Basic.Confirm ref="confirm-activate" level="success"/>

        <Basic.Modal bsSize="large" show={detail.show} showLoading={detail.showLoading} onHide={this._closeModal.bind(this)}>
          <Basic.Modal.Header text={this.i18n('result-codes.title')} />
          <Basic.Modal.Body>
            <Basic.Table
              data={_resultCodes}
              noData={this.i18n('component.basic.Table.noData')}
              rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}>
              <Basic.Column property="statusCode" header={this.i18n('result-codes.status')}/>
              <Basic.Column property="statusEnum" header={this.i18n('result-codes.code')}/>
              <Basic.Column property="message" header={this.i18n('result-codes.message')}
                cell={
                  ({ rowIndex, data }) => {
                    const message = this.getFlashManager().convertFromResultModel(data[rowIndex]);
                    const key = 'flash-message-' + message.id;
                    return (
                      <Basic.FlashMessage
                        key={key}
                        message={message}
                        showDate={false} />);
                  }
                }/>
            </Basic.Table>
          </Basic.Modal.Body>
          <Basic.Modal.Footer>
            <Basic.Button level="link" onClick={this._closeModal.bind(this)}>{this.i18n('button.cancel')}</Basic.Button>
          </Basic.Modal.Footer>
        </Basic.Modal>
        <Basic.Table
          data={_installedModules}
          showLoading={showLoading}
          noData={this.i18n('component.basic.Table.noData')}
          rowClass={({rowIndex, data}) => { return Utils.Ui.getRowClass(data[rowIndex]); }}>

          <Basic.Column property="id" header={this.i18n('entity.Module.id')}/>
          <Basic.Column property="name" header={this.i18n('entity.Module.name')}/>
          <Basic.Column property="vendor" header={this.i18n('entity.Module.vendor')}/>
          <Basic.Column property="version" header={this.i18n('entity.Module.version')}/>
            <Basic.Column
              property="build"
              header={this.i18n('entity.Module.build')}
              cell={
                ({rowIndex, data}) => {
                  const moduleDescriptor = data[rowIndex];
                  if (moduleDescriptor.buildNumber === '@buildNumber@') {
                    // buildNumber doesn't be filled on development stage
                    // TODO: build with eclipse skips some maven plugins (e.g. buildNumber)
                    return null;
                  }
                  return (
                    <span>
                      <span title={ this.i18n('entity.Module.buildNumber') }>{ moduleDescriptor.buildNumber }</span>
                      <br />
                      <Advanced.DateValue
                        value={ parseInt(moduleDescriptor.buildTimestamp, 10) }
                        title={ this.i18n('entity.Module.buildTimestamp') }
                        showTime/>
                    </span>);
                }
              }/>
          <Basic.Column property="description" header={this.i18n('entity.Module.description')}/>
          <Basic.Column
            property="documentation"
            header={this.i18n('entity.Module.documentation')}
            width="250px"
            cell={
              /* eslint-disable react/no-multi-comp */
              ({rowIndex, data}) => {
                const moduleDescriptor = data[rowIndex];
                const links = [];
                //
                if (moduleDescriptor.documentationAvailable) {
                  links.push(
                    <Basic.Link
                      href={ `${ConfigLoader.getServerUrl().replace('/v1', '')}?group=${moduleDescriptor.id}` }
                      text="Api"
                      style={{ marginRight: 5 }}/>
                  );
                  links.push(
                    <Basic.Link
                      href={ `${ConfigLoader.getServerUrl().replace('/api/v1', '')}/webjars/${moduleDescriptor.id}/${moduleDescriptor.version}/doc/index.html` }
                      text="Html"
                      style={{ marginRight: 5 }}/>
                  );
                  links.push(
                    <Basic.Button
                      level="success"
                      className="btn-xs"
                      onClick={this.showResultCodes.bind(this, moduleDescriptor.id)}
                      text={ this.i18n('result-codes.button-show') }
                      style={{ marginRight: 5 }}/>
                  );
                }
                // javadoc
                return links;
              }
            }/>
          <Basic.Column
            property="disabled"
            header={<Basic.Cell className="column-face-bool">{this.i18n('entity.Module.disabled')}</Basic.Cell>}
            cell={<Basic.BooleanCell className="column-face-bool"/>}
            width="100px"
            rendered={false}/>
          <Basic.Column
            header={this.i18n('label.action')}
            className="action"
            cell={
              ({rowIndex, data}) => {
                if (!data[rowIndex].disabled) {
                  return (
                    <Basic.Button
                      level="warning"
                      onClick={this.onEnable.bind(this, data[rowIndex], false)}
                      className="btn-xs"
                      title={this.i18n('button.deactivate')}
                      titlePlacement="bottom"
                      rendered={data[rowIndex].disableable}>
                      {this.i18n('button.deactivate')}
                    </Basic.Button>
                  );
                }
                return (
                  <Basic.Button
                    level="success"
                    onClick={this.onEnable.bind(this, data[rowIndex], true)}
                    className="btn-xs"
                    title={this.i18n('button.activate')}
                    titlePlacement="bottom">
                    {this.i18n('button.activate')}
                  </Basic.Button>
                );
              }
            }
            rendered={SecurityManager.hasAuthority('MODULE_UPDATE')}/>
        </Basic.Table>
      </div>
    );
  }
}

BackendModules.propTypes = {
  userContext: PropTypes.object,
  installedModules: PropTypes.object,
  showLoading: PropTypes.bool,
  resultCodes: PropTypes.object
};
BackendModules.defaultProps = {
  userContext: null,
  installedModules: null,
  showLoading: true,
  resultCodes: null
};

function select(state) {
  return {
    userContext: state.security.userContext,
    installedModules: DataManager.getData(state, BackendModuleManager.UI_KEY_MODULES),
    showLoading: Utils.Ui.isShowLoading(state, BackendModuleManager.UI_KEY_MODULES),
    resultCodes: DataManager.getData(state, BackendModuleManager.UI_KEY_RESULT_CODES)
  };
}

export default connect(select)(BackendModules);
