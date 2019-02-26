import React from 'react';
import { Provider } from 'react-redux';
import { ConnectedRouter } from 'connected-react-router';
import configureStore, { history } from './Store';
import 'Style/index.scss';
import NavBar from './NavBar';
import SideBar from './SideBar';
import Content from './Content';

const store = configureStore();

const Admin = () => (
  <Provider store={store}>
    <ConnectedRouter history={history}>
      <div>
        <NavBar />
        <SideBar />
        <Content />
      </div>
    </ConnectedRouter>
  </Provider>
);

export default Admin;
