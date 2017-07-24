package com.trekglobal.vaadin.ui;

import javax.servlet.ServletException;

import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Msg;

import com.trekglobal.vaadin.mobile.MobileEnv;
import com.trekglobal.vaadin.mobile.MobileSessionCtx;
import com.trekglobal.vaadin.mobile.VEnv;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Viewport;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletService;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

@Theme("mobiletheme")
@Viewport("user-scalable=no,initial-scale=1.0")
@PreserveOnRefresh
public class WNavigatorUI extends UI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7582342143333788115L;
	private static final String DEFAULT_UI_PATH = "/m";

	// 
	private Navigator navigator;
	protected MobileSessionCtx wsc;
	protected VaadinRequest request;

	// UI elements
	protected WLoginPanel loginPanel;
	protected WRolePanel  rolePanel;

	@Override
	protected void init(VaadinRequest request) {

		this.request = request;

		//  Get Cookie Properties
		//cookieProps = MobileUtil.getCookieProperties(VaadinServletService.getCurrentServletRequest());

		//  Create Context
		wsc = MobileSessionCtx.get(VaadinServletService.getCurrentServletRequest());

		if (!MobileEnv.initWeb(VaadinServlet.getCurrent().getServletConfig()))
			try {
				throw new ServletException("WLogin.init");
			} catch (ServletException e1) {
				e1.printStackTrace();
			}

		request.getWrappedSession().setMaxInactiveInterval(MobileEnv.TIMEOUT);

		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(wsc.ctx, "WLoginNoDB");
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			Notification.show(msg,
					Type.WARNING_MESSAGE);
		}

		navigator = new Navigator(this, this);
		// Create and register the views
		navigator.addView(WLoginPanel.NAME, new WLoginPanel(wsc, this));
	}

	public void loginOk(String userName, KeyNamePair[] clientsKNPairs) {
		createRolePanel(userName, clientsKNPairs);
	}

	protected void createRolePanel(String userName, KeyNamePair[] clientsKNPairs) {
		navigator.addView(WRolePanel.NAME, new WRolePanel(wsc, this, userName, clientsKNPairs));
		navigator.navigateTo(WRolePanel.NAME);
	}

	public void loginCompleted() {
		navigator.addView(WMenuView.NAME, new WMenuView(wsc, this));
		navigator.navigateTo(WMenuView.NAME);
	}
	
	public void openWindow(int AD_Menu_ID) {
		navigator.addView(WWindowView.NAME, new WWindowView(wsc, this, AD_Menu_ID));
		navigator.navigateTo(WWindowView.NAME);
	}
	
	public void openProcessWindow(int AD_Menu_ID) {
		navigator.addView(WProcessView.NAME, new WProcessView(wsc, this, AD_Menu_ID));
		navigator.navigateTo(WProcessView.NAME);
	}
	
	public void openMainMenu() {
		loginCompleted();
	}
	
	public void onBackPressed() {
		JavaScript.getCurrent().execute("history.back()");
	}
	
	public void logout() {
    	//logout ad_session
		VEnv.logout();
		
		//clear context, invalidate session
    	Env.getCtx().clear();

    	// Close the VaadinServiceSession
    	getPage().setLocation(DEFAULT_UI_PATH);
        getSession().close();
    	VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
	}

}
