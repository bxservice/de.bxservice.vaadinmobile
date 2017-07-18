package com.trekglobal.vaadin.ui;

import com.trekglobal.vaadin.mobile.MobileLookupGenericObject;

public interface IWebFieldView {

	void onChange(WebField webField);
	void onLocationLookUp(WebField webField);
	void onLookUp(WebField webField);
	void onLookUpOK(WebField webField, MobileLookupGenericObject selectedRecord);
	void onLookUpCancel();
	
}
