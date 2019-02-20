import client from './Client';

const loginUri = '/account/login';

function processResponse(response) {
  // const { request: { path } = {}, raw: { responseURL = null } = {} } = response || {};
  // if (path && !path.endsWith('/account/login')
  //   && responseURL && path.endsWith('/account/login')) {
  //   window.location.href = loginUri;
  //   return Promise.resolve();
  // }
  return Promise.resolve(response);
}

function processException(exception) {
  // const { status: { code } } = exception;
  // if (code === 401) {
  //   window.location.href = loginUri;
  //   return Promise.resolve();
  // }

  return Promise.reject(exception);
}




export function createResource(resource, payload) {
  const url = `${resource}`;
  const options = {
    path: url,
    method: 'POST',
    origin: ORIGIN,
    entity: payload,
    headers: {
      'Content-Type': ((typeof payload) === 'string') ? 'text/plain' : 'application/json',
    },
    credentials: 'same-origin',
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function updateResource(resource, payload) {
  const url = `${resource}`;
  const options = {
    path: url,
    method: 'PATCH',
    entity: payload,
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'same-origin',
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function uploadResource(resource, formData) {
  const url = `${resource}`;
  const options = {
    path: url,
    method: 'POST',
    entity: formData,
    credentials: 'same-origin',
    headers: { 'Content-Type': 'multipart/form-data', Accept: '*/*' },
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function destroyResource(resource, payload) {
  const url = `${resource}`;
  const options = {
    path: url,
    method: 'DELETE',
    entity: payload,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    credentials: 'same-origin',
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function getResource(resource, data) {

  const url = `${ORIGIN}${resource}`;
  const options = {
    path: url,
    method: 'GET',
    params: data,
    dataType: 'json',
    headers: {
      'Content-Type': 'application/json',
    },
    // credentials: 'same-origin',
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function submitResource(resource, payload) {
  const url = `${resource}`;
  const options = {
    path: url,
    method: 'POST',
    entity: payload,
    credentials: 'same-origin',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  };
  return client(options)
    .catch(exception => Promise.reject(exception));
}

export function putResource(resource, payload) {
  const url = `${resource}`;
  const options = {
    path: url,
    method: 'PUT',
    body: JSON.stringify(payload),
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    credentials: 'same-origin',
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}


export function downloadResource(resource, payload) {
  const url = `${resource}`;
  const options = {
    path: url,
    method: 'POST',
    entity: payload,
    headers: {
      'Content-Type': 'application/json',
      Accept: '*/*',
    },
    credentials: 'same-origin',
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}
