const rest = require('rest');
const defaultRequest = require('rest/interceptor/defaultRequest');
const mime = require('rest/interceptor/mime');
const template = require('rest/interceptor/template');
const errorCode = require('rest/interceptor/errorCode');
// const csrf = require('rest/interceptor/csrf');
const baseRegistry = require('rest/mime/registry');

const registry = baseRegistry.child();

// const csrfToken = document.getElementsByTagName('meta').csrf.content;

registry.register('multipart/form-data', require('rest/mime/type/multipart/form-data'));
registry.register('application/json', require('./json'));
registry.register('text/plain', require('rest/mime/type/text/plain'));

module.exports = rest
  .wrap(mime, { registry })
  .wrap(template, {})
  .wrap(errorCode)
  // .wrap(csrf, { token: csrfToken })
  .wrap(defaultRequest, { headers: { Accept: 'application/json' } });
