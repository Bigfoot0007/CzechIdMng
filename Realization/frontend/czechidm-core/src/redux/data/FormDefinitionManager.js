import EntityManager from './EntityManager';
import { FormDefinitionService } from '../../services';
import DataManager from './DataManager';
import * as Utils from '../../utils';

export default class FormDefinitionManager extends EntityManager {

  constructor() {
    super();
    this.service = new FormDefinitionService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'FormDefinition';
  }

  getCollectionType() {
    return 'formDefinitions';
  }

  /**
   * Extended nice label.
   *
   * @param  {entity} entity
   * @param  {boolean} showType show form definition type
   * @return {string}
   * @since 10.2.0
   */
  getNiceLabel(entity, showType = true) {
    return this.getService().getNiceLabel(entity, showType);
  }

  /**
   * Return search paramaters for endpoind with information about form definition types.
   */
  getDefinitionTypesSearchParameters() {
    return this.getService().getDefinitionTypesSearchParameters();
  }

  fetchTypes(uiKey = null, cb = null) {
    return (dispatch) => {
      dispatch(this.requestEntities(null, uiKey));
      this.getService().getTypes()
        .then(json => {
          if (cb) {
            cb(json, null);
          }
          dispatch(this.dataManager.receiveData(uiKey, json));
        })
        .catch(error => {
          dispatch(this.receiveError({}, uiKey, error, cb));
        });
    };
  }

  getLocalization(formDefinition, property, defaultValue = null) {
    if (!formDefinition) {
      return defaultValue;
    }
    const key = `${ FormDefinitionManager.getLocalizationPrefix(formDefinition, false) }.${ property }`;
    const keyWithModule = `${ FormDefinitionManager.getLocalizationPrefix(formDefinition, true) }.${ property }`;
    const localizeMessage = this.i18n(keyWithModule);
    //
    // if localized message is exactly same as key, that means message isn't localized
    if (key === null || key === localizeMessage || keyWithModule === localizeMessage) {
      return defaultValue;
    }
    return localizeMessage;
  }

  /**
   * Returns prefix for localization
   *
   * @param  {object} formDefinition
   * @return {string}
   */
  static getLocalizationPrefix(formDefinition, withModule = true) {
    if (!formDefinition) {
      return undefined;
    }
    const formType = Utils.Ui.spinalCase(formDefinition.type);
    const formCode = Utils.Ui.spinalCase(formDefinition.code);
    //
    return `${withModule && formDefinition.module ? formDefinition.module + ':' : ''}eav.${formType}.${formCode}`;
  }
}
