module.exports = {
  module: 'vs',
  childRoutes: [
    {
      path: '/vs/requests',
      component: require('./src/content/vs-request/VsRequests'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['VSREQUEST_READ'] } ]
    },
    {
      path: '/vs/systems',
      component: require('./src/content/vs-system/VsSystems'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['VSREQUEST_READ'] } ]
    },
    {
      path: 'vs/request/:entityId/detail',
      component: require('./src/content/vs-request/VsRequestContent'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['VSREQUEST_READ'] } ]
    },
    {
      path: 'vs/request/:entityId/new',
      component: require('./src/content/vs-request/VsRequestContent'),
      access: [ { type: 'HAS_ANY_AUTHORITY', authorities: ['VSREQUEST_CREATE'] } ]
    }
  ]
};
