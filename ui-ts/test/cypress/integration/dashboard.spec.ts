describe('Dashboard', () => {
  beforeEach(() => {
    cy.visit('/');
  });
  it('.should() - assert that <title> is correct', () => {
    cy.dataCy('header')
      .should('contain', 'Champagne')
      .and('contain', 'Toasting successful deployments');
  });
});
