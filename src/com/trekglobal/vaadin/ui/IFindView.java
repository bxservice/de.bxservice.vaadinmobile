package com.trekglobal.vaadin.ui;

public interface IFindView {

	void onSearch(String value, String name, String description, String docNo);
	void onCancelSearch();
	
}
