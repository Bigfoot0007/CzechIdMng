import { Managers } from 'czechidm-core';
import { AccountService } from '../services';

const service = new AccountService();

/**
 * Accounts.
 *
 * @author Radek Tomiška
 */
export default class AccountManager extends Managers.EntityManager {

  getService() {
    return service;
  }

  getEntityType() {
    return 'Account'; // TODO: constant or enumeration
  }

  getCollectionType() {
    return 'accounts';
  }
}
