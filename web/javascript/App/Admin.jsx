import React from 'react';
import { getResource } from './Rest';

class Admin extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      data: '',
    };
  }

  componentDidMount() {
    getResource('/').then((data) => {
      this.setState({
        data: data.entity,
      });
    }).catch(e => console.log(e, 'error'));
  }

  render() {
    const { data } = this.state;
    return (
      <div>
        this is the home page!
        <div>{data}</div>
      </div>
    );
  }
}

export default Admin;
