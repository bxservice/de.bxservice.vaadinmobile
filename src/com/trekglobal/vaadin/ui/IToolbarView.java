package com.trekglobal.vaadin.ui;

import com.vaadin.navigator.View;

/**
 * Interface that define the methods that must
 * be implemented to listen to the toolbar buttons
 * @author Diego Ruiz - Bx Service GmbH
 */
public interface IToolbarView extends View {
	
	public void onLeftButtonPressed();
	public void onRightButtonPressed();
	public String getWindowTitle();
}
