import React  from 'react';
import Loader from 'react-loader';
import Base   from './Base';
import Report from './Report';

import { LinkContainer } from 'react-router-bootstrap';
import { Alert, Button, ButtonToolbar, ListGroup, ListGroupItem } from 'react-bootstrap';

import S      from 'string';
import moment from 'moment';
import { file, status, queuePosition, statusMultiple } from '../utils/api_helpers';
import DownloadButton from './report/DownloadButton';
import { createIdentityDataChart, getReferences, createRepeatChart } from './report/HeatMap';
import ReactSpinner from 'reactjs-spinner';


function CSVToArray( strData, strDelimiter ){
        // Check to see if the delimiter is defined. If not,
        // then default to comma.
        strDelimiter = (strDelimiter || ",");
        // Create a regular expression to parse the CSV values.
        var objPattern = new RegExp(
            (
                // Delimiters.
                "(\\" + strDelimiter + "|\\r?\\n|\\r|^)" +
                // Quoted fields.
                "(?:\"([^\"]*(?:\"\"[^\"]*)*)\"|" +
                // Standard fields.
                "([^\"\\" + strDelimiter + "\\r\\n]*))"
            ),
            "gi"
            );
        // Create an array to hold our data. Give the array
        var arrData = [[]];
        // Create an array to hold our individual pattern
        var arrMatches = null;
        while (arrMatches = objPattern.exec( strData )){
            // Get the delimiter that was found.
            var strMatchedDelimiter = arrMatches[ 1 ];
            if (
                strMatchedDelimiter.length &&
                strMatchedDelimiter !== strDelimiter
                ){
                // Since we have reached a new row of data,
                // add an empty row to our data array.
                arrData.push( [] );
            }
            var strMatchedValue;
            if (arrMatches[ 2 ]){
                strMatchedValue = arrMatches[ 2 ].replace(
                    new RegExp( "\"\"", "g" ),
                    "\""
                    );

            } else {
                // We found a non-quoted value.
                strMatchedValue = arrMatches[ 3 ];
            }

            // Now that we have our value string, let's add
            // it to the data array.
            arrData[ arrData.length - 1 ].push( strMatchedValue );
        }
        // Return the parsed data.
        return( arrData );
    }
 


const ShowNotFound = ({uuid}) => {
  return (
    <Alert bsStyle="danger">
      <strong>Project {uuid} not found</strong>
    </Alert>
  );
}
const HeatMap = ({ configIdentity, graphIdentity, configRepeat, graphRepeat }) => {
  return(
    <div>
    <br/>
    <p><b>Similarity percentage heatmap</b></p>
      { graphIdentity
          ? <AmCharts.React options={configIdentity} style={{width: "100%", height: "500px"   }} />
          : <ReactSpinner size={80} borderColor='#f2f0f0' borderTopColor='#e60000'>  </ReactSpinner>
      }
      <br/><p><b>Alignment matches heatmap</b></p>
      { graphRepeat
          ? <AmCharts.React options={configRepeat} style={{width: "100%", height: "500px"   }} />
          : <ReactSpinner size={80} borderColor='#f2f0f0' borderTopColor='#e60000'>  </ReactSpinner>
      }
      <br/><br/>
    </div>
  );
}

// if we find project then show analysis and graph
const ShowProject = ({project, analysisData, configIdentity, graphIdentity, configRepeat, graphRepeat, heatmaps }) => {

  return (
    <div>
      <h3>Project '{project.uuid}' </h3>
      <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p><br/>
      <p><b>Reference file:</b>  <DownloadButton uuid={project.fileUuid} name='reference.fasta' span={project.referenceFile + '.fasta'} /></p>
      <p><b>Project analyses:</b></p>
      <ul className="list-group" id="analysisList">
          {analysisData.map((analysis) => 
            <li className="list-group-item" key={analysis.uuid}>
              <p><b>Analysis '<a target="_blank" href={'/status/' + analysis.uuid}>{analysis.uuid}</a>'</b></p>
              <p><b>Status:</b>  <span className={analysis.status.status} > {analysis.status.status}  </span> </p>
              <p><b>Comparing File:</b>  <DownloadButton uuid={analysis.uuid} name='comparing.fasta' span={analysis.name + '.fasta'} /></p>
            </li>
          )}
      </ul>


        { heatmaps &&
          <HeatMap configIdentity={configIdentity} graphIdentity={graphIdentity} configRepeat={configRepeat} graphRepeat={graphRepeat} />
        }

    </div>
    
  );
}
///




class Analysis extends Base {

  constructor(props) {
    super(props);

    this.state = {
      loaded: false,
      found:  false,
      queue:  false,
      project: {},
      analysisData: [],
      heatmaps: true,
      identityChart: {
        graphs:[],
        sourceData: [], 
        data: [],
        graph: false,
        config: {"type": "serial"}
      },
      repeatChart: {
        graphs:[],
        sourceData: [], 
        data: [],
        graph: false,
        config: {"type": "serial"}
      },
      references: [],
    }
    this.bindThis(
      'onProjectSuccess',
      'onStatusSuccess',
      'onStatusFailure',
      'onQueueSuccess',
      'onFileFailure',
      'onQueueFailure'
    );
  }

  componentDidMount() {
      var uuid = this.props.params.uuid;
      var project = {uuid: uuid}
      this.setState(project: project);
     
      statusMultiple(this.props.params.uuid)
        .then(this.onProjectSuccess)
        .catch(this.onProjectFailure);

      // load heatmap
      setTimeout(
          function(){
              if ( this.state.identityChart.data != [] )
              {

                var identityChart = this.state.identityChart;
                identityChart.config = this.state.identityChart.data;
                identityChart.graph = true;
                this.setState({identityChart: identityChart}); 
              }
              if ( this.state.repeatChart.data != [] )
              {
                var repeatChart = this.state.repeatChart;
                repeatChart.config = this.state.repeatChart.data;
                repeatChart.graph = true;
                this.setState({repeatChart: repeatChart}); 
              }
              
            }
        .bind(this), 3000);

  }


  onProjectSuccess(response){
    var parser = new DOMParser();
    var xmlDoc = parser.parseFromString(response.data,"text/xml");
    var analysis = xmlDoc.getElementsByTagName("analysis");
    var analysisData = [];
    this.setState({loaded: true});
    // if project is empty

    if ( analysis.length != 0 )
    {
      this.setState({found: true});

      var projectNode = analysis[0].getElementsByTagName('project')[0];
      var projectReferenceFile  = projectNode.getElementsByTagName('reference')[0].firstChild.nodeValue;  
      var projectUuid = projectNode.getElementsByTagName('uuid')[0].firstChild.nodeValue;
      var project = {
                        uuid: projectUuid,
                        referenceFile: projectReferenceFile,
                        fileUuid: analysis[0].getElementsByTagName('uuid')[1].firstChild.nodeValue
                      };

      this.setState({project: project});


      var self = this;
      
      file(analysis[0].getElementsByTagName('uuid')[1].firstChild.nodeValue, "reference.fasta")
        .then(function(response){
            var sequences = [];
            sequences = getReferences(response.data);
            if ( sequences != [] )
            {

              self.setState({references: sequences});
              // loop for analysis values
              for ( var i = 0; i < analysis.length; i++)
              {
                var analysisName = analysis[i].getElementsByTagName('comparing')[0].firstChild.nodeValue;
                var analysisUuid = analysis[i].getElementsByTagName('uuid')[1].firstChild.nodeValue;
                var analysisData = self.state.analysisData.slice();
                var newAnalysis = {
                          loaded: true,
                          found: true,
                          uuid: analysisUuid,
                          name: analysisName,
                          status:''
                        };
                analysisData.push(newAnalysis);
                self.setState({analysisData: analysisData});
                
                status(analysisUuid)
                  .then(self.onStatusSuccess)
                  .catch(self.onStatusFailure);
              }
            }

        
        });// read CSV file and take data charts
   
    }
   
  }


  onProjectFailure(response) {
    this.setState({
      loaded: false,
      found:  false,
    });
  }



  onStatusSuccess(response) {


    // first, add status atribute at state analysis

    var analysis = this.state.analysisData.slice();
    for ( var i = 0; i < analysis.length; i++)
    {
      if ( analysis[i].uuid == response.data.uuid )
      {
        analysis[i]['status'] = response.data.status; 
      }
    }
    if (response.data.status.status === 'created') {
      this.setState({heatmaps:false});
      queuePosition(response.data.uuid)
        .then(this.onQueueSuccess)
        .catch(this.onQueueFailure);
    }
    if (response.data.status.status === 'started') {
      this.setState({heatmaps:false});
    }
    // if status is finished create graph
    if( response.data.status.status == 'finished' || response.data.status.status == 'failed')
    {
      // take CSV for create chart
      var self= this;
      var uuid = response.data.uuid;
      var status = response.data.status;

    
      file(response.data.uuid, "report.csv")
        .then(function(response)
        {
          // IDENTITY CHART
          var analysis = self.state.analysisData.slice();
        
          var sourceDataIdentity = self.state.identityChart.sourceData.slice();
          var graphsIdentity = self.state.identityChart.graphs.slice();

          // create data necessary data chart (sourceData, graphs and comparing names)
          var CSVElements = CSVToArray(response.data, ",");
          var uuidAnalysis = response.config.url.split("/")[response.config.url.split("/").length-2];
          var comparingName = '';
          for ( var i = 0; i < analysis.length; i++)
          {
            if ( analysis[i].uuid == uuidAnalysis )
            {
              comparingName = analysis[i].name
            }
          }
          

          var dataChartIdentity = createIdentityDataChart(CSVElements, sourceDataIdentity, graphsIdentity, self.state.references, comparingName);
            

          var configIdentity = {
            "type": "serial",
            "dataProvider": dataChartIdentity.sourceData,
            "graphs": dataChartIdentity.graphs,
            "precision": 1,
            "valueAxes": [{
              "stackType": "regular",
              "axisAlpha": 0,
              "gridAlpha": 0,
              "position": "left",
              "integersOnly": true
            }],
            "startDuration": 1,
            "columnWidth": 1,
            "categoryField": "Reference",
            "categoryAxis": {
              "categoryField": "Comparing",
              "axisAlpha": 0,
              "gridAlpha": 0,
              "position": "left",
              "gridPosition": "start",

            },
            "export": {
              "enabled": true
            }
          };

          var identityChart = {
              sourceData: dataChartIdentity.sourceData,
              graphs: dataChartIdentity.graphs,
              data: configIdentity,
              graph:false

          };

          self.setState({ references: dataChartIdentity.references ,
                          identityChart: identityChart});


          // REPEAT CHART

          var sourceDataRepeat = self.state.repeatChart.sourceData.slice();
          var graphsRepeat = self.state.repeatChart.graphs.slice();

          // create data necessary data chart (sourceData, graphs and comparing names)
          var datachartRepeat = createRepeatChart(CSVElements, sourceDataRepeat, graphsRepeat, self.state.references, comparingName);

          var configRepeat = {
            "type": "serial",
            "dataProvider": datachartRepeat.sourceData,
            "graphs": datachartRepeat.graphs,
            "valueAxes": [{
              "stackType": "regular",
              "axisAlpha": 0,
              "gridAlpha": 0,
              "integersOnly": true,
              "position": "left",
            }],
            "startDuration": 1,
            "columnWidth": 1,
            "categoryField": "Reference",
            "categoryAxis": {
              "categoryField": "Comparing",
              "axisAlpha": 0,
              "gridAlpha": 0,
              "position": "left",
              "gridPosition": "start",

            },
            "export": {
              "enabled": true
            }
          };

          var repeatChart = {
              sourceData: datachartRepeat.sourceData,
              graphs: datachartRepeat.graphs,
              data: configRepeat,
              graph:false

          };

          self.setState({ repeatChart: repeatChart});


        })
        .catch(this.onFileFailure);

    }// end of if finished

  }// end of funcion onStatusSuccess

  onStatusFailure(response) {
    this.setState({
      loaded: true,
      found:  false,
      uuid:   '',
      status: {},
    });
  }

  onFileFailure(response){
    this.setState({ heatmaps: false });
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
        
        { this.state.found
        ? <ShowProject project={this.state.project} analysisData={this.state.analysisData}
        configIdentity={this.state.identityChart.config} graphIdentity={this.state.identityChart.graph}  
        configRepeat={this.state.repeatChart.config} graphRepeat={this.state.repeatChart.graph} heatmaps={this.state.heatmaps} />
        : <ShowNotFound uuid={this.state.uuid} />
        }
          
      </div>
    );
  }

}


export default Analysis;
