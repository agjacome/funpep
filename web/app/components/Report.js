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
import Phylocanvas from 'react-phylocanvas';




const PhyloTree = ({tree}) => {
  return (
    <Phylocanvas data={tree} treeType="rectangular"/>
  );
}

const ShowStatus = ({uuid, status, report, tree}) => {
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
      <b>Phylogenetic tree:</b>
      <div className="row">
        <div className="col-lg-4">
          <DownloadButton uuid={uuid} name='similar.newick' span='Download in Newick format' />
        </div>
        <div className="col-lg-8">
          <DownloadButton uuid={uuid} name='similar.phylo.xml' span='Download in PhyloXML format' />  
        </div>
      </div>
      <PhyloTree tree={tree}/> 
      <b>Analysis similarity report:</b>
      <div className="row">
        <div className="col-lg-12">
          <DownloadButton uuid={uuid} name='report.csv' span='Download as CSV' />
        </div>
      </div>
      <br/> 
      <ReportTable products={report} /><br/> 
      <div className="buttons">
         <LinkContainer to={'/status/' + uuid}><Button>Back</Button></LinkContainer>
      </div>
    </div>
  )
;
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
      tree: '',
      status: {}
    }

    this.bindThis(
      'onStatusSuccess',
      'onReportSuccess',
      'onTreeSuccess',
      'onAxiosFailure'
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
      .catch(this.onAxiosFailure);
  }
    
  onReportSuccess(response) {
    this.setState({
      loaded: true,
      found:  true,
      uuid:   this.state.uuid,
      report: this.reportToTable(response.data),
      tree:   '',
      status: this.state.status
    });
    file(this.state.uuid, 'similar.newick')
      .then(this.onTreeSuccess)
      .catch(this.onAxiosFailure);
  }
    
  onTreeSuccess(response) {
    this.setState({
      loaded: true,
      found:  true,
      uuid:   this.state.uuid,
      report: this.state.report,
      tree:   response.data,
      status: this.state.status
    });
  }

  onAxiosFailure(response) {
    this.setState({
      loaded: true,
      found:  false,
      uuid:   '',
      report: '',
      tree: '',
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
            ? <ShowStatus uuid={this.state.uuid} status={this.state.status} report={this.state.report} tree={this.state.tree}/>
            : <ShowNotFound uuid={this.props.params.uuid} />
          }
      </div>
    );
  }

}


export default Report;
