import React, { PropTypes } from 'react';
//
import * as Basic from '../../../components/basic';
import * as Utils from '../../../utils';
import { TreeTypeManager, SecurityManager, TreeNodeManager } from '../../../redux';

/**
 * Type detail content
 */
export default class TypeDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.treeTypeManager = new TreeTypeManager();
    this.treeNodeManager = new TreeNodeManager();
    this.state = {
      showLoading: false
    };
  }

  getContentKey() {
    return 'content.tree.types';
  }

  componentDidMount() {
    const { entity } = this.props;
    this.selectNavigationItem('tree-types');

    if (entity !== undefined) {
      const data = {
        ...entity,
      };
      data.defaultTreeNode = (entity._embedded && entity._embedded.defaultTreeNode) ? entity._embedded.defaultTreeNode : entity.defaultTreeNode;
      //
      this.refs.form.setData(data);
      this.refs.code.focus();
    }
  }

  save(event) {
    const { uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      showLoading: true
    }, this.refs.form.processStarted());

    const entity = this.refs.form.getData();
    // transform defaultTreeNode
    if (entity.defaultTreeNode) {
      entity.defaultTreeNode = this.treeNodeManager.getSelfLink(entity.defaultTreeNode);
    }
    //
    if (entity.id === undefined) {
      this.context.store.dispatch(this.treeTypeManager.createEntity(entity, `${uiKey}-detail`, (createdEntity, error) => {
        this._afterSave(createdEntity, error);
      }));
    } else {
      this.context.store.dispatch(this.treeTypeManager.patchEntity(entity, `${uiKey}-detail`, this._afterSave.bind(this)));
    }
  }

  _afterSave(entity, error) {
    if (error) {
      this.setState({
        showLoading: false
      }, this.refs.form.processEnded());
      this.addError(error);
      return;
    }
    this.addMessage({ message: this.i18n('save.success', { name: entity.name }) });
    this.context.router.goBack();
  }

  closeDetail() {
  }

  render() {
    const { uiKey, entity } = this.props;
    const { showLoading } = this.state;
    //
    return (
      <div>
        <form onSubmit={this.save.bind(this)} >
          <Basic.AbstractForm
            ref="form"
            uiKey={uiKey}
            readOnly={ !SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'TREETYPE_CREATE' : 'TREETYPE_UPDATE') }
            style={{ padding: '15px 15px 0px 15px' }} >
            <Basic.Row>
              <div className="col-lg-2">
                <Basic.TextField
                  ref="code"
                  label={this.i18n('entity.TreeType.code')}
                  required
                  max={255}/>
              </div>
              <div className="col-lg-10">
                <Basic.TextField
                  ref="name"
                  label={this.i18n('entity.TreeType.name')}
                  required
                  min={0}
                  max={255}/>
              </div>
            </Basic.Row>
          </Basic.AbstractForm>

          <Basic.PanelFooter showLoading={showLoading} >
            <Basic.Button type="button" level="link" onClick={this.context.router.goBack}>{this.i18n('button.back')}</Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoadingIcon
              showLoadingText={this.i18n('button.saving')}
              rendered={SecurityManager.hasAuthority(Utils.Entity.isNew(entity) ? 'TREETYPE_CREATE' : 'TREETYPE_UPDATE')}>
              {this.i18n('button.save')}
            </Basic.Button>
          </Basic.PanelFooter>
        </form>
      </div>
    );
  }
}

TypeDetail.propTypes = {
  entity: PropTypes.object,
  uiKey: PropTypes.string.isRequired,
};
TypeDetail.defaultProps = {
};
