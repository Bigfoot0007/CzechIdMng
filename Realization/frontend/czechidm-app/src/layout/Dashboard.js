import React, { PropTypes } from 'react';
import { connect } from 'react-redux';
//
import { Basic, ComponentService } from 'czechidm-core';
import IdentityDashboard from 'czechidm-core/src/content/identity/IdentityDashboard';

const DEFAULT_SPAN = 6;

/**
 * Identity dashdoard - logged user dashboard
 *
 * @author Radek Tomiška
 */
class Dashboard extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
    };
    this.componentService = new ComponentService();
  }

  componentDidMount() {
    this.selectNavigationItem('home');
  }

  render() {
    const { userContext } = this.props;
    //
    /* TODO: remove after identity dashboards registration will be implemented
    const dashboards = [];
    const rowKeys = [];
    let rowDashboards = [];
    let spanCounter = 0;
    this.componentService.getComponentDefinitions(ComponentService.DASHBOARD_COMPONENT_TYPE).forEach(component=> {
      const DashboardComponent = component.component;
      const _span = component.span ? component.span : DEFAULT_SPAN;
      const spanDecorator = (
        <div key={`${ComponentService.DASHBOARD_COMPONENT_TYPE}-${component.id}`} className={`col-lg-${_span} ${component.offset ? `col-lg-offset-${component.offset}` : ''}`}>
          <DashboardComponent entityId={userContext.username}/>
        </div>
      );

      rowKeys.push(component.id);
      rowDashboards.push(spanDecorator);
      spanCounter = spanCounter + _span;
      if (spanCounter > 12) {
        spanCounter = 0;
        dashboards.push(
          <Basic.Row key={`${ComponentService.DASHBOARD_COMPONENT_TYPE}-row-${rowKeys.join('-')}`}>
            {rowDashboards}
          </Basic.Row>
        );
        rowDashboards = [];
      }
    });
    if (rowDashboards.length > 0) {
      dashboards.push(
        <Basic.Row>
          {rowDashboards}
        </Basic.Row>
      );
    }*/
    //
    return (
      <IdentityDashboard params={{ ...this.props.params, entityId: userContext.username }}/>
    );
  }
}

Dashboard.propTypes = {
  userContext: PropTypes.object
};
Dashboard.defaultProps = {
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(Dashboard);
