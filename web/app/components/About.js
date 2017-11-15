import React from 'react';

const About = () => {
  return (
    <div className="col-lg-12 content">
      <h3>About us</h3><br />
      <div className="row grey-container">          
            Please send comments concerning:
            <ul>
                <li>General development and scientific contents to Dr. Anália Lourenço(<a href="mailto:analia@uvigo.es?cc=borja.sanchez@csic.es&amp;subject=[Funpep]" itemprop="email">analia@uvigo.es</a>) and Dr. Borja Sánchez (<a href="mailto:borja.sanchez@csic.es?cc=analia@uvigo.es&amp;subject=[Funpep]" itemprop="email">borja.sanchez@csic.es</a>)
                </li>
                <li>Computing and user interfaces to core developers Aitor Blanco Míguez (<a href="mailto:aiblanco@uvigo.es&amp;subject=[Funpep]" itemprop="email">aiblanco@uvigo.es</a>) and Alberto Gutierrez Jácome (<a href="mailto:agjacome@esei.uvigo.es&amp;subject=[Funpep]" itemprop="email">agjacome@esei.uvigo.es</a>)
                </li>
            </ul>
        </div>
        <br/><br/>
        <h4><b>Our addresses</b></h4>

        <div className="row">
            <div className="col-lg-6">
                <p>Department of Computer Science<br/>
                University of Vigo<br/>
                ESEI - Escuela Superior de Ingeniería Informática Edificio politécnico<br/>
                Campus Universitario As Lagoas s/n<br/>
                32004 Ourense, Spain<br/></p>
            </div>
            <div className="col-lg-6">
                <p>Department of Microbiology and Biochemistry<br/>
                Instituto de Productos Lácteos de Asturias<br/>
                CSIC - Consejo Superior de Investigaciones Científicas<br/>
                Paseo Río Linares s/n<br/>
                33300 Villaviciosa, Asturias, Spain<br/>
                </p>
            </div>                         
        </div>
        <br/><br/>
        <h4><b>Team</b></h4>
        <div className="row">
            <div className="col-lg-4">
                analia@uvigo.es<br/>
                borja.sanchez@csic.es<br/>
                riverola@uvigo.es<br/>
                aiblanco@uvigo.es<br/>
                agjacome@esei.uvigo.es
            </div>
            <div className="col-lg-4 center">
                <a href="http://www.ceb.uminho.pt/People/Details/7ecbdb79-4397-4665-8259-197d2d562708" target="_blank">Dr. Anália Lourenço</a><br/>
                <a href="https://www.researchgate.net/profile/Borja_Sanchez" target="_blank">Dr. Borja Sanchez García</a><br/>
                <a href="http://sing.ei.uvigo.es/~riverola/" target="_blank">Dr. Florentino Fdez-Riverola</a><br/>
                <a href="https://www.researchgate.net/profile/Aitor_Blanco-Miguez" target="_blank"> Aitor Blanco Míguez</a><br/>
                Alberto Gutierrez Jácome
            </div>  
            <div className="col-lg-4 right">
                Project leader<br/>
                Project leader<br/>
                Since 2015<br/>
                Since 2015<br/>
                Since 2015
            </div>                        
        </div><br/><br/>
        <h4><b>Funding</b></h4>
        <div className="row centered mt grid">
            <div className="col-lg-5 col-lg-offset-1">
                <a target="_blank" href="http://www.mineco.gob.es/"><img className="img-responsive" src={require('../img/mec-w.png')} alt="Mineco" /></a>
            </div>
            <div className="col-lg-5">
                <a target="_blank" href="https://www.aecc.es/Paginas/PaginaPrincipal.aspx"><img className="img-responsive" src={require('../img/aecc-w.png')} alt="AECC" /></a>
            </div>
        </div>
        <br/>                                                                                                                  
    </div>
  );
}

export default About;
