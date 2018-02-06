import React from 'react';

import Main     from '../components/Main';
import Home     from '../components/Home';
import Check    from '../components/Check';
import Status   from '../components/Status';
import Help     from '../components/Help';
import About    from '../components/About';
import Report   from '../components/Report';
import New      from '../components/New';
import Project  from '../components/Project';

import { Route, IndexRoute } from 'react-router';

export default (
  <Route path="/" component={Main}>
    <IndexRoute component={Home} />

    <Route path="/new" component={New} />
    <Route path="/project/:uuid" component={Project} />

    <Route path="/check"        component={Check} />
    <Route path="/status/:uuid" component={Status} />
    <Route path="/report/:uuid" component={Report} />

    <Route path="/help"  component={Help} />
    <Route path="/about" component={About} />
  </Route>
);
