import React from 'react';

import { Col, Grid, Row } from 'react-bootstrap';

const Content = ({children}) => {
  return (
    <Grid>
      <Row>
        <Col xs={10} xsOffset={1}>
          {children}
        </Col>
      </Row>
    </Grid>
  );
}

export default Content;
