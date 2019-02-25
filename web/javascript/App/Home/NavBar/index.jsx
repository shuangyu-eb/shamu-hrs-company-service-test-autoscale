import React from 'react';
import { Link } from 'react-router-dom';
import {
  Navbar, NavbarBrand, NavbarItem, NavbarMenu,
  NavbarStart, NavbarEnd, Field, Control, Button, DropdownContent,
  Dropdown, DropdownMenu, DropdownItem, DropdownTrigger,
} from 'bloomer';
import { search } from 'App/Image/Icon';
import logo from './logo.svg';
import './NavBar.scss';


class NavBar extends React.Component {
  onSearCh = () => {

  }

  render() {
    return (
      <Navbar role="navigation" aria-label="main navigation">
        <NavbarBrand>
          <Link className="navbar-item logo" to="/">
            <img src={logo} alt="logo" />
          </Link>
        </NavbarBrand>

        <NavbarMenu>
          <NavbarStart>
            <NavbarItem>
              <Field>
                <Control hasIcons="right">
                  <input className="input is-medium search-input" onChange={this.onSearCh} type="text" placeholder="Search employees & documents" />
                  {/* <Icon isSize="small" isAlign="right" className="material-icons"> */}
                  <span className="icon is-small is-right">
                    <img src={search} alt="search" className="search-icon" />
                  </span>
                  {/* <Icon isSize="small" isAlign="right" src={search}> */}
                  {/* /!*<FontAwesomeIcon icon="search"/>*!/ */}
                  {/* <img src={search} alt="search" /> */}
                  {/* </Icon> */}
                </Control>
              </Field>
            </NavbarItem>
          </NavbarStart>

          <NavbarEnd>
            <NavbarItem>
              <span className="icon is-small">
                <i className="fal fa-bell" />
              </span>
            </NavbarItem>
            <NavbarItem>
              <Dropdown>
                <DropdownTrigger>
                  <Button isOutlined className="profile-picture" aria-haspopup="true" aria-controls="dropdown-menu">
                    <span>picture</span>
                  </Button>
                </DropdownTrigger>
                <DropdownMenu>
                  <DropdownContent>
                    <DropdownItem href="#">First item</DropdownItem>
                  </DropdownContent>
                </DropdownMenu>
              </Dropdown>
            </NavbarItem>
          </NavbarEnd>
        </NavbarMenu>
      </Navbar>
    );
  }
}

export default NavBar;
