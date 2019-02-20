import React from 'react';
import { getResource } from './Common/Rest';

class Home extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      data: ''
    };
  }

  componentDidMount() {
    getResource('/').then(data => {
      this.setState({
        data: data.entity
      });
    }).catch(e => console.log(e, 'error'));
  }

  render() {
    return (
      <div>
        this is the home page!
        <div>{this.state.data}</div>
      </div>);
  }
}

export default Home;
