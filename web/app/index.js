import 'core-js';
import 'bootstrap';

import './index.html';
import './favicon.ico';
import './styles/index.less';

import React    from 'react';
import ReactDOM from 'react-dom';

import routes from './config/routes';
import { Router, browserHistory } from 'react-router';

ReactDOM.render(
  <Router history={browserHistory}>{routes}</Router>,
  document.getElementById('app')
);
