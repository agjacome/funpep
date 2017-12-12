import React from 'react';

import { IndexLink }     from 'react-router';
import { LinkContainer } from 'react-router-bootstrap';
import { Nav, Navbar, NavDropdown, NavItem, MenuItem } from 'react-bootstrap';

const Header = () => {
  return (
    <header id="header" className="clearfix">
      <Navbar staticTop>
        <Navbar.Header>
          <Navbar.Brand className="logo">
            <IndexLink to={'/'}>funpep</IndexLink>
          </Navbar.Brand>
          <Navbar.Toggle />
        </Navbar.Header>
        <Navbar.Collapse>
          <Nav pullRight>
            <LinkContainer to={'/new'}><NavItem>New Analysis</NavItem></LinkContainer>
            <LinkContainer to={'/status'}><NavItem>Check Status</NavItem></LinkContainer>
            <LinkContainer to={'/help'}><NavItem>Help</NavItem></LinkContainer>
            <LinkContainer to={'/about'}><NavItem>About</NavItem></LinkContainer>
          </Nav>
        </Navbar.Collapse>
      </Navbar>
    </header>
  );
}

export default Header;
