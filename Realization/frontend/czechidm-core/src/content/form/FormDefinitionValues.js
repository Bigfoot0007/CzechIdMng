import React from 'react';
import * as Basic from '../../components/basic';
import FormValueTable from './FormValueTable';
import SearchParameters from '../../domain/SearchParameters';

/**
 * Form values - for given form definition
 *
 * @author Roman Kučera
 * @author Radek Tomiška
 */
export default class FormValues extends Basic.AbstractContent {

  getContentKey() {
    return 'content.form-values';
  }

  getNavigationKey() {
    return 'form-definition-values';
  }

  render() {
    const forceSearchParameters = new SearchParameters().setFilter('definitionId', this.props.params.entityId);
    //
    return (
      <div className="tab-pane-table-body">
        { this.renderContentHeader({ style: { marginBottom: 0 }}) }

        <FormValueTable
          uiKey="form-definition-values-table"
          forceSearchParameters={ forceSearchParameters }
          className="no-margin" />
      </div>
    );
  }
}
