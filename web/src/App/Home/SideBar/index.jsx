import React from 'react';
import { Link } from 'react-router-dom';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import {
  infoActive,
  info,
  dashboardActive,
  dashboard,
  timeOffActive,
  timeOff,
  employeesActive,
  employees,
  documentsActive,
  documents,
  benefitsActive,
  benefits,
} from './Icon';
import { MODULE } from '../Contants';
import './SideBar.scss';


const getIcon = (module, active) => {
  switch (module) {
    case MODULE.DASHBOARD:
      return active ? dashboardActive : dashboard;
    case MODULE.MY_INFO:
      return active ? infoActive : info;
    case MODULE.BENEFITS:
      return active ? benefitsActive : benefits;
    case MODULE.DOCUMENTS:
      return active ? documentsActive : documents;
    case MODULE.TIME_OFF:
      return active ? timeOffActive : timeOff;
    case MODULE.EMPLOYEES:
      return active ? employeesActive : employees;
    default:
      return null;
  }
};

const mapStateToProps = (store) => {
  const module = store.router.location.pathname.split('/')[1];
  return { module };
};

class Index extends React.Component {
  getClassName = (key) => {
    const { module } = this.props;
    return module === key ? 'item-selected' : '';
  }

  getIconByModule = (key) => {
    const { module } = this.props;
    return getIcon(key, module === key);
  }

  getItem = (module, name) => ((
    <div className={`menu-item ${this.getClassName(module)}`}>
      <Link to={`/${module}`}>
        <div className="menu-item-icon">
          <img src={this.getIconByModule(module)} alt="icon-dashboard" />
        </div>
        <span>{name}</span>
      </Link>
    </div>
  ));

  render() {
    return (
      <div className="sidebar">
        <div className="sidebar-inner">
          {this.getItem(MODULE.DASHBOARD, 'Dashboard')}
          {this.getItem(MODULE.MY_INFO, 'My Info')}
          {this.getItem(MODULE.TIME_OFF, 'Time Off')}
          {this.getItem(MODULE.DOCUMENTS, 'Documents')}
          {this.getItem(MODULE.BENEFITS, 'Benefits')}
          {this.getItem(MODULE.EMPLOYEES, 'Employees')}
        </div>
      </div>
    );
  }
}

Index.defaultProps = {
  module: MODULE.DASHBOARD,
};

Index.propTypes = {
  module: PropTypes.string,
};

export default connect(mapStateToProps)(Index);
