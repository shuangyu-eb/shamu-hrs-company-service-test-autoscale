import React from 'react';
import { Router } from 'react-router-dom';
import { Provider } from 'react-redux';
import history from 'App/history';
import 'App/Style';
import configureStore from './Store';
import NavBar from './NavBar';
import SideBar from './SideBar';

const store = configureStore();


const Admin = () => (
  <Provider store={store}>
    <Router history={history}>
      <div>
        <NavBar />
        <SideBar />
      </div>
    </Router>
  </Provider>
);

export default Admin;
