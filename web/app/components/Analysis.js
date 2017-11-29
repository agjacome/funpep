import React from 'react';
import Base  from './Base';
import ReactFileReader from 'react-file-reader';
import base64 from 'base-64';
import { sendAnalysis } from '../utils/api_helpers';
//import fasta-parse from 'fasta-parse';
import { Input, ButtonInput } from 'react-bootstrap';

function ShowFirstFasta(fasta) {
  const array = (
    <ul id="fastaList">
      {fasta.fasta.firstFastaFiles.map((fastaFile) =>
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
        firstFastaFiles: [],
        secondFastaFile: {},
        threshold: '',
        results:[],
        disabledButton: ''
      };


      this.bindThis(
        'UpdateThreshold',
        'submitForm',
        'createFirstFasta',
        'createSecondFasta'
      );
  }


  UpdateThreshold(e){
     var formThreshold = this.refs.threshold.getValue();
     this.setState({threshold: formThreshold});

  }


  createFirstFasta(files) {      
    var arrayFastaFiles = [];
    /// read fasta files
    for(var i =0; i<files.fileList.length; i++)
    {
      //take file atributtes 
      var name  = files.fileList[i].name;
      var size = files.fileList[i].size;

      // filter for size
      if( size <= 3145728 )
      {
        
        //take and format fasta sequence
        var encodeFasta = files.base64[i].substring(13, files.base64[i].length);
        var decodeFasta = base64.decode(encodeFasta);
        var formatFasta = decodeFasta.replace(/(?:\r\n|\r|\n)/g, '\n');
        var formatFasta = formatFasta.concat('\\n');
        
        //indentify format Fasta
        var correctFormat = parseFasta(formatFasta);
        

        if ( correctFormat == true ){
            status = 'Success';
            arrayFastaFiles.push({
                'name': name,
                'size': size,
                'fasta': formatFasta,
                'status': status
              });
        }else{
          this.setState({disabledButton: 'error'});
          status = 'Error in format fasta';
          arrayFastaFiles.push({
                'name': name,
                'size': size,
                'status': status
              });
        }
        
        this.setState({firstFastaFiles: arrayFastaFiles});
        

      }else{
          status = 'The file size exceeds 3 MB ';
          arrayFastaFiles.push({
            'name': name,
            'size': size,
            'status': status
          });
          this.setState({firstFastaFiles: arrayFastaFiles});
      
      }// end of if/else filter size
      
      
    }// end of loop for of files    
    
  }

  createSecondFasta(files){
  
    var name = files.fileList[0].name;
    var size = files.fileList[0].size;
    var status = '';
    if( size <= 3145728 )
    {
         //take and format fasta sequence
        var encodeFasta = files.base64.substring(13, files.base64.length);
        var decodeFasta = base64.decode(encodeFasta);
        var formatFasta = decodeFasta.replace(/(?:\r\n|\r|\n)/g, '\n');
        var formatFasta =  formatFasta.concat('\n');
        
        //indentify format Fasta
        var correctFormat = parseFasta(formatFasta);
        if ( correctFormat == true)
        {
          status = 'Success';
          var fasta = {
            'name': name,
            'size': size,
            'fasta': formatFasta,
            'status': status
          }
        }else{
          this.setState({disabledButton: 'error'});
          status = 'Error in format fasta';
          var fasta = {
            'name': name,
            'size': size,
            'status': status
          }
        }


        this.setState({secondFastaFile: fasta});

    }else{
        var fasta = {
            'name': name,
            'size': size,
            'status': 'The file size exceeds 3 MB'
        }
        this.setState({secondFastaFile: fasta});
        
    }
  
  }// end of create second fasta function

  submitForm(e){
    e.preventDefault();
    console.log(this.state)
    var link = "https://sing-group.org/funpep/status/";
    var resultArray = [];

    for (var i = 0; i<this.state.firstFastaFiles.length; i++)
    {

      //create json for api 
      var comparingFasta = this.state.firstFastaFiles[i].fasta;
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
          console.log(resultArray);

          //console.log(response.data);
      })
    
    }
  }


  render() {
   var disabledButton = false;
    // disabled / enabled button
     if( this.state.threshold != '' && this.state.threshold > 0 && this.state.threshold <= 100 && this.state.disabledButton != 'error' )
     {
        disabledButton = true;
     }
      
    return (
        <div className="content">
          <h3>Check analysis status</h3>
          
          
            <label> Upload the comparing files </label>
            <ReactFileReader multipleFiles={true} fileTypes={[".faa",".fasta"]} base64={true} handleFiles={this.createFirstFasta}>
              <button className='btn btn-default'>Upload comparing files</button>
            </ReactFileReader>

            <ShowFirstFasta fasta={this.state} />


            <label> Upload the reference file </label>
            <ReactFileReader multipleFiles={false} fileTypes={[".faa",".fasta"]} base64={true} handleFiles={this.createSecondFasta}>
              <button className='btn btn-default'>Upload reference file</button>
            </ReactFileReader>

            <div id="referenceFastaFile">
              <p className={this.state.secondFastaFile.status}> {this.state.secondFastaFile.status} {this.state.secondFastaFile.name}{this.state.secondFastaFile.name ? ' - Size:' : ''} {this.state.secondFastaFile.size} </p>
            </div>

            <Input type="text" placeholder="Enter threshold" ref="threshold" onChange={this.UpdateThreshold}  />
            
            <div className="buttons">
                <ButtonInput type="submit" value="Check"  disabled={!disabledButton} onClick={this.submitForm} />
            </div>
          
            <ShowResults fasta={this.state} />
        </div>

    );
  }
}



export default Analysis