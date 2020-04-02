import AbstractRequestService from './AbstractRequestService';
import SearchParameters from '../domain/SearchParameters';
import RoleService from './RoleService';
import {i18n} from './LocalizationService';

const roleService = new RoleService();

/**
 * Incompatible role - defines Segregation of Duties.
 *
 * @author Radek Tomiška
 * @since 9.4.0
 */
export default class IncompatibleRoleService extends AbstractRequestService {

  /**
   * Using in the request
   */
  getSubApiPath() {
    return '/incompatible-roles';
  }

  getNiceLabel(entity) {
    if (!entity || !entity._embedded || !entity._embedded.superior || entity._embedded.sub) {
      return i18n('entity.IncompatibleRole._type');
    }
    let label = `${roleService.getNiceLabel(entity._embedded.superior)}`;
    label += ` - ${roleService.getNiceLabel(entity._embedded.sub)}`;
    //
    return label;
  }

  supportsPatch() {
    if (this.isRequestModeEnabled()) {
      return false;
    }
    return true;
  }

  getGroupPermission() {
    return 'INCOMPATIBLEROLE';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('superior.name', 'asc').setSort('sub.name', 'asc');
  }
}
