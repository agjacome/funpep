import React from 'react';
import Main from './Main';

import { Router, Route, browserHistory } from 'react-router';

const App = () => {
  return (
    <Router history={browserHistory}>
      <Route path="/" component={Main}>
      </Route>
    </Router>
  );
}

export default App;
