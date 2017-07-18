package com.trekglobal.vaadin.ui;

import org.compiere.model.GridTab;

import com.trekglobal.vaadin.mobile.MobileLookup;
import com.trekglobal.vaadin.mobile.MobileLookupGenericObject;
import com.trekglobal.vaadin.mobile.MobileSessionCtx;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PopupView;

public abstract class AbstractWebFieldView extends AbstractToolbarView implements IWebFieldView {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9222751548452760308L;
	protected PopupView lookupPopup;
	protected WLookupView lookupContent;

	public AbstractWebFieldView(MobileSessionCtx wsc, WNavigatorUI loginPage) {
		super(wsc, loginPage);
	}
	
	public abstract GridTab getCurTab();
	
	@Override
	public void onLookUp(WebField webField) {
		MobileLookup lookup = new MobileLookup(wsc, webField, getCurTab());

		if (!lookup.isDataSafe()) {
			Notification.show("ParameterMissing",
					Type.ERROR_MESSAGE);
			return;
		}

		lookup.runLookup();

		//  Create Document
		lookupContent = new WLookupView(this, lookup);
		lookupPopup = new PopupView(null, lookupContent);
		lookupPopup.addStyleName("searchdialog");
		addComponent(lookupPopup);

		lookupPopup.addPopupVisibilityListener(event -> {
			if (event.isPopupVisible())
				content.addStyleName("bxwindow-content-busy");
			else {
				content.removeStyleName("bxwindow-content-busy");
			}
		});

		lookupPopup.setPopupVisible(!lookupPopup.isPopupVisible());
	}
	
	@Override
	public void onLookUpOK(WebField webField, MobileLookupGenericObject selectedRecord) {
		webField.setNewValue(String.valueOf(selectedRecord.getId()), selectedRecord.getQueryValue());
		lookupPopup.setPopupVisible(false);
		
		if (webField.isHasDependents() || webField.isHasCallout())
			onChange(webField);
	}
	
	@Override
	public void onLookUpCancel() {
		lookupPopup.setPopupVisible(false);
	}
	
	@Override
	public void onLocationLookUp(WebField webField) {

	}
}
