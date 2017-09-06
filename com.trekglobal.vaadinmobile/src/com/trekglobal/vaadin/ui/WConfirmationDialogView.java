/**********************************************************************
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Diego Ruiz - Bx Service GmbH                                      *
 * Sponsored by:                                                       *
 * - TrekGlobal                                                        *
 **********************************************************************/
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
