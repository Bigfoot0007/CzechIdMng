import React from 'react';
import _ from 'lodash';
//
import { Basic, Domain } from 'czechidm-core';
import RoleSystemTableComponent, { RoleSystemTable } from '../role/RoleSystemTable';

const uiKey = 'system-roles-table';

/**
 * Table to display roles, assigned to system
 *
 * @author Petr Hanák
 * @author Radek Tomiška
 */
export default class SystemRoles extends Basic.AbstractContent {

  getUiKey() {
    return uiKey;
  }

  getContentKey() {
    return 'acc:content.system.roles';
  }

  getNavigationKey() {
    return 'system-roles';
  }

  render() {
    const { entityId } = this.props.match.params;
    const forceSearchParameters = new Domain.SearchParameters().setFilter('systemId', entityId);
    //
    return (
      <div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <Basic.Panel className="no-border last">
          <RoleSystemTableComponent
            columns={ _.difference(RoleSystemTable.defaultProps.columns, ['system']) }
            showRowSelection
            uiKey={ `${this.getUiKey()}-${entityId}` }
            forceSearchParameters={ forceSearchParameters }
            menu="system"
            match={ this.props.match }
            className="no-margin"/>
        </Basic.Panel>
      </div>
    );
  }
}
