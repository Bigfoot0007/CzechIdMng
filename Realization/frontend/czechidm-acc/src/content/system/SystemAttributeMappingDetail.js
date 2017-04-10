import React, { PropTypes } from 'react';
import Helmet from 'react-helmet';
import { connect } from 'react-redux';
//
import { Basic, Advanced, Utils, Domain } from 'czechidm-core';
import { SystemMappingManager, SystemAttributeMappingManager, SchemaAttributeManager} from '../../redux';
import AttributeMappingStrategyTypeEnum from '../../domain/AttributeMappingStrategyTypeEnum';
import SystemEntityTypeEnum from '../../domain/SystemEntityTypeEnum';

const uiKey = 'system-attribute-mapping';
const manager = new SystemAttributeMappingManager();
const systemMappingManager = new SystemMappingManager();
const schemaAttributeManager = new SchemaAttributeManager();

class SystemAttributeMappingDetail extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
  }

  getManager() {
    return manager;
  }

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.attributeMappingDetail';
  }

  componentWillReceiveProps(nextProps) {
    const {_attribute} = nextProps;
    const { attributeId} = nextProps.params;
    if (attributeId && attributeId !== this.props.params.attributeId) {
      this._initComponent(nextProps);
    }
    if (_attribute && _attribute !== this.props._attribute) {
      if (_attribute && this.refs.form) {
        this.setState({disabledAttribute: _attribute.disabledAttribute});
        this.setState({entityAttribute: _attribute.entityAttribute});
        this.setState({extendedAttribute: _attribute.extendedAttribute});
      }
    }
  }

  // Did mount only call initComponent method
  componentDidMount() {
    this._initComponent(this.props);
  }

  /**
   * Method for init component from didMount method and from willReceiveProps method
   * @param  {properties of component} props For didmount call is this.props for call from willReceiveProps is nextProps.
   */
  _initComponent(props) {
    const { attributeId} = props.params;
    if (this._getIsNew(props)) {
      this.setState({
        attribute: {
          systemMapping: props.location.query.mappingId,
          objectClassId: props.location.query.objectClassId,
          strategyType: AttributeMappingStrategyTypeEnum.findKeyBySymbol(AttributeMappingStrategyTypeEnum.SET)
        }
      });
    //  this.context.store.dispatch(systemMappingManager.fetchEntity(props.location.query.mappingId));
    } else {
      this.context.store.dispatch(this.getManager().fetchEntity(attributeId));
    }
    this.selectNavigationItems(['sys-systems', 'system-mappings']);
  }

  _getIsNew(nextProps) {
    const { query } = nextProps ? nextProps.location : this.props.location;
    return (query) ? query.new : null;
  }

  save(event) {
    const formEntity = this.refs.form.getData();
    formEntity.systemMapping = systemMappingManager.getSelfLink(formEntity.systemMapping);
    formEntity.schemaAttribute = schemaAttributeManager.getSelfLink(formEntity.schemaAttribute);
    //
    super.save(formEntity, event);
  }

  afterSave(entity, error) {
    if (!error) {
      if (this._getIsNew()) {
        this.addMessage({ message: this.i18n('create.success', { name: entity.name }) });
      } else {
        this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
      }
      this.context.router.goBack();
    } else {
      this.addError(error);
    }
    super.afterSave();
  }

  closeDetail() {
    this.refs.form.processEnded();
  }

  _checkboxChanged(key, uncheckKey, event) {
    const checked = event.currentTarget.checked;
    this.setState({[key]: checked}, ()=>{
      if (checked && uncheckKey !== null) {
        this.setState({[uncheckKey]: false});
        this.refs[uncheckKey].setState({value: false}, () => {
          this.forceUpdate();
        });
      }
      if (key === 'entityAttribute') {
        this.refs.idmPropertyName.setValue(null);
        this.refs.idmPropertyEnum.setValue(null);
      }
    });
  }

  _schemaAttributeChange(value) {
    if (!this.refs.name.getValue()) {
      this.refs.name.setValue(value.name);
    }
  }

  _onChangeEntityEnum(item) {
    const {_systemMapping} = this.props;
    if (item) {
      const field = SystemEntityTypeEnum.getEntityEnum(_systemMapping ? _systemMapping.entityType : 'IDENTITY').getField(item.value);
      this.refs.idmPropertyName.setValue(field);
    } else {
      this.refs.idmPropertyName.setValue(null);
    }
  }

  render() {
    const { _showLoading, _attribute, _systemMapping} = this.props;
    const { disabledAttribute, entityAttribute, extendedAttribute} = this.state;
    const isNew = this._getIsNew();
    const attribute = isNew ? this.state.attribute : _attribute;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('objectClassId',
     attribute && attribute.objectClassId ? attribute.objectClassId : Domain.SearchParameters.BLANK_UUID);
    const _isDisabled = disabledAttribute;
    const _isEntityAttribute = entityAttribute;
    const _isExtendedAttribute = extendedAttribute;
    const _showNoRepositoryAlert = (!_isExtendedAttribute && !_isEntityAttribute);
    const entityTypeEnum = SystemEntityTypeEnum.getEntityEnum(_systemMapping ? _systemMapping.entityType : 'IDENTITY');
    const _isRequiredIdmField = (_isEntityAttribute || _isExtendedAttribute) && !_isDisabled;

    return (
      <div>
        <Helmet title={this.i18n('title')} />
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.ContentHeader>
          <Basic.Icon value="list-alt"/>
          {' '}
          <span dangerouslySetInnerHTML={{ __html: this.i18n('header', attribute ? { name: attribute.idmPropertyName} : {})}}/>
        </Basic.ContentHeader>
        <form onSubmit={this.save.bind(this)}>
          <Basic.Panel className="no-border last">
            <Basic.AbstractForm ref="form" data={attribute} showLoading={_showLoading}>
              <Basic.Checkbox
                ref="disabledAttribute"
                onChange={this._checkboxChanged.bind(this, 'disabledAttribute', null)}
                tooltip={this.i18n('acc:entity.SystemAttributeMapping.disabledAttribute.tooltip')}
                label={this.i18n('acc:entity.SystemAttributeMapping.disabledAttribute.label')}/>
              <Basic.SelectBox
                ref="systemMapping"
                manager={systemMappingManager}
                label={this.i18n('acc:entity.SystemAttributeMapping.systemMapping')}
                readOnly
                required/>
              <Basic.SelectBox
                ref="schemaAttribute"
                manager={schemaAttributeManager}
                forceSearchParameters={forceSearchParameters}
                onChange={this._schemaAttributeChange.bind(this)}
                label={this.i18n('acc:entity.SystemAttributeMapping.schemaAttribute')}
                required/>
              <Basic.TextField
                ref="name"
                label={this.i18n('acc:entity.SystemAttributeMapping.name.label')}
                helpBlock={this.i18n('acc:entity.SystemAttributeMapping.name.help')}
                required
                max={255}/>
              <Basic.EnumSelectBox
                ref="strategyType"
                enum={AttributeMappingStrategyTypeEnum}
                label={this.i18n('acc:entity.SystemAttributeMapping.strategyType')}
                required/>
              <Basic.Checkbox
                ref="sendAlways"
                tooltip={this.i18n('acc:entity.SystemAttributeMapping.sendAlways.tooltip')}
                label={this.i18n('acc:entity.SystemAttributeMapping.sendAlways.label')}
                readOnly = {_isDisabled}/>
              <Basic.Checkbox
                ref="sendOnlyIfNotNull"
                tooltip={this.i18n('acc:entity.SystemAttributeMapping.sendOnlyIfNotNull.tooltip')}
                label={this.i18n('acc:entity.SystemAttributeMapping.sendOnlyIfNotNull.label')}
                readOnly = {_isDisabled}/>
              <Basic.Checkbox
                ref="uid"
                onChange={this._checkboxChanged.bind(this, 'uid', null)}
                tooltip={this.i18n('acc:entity.SystemAttributeMapping.uid.tooltip')}
                label={this.i18n('acc:entity.SystemAttributeMapping.uid.label')}
                readOnly = {_isDisabled}/>
              <Basic.Checkbox
                ref="entityAttribute"
                onChange={this._checkboxChanged.bind(this, 'entityAttribute', 'extendedAttribute')}
                label={this.i18n('acc:entity.SystemAttributeMapping.entityAttribute')}
                readOnly = {_isDisabled}/>
              <Basic.Checkbox
                ref="extendedAttribute"
                onChange={this._checkboxChanged.bind(this, 'extendedAttribute', 'entityAttribute')}
                label={this.i18n('acc:entity.SystemAttributeMapping.extendedAttribute')}
                readOnly = {_isDisabled}/>
              <Basic.Checkbox
                ref="confidentialAttribute"
                label={this.i18n('acc:entity.SystemAttributeMapping.confidentialAttribute')}
                readOnly = {_isDisabled || !_isRequiredIdmField}/>
              <Basic.Checkbox
                ref="authenticationAttribute"
                label={this.i18n('acc:entity.SystemAttributeMapping.authenticationAttribute.label')}
                helpBlock={this.i18n('acc:entity.SystemAttributeMapping.authenticationAttribute.help')}/>
              <Basic.Row>
                <div className="col-lg-6">
                  <Basic.EnumSelectBox
                    ref="idmPropertyEnum"
                    readOnly = {_isDisabled || !_isEntityAttribute}
                    enum={entityTypeEnum}
                    onChange={this._onChangeEntityEnum.bind(this)}
                    label={this.i18n('acc:entity.SystemAttributeMapping.idmPropertyEnum')}
                    />
                </div>
                <div className="col-lg-6">
                  <Basic.TextField
                    ref="idmPropertyName"
                    readOnly = {_isDisabled || !_isRequiredIdmField || _isEntityAttribute}
                    label={this.i18n('acc:entity.SystemAttributeMapping.idmPropertyName.label')}
                    helpBlock={this.i18n('acc:entity.SystemAttributeMapping.idmPropertyName.help')}
                    required = {_isRequiredIdmField}
                    max={255}/>
                </div>
              </Basic.Row>
              <Basic.LabelWrapper label=" ">
                <Basic.Alert
                   rendered={_showNoRepositoryAlert}
                   key="no-repository-alert"
                   icon="exclamation-sign"
                   className="no-margin"
                   text={this.i18n('alertNoRepository')}/>
              </Basic.LabelWrapper>
              <Basic.ScriptArea
                ref="transformFromResourceScript"
                helpBlock={this.i18n('acc:entity.SystemAttributeMapping.transformFromResourceScript.help')}
                readOnly = {_isDisabled}
                label={this.i18n('acc:entity.SystemAttributeMapping.transformFromResourceScript.label')}/>
              <Basic.ScriptArea
                ref="transformToResourceScript"
                helpBlock={this.i18n('acc:entity.SystemAttributeMapping.transformToResourceScript.help')}
                readOnly = {_isDisabled}
                label={this.i18n('acc:entity.SystemAttributeMapping.transformToResourceScript.label')}/>
            </Basic.AbstractForm>
            <Basic.PanelFooter>
              <Basic.Button type="button" level="link"
                onClick={this.context.router.goBack}
                showLoading={_showLoading}>
                {this.i18n('button.back')}
              </Basic.Button>
              <Basic.Button
                onClick={this.save.bind(this)}
                level="success"
                type="submit"
                showLoading={_showLoading}>
                {this.i18n('button.save')}
              </Basic.Button>
            </Basic.PanelFooter>
          </Basic.Panel>
        </form>
      </div>
    );
  }
}

SystemAttributeMappingDetail.propTypes = {
  _showLoading: PropTypes.bool,
};
SystemAttributeMappingDetail.defaultProps = {
  _showLoading: false,
};

function select(state, component) {
  const entity = Utils.Entity.getEntity(state, manager.getEntityType(), component.params.attributeId);
  let systemMapping = null;
  if (component && component.location && component.location.query.new) {
    systemMapping = Utils.Entity.getEntity(state, systemMappingManager.getEntityType(), component.location.query.mappingId);
  }
  if (entity) {
    systemMapping = entity._embedded && entity._embedded.systemMapping ? entity._embedded.systemMapping : null;
    const schemaAttribute = entity._embedded && entity._embedded.schemaAttribute ? entity._embedded.schemaAttribute : null;
    entity.systemMapping = systemMapping;
    entity.schemaAttribute = schemaAttribute;
    entity.objectClassId = systemMapping ? systemMapping.objectClass.id : Domain.SearchParameters.BLANK_UUID;
    entity.idmPropertyEnum = SystemEntityTypeEnum.getEntityEnum(systemMapping ? systemMapping.entityType : 'IDENTITY').getEnum(entity.idmPropertyName);
  }
  return {
    _attribute: entity,
    _showLoading: Utils.Ui.isShowLoading(state, `${uiKey}-detail`),
    _systemMapping: systemMapping
  };
}

export default connect(select)(SystemAttributeMappingDetail);
