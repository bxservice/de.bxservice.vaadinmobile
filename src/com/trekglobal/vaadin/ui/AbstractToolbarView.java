package com.trekglobal.vaadin.ui;

import com.trekglobal.vaadin.mobile.MobileSessionCtx;
import com.vaadin.ui.CssLayout;

public abstract class AbstractToolbarView extends CssLayout implements IToolbarView {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3558812737278075168L;
	protected boolean initialized = false;

	protected WNavigatorUI loginPage;
	protected MobileSessionCtx wsc;
	protected String windowTitle ="";
	
	//Main UI components
	protected CssLayout content;
	protected WHeader   header;
	
	public AbstractToolbarView(MobileSessionCtx wsc, WNavigatorUI loginPage) {
		this.wsc = wsc;
		this.loginPage = loginPage;
	}
	
	protected abstract void initComponents();
	protected abstract void init();
	
	public void initView() {
		//Avoid problem of double initialization
	    if (initialized)
	        return;
	    
        initComponents();
        init();
        initialized = true;
	}
	
	protected void openMenu() {
		loginPage.openMainMenu();
	}
	
	protected void backButton() {
		loginPage.onBackPressed();
	}
	
	public void logout() {
		loginPage.logout();
	}
	
	@Override
	public String getWindowTitle() {
		return windowTitle;
	}
	
	@Override
	public void onLeftButtonPressed() {		
	}

	@Override
	public void onRightButtonPressed() {
	}

}
