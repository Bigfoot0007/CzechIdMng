import React from 'react';
import { connect } from 'react-redux';
//
import { Utils} from 'czechidm-core';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import MonitoringStateEnum from '../../enums/MonitoringStateEnum';
import { ConfigurationManager } from '../../redux';

const uiKey = 'monitoring_table';
const manager = new ConfigurationManager();

/**
 * @Beta
 * Monitoring
 *
 * @author Vít Švanda
 */
class Monitorings extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: true,
      detail: {
        show: false,
        entity: {}
      }
    };
  }

  componentDidMount() {
    super.componentDidMount();
    this.initData();
    this.selectNavigationItem('monitoring');
  }

  initData() {
    manager.getService().getMonitoringType('monitoring-database')
      .then(json => {
        this.setState({monitoringDatabase: json});
      })
      .catch(error => {
        this.addError(error);
      });
    manager.getService().getMonitoringType('monitoring-sync')
      .then(json => {
        this.setState({monitoringSync: json});
      })
      .catch(error => {
        this.addError(error);
      });
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'content.monitoring';
  }

  /**
  * Method get last string of split string by dot.
  * Used for get niceLabel for type entity.
  */
  _getType(name) {
    return Utils.Ui.getSimpleJavaType(name);
  }

  _renderType(monitoringType) {
    return (
      <Basic.Div rendered={!!monitoringType}>
        <Basic.ContentHeader>
          { this.i18n(`${monitoringType ? monitoringType.module : null}:content.monitoring.type.${monitoringType ? monitoringType.type : null}.name`,
            { escape: false }) }
        </Basic.ContentHeader>

        <Basic.Panel>
          <Basic.Table
            data={ monitoringType ? monitoringType.results : null }
            noData={ this.i18n('component.basic.Table.noData') }>
            <Basic.Column
              property="level"
              header={ this.i18n('entity.IdmMonitoringType.level') }
              cell={
                ({ rowIndex, data }) => {
                  const entity = data[rowIndex];
                  return (
                    <Basic.EnumValue
                      style={{padding: '.2em .2em .3em .4em'}}
                      value={entity.level}
                      enum={ MonitoringStateEnum }/>
                  );
                }
              }
            />
            <Basic.Column
              property="name"
              header={ this.i18n('entity.IdmMonitoringType.name') }
              cell={
                /* eslint-disable react/no-multi-comp */
                ({ rowIndex, data }) => {
                  const value = data[rowIndex];
                  if (!value) {
                    return null;
                  }
                  if (!value.dto) {
                    return value.name;
                  }
                  return (
                    <Advanced.EntityInfo
                      entityType={ this._getType(value.dtoType) }
                      entityIdentifier={ value.dto ? value.dto.id : null}
                      face="popover"
                      entity={ value.dto }
                      showEntityType
                      showIcon/>
                  );
                }
              }/>
            <Basic.Column property="value" header={ this.i18n('entity.IdmMonitoringType.value') }/>
            <Basic.Column property="threshold" header={ this.i18n('entity.IdmMonitoringType.threshold') }/>
            <Basic.Column property="module" header={ this.i18n('entity.IdmMonitoringType.module') }/>
          </Basic.Table>
        </Basic.Panel>
      </Basic.Div>
    );
  }

  render() {
    //
    const {
      monitoringDatabase,
      monitoringSync
    } = this.state;
    //
    return (
      <Basic.Div>
        { this.renderPageHeader({ icon: 'fa:heartbeat' }) }
        <Basic.Toolbar>
          <div className="pull-right">
            <Advanced.RefreshButton
              onClick={this.initData.bind(this)}
              title={this.i18n('button.refresh')}/>
          </div>
        </Basic.Toolbar>
        {this._renderType(monitoringDatabase)}
        {this._renderType(monitoringSync)}
      </Basic.Div>
    );
  }
}

function select(state) {
  return {
    i18nReady: state.config.get('i18nReady')
  };
}

export default connect(select)(Monitorings);
