import React from 'react';

import { ButtonToolbar, Button } from 'react-bootstrap';
import { LinkContainer }         from 'react-router-bootstrap';

const Home = () => {
  return (
    <div className="home-content">
      <h2>Serpent</h2>
      <h3> SEquence-based, Rapid Proteome ENrichment Tool, version 0.1.0-<strong>alpha</strong></h3>
      <p>
         The purpose of Serpent is to automatically arrange and enrich sets of amino acid sequences. In order to do that, the process always starts with two or more protein data sets: a reference one and anothers to be enriched and functionally annotated. <br/> 
         Each file will be enriched and annotated individually using the reference file by the following steps: a similarity filter is performed by computing the distance between each sequence alignment of both sets. In that way, those proteins that do not exceed a previously established threshold are deleted and a report is generated detailing the similarity percentage achieved between those that do have passed the threshold. Moreover, a phylogenetic tree is also generated with those proteins and the reference set, displaying in a visual manner the relationships between the reference sequences and the resulting ones. SERPENT also integrates an biomedical ontology based protein annotator, adding the possibility of manually annotate the reference set and, with those annotations, obtaining a filtered set automatically annotated by using their similarity level with those first ones

      </p>
      <div className="buttons">
        <ButtonToolbar>
          <LinkContainer to={'/new'}>
            <Button bsStyle="info" bsSize="large">New Project</Button>
          </LinkContainer>
          <LinkContainer to={'/check'}>
            <Button bsStyle="info" bsSize="large">Check Status</Button>
          </LinkContainer>
        </ButtonToolbar>
      </div>
    </div>
  );
}

export default Home;
