import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import ResponseUtils from '../utils/ResponseUtils';

/**
 * Service for universal requests
 *
 * @extends AbstractService
 * @author Vít Švanda
 */
class RequestService extends AbstractService {

  getApiPath() {
    return '/requests';
  }

  getNiceLabel(request) {
    if (!request) {
      return '';
    }
    if (request._embedded && request._embedded.role) {
      return `${request._embedded.role.name} (${request.state})`;
    }
    return request.id;
  }

  supportsAuthorization() {
    return true;
  }

  getGroupPermission() {
    return 'REQUEST';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('created', 'desc');
  }

  createRequest(endpoint, entity) {
    return RestApiService
      .post(`${ this.getApiPath() }/${ endpoint }`, entity)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (ResponseUtils.hasError(json)) {
          throw ResponseUtils.getFirstError(json);
        }
        return json;
      });
  }

  startRequest(idRequest) {
    return RestApiService
      .put(`${ this.getApiPath() }/${ idRequest }/start`, null)
      .then(response => {
        if (response.status === 403) {
          throw new Error(403);
        }
        if (response.status === 404) {
          throw new Error(404);
        }
        return response.json();
      })
      .then(json => {
        if (ResponseUtils.hasError(json)) {
          throw ResponseUtils.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Get changes for given request and entity
   * @param  id Id of request
   * @param  entityId Id of reqeustable entity
   */
  getChanges(id, entityId) {
    return RestApiService
      .get(`${ this.getApiPath() }/${ encodeURIComponent(id) }/entity/${ encodeURIComponent(entityId) }/changes`)
      .then(response => {
        if (response.status === 204) {
          return null;
        }
        return response.json();
      })
      .then(jsonResponse => {
        if (ResponseUtils.hasError(jsonResponse)) {
          throw ResponseUtils.getFirstError(jsonResponse);
        }
        if (ResponseUtils.hasInfo(jsonResponse)) {
          throw ResponseUtils.getFirstInfo(jsonResponse);
        }
        return jsonResponse;
      });
  }

}

export default RequestService;
