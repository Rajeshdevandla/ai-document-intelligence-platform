describe('Upload Flow', () => {
  beforeEach(() => {
    // Set a fake JWT so the app thinks we're logged in
    window.localStorage.setItem('jwt_token', 'fake-jwt-token');
    cy.visit('/upload');
  });

  it('should display the upload page', () => {
    cy.contains('Upload Document').should('be.visible');
    cy.contains('Drag & drop a document here').should('be.visible');
  });

  it('should show accepted file types', () => {
    cy.contains('PDF, PNG, JPG').should('be.visible');
  });

  it('should show feature cards', () => {
    cy.contains('OCR Extraction').should('be.visible');
    cy.contains('LLM Analysis').should('be.visible');
    cy.contains('Analytics').should('be.visible');
  });

  it('should navigate to dashboard', () => {
    cy.get('a[href="/dashboard"]').click();
    cy.url().should('include', '/dashboard');
    cy.contains('Analytics Dashboard').should('be.visible');
  });
});
