import React from 'react';
import axios   from 'axios';
import Base  from './Base';
import ReactFileReader from 'react-file-reader';
import base64 from 'base-64';
import { sendAnalysis } from '../utils/api_helpers';
import { Input, ButtonInput } from 'react-bootstrap';
import Dropzone from 'react-dropzone';
import ReactSpinner from 'reactjs-spinner';
import { funpep_url, funprot_url } from '../config/api_url'

function ShowFirstFasta(fasta) {
  const array = (
    <ul id="fastaList">
      {fasta.fasta.acceptedFasta.map((fastaFile) =>
        <li  key={fastaFile.name} className={fastaFile.status}>
            {fastaFile.status} : {fastaFile.name} 
        </li>
      )}
    </ul>
  );  
  return (<div>{array}</div>);
}
function ShowResults(result) {   
  var array = (<span />); 
  var link = "project/"+result.result;
  if(result.result && 0 != result.result.length){
    if(result.result == "error"){
    array = (
        <span>There was an error in communication with the server, please try again.</span>
      );
    }else{
      array = (
        <span>Your new analysis has been created correctly, it is available in the following link: <a target="_blank" href={link} >
              {funpep_url}/project/{result.result}</a><br/></span>
      );
      document.getElementById("button-input").onclick = function(e){e.preventDefault();location.href='new'}
      document.getElementById("button-input").value = "Clear form";
    }
  }
  return (<div>{array}</div>);
}
function parseFasta(formatFasta){
  var state = false;
  try {
    var fasta = require('fasta-parser');
    var fastaData = new Buffer(formatFasta);
    var parser = fasta();
    parser.on('data', function(data) {});
    state = true;
    parser.write(fastaData);

    var regExp = new RegExp(('^>.*'),"gim");
    var matches = null;
    var i = 0;
    var sequences = [];
    while (matches = regExp.exec( formatFasta )){
      sequences[i] = matches[0];
      i++;
    }
    var j = 0;
    var l = 0;
    var repeat = false;
    var repeatArray = [];
    while ( j<sequences.length && !repeat )
    {
      for (var k = 0; k<repeatArray.length; k++)
      {
        if (repeatArray[k] == sequences[j])
        {
          repeat = true;
          state = false;
        }
      }
      repeatArray[l] = sequences[j];
      l++;
      j++;
    }
    parser.end();
  }
  catch(err) {
    status = 'Error in format Fasta';
    var state = false;
  } 
  return state;
}

class New extends Base {

  constructor(props) {
      super(props);

      this.state = {
        acceptedFasta: [],
        rejectedFasta: [],
        referenceFasta: {},
        threshold: '',
        result: ''
      };   

      this.bindThis(
        'dealAccepted',
        'dealRejected',
        'updateThreshold',
        'submitForm',
        'deleteElement',
        'dropComparing',
        'dropReference'
      );
  }
  
  containsKey(vendors, key){
    var found = false;
    for(var i = 0; i < vendors.length; i++) {
      if (vendors[i].key == key) {
          found = true;
          break;
      }
    }
    return found;
  }
  
  updateThreshold(e){
     this.setState({threshold: this.refs.threshold.getValue()});
  }

  deleteElement(key, state, callback){    
    var removed = [];
    if(state.length == 1){
        callback(removed);
    }else{
      for(var i = 0; i<state.length; i++){
        if(state[i].key != key){
          removed.push(state[i]);
        }
      }
      callback(removed);
    }  
  }
  
  dealRejected(rejected){
    var rejectedFasta = [];
    rejected.forEach(file =>{        
      if (file.size >= 3145728 ){
        rejectedFasta.push({
          'name': file.name,
          'size': file.size,
          'key': file.size + file.lastModified,
          'status': 'The file size exceeds 3 MB'
        });       
      }else{
        rejectedFasta.push({
          'name': file.name,
          'size': file.size,
          'key': file.size + file.lastModified,
          'status': 'The file have not the correct extension'
        });
      }
    }); 
    return rejectedFasta;
  }
  
  dealAccepted(accepted, callback){
    var acceptedFasta = {};
    var rejectedFasta = {};
    accepted.forEach(file =>{
      const reader = new FileReader();
      reader.onload = () => {
        var formatFasta = reader.result.replace(/(?:\r\n|\r|\n)/g, '\n');            
        if (parseFasta(formatFasta) == true){
          acceptedFasta = {
            'name': file.name,
            'size': file.size,
            'key': file.size + file.lastModified,
            'fasta': formatFasta,
            'status': 'Success'
          };
        }else{
            rejectedFasta = {
            'name': file.name,
            'size': file.size,
            'key': file.size + file.lastModified,
            'status': 'Error in format fasta'
          };
        }   
        callback(acceptedFasta, rejectedFasta);
      }    
      reader.onabort = () => console.log('File reading was aborted');
      reader.onerror = () => console.log('File reading has failed');
      reader.readAsBinaryString(file);        
    });   
  }

  dropComparing(accepted, rejected) {     
    var self = this;
    var rejectedFasta = this.state.rejectedFasta;
    this.dealRejected(rejected).forEach(f => {
      if(!self.containsKey(rejectedFasta, f.key)){
        rejectedFasta.push(f);
      }
    });    
    this.setState({       
      rejectedFasta: rejectedFasta
    });
    this.dealAccepted(accepted, function(a,r){
      var rF = self.state.rejectedFasta;
      var aF = self.state.acceptedFasta;
      if(Object.keys(a).length != 0 && !self.containsKey(aF, a.key)){
        aF.push(a);
      }
      if(Object.keys(r).length != 0 && !self.containsKey(rF, r.key)){
        rF.push(r);
      }
      self.setState({       
        acceptedFasta: aF,
        rejectedFasta: rF
      });
    });
    
  }

  dropReference(accepted, rejected) {
   var self = this;
   var rejectedFasta = this.dealRejected(rejected);
   if(rejectedFasta.length>0){
     this.setState({
       referenceFasta: rejectedFasta[0]
     });
   }else{
     this.dealAccepted(accepted, function(a,r){
       if(Object.keys(a).length != 0 || Object.keys(r).length != 0){
         var reference;
         if(Object.keys(a).length != 0){
           reference = a;
         }else{
           reference = r;
         }
         self.setState({       
           referenceFasta: reference
         });
       }
      }); 
    }   
  }

  submitForm(e){
    e.preventDefault();
    
    var self = this; 
    var spinnerDiv = document.getElementById("spinner-div");
    spinnerDiv.classList.remove("d-none");
    
    var xml = "<?xml version='1.0' encoding='utf-8'?><loadAnalysis><reference>"+this.state.referenceFasta.name.replace(".faa","").replace(".fasta","")+"</reference><analyses>";
    var promises = [];

    for (var i = 0; i<this.state.acceptedFasta.length; i++){ 
      var json ={
        'reference': this.state.referenceFasta.fasta,
        'comparing': this.state.acceptedFasta[i].fasta,
        'threshold': parseFloat(this.state.threshold),
        'file': this.state.acceptedFasta[i].name,
        'annotations':{}
      }      
      promises.push(sendAnalysis(json));  
    }
       
    axios.all(promises)
    .then(function (results) {
      results.forEach(function(response) {
        xml = xml + "<analysis><project/><uuid>" + response.data.uuid+"</uuid><comparing>"+JSON.parse(response.config.data).file.replace(".faa","").replace(".fasta","")+"</comparing></analysis>";        
      })     
      xml  = xml+"</analyses></loadAnalysis>";
      
      axios.post(`${funprot_url}/analysis/`, xml, {
        headers: {
            'Content-Type': 'application/xml'
        }}).then(function(response){
          self.setState({result: new DOMParser().parseFromString(response.data,"text/xml").getElementsByTagName("uuid")[0].textContent});
          spinnerDiv.classList.add("d-none");
      }).catch(error => {
          self.setState({result: "error"});
          spinnerDiv.classList.add("d-none");
      })
    });
  }

  render() {
    var disabledButton = false;
    if( this.state.threshold != '' && this.state.threshold > 0 && this.state.threshold <= 100 ){
      if( this.state.rejectedFasta.length == 0 && this.state.acceptedFasta.length > 0 && this.state.referenceFasta.status == 'Success' ){
        disabledButton = true;
      }
    }  
    
    return (
      <div className="content p-relative">
        <div id="spinner-div" className="d-none"><ReactSpinner size={130} borderColor='#dbdbdb' borderTopColor='#c0392b'>Example</ReactSpinner></div>
        <h3>Upload a new project</h3>
        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p>
      
        <div className="row">
          <div className="col-lg-6">
            <label>FASTA comparing file(s)</label>
            <div className="dropzone">
              <Dropzone maxSize={3145728} onDrop={this.dropComparing} accept={".faa, .fasta"}>
               <p>Try dropping some files here, or click to select files to upload.</p>
              </Dropzone>
            </div>
            <div>
              <ul id="fastaList">
                {this.state.acceptedFasta.map((fastaFile) => 
                  <li className="success" key={fastaFile.key}>{fastaFile.name}<button type="button" onClick={this.deleteElement.bind(null, fastaFile.key, this.state.acceptedFasta, function(removed){this.setState({acceptedFasta: removed})}.bind(this))} className="close pull-left" aria-label="Close">
  <span aria-hidden="true">&times;</span>
</button><span className="clear"></span></li>
                )}
              </ul>
              <ul id="fastaList">
                {this.state.rejectedFasta.map((fastaFile) => 
                  <li className="failure" key={fastaFile.key}>{fastaFile.name} - {fastaFile.status} <button type="button" onClick={this.deleteElement.bind(null, fastaFile.key, this.state.rejectedFasta, function(removed){this.setState({rejectedFasta: removed})}.bind(this))} className="close pull-left" aria-label="Close">
  <span aria-hidden="true">&times;</span>
</button><span className="clear"></span></li>
                )}
              </ul>
            </div>            
          </div>

          <div className="col-lg-6">
            <label>FASTA reference file</label>
            <div className="dropzone">
              <Dropzone maxSize={3145728} multiple={false} onDrop={this.dropReference} accept={".faa, .fasta"}>
               <p>Try dropping some files here, or click to select files to upload.</p>
              </Dropzone>
            </div>
            <div id="referenceFastaFile">
              <ul id="fastaList">
                <li className={this.state.referenceFasta.status}>{this.state.referenceFasta.name}</li>
              </ul>              
            </div>           
          </div>
        </div>
        <br/>
        <div className="row">
          <div className="col-lg-3">
            <label>Alignment threshold</label>
              <Input type="number" min="0.00" max="100.00" step="0.01" placeholder="Enter threshold" ref="threshold" onChange={this.updateThreshold}  />
            </div>
            <div className="col-lg-9"></div>
          </div>
          <div className="buttons">
            <ButtonInput id="button-input" type="submit" value="New project"  disabled={!disabledButton}  onClick={this.submitForm} />
          </div>          
            <ShowResults result={this.state.result} />            
        </div>

    );
  }
}

export default New