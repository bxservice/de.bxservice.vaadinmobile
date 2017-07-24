package com.trekglobal.vaadin.ui;

public interface IFindListener {

	void onSearch(String value, String name, String description, String docNo);
	void onCancelSearch();
	
}
