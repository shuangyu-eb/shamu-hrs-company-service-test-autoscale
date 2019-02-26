import axios from 'axios';

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
    data: payload,
    headers: {
      'Content-Type': ((typeof payload) === 'string') ? 'text/plain' : 'application/json',
    },
    withCredentials: true,
  };
  return axios(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function updateResource(resource, payload) {
  const options = {
    path: getUrl(resource),
    method: 'PATCH',
    data: payload,
    headers: {
      'Content-Type': 'application/json',
    },
    withCredentials: true,
  };
  return axios(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function uploadResource(resource, formData) {
  const options = {
    path: getUrl(resource),
    method: 'POST',
    data: formData,
    withCredentials: true,
    headers: { 'Content-Type': 'multipart/form-data', Accept: '*/*' },
  };
  return axios(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function destroyResource(resource, payload) {
  const options = {
    path: getUrl(resource),
    method: 'DELETE',
    data: payload,
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    withCredentials: true,
  };
  return axios(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function getResource(resource, data) {
  const options = {
    url: getUrl(resource),
    method: 'GET',
    baseUrl: ORIGIN,
    data,
    headers: {
      'Content-Type': 'application/json',
    },
    withCredentials: true,
  };
  return axios(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}

export function submitResource(resource, payload) {
  const options = {
    path: getUrl(resource),
    method: 'POST',
    data: payload,
    withCredentials: true,
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  };
  return axios(options)
    .catch(exception => Promise.reject(exception));
}

export function putResource(resource, payload) {
  const options = {
    path: getUrl(resource),
    method: 'PUT',
    data: JSON.stringify(payload),
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    withCredentials: true,
  };
  return axios.put(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}


export function downloadResource(resource, payload) {
  const options = {
    path: getUrl(resource),
    method: 'POST',
    data: payload,
    headers: {
      'Content-Type': 'application/json',
      Accept: '*/*',
    },
    withCredentials: true,
  };
  return axios(options)
    .then(response => processResponse(response))
    .catch(exception => processException(exception));
}
