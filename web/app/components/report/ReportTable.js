import React  from 'react';
import Base   from '../Base';
import { BootstrapTable, TableHeaderColumn } from 'react-bootstrap-table';
import '../../../node_modules/react-bootstrap-table/dist/react-bootstrap-table-all.min.css';

class ReportTable extends Base {
 constructor(props) {
    super(props);

    this.options = {
      defaultSortName: 'similarity',
      defaultSortOrder: 'desc' 
    };
  }
  
  render() {
    return (
      <div>
        <BootstrapTable ref='table' data={ this.props.products } options={ this.options } striped hover condensed >
            <TableHeaderColumn dataField='comparing' isKey dataSort>Comparing ID</TableHeaderColumn>
            <TableHeaderColumn dataField='reference' dataSort>Reference ID</TableHeaderColumn>
            <TableHeaderColumn dataField='similarity' dataSort>Similarity Percentage</TableHeaderColumn>
        </BootstrapTable>
      </div>
    );
  }
}

export default ReportTable;