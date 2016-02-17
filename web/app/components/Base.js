import React from 'react';

class BaseComponent extends React.Component {

  constructor(props) {
    super(props);
  }

  bindThis(...methods) {
    methods.forEach((method) => this[method] = this[method].bind(this));
  }

}

export default BaseComponent;
