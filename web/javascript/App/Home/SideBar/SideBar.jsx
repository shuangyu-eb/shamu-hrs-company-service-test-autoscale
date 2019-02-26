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
} from 'Image/Icon';
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
  const module = store.router.location.pathname.split('/')[1] || MODULE.DASHBOARD;
  return { module };
};

class SideBar extends React.Component {
  getClassName = (key) => {
    const { module } = this.props;
    return module === key ? 'item-selected' : '';
  }

  getIconByModule = (key) => {
    const { module } = this.props;
    return getIcon(key, module === key);
  }

  render() {
    return (
      <div className="sidebar">
        <div className="sidebar-inner">
          <div>
            <div className={`menu-item ${this.getClassName(MODULE.DASHBOARD)}`}>
              <Link to="/">
                <div className="menu-item-icon">
                  <img src={this.getIconByModule(MODULE.DASHBOARD)} alt="icon-dashboard" />
                </div>
                <span>Dashboard</span>
              </Link>
            </div>
            <div className={`menu-item ${this.getClassName(MODULE.MY_INFO)}`}>
              <Link to={`/${MODULE.MY_INFO}`}>
                <div className="menu-item-icon">
                  <img src={this.getIconByModule(MODULE.MY_INFO)} alt="icon-dashboard" />
                </div>
                <span>My Info</span>
              </Link>
            </div>
            <div className={`menu-item ${this.getClassName(MODULE.TIME_OFF)}`}>
              <Link to={`/${MODULE.TIME_OFF}`}>
                <img src={this.getIconByModule(MODULE.TIME_OFF)} alt="icon-dashboard" />
                <span>Time Off</span>
              </Link>
            </div>
            <div className={`menu-item ${this.getClassName(MODULE.DOCUMENTS)}`}>
              <Link to={`/${MODULE.DOCUMENTS}`}>
                <div className="menu-item-icon">
                  <img src={this.getIconByModule(MODULE.DOCUMENTS)} alt="icon-dashboard" />
                </div>
                <span>Documents</span>
              </Link>
            </div>
            <div className={`menu-item ${this.getClassName(MODULE.BENEFITS)}`}>
              <Link to={`/${MODULE.BENEFITS}`}>
                <div className="menu-item-icon">
                  <img src={this.getIconByModule(MODULE.BENEFITS)} alt="icon-dashboard" />
                </div>
                <span>Benefits</span>
              </Link>
            </div>
            <div className={`menu-item ${this.getClassName(MODULE.EMPLOYEES)}`}>
              <Link to={`/${MODULE.EMPLOYEES}`}>
                <div className="menu-item-icon">
                  <img src={this.getIconByModule(MODULE.EMPLOYEES)} alt="icon-dashboard" />
                </div>
                <span>Employee</span>
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

SideBar.defaultProps = {
  module: MODULE.DASHBOARD,
};

SideBar.propTypes = {
  module: PropTypes.string,
};

export default connect(mapStateToProps)(SideBar);
