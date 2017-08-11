package com.trekglobal.vaadin.ui;

import java.util.Properties;

import org.adempiere.util.ServerContext;

import com.vaadin.ui.CssLayout;

public abstract class AbstractToolbarView extends CssLayout implements IToolbarView {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3558812737278075168L;
	protected boolean initialized = false;

	protected WNavigatorUI loginPage;
	protected String windowTitle ="";
    protected Properties ctx;
	
	//Main UI components
	protected CssLayout content;
	protected WHeader   header;
	
	public AbstractToolbarView(WNavigatorUI loginPage) {
		this.loginPage = loginPage;
		ctx = WNavigatorUI.getContext();
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
	
	protected void syncCtx() {
		ServerContext.setCurrentInstance(ctx);
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
