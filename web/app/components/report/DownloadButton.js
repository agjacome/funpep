import React  from 'react';
import Base   from '../Base';
import { file,  image } from '../../utils/api_helpers';

class DownloadButton extends Base {
  constructor(props) {
    super(props);
    
    this.bindThis(
      'onFileSuccess',
      'onFileFailure',
      'handleClick'
    );
  }    
  
  handleClick() {
   file(this.props.uuid, this.props.name)
    .then(this.onFileSuccess)
    .catch(this.onFileFailure);     
  }
  
  onFileSuccess(response){
    var fileDownload = require('react-file-download');    
    fileDownload(response.data, this.props.name);
  }
  
  onFileFailure(response){
    alert('File not found.');
  } 
  
  render() {
      return (
          <a href="#" onClick={this.handleClick}>{this.props.span}</a>
      );
  }
}

export default DownloadButton;