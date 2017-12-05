import React from 'react';
import Base  from './Base';
import ReactFileReader from 'react-file-reader';
import base64 from 'base-64';
import { sendAnalysis } from '../utils/api_helpers';
//import fasta-parse from 'fasta-parse';
import { Input, ButtonInput } from 'react-bootstrap';
import Dropzone from 'react-dropzone';

function ShowFirstFasta(fasta) {
  const array = (
    <ul id="fastaList">
      {fasta.fasta.acceptedFasta.map((fastaFile) =>
        <li  key={fastaFile.name} className={fastaFile.status}>
            {fastaFile.status} : {fastaFile.name} - Size {fastaFile.size} 
        </li>
      )}
    </ul>
  );
  
  return (<div>{array}</div>);
}


/// function for show results
function ShowResults(fasta) {
   
  const array = (
    <ul id="resultList">
      {fasta.fasta.results.map((result) =>
        
        <li key={result.uuid}>
          <a target="_blank" href={result.link} >
              {result.uuid}
          </a>
        </li>
      )}
    </ul>
  );
  
  return (<div>{array}</div>);
}
/// function parse fasta
function parseFasta(formatFasta){
      var state = false;
      try {
          var fasta = require('fasta-parser');
          var fastaData = new Buffer(formatFasta);
          var parser = fasta();
         
          parser.on('data', function(data) {              
              
          });
          state = true;
          parser.write(fastaData);
          parser.end();
          
        }
      catch(err) {
          status = 'Error in format Fasta';
          var state = false;
      } 
        return state;
}



class Analysis extends Base {

  constructor(props) {
      super(props);

      this.state = {
        acceptedFasta: [],
        rejectedFasta: [],
        secondFastaFile: {},
        threshold: '',
        results:[]
      };

      


      this.bindThis(
        'UpdateThreshold',
        'submitForm',
        'dropFirstFasta',
        'deleteElement',
        'deleteRejectedElement',
        'dropSecondFasta'

      );
  }


  UpdateThreshold(e){
     var formThreshold = this.refs.threshold.getValue();
     this.setState({threshold: formThreshold});

  }

  deleteElement(key){
    
    var removed = [];
    if ( this.state.acceptedFasta.length == 1)
    {
        this.setState({acceptedFasta: removed});
    }else{
      for ( var i = 0; i<this.state.acceptedFasta.length; i++)
      {
        if ( this.state.acceptedFasta[i].key != key)
        {
          removed.push(this.state.acceptedFasta[i]);
        }
      }
      this.setState({acceptedFasta: removed});

    }
    
    
  }// end of function delete rejected element

  deleteRejectedElement(key){
   
    var removed = [];
    if ( this.state.rejectedFasta.length == 1)
    {
        this.setState({rejectedFasta: removed});
    }else{
      for ( var i = 0; i<this.state.rejectedFasta.length; i++)
      {
        if ( this.state.rejectedFasta[i].key != key)
        {
          removed.push(this.state.rejectedFasta[i]);
        }
      }
      this.setState({rejectedFasta: removed});

    }
    
    
  }// end of function delete element


  dropFirstFasta(accepted, rejected) {
    var arrayFastaFiles = this.state.acceptedFasta;
    var rejectedFastaFiles = this.state.rejectedFasta;

    if ( rejected.legnth != 0 )
    {
      rejected.forEach(file =>{
        
        if ( file.size >= 3145728 )
        {
          var name = file.name;
          var size = file.size;
          var key = file.size + file.lastModified;
          status = 'The file size exceeds 3 MB ';
              rejectedFastaFiles.push({
                'name': name,
                'size': size,
                'key': key,
                'status': status
              });
              this.setState({rejectedFasta: rejectedFastaFiles});
        
        }else{
          var name = file.name;
          var size = file.size;
          status = 'The file have not the correct extension';
              rejectedFastaFiles.push({
                'name': name,
                'size': size,
                'key': key,
                'status': status
              });
              this.setState({rejectedFastaFiles: rejectedFastaFiles});

        }

      });
    }// end of if rejected length
     
    accepted.forEach(file =>{
        const reader = new FileReader();
          reader.onload = () => {
          var name = file.name;
          var size = file.size;
          var key = file.size + file.lastModified;
          
          const fasta = reader.result;
          var formatFasta = fasta.replace(/(?:\r\n|\r|\n)/g, '\n');
          //var formatFasta = formatFasta.concat('\\n');
          //indentify format Fasta
          var correctFormat = parseFasta(formatFasta);

          if ( correctFormat == true ){
              status = 'Success';
              arrayFastaFiles.push({
                  'name': name,
                  'size': size,
                  'key': key,
                  'fasta': formatFasta,
                  'status': status
                });
          }else{
            status = 'Error in format fasta';
            rejectedFastaFiles.push({
                  'name': name,
                  'size': size,
                  'key': key,
                  'status': status
                });
          }
          
          this.setState({referenceFasta: rejectedFastaFiles});

        }
        

        reader.onabort = () => console.log('file reading was aborted');
        reader.onerror = () => console.log('file reading has failed');

        reader.readAsBinaryString(file);

        
    });
    
  }// end of function drop first fasta

  dropSecondFasta(accepted, rejected) {
    var arrayFastaFiles = [];
    var rejectedFastaFiles = [];

    if ( rejected.legnth != 0 )
    {
      rejected.forEach(file =>{
        
        if ( file.size >= 3145728 )
        {
          var name = file.name;
          var size = file.size;
          var key = file.size + file.lastModified;
          status = 'The file size exceeds 3 MB ';
              rejectedFastaFiles= {
                'name': name,
                'size': size,
                'key': key,
                'status': status
              };
          this.setState({secondFastaFile: rejectedFastaFiles});
        
        }else{
          var name = file.name;
          var size = file.size;
          status = 'The file have not the correct extension';
              rejectedFastaFiles = {
                'name': name,
                'size': size,
                'key': key,
                'status': status
              };
          this.setState({secondFastaFile: rejectedFastaFiles});

        }

      });
    }// end of if rejected length
     
    accepted.forEach(file =>{
        const reader = new FileReader();
          reader.onload = () => {
          var name = file.name;
          var size = file.size;
          var key = file.size + file.lastModified;
          
          const fasta = reader.result;
          var formatFasta = fasta.replace(/(?:\r\n|\r|\n)/g, '\n');
          //var formatFasta = formatFasta.concat('\\n');
          //indentify format Fasta
          var correctFormat = parseFasta(formatFasta);

          if ( correctFormat == true ){
              status = 'Success';
              arrayFastaFiles = {
                  'name': name,
                  'size': size,
                  'key': key,
                  'fasta': formatFasta,
                  'status': status
                };
          }else{
            status = 'Error in format fasta';
            arrayFastaFiles = {
                  'name': name,
                  'size': size,
                  'key': key,
                  'status': status
                };
          }
          this.setState({secondFastaFile: arrayFastaFiles});
        }
        

        reader.onabort = () => console.log('file reading was aborted');
        reader.onerror = () => console.log('file reading has failed');

        reader.readAsBinaryString(file);

        
    });
    
  }// end of function drop seconfasta


  

  submitForm(e){
    e.preventDefault();
    var link = "https://sing-group.org/funpep/status/";
    var resultArray = [];

    for (var i = 0; i<this.state.acceptedFasta.length; i++)
    {

      //create json for api 
      var comparingFasta = this.state.acceptedFasta[i].fasta;
      var referenceFasta = this.state.secondFastaFile.fasta;

      var json ={
        'reference': referenceFasta,
        'comparing': comparingFasta,
        'threshold': parseFloat(this.state.threshold),
        'annotations':{}
      }

      var self = this;
      sendAnalysis(json)
      .then(function (response) {
        link = link.concat(response.data.uuid);
        var  result = {
            'status': response.data.status.status,
            'threshold': response.data.threshold,
            'uuid': response.data.uuid,
            'link': link
          }
          resultArray.push(result);
          self.setState({results: resultArray});

      })
    
    }
  }// end of submit function



  render() {
   var disabledButton = false;
    // disabled / enabled button
   

     if( this.state.threshold != '' && this.state.threshold > 0 && this.state.threshold <= 100 ) 
     {
        if( this.state.rejectedFasta.length == 0 && this.state.acceptedFasta.length > 0 && this.state.secondFastaFile.status == 'Success' )
        {
          disabledButton = true;
        }
     }
      
    return (

      <div className="content">
        <h3>Upload a new analysis</h3>
        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p>
        <section>
        
        

      </section>

        <div className="row">
          <div className="col-lg-6">
            <label>FASTA comparing file</label>
            <div className="dropzone">
              <Dropzone maxSize={3145728} onDrop={this.dropFirstFasta.bind(this)} accept={".faa, .fasta"}>
               <p>Try dropping some files here, or click to select files to upload.</p>
              </Dropzone>
            </div>
            <div>
              <ul id="fastaList">
                {this.state.acceptedFasta.map((fastaFile) => 
                  <li className="Success" key={fastaFile.key}>{fastaFile.name} - {fastaFile.size} <button className="btn btn-default circleButton" onClick={this.deleteElement.bind(null, fastaFile.key)}> X </button></li>
                )}
              </ul>
              <ul id="fastaList">
                {this.state.rejectedFasta.map((fastaFile) => 
                  <li key={fastaFile.key}>{fastaFile.name} - {fastaFile.size} - {fastaFile.status} <button className="btn btn-default circleButton" onClick={this.deleteRejectedElement.bind(null, fastaFile.key)}> X </button></li>
                )}
              </ul>
            </div>
            
          </div>


          <div className="col-lg-6">
            <label>FASTA reference file</label>
            <div className="dropzone">
              <Dropzone maxSize={3145728} multiple={false} onDrop={this.dropSecondFasta.bind(this)} accept={".faa, .fasta"}>
               <p>Try dropping some files here, or click to select files to upload.</p>
              </Dropzone>
            </div>
            <div id="referenceFastaFile">
              <p className={this.state.secondFastaFile.status}> {this.state.secondFastaFile.status} {this.state.secondFastaFile.name}{this.state.secondFastaFile.name ? ' - Size:' : ''} {this.state.secondFastaFile.size} </p>
            </div>
           
          </div>
        </div>
        <br/>
        <div className="row">
          <div className="col-lg-3">
            <label>Alignment threshold</label>
              <Input type="number" min="0.00" max="100.00" step="0.01" placeholder="Enter threshold" ref="threshold" onChange={this.UpdateThreshold}  />
            </div>
            <div className="col-lg-9"></div>
          </div>
          <div className="buttons">
            <ButtonInput type="submit" value="Check"  disabled={!disabledButton}  onClick={this.submitForm} />
          </div>          
            <ShowResults fasta={this.state} />
        </div>

    );
  }
}



export default Analysis