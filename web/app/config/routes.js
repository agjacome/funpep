import React from 'react';

import Main   from '../components/Main';
import Home   from '../components/Home';
import Check  from '../components/Check';
import Status from '../components/Status';
import Help   from '../components/Help';
import About  from '../components/About';

import { Route, IndexRoute } from 'react-router';

export default (
  <Route path="/" component={Main}>
    <IndexRoute component={Home} />

    <Route path="/analysis" component={Home} />

    <Route path="/status"       component={Check} />
    <Route path="/status/:uuid" component={Status} />
    <Route path="/report/:uuid" component={Home} />

    <Route path="/help"  component={Help} />
    <Route path="/about" component={About} />
  </Route>
);
