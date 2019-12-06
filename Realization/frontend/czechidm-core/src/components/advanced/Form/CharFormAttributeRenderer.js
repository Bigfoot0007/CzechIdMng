import Joi from 'joi';
//
import TextFormAttributeRenderer from './TextFormAttributeRenderer';

/**
 * Char form value component
 * - supports multiple and confidential attributes
 *
 * @author Radek Tomiška
 */
export default class CharFormAttributeRenderer extends TextFormAttributeRenderer {

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    let validation = Joi.string().max(1);
    if (!this.isRequired()) {
      validation = validation.concat(Joi.string().allow(null).allow(''));
    }
    return validation;
  }
}
