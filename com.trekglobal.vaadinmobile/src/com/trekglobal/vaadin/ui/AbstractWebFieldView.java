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

import org.compiere.model.GridTab;
import org.compiere.model.MLocation;

import com.trekglobal.vaadin.mobile.MobileLookup;
import com.trekglobal.vaadin.mobile.MobileLookupGenericObject;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PopupView;

public abstract class AbstractWebFieldView extends AbstractToolbarView implements IWebFieldListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9222751548452760308L;
	protected PopupView popupView;

	public AbstractWebFieldView(WNavigatorUI loginPage) {
		super(loginPage);
	}
	
	public abstract GridTab getCurTab();
	
	@Override
	public void onLookUp(WebField webField) {
		MobileLookup lookup = new MobileLookup(webField, getCurTab());
		syncCtx();

		if (!lookup.isDataSafe()) {
			Notification.show("ParameterMissing",
					Type.ERROR_MESSAGE);
			return;
		}

		lookup.runLookup();

		//  Create Document
		WLookupView lookupContent = new WLookupView(this, lookup);
		popupView = new PopupView(null, lookupContent);
		popupView.addStyleName("searchdialog");
		addComponent(popupView);

		popupView.addPopupVisibilityListener(event -> {
			if (event.isPopupVisible())
				content.addStyleName("bxwindow-content-busy");
			else {
				content.removeStyleName("bxwindow-content-busy");
			}
		});

		popupView.setPopupVisible(!popupView.isPopupVisible());
	}
	
	@Override
	public void onLookUpOK(WebField webField, MobileLookupGenericObject selectedRecord) {
		webField.setNewValue(String.valueOf(selectedRecord.getId()), selectedRecord.getQueryValue());
		popupView.setPopupVisible(false);
		
		if (webField.isHasDependents() || webField.isHasCallout())
			onChange(webField);
	}
	
	@Override
	public void onLookUpCancel() {
		popupView.setPopupVisible(false);
	}
	
	@Override
	public void onLocation(WebField webField, MLocation location, int m_windowNo) {
		syncCtx();

		WLocationView locationContent = new WLocationView(this, location, webField, m_windowNo);
		popupView = new PopupView(null, locationContent);
		popupView.addStyleName("searchdialog");
		addComponent(popupView);

		popupView.addPopupVisibilityListener(event -> {
			if (event.isPopupVisible())
				content.addStyleName("bxwindow-content-busy");
			else {
				content.removeStyleName("bxwindow-content-busy");
			}
		});

		popupView.setPopupVisible(!popupView.isPopupVisible());
	}
	
	@Override
	public void onLocationOk(WLocationView locationView) {
		popupView.setPopupVisible(false);
		MLocation location = locationView.getLocation();
		
		if (!locationView.isChanged())
			return;
		
		WebField webField = locationView.getWebField();
		webField.setNewValue(String.valueOf(location.getC_Location_ID()), location.getCity());
		if (webField.isHasDependents() || webField.isHasCallout())
			onChange(webField);
	}
}
