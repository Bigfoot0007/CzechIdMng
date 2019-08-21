import React from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router';
import classnames from 'classnames';
//
import * as Basic from '../../basic';

/**
 * Single navigation item
 *
 * @author Radek Tomiška
 */
export default class NavigationItem extends Basic.AbstractContextComponent {

  render() {
    const { id, className, to, icon, iconColor, active, title, titlePlacement, text, rendered, showLoading } = this.props;
    const itemClassNames = classnames(className, { active });
    const linkClassNames = classnames({ active });
    //
    if (!rendered) {
      return null;
    }

    if (!to) {
      this.getLogger().error(`[Advanced.NavigationItem] item [${id}] in module descriptor has to be repaired. Target link is undefined and will be hidden.`);
      return null;
    }
    // icon resolving
    let iconContent = null;
    let _icon = ( icon === undefined || icon === null ? 'fa:circle-o' : icon );
    if (showLoading) {
      _icon = 'refresh';
    }
    if (_icon) {
      iconContent = (
        <Basic.Icon icon={ _icon } color={ iconColor } showLoading={ showLoading }/>
      );
    }
    //
    return (
      <li className={itemClassNames}>
        <Basic.Tooltip id={`${id}-tooltip`} placement={titlePlacement} value={title}>
          {
            <Link to={to} className={linkClassNames}>
              { iconContent }
              <span className="item-text">{ text }</span>
            </Link>
          }
        </Basic.Tooltip>
      </li>
    );
  }
}

NavigationItem.propTypes = {
  ...Basic.AbstractComponent.propTypes,
  id: PropTypes.string,
  to: PropTypes.string,
  title: PropTypes.string,
  icon: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.bool // false => no icon
  ]),
  active: PropTypes.bool,
  text: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.node,
    PropTypes.arrayOf(PropTypes.object)
  ])
};

NavigationItem.defaultProps = {
  ...Basic.AbstractComponent.defaultProps,
  active: false,
  icon: null,
  text: null
};
