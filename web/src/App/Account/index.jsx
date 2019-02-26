import React from 'react';
import { Provider } from 'react-redux';
import { Router } from 'react-router';
import App from './Containers/Login';
import ConfigureStore from './Store';
import history from './history';

const store = ConfigureStore();

const Account = () => (
  <Provider store={store}>
    <Router history={history}>
      <App />
    </Router>
  </Provider>
);

export default Account;
