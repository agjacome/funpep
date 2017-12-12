import React  from 'react';
import Loader from 'react-loader';
import Base   from './Base';
import Report from './Report';

import { LinkContainer } from 'react-router-bootstrap';
import { Alert, Button, ButtonToolbar, ListGroup, ListGroupItem } from 'react-bootstrap';

import S      from 'string';
import moment from 'moment';
import { status, queuePosition } from '../utils/api_helpers';

const ShowStatus = ({uuid, status, queue}) => {
  const created  = status.status === 'created';
  const started  = status.status === 'started';
  const finished = status.status === 'finished';
  const failed   = status.status === 'failed';

  const style = created  ? "info"    :
                started  ? "warning" :
                finished ? "success" :
                failed   ? "danger"  :
                null;

  return (
    <div>
      <h3>Analysis '{uuid}'</h3>
      <p>
        Any existing analysis process in our system will be in one of four
        different statuses: <em>Created, Started, Finished or Failed</em>. This
        page will show you the current status of your analysis and the date and
        time when that status was last changed.
      </p>
      <p>
        <em>Created</em> means that the analysis has been enqueued, but not yet
        started to process; in this case, the current job queue position will
        also be displayed. <em>Started</em> means that the analysis process has
        already started and is currently ongoing, but has not yet finished.&nbsp;
        <em>Finished</em> means that the analysis process has finished
        successfully; in this case a link to the analysis report page will also
        be displayed. <em>Failed</em> means that the analysis process has found
        an error and could not complete successfully; in this case, the error
        message will also be displayed.
      </p>
      <ListGroup>
        <ListGroupItem bsStyle={style}>Status: <strong>{ S(status.status).capitalize().s }</strong></ListGroupItem>

        { created  && <ListGroupItem>Position: <strong>{ queue }</strong></ListGroupItem> }
        { failed   && <ListGroupItem>Error: { status.error }</ListGroupItem>              }

        <ListGroupItem>Date: { moment.unix(status.time).toString() }</ListGroupItem>

        { finished &&
          <div className="buttons">
            <LinkContainer to={'/report/' + uuid}><Button>View Reports</Button></LinkContainer>
          </div>
        }
         
         
          
      </ListGroup>
    </div>
  );
}

const ShowNotFound = ({uuid}) => {
  return (
    <Alert bsStyle="danger">
      <strong>Analysis '{uuid}' not found</strong>
    </Alert>
  );
}

class Status extends Base {

  constructor(props) {
    super(props);

    this.state = {
      loaded: false,
      found:  false,
      uuid:   '',
      status: {},
      queue:  false
    }

    this.bindThis(
      'onStatusSuccess',
      'onStatusFailure',
      'onQueueSuccess',
      'onQueueFailure'
    );
  }

  componentDidMount() {
    status(this.props.params.uuid)
      .then(this.onStatusSuccess)
      .catch(this.onStatusFailure);
  }

  onStatusSuccess(response) {
    this.setState({
      loaded: true,
      found:  true,
      uuid:   response.data.uuid,
      status: response.data.status,
    });

    if (response.data.status.status === 'created') {
      queuePosition(this.state.uuid)
        .then(this.onQueueSuccess)
        .catch(this.onQueueFailure);
    }
  }

  onStatusFailure(response) {
    this.setState({
      loaded: true,
      found:  false,
      uuid:   '',
      status: {},
    });
  }

  onQueueSuccess(response) {
    this.setState({ queue: response.data.position });
  }

  onQueueFailure(response) {
    this.setState({ queue: 'not found' });
  }

  render() {
    return (
      <div className="content">
        <Loader loaded={this.state.loaded}>
        {
          this.state.found
            ? <ShowStatus uuid={this.state.uuid} status={this.state.status} queue={this.state.queue} />
            : <ShowNotFound uuid={this.props.params.uuid} />
        }
        </Loader>
      </div>
    );
  }

}


export default Status;
