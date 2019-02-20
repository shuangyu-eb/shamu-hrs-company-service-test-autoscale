import client from './Client';

function processResponse(response) {
  return Promise.resolve(response);
}

function processException(exception) {
  return Promise.reject(exception);
}

const getUrl = resource => `${ORIGIN}${resource}`;

export function createResource(resource, payload) {
  const options = {
    path: getUrl(resource),
    method: 'POST',
    entity: payload,
    headers: {
      'Content-Type': ((typeof payload) === 'string') ? 'text/plain' : 'application/json',
    },
    credentials: 'include',
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function updateResource(resource, payload) {
  const options = {
    path: getUrl(resource),
    method: 'PATCH',
    entity: payload,
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function uploadResource(resource, formData) {
  const options = {
    path: getUrl(resource),
    method: 'POST',
    entity: formData,
    credentials: 'include',
    headers: { 'Content-Type': 'multipart/form-data', Accept: '*/*' },
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function destroyResource(resource, payload) {
  const options = {
    path: getUrl(resource),
    method: 'DELETE',
    entity: payload,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function getResource(resource, data) {
  const options = {
    path: getUrl(resource),
    method: 'GET',
    params: data,
    dataType: 'json',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function submitResource(resource, payload) {
  const options = {
    path: getUrl(resource),
    method: 'POST',
    entity: payload,
    credentials: 'include',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  };
  return client(options)
    .catch(exception => Promise.reject(exception));
}

export function putResource(resource, payload) {
  const options = {
    path: getUrl(resource),
    method: 'PUT',
    body: JSON.stringify(payload),
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}


export function downloadResource(resource, payload) {
  const options = {
    path: getUrl(resource),
    method: 'POST',
    entity: payload,
    headers: {
      'Content-Type': 'application/json',
      Accept: '*/*',
    },
    credentials: 'include',
  };
  return client(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}
