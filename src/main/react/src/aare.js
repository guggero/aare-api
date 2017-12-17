import React, {Component} from 'react';
import * as $ from 'jquery';
import './aare.css';

export const API_URL = 'https://api.illubits.ch/aare/v1/current';
export const STATION_NUMBER = '2135';

class Aare extends Component {

  constructor(props) {
    super(props);
    this.state = {temperature: '0.0'};
  }

  componentDidMount() {
    this.serverRequest = $.get(API_URL, function (result) {
      result.stations.forEach(station => {
        if (station.number === STATION_NUMBER) {
          this.setState({temperature: station.temperature});
        }
      });
    }.bind(this));
  }

  componentWillUnmount() {
    this.serverRequest.abort();
  }

  render() {
    return (
      <div className="aare">
        <div className="temperature">{this.state.temperature} °C</div>
      </div>
    );
  }
}

export default Aare;
