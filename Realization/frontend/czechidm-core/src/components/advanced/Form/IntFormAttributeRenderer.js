import Joi from 'joi';
//
import LongFormAttributeRenderer from './LongFormAttributeRenderer';

/**
 * Integer form value component
 * - supports multiple and confidential attributes
 * - TODO: validation for multiple attrs
 *
 * @author Radek Tomiška
 */
export default class IntFormAttributeRenderer extends LongFormAttributeRenderer {

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    const { attribute } = this.props;
    const min = attribute.min || -2147483648;
    const max = attribute.max || 2147483647;
    //
    let validation = Joi.number().integer().min(min).max(max);
    if (!this.isRequired()) {
      validation = validation.concat(Joi.number().allow(null));
    }
    return validation;
  }
}
