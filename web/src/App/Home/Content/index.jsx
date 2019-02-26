import React from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import Dashboard from 'App/Dashboard';
import TimeOff from 'App/TimeOff';
import Benefits from 'App/Benefits';
import Employees from 'App/Employees';
import Documents from 'App/Documents';
import Info from 'App/Info';
import { MODULE } from '../Contants';
import './Content.scss';

const Index = () => ((
  <div className="content">
    <Switch>
      <Redirect from="/account/login" to="/" />
      <Route exact path="/" component={Dashboard} />
      <Route path={`/${MODULE.MY_INFO}`} component={Info} />
      <Route path={`/${MODULE.TIME_OFF}`} component={TimeOff} />
      <Route path={`/${MODULE.DOCUMENTS}`} component={Documents} />
      <Route path={`/${MODULE.BENEFITS}`} component={Benefits} />
      <Route path={`/${MODULE.EMPLOYEES}`} component={Employees} />
    </Switch>
  </div>
));

export default Index;
