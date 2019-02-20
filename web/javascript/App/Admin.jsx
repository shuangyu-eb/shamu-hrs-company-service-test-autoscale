import React from 'react';
import { getResource } from './Common/Rest';
class Admin extends React.Component {
  componentDidMount() {
    getResource('/').then(data => {
      console.log(data);
    }).catch(e => console.log(e, 'admin'));
  }

  render() {
    console.log(ORIGIN);
    return <div>this is admin page!</div>;
  }
}

export default Admin;
