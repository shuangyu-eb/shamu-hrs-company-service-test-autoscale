import React from 'react';
import { Route, Switch, Redirect } from 'react-router-dom';
import { MODULE } from '../Contants';
import Dashboard from '../../Dashboard';
import TimeOff from '../../TimeOff';
import Benefits from '../../Benefits/index';
import Employees from '../../Employees';
import Documents from '../../Documents';
import Info from '../../Info';
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
