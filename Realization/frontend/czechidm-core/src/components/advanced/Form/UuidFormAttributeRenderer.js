import Joi from 'joi';
//
import TextFormAttributeRenderer from './TextFormAttributeRenderer';

/**
 * Uuid form value component
 * - supports multiple and confidential attributes
 *
 * @author Radek Tomiška
 */
export default class UuidFormAttributeRenderer extends TextFormAttributeRenderer {

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    let validation = Joi.string().guid();
    if (!this.isRequired()) {
      validation = validation.concat(Joi.string().guid().allow(null).allow(''));
    }
    return validation;
  }

  /**
   * Fill form value field by persistent type from input value
   *
   * @param  {FormValue} formValue - form value
   * @param  {object} formComponent value
   * @return {FormValue}
   */
  fillFormValue(formValue, rawValue) {
    formValue.uuidValue = rawValue;
    // common value can be used without persistent type knowlege (e.g. conversion to properties object)
    formValue.value = formValue.uuidValue;
    // TODO: validations for uuid
    return formValue;
  }

  /**
   * Returns value to ipnut from given (persisted) form value
   *
   * @param  {FormValue} formValue
   * @return {object} value by persistent type
   */
  getInputValue(formValue) {
    return formValue.uuidValue ? formValue.uuidValue : formValue.value;
  }
}
