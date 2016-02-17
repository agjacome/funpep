import React from 'react';

import { ButtonToolbar, Button } from 'react-bootstrap';
import { LinkContainer }         from 'react-router-bootstrap';

const Home = () => {
  return (
    <div className="home-content">
      <h2>funpep</h2>
      <h3>Functional enrichment of peptide datasets, version 0.1.0-<strong>alpha</strong></h3>
      <p>
         Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc vehicula
         velit id diam sagittis dignissim. Duis dictum feugiat turpis fermentum
         hendrerit. Pellentesque ullamcorper iaculis est id finibus. In sit
         amet lacinia lacus, at scelerisque augue. Duis rhoncus eu ex id
         condimentum. Maecenas a neque rutrum, convallis odio id, pretium dui.
         Sed eu lorem quis tortor tincidunt tincidunt. Etiam vel nisl rutrum,
         molestie leo a, fringilla lectus. Praesent cursus euismod metus ut
         suscipit. Pellentesque at semper libero. Praesent lacinia laoreet
         velit, eget placerat eros efficitur sed. Nam lobortis eleifend
         egestas. Nulla efficitur, ligula eget fringilla convallis, velit diam
         ultricies ipsum, eget tempor purus mauris eu lectus.
      </p>
      <div className="buttons">
        <ButtonToolbar>
          <LinkContainer to={'/analysis'}>
            <Button bsStyle="info" bsSize="large">New Analysis</Button>
          </LinkContainer>
          <LinkContainer to={'/status'}>
            <Button bsStyle="info" bsSize="large">Check Status</Button>
          </LinkContainer>
        </ButtonToolbar>
      </div>
    </div>
  );
}

export default Home;
