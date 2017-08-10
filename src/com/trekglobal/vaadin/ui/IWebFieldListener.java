package com.trekglobal.vaadin.ui;

import org.compiere.model.MLocation;

import com.trekglobal.vaadin.mobile.MobileLookupGenericObject;

public interface IWebFieldListener {

	void onChange(WebField webField);
	void onLocation(WebField webField);
	void onLocation(WebField webField, MLocation location, int m_windowNo);
	void onLocationOk(WLocationView locationView);
	void onLookUp(WebField webField);
	void onLookUpOK(WebField webField, MobileLookupGenericObject selectedRecord);
	void onLookUpCancel();
	
}
