import React from 'react';

import { ButtonToolbar, Button } from 'react-bootstrap';
import { LinkContainer }         from 'react-router-bootstrap';

const Home = () => {
  return (
    <div className="home-content">
      <h2>funpep</h2>
      <h3>Functional enrichment of peptide datasets, version 0.1.0-<strong>alpha</strong></h3>
      <p>
         The purpose of FunPep is to automatically arrange and enrich sets of amino acid sequences. In order to do that, the process always starts with two peptide data sets: a reference one and another to be enriched and functionally annotated. Next, a similarity filter is performed by computing the distance between each sequence alignment of both sets. In that way, those peptides that do not exceed a previously established threshold are deleted and a report is generated detailing the similarity percentage achieved between those that do have passed the threshold. Moreover, a phylogenetic tree is also generated with those peptides and the reference set, displaying in a visual manner the relationships between the reference sequences and the resulting ones. FunPep also integrates an biomedical ontology based peptide annotator, adding the possibility of manually annotate the reference set and, with those annotations, obtaining a filtered set automatically annotated by using their similarity level with those first ones.
      </p>
      <div className="buttons">
        <ButtonToolbar>
          <LinkContainer to={'/new'}>
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
