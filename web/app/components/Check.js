import React from 'react';
import Base  from './Base';

import validate               from 'uuid-validate';
import { Input, ButtonInput } from 'react-bootstrap';

class Check extends Base {

  constructor(props) {
    super(props);

    this.state = {
      uuid: '',
      valid: false
    };

    this.bindThis(
      'validationState',
      'handleChange',
      'handleSubmit'
    );
  }

  validationState() {
    return this.state.uuid.length > 0 ? this.state.valid ? 'success' : 'error' : null;
  }

  handleChange() {
    const uuid = this.refs.uuid.getValue();
    this.setState({ uuid: uuid, valid: validate(uuid) });
  }

  handleSubmit() {
    this.context.router.push('/status/' + this.state.uuid);
  }

  render() {
    return (
      <div className="content">
        <h3>Check analysis status</h3>
        <p>
          You can check the status of an ongoing analysis process by providing
          its unique identifier in the form of an <a target="_blank" href="https://en.wikipedia.org/wiki/Universally_unique_identifier">UUID</a>.
          You should have received the Analysis ID upon correctly submitting it
          into our system.
        </p>
        <form onSubmit={this.handleSubmit}>
          <Input type="text" placeholder="Enter analysis UUID" ref="uuid" onChange={this.handleChange} bsStyle={this.validationState()} hasFeedback />
          <div className="buttons">
            <ButtonInput type="submit" value="Check" disabled={!this.state.valid} />
          </div>
        </form>
      </div>
    );
  }

}

Check.contextTypes = {
  router: React.PropTypes.object.isRequired
}

export default Check;
