import React from 'react';
import ReactDOM from 'react-dom';
import Aare from './aare';

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<Aare />, div);
});
