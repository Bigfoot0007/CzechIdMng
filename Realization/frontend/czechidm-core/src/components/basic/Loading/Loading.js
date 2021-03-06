import React from 'react';
import PropTypes from 'prop-types';
import ReactDOM from 'react-dom';
import classNames from 'classnames';
//
import AbstractComponent from '../AbstractComponent/AbstractComponent';

/**
 * Loading indicator.
 *
 * Lookout: prevent to use Basic.Div inside => cicrular reference.
 *
 * @author Radek Tomiška
 */
class Loading extends AbstractComponent {

  _showLoading() {
    const { showLoading, show } = this.props;
    //
    return showLoading || show;
  }

  _resize() {
    const showLoading = this._showLoading();
    if (!showLoading) {
      return;
    }
    if (typeof $ !== 'undefined') {
      const panel = $(ReactDOM.findDOMNode(this.refs.container));
      const loading = panel.find('.loading');
      if (loading.hasClass('global') || loading.hasClass('static')) {
        // we don't want resize loading container
        return;
      }
      // TODO: offset, scroll
      loading.css({
        top: panel.position().top, // TODO: check, if panel contains top header and calculate with header height (now 50 hardcoded)
        left: panel.position().left,
        width: panel.width(),
        height: panel.height()
      });
    }
  }

  componentDidMount() {
    this._resize();
    // window.addEventListener('resize', this._resize);
  }

  componentDidUpdate() {
    this._resize();
    // window.removeEventListener('resize', this._resize);
  }

  render() {
    const { rendered, className, containerClassName, showAnimation, isStatic, loadingTitle, style, containerTitle } = this.props;
    if (!rendered) {
      return null;
    }
    const showLoading = this._showLoading();
    //
    // Loading is used as standard div => wee need to render css even if loading is not active
    const _containerClassNames = classNames(
      'loader-container',
      containerClassName
    );
    const loaderClassNames = classNames(
      className,
      'loading',
      { hidden: !showLoading },
      { static: isStatic }
    );
    return (
      <div ref="container" className={ _containerClassNames } style={ style } title={ containerTitle }>
        {
          showLoading
          ?
          <div className={ loaderClassNames }>
            <div className="loading-wave-top" />
            {
              showAnimation
              ?
              <div className="loading-wave-container" title={ loadingTitle }>
                <div className="loading-wave">
                  <div/><div/><div/><div/><div/>
                </div>
              </div>
              :
              null
            }
            <div className="title hidden">{ loadingTitle }</div>
          </div>
          :
          null
        }
        { this.props.children }
      </div>
    );
  }
}

Loading.propTypes = {
  ...AbstractComponent.propTypes,
  /**
   * Shows loading overlay (showLoadin alias)
   */
  show: PropTypes.bool,
  /**
   * when loading is visible, then show animation too
   */
  showAnimation: PropTypes.bool,
  /**
   * static loading without overlay
   */
  isStatic: PropTypes.bool,
  /**
   * Loading title
   */
  loadingTitle: PropTypes.string,
  /**
   * Title - static container (div wrapper).
   */
  containerTitle: PropTypes.string,
  /**
   * Css - static container (div wrapper).
   */
  containerClassName: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.func
  ])
};
Loading.defaultProps = {
  ...AbstractComponent.defaultProps,
  show: false,
  showAnimation: true,
  isStatic: false,
  loadingTitle: 'Zpracovávám ...' // TODO: localization or undefined ?
};

export default Loading;
