/* eslint-disable */
import React from 'react';
import Logo from 'App/Logo';
import './Login.scss';

class NormalLoginForm extends React.Component {
  render() {
    return (
      <div className="login">
        <Logo />
        <div className="sign-in">
          <div onSubmit={() => console.log('login')} className="login-form">

            <h1 className="p-style">Sign In</h1>

            <div className="field">
              <label htmlFor="email" className="label">
                Email Address
                <p className="control has-icons-left has-icons-right">
                  <input className="input" type="email" placeholder="Email" />
                  <span className="icon is-small is-left">
                    <i className="fas fa-envelope" />
                  </span>
                  <span className="icon is-small is-right">
                    <i className="fas fa-check" />
                  </span>
                </p>
              </label>
            </div>
            <div className="field">
              <label htmlFor="password" className="label">
                Password
                <p className="control has-icons-left">
                  <input className="input" type="password" placeholder="Password" />
                  <span className="icon is-small is-left">
                    <i className="fas fa-lock" />
                  </span>
                </p>
              </label>
            </div>

            <div className="field is-grouped">
              <div className="control">
                <button type="submit" className="button is-link">Submit</button>
              </div>
              <div className="control">
                <button type="button" className="button is-text">Cancel</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

NormalLoginForm.propTypes = {};

export default NormalLoginForm;
