import React from 'react';
import { Link } from 'react-router-dom';
import {
  Navbar, NavbarBrand, NavbarItem, NavbarMenu,
  NavbarStart, NavbarEnd, Field, Control, Button, DropdownContent,
  Dropdown, DropdownMenu, DropdownItem, DropdownTrigger,
} from 'bloomer';
import { search, notifications } from 'Image/Icon';
import logo from './logo.svg';
import './NavBar.scss';


class NavBar extends React.Component {
  onSearCh = () => {

  }

  render() {
    return (
      <nav className="navbar" role="navigation" aria-label="main navigation">
        <div className="navbar-brand">
          <Link className="navbar-item logo" to="/">
            <img src={logo} alt="logo" />
          </Link>
        </div>

        <div className="navbar-menu">
          <div className="navbar-start">
            <div className="navbar-item">
              <div className="field">
                <div className="control has-icons-right">
                  <input className="input is-medium search-input" onChange={this.onSearCh} type="text" placeholder="Search employees & documents" />
                  <span className="icon is-small is-right">
                    <img src={search} alt="search" />
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div className="navbar-end">
            <div className="navbar-item">
              <span className="icon is-large">
                <img src={notifications} alt="notifications" className="notifications" />
              </span>
            </div>
            <div className="navbar-item">
              <div className="dropdown">
                <div className="dropdown-trigger">
                  <button type="button" className="button profile-picture" aria-haspopup="true" aria-controls="dropdown-menu">
                    <span>picture</span>
                  </button>
                </div>
                <div className="dropdown-menu" id="dropdown-menu" role="menu">
                  <div className="dropdown-content">
                    <button type="button" className="dropdown-item">First item</button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </nav>
    );
  }
}

export default NavBar;
