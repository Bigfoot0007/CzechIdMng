import React from 'react';
import { connect } from 'react-redux';
import * as Basic from '../../components/basic';
import AuditTable from './AuditTable';
import Helmet from 'react-helmet';

/**
 *
 * @author Ondřej Kopr
 */
class AuditContent extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.audit';
  }

  getNavigationKey() {
    return 'audit-entities';
  }

  render() {
    return (
      <Basic.Div>
        <Helmet title={this.i18n('title')} />
        <AuditTable uiKey="audit-table"/>
      </Basic.Div>
    );
  }
}

AuditContent.propTypes = {
};

AuditContent.defaultProps = {
};

function select() {
  return {
  };
}

export default connect(select)(AuditContent);
