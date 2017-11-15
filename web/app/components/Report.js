import React  from 'react';
import Loader from 'react-loader';
import Base   from './Base';
import DownloadButton from './report/DownloadButton';
import ReportTable from './report/ReportTable';

import { LinkContainer } from 'react-router-bootstrap';
import { Alert, Button, ListGroup, ListGroupItem } from 'react-bootstrap';

import S      from 'string';
import moment from 'moment';
import { status, file } from '../utils/api_helpers';

const ShowStatus = ({uuid, status, report}) => {
  return (
    <div>
      <h3>Reports for analysis '{uuid}'</h3>
      <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc vehicula
         velit id diam sagittis dignissim.</p>
      <ListGroup>
        <ListGroupItem>Date: { moment.unix(status.time).toString() }</ListGroupItem>
      </ListGroup>
      <b>Download analysis FASTA files:</b>
      <div className="row">
        <div className="col-lg-4">
          <DownloadButton uuid={uuid} name='comparing.fasta' span='Comparing.fasta' />
        </div>
        <div className="col-lg-4">
          <DownloadButton uuid={uuid} name='reference.fasta' span='Reference.fasta' />  
        </div>
        <div className="col-lg-4">
          <DownloadButton uuid={uuid} name='alignment.fasta' span='Alignment.fasta' />    
        </div>
      </div><br/>
      <b>Download tree files:</b>
      <div className="row">
        <div className="col-lg-4">
          <DownloadButton uuid={uuid} name='similar.newick' span='Newick format' />
        </div>
        <div className="col-lg-8">
          <DownloadButton uuid={uuid} name='similar.phylo.xml' span='PhyloXML format' />  
        </div>
      </div><br/>
      <b>Analysis similarity report:</b>
      <div className="row">
        <div className="col-lg-12">
          <DownloadButton uuid={uuid} name='report.csv' span='Download as CSV' />
        </div>
      </div>
      <br/>
      <ReportTable products={report} />  
      <div className="buttons">
         <LinkContainer to={'/status/' + uuid}><Button>Back</Button></LinkContainer>
      </div>
    </div>
  );
}

const ShowNotFound = ({uuid}) => {
  return (
    <Alert bsStyle="danger">
      <strong>Reports not found for analysis '{uuid}'</strong>
    </Alert>
  );
}

class Report extends Base {

  constructor(props) {
    super(props);

    this.state = {
      loaded: false,
      found:  false,
      uuid:   '',
      report: '',
      status: {}
    }

    this.bindThis(
      'onStatusSuccess',
      'onStatusFailure',
      'onReportSuccess',
      'onReportFailure'
    );
  }
    
  onStatusSuccess(response) {
    this.setState({
      loaded: true,
      found:  true,
      uuid:   response.data.uuid,
      status: response.data.status
    });
    file(response.data.uuid, 'report.csv')
      .then(this.onReportSuccess)
      .catch(this.onReportFailure);
  }

  onStatusFailure(response) {
    this.setState({
      loaded: true,
      found:  false,
      uuid:   '',
      status: {}
    });
  }
    
  onReportSuccess(response) {
    this.setState({
      loaded: true,
      found:  true,
      uuid:   this.state.uuid,
      report: this.reportToTable(response.data),
      status: this.state.status
    });
  }

  onReportFailure(response) {
    this.setState({
      loaded: true,
      found:  false,
      uuid:   '',
      report:  '',
      status: {}
    });
  }
  
  reportToTable(report){
    var lines = report.split("\n");
    var result = [];
    var headers = ["comparing","reference","similarity"];
    for(var i=1; i<lines.length-1; i++){
      var obj = {};
      var currentline = lines[i].split(",");
      
      for(var j=0; j<headers.length;j++){
        obj[headers[j]] = currentline[j].substring(1,currentline[j].length-1);
      }
      result.push(obj);
    }
    
    return result;
  }

  componentDidMount() {
    status(this.props.params.uuid)
      .then(this.onStatusSuccess)
      .catch(this.onStatusFailure);
  }

  render() {
    return (
      <div className="content">
        {
          this.state.found
            ? <ShowStatus uuid={this.state.uuid} status={this.state.status} report={this.state.report}/>
            : <ShowNotFound uuid={this.props.params.uuid} />
          }
      </div>
    );
  }

}


export default Report;
