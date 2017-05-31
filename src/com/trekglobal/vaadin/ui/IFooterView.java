package com.trekglobal.vaadin.ui;

import com.vaadin.navigator.View;

/**
 * Interface that define the methods that must
 * be implemented to listen to the footer buttons
 * @author Diego Ruiz - Bx Service GmbH
 */
public interface IFooterView extends View {
	
	void onNewButtonPressed();
	void onEditButtonPressed();
	void onSearchButtonPressed();
	void onProcessButtonPressed();
	void onDeleteButtonPressed();
	void onSaveButtonPressed();
}
