import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { IncompatibleRoleManager, RoleManager } from '../../redux';

let manager = new IncompatibleRoleManager();
let roleManager = new RoleManager();

/**
* Table of incompatible roles - defines Segregation of Duties.
*
* @author Radek Tomiška
* @since 9.4.0
*/
export class IncompatibleRoleTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.role.incompatible-roles';
  }

  getUiKey() {
    return this.props.uiKey;
  }

  getManager() {
    // Init manager - evaluates if we want to use standard (original) manager or
    // universal request manager (depends on existing of 'requestId' param)
    manager = this.getRequestManager(this.props.match.params, manager);
    roleManager = this.getRequestManager(this.props.match.params, roleManager);
    return manager;
  }

  showDetail(entity) {
    if (!Utils.Entity.isNew(entity)) {
      this.context.store.dispatch(this.getManager().fetchPermissions(entity.id, `${this.getUiKey()}-detail`));
    }
    //
    super.showDetail(entity, () => {
      this.refs.superior.focus();
    });
  }

  save(entity, event) {
    const formEntity = this.refs.form.getData();
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      this.addMessage({ level: 'success', message: this.i18n('save.success', { count: 1, record: this.getManager().getNiceLabel(entity) }) });
    }
    //
    super.afterSave(entity, error);
  }

  render() {
    const { uiKey, forceSearchParameters, _showLoading, _permissions, className, readOnly } = this.props;
    const { detail } = this.state;
    let superiorId = null;
    if (forceSearchParameters.getFilters().has('superiorId')) {
      superiorId = forceSearchParameters.getFilters().get('superiorId');
    }
    let subId = null;
    if (forceSearchParameters.getFilters().has('subId')) {
      subId = forceSearchParameters.getFilters().get('subId');
    }
    //
    return (
      <div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ manager }
          forceSearchParameters={ forceSearchParameters }
          showRowSelection={ manager.canDelete() && !readOnly }
          className={ className }
          _searchParameters={ this.getSearchParameters() }
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
            ]
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={ this.showDetail.bind(this, { sub: subId, superior: superiorId }) }
                rendered={ manager.canSave() && !readOnly }>
                <Basic.Icon type="fa" icon="plus"/>
                {' '}
                { this.i18n('button.add') }
              </Basic.Button>
            ]
          }>

          <Advanced.Column
            header=""
            className="detail-button"
            rendered={ !readOnly }
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={this.i18n('button.detail')}
                    onClick={this.showDetail.bind(this, data[rowIndex])}/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column
            property="superior"
            sortProperty="superior.name"
            face="text"
            header={ this.i18n('entity.IncompatibleRole.superior.label') }
            sort
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                return (
                  <Advanced.EntityInfo
                    entityType="role"
                    entityIdentifier={ entity.superior }
                    entity={ entity._embedded.superior }
                    face="popover"
                    showIcon/>
                );
              }
            }
            rendered={ subId !== null }/>
            <Advanced.Column
              property="sub"
              sortProperty="sub.name"
              face="text"
              header={ this.i18n('entity.IncompatibleRole.sub.label') }
              sort
              cell={
                ({ rowIndex, data }) => {
                  const entity = data[rowIndex];
                  return (
                    <Advanced.EntityInfo
                      entityType="role"
                      entityIdentifier={ entity.sub }
                      entity={ entity._embedded.sub }
                      face="popover"
                      showIcon/>
                  );
                }
              }
              rendered={ superiorId !== null }/>
        </Advanced.Table>

        <Basic.Modal
          bsSize="large"
          show={detail.show}
          onHide={this.closeDetail.bind(this)}
          backdrop="static"
          keyboard={!_showLoading}>

          <form onSubmit={this.save.bind(this, {})}>
            <Basic.Modal.Header closeButton={ !_showLoading } text={ this.i18n('create.header')} rendered={ Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Header closeButton={ !_showLoading } text={ this.i18n('edit.header', { name: manager.getNiceLabel(detail.entity) }) } rendered={ !Utils.Entity.isNew(detail.entity) }/>
            <Basic.Modal.Body>
              <Basic.AbstractForm
                ref="form"
                showLoading={ _showLoading }
                readOnly={ !manager.canSave(detail.entity, _permissions) }>

                <Basic.SelectBox
                  ref="superior"
                  manager={ roleManager }
                  label={ this.i18n('entity.IncompatibleRole.superior.label') }
                  helpBlock={ this.i18n('entity.IncompatibleRole.superior.help') }
                  readOnly={ !Utils.Entity.isNew(detail.entity) || superiorId !== null }
                  required/>
                <Basic.SelectBox
                  ref="sub"
                  manager={ roleManager }
                  label={ this.i18n('entity.IncompatibleRole.sub.label') }
                  helpBlock={ this.i18n('entity.IncompatibleRole.sub.help') }
                  readOnly={ !Utils.Entity.isNew(detail.entity) || subId !== null }
                  required/>
              </Basic.AbstractForm>
            </Basic.Modal.Body>

            <Basic.Modal.Footer>
              <Basic.Button
                level="link"
                onClick={ this.closeDetail.bind(this) }
                showLoading={ _showLoading }>
                { this.i18n('button.close') }
              </Basic.Button>
              <Basic.Button
                type="submit"
                level="success"
                rendered={ manager.canSave(detail.entity, _permissions) && Utils.Entity.isNew(detail.entity) }
                showLoading={ _showLoading}
                showLoadingIcon
                showLoadingText={ this.i18n('button.saving') }>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.Modal.Footer>
          </form>
        </Basic.Modal>
      </div>
    );
  }
}

IncompatibleRoleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  /**
   * "Hard filters"
   */
  forceSearchParameters: PropTypes.object,
  //
  _showLoading: PropTypes.bool,
  /**
   * Read only table
   */
  readOnly: PropTypes.bool
};

IncompatibleRoleTable.defaultProps = {
  forceSearchParameters: null,
  _showLoading: false,
  readOnly: false
};

function select(state, component) {
  return {
    _showLoading: Utils.Ui.isShowLoading(state, `${component.uiKey}-detail`),
    _permissions: Utils.Permission.getPermissions(state, `${component.uiKey}-detail`),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(IncompatibleRoleTable);
