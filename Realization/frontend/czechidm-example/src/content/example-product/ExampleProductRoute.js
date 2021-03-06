import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
//
import { Basic, Advanced } from 'czechidm-core';
import { ExampleProductManager } from '../../redux';

const manager = new ExampleProductManager();

/**
 * ExampleProduct detail with tabs
 *
 * @author Radek Tomiška
 */
class ExampleProductRoute extends Basic.AbstractContent {

  componentDidMount() {
    const { entityId } = this.props.match.params;
    // load entity from BE - for nice labels etc.
    this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId));
  }

  render() {
    const { entity, showLoading } = this.props;
    //
    return (
      <Basic.Div>
        <Advanced.DetailHeader
          showLoading={ showLoading }
          icon="link"
          entity={ entity }
          back="/example/products">
          { this.i18n('example:content.example-product.detail.edit.header', { name: manager.getNiceLabel(entity), escape: false }) }
        </Advanced.DetailHeader>

        <Advanced.TabPanel parentId="example-products" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}

ExampleProductRoute.propTypes = {
  entity: PropTypes.instanceOf(PropTypes.object),
  showLoading: PropTypes.bool
};
ExampleProductRoute.defaultProps = {
  entity: null,
  showLoading: false
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(ExampleProductRoute);
