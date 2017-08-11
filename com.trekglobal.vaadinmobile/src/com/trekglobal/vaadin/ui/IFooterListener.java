package com.trekglobal.vaadin.ui;

/**
 * Interface that define the methods that must
 * be implemented to listen to the footer buttons
 * @author Diego Ruiz - Bx Service GmbH
 */
public interface IFooterListener {
	
	void onNewButtonPressed();
	void onEditButtonPressed();
	void onSearchButtonPressed();
	void onProcessButtonPressed();
	void onDeleteButtonPressed();
	void onSaveButtonPressed();
}
