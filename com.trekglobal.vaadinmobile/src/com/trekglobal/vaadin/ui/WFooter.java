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
import com.vaadin.server.Resource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeButton;

public class WFooter extends HorizontalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1444858543479291226L;
	
	private IFooterListener footerListener;
	
	//Possible footer buttons
	private NativeButton newButton;
	private NativeButton editButton;
	private NativeButton searchButton;
	private NativeButton processButton;
	private NativeButton deleteButton;
	private NativeButton saveButton;
	
	public WFooter(IFooterListener footerListener) {
		this.footerListener = footerListener;
		
		//Takes care of the right alignment
		setSizeFull();
		addStyleName("bx-footer");
	}
	
	public void setMultirowButtons() {
		setSearchButton();
		setNewButton();
		
		removeAllComponents();
		addComponents(searchButton, newButton);
	}
	
	public void setSinglerowButtons(boolean showProcessButton) {
		setEditButton();
		setDeleteButton();
		
		removeAllComponents();
		addComponents(editButton, deleteButton);

		if (showProcessButton) {
			setProcessButton();
			addComponent(processButton);
		}
	}
	
	public void setEditRowButtons() {
		setSaveButton();

		removeAllComponents();
		addComponents(saveButton);

	}

	public void setNewButton() {
		newButton = addButton(VaadinIcons.FILE_ADD);
		newButton.addClickListener(event -> footerListener.onNewButtonPressed());
	}
	
	public void setEditButton() {
		editButton = addButton(VaadinIcons.EDIT);
		editButton.addClickListener(event -> footerListener.onEditButtonPressed());
	}
	
	public void setSearchButton() {
		searchButton = addButton(VaadinIcons.SEARCH);
		searchButton.addClickListener(event -> footerListener.onSearchButtonPressed());
	}
	
	public void setProcessButton() {
		processButton = addButton(VaadinIcons.COG);
		processButton.addClickListener(event -> footerListener.onProcessButtonPressed());
	}
	
	public void setDeleteButton() {
		deleteButton = addButton(VaadinIcons.TRASH);
		deleteButton.addClickListener(event -> footerListener.onDeleteButtonPressed());
	}
	
	public void setSaveButton() {
		saveButton = addButton(VaadinIcons.CHECK);
		//editButton.setCaption("SAVE");
		saveButton.addClickListener(event -> footerListener.onSaveButtonPressed());
	}
	
	private NativeButton addButton(Resource icon) {
		NativeButton footerButton = new NativeButton();
		footerButton.setStyleName("bx-footer-button");
		footerButton.setIcon(icon);
				
		return footerButton;
	}
}
