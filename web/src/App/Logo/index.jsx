import React from 'react';
import { background } from '../Image';
import './Logo.scss';

export default () => (
  <div className="div-logo-root">
    <div className="div-bg">
      <img src={background} width="100%" height="100%" alt="background" />
    </div>
    <div className="div-logo" />
  </div>
);
