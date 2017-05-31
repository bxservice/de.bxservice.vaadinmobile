package com.trekglobal.vaadin.ui;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

public class WConfirmationDialogView extends VerticalLayout {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3641084022812299515L;
	private Button okButton; 
	private Button cancelButton;
	private Label message;
	
	public WConfirmationDialogView(boolean showOkButton, boolean showCancelButton, String confirmationMessage) {
		
		if (confirmationMessage != null) {
			message = new Label(confirmationMessage);
			addComponent(message);
		}
		
		HorizontalLayout buttonRow = new HorizontalLayout();
		buttonRow.addStyleName("confirmation-button-row");

		if (showCancelButton) {
			cancelButton = new Button();
			cancelButton.setIcon(VaadinIcons.CLOSE_SMALL);
			cancelButton.addStyleName("cancel-button");
			buttonRow.addComponent(cancelButton);
		}
		
		if (showOkButton) {
			okButton = new Button();
			okButton.setIcon(VaadinIcons.CHECK);
			okButton.addStyleName("ok-button");
			buttonRow.addComponent(okButton);
		}
		
		addComponent(buttonRow);
	}

	public Button getOkButton() {
		return okButton;
	}

	public Button getCancelButton() {
		return cancelButton;
	}

}
