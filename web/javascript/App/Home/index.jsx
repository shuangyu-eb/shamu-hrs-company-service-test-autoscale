import React from 'react';
import { Provider } from 'react-redux';
import { ConnectedRouter } from 'connected-react-router';
import configureStore, { history } from './Store';
import 'App/Style';
import NavBar from './NavBar';
import SideBar from './SideBar';

const store = configureStore();


const Admin = () => (
  <Provider store={store}>
    <ConnectedRouter history={history}>
      <div>
        <NavBar />
        <SideBar />
      </div>
    </ConnectedRouter>
  </Provider>
);

export default Admin;
