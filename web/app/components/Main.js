import React  from 'react';

import Header  from './Header';
import Content from './Content';

const Main = ({children}) => {
  return (
    <div>
      <Header />
      <Content>{children}</Content>
    </div>
  );
}

export default Main;
