import React from 'react';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import SearchParameters from '../../domain/SearchParameters';
import RoleGuaranteeRoleTable from './RoleGuaranteeRoleTable';
import RoleGuaranteeTable from './RoleGuaranteeTable';
import { SecurityManager } from '../../redux';

/**
 * Role guarantees - by role only now
 *
 * @author Radek Tomiška
 */
export default class RoleGuarantees extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.role.guarantees';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('role-guarantees', this.props.params);
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('role', this.props.params.entityId);
    //
    return (
      <div>
        <Helmet title={ this.i18n('title') } />
        {
          !SecurityManager.hasAuthority('ROLEGUARANTEEROLE_READ')
          ||
          <div>
            <Basic.ContentHeader icon="fa:key" text={ this.i18n('role.header') } style={{ marginBottom: 0 }}/>
            <RoleGuaranteeRoleTable
              uiKey="role-guarantee-role-table"
              forceSearchParameters={ forceSearchParameters }
              className="no-margin"
              params={ this.props.params }/>
          </div>
        }
        {
          !SecurityManager.hasAuthority('ROLEGUARANTEE_READ')
          ||
          <div>
            <Basic.ContentHeader icon="fa:group" text={ this.i18n('identity.header') } style={{ marginBottom: 0 }}/>
            <RoleGuaranteeTable
              uiKey="role-guarantee-table"
              forceSearchParameters={ forceSearchParameters }
              className="no-margin"
              params={ this.props.params }/>
          </div>
        }
      </div>
    );
  }
}
