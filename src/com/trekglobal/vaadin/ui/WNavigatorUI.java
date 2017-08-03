package com.trekglobal.vaadin.ui;

import java.sql.Timestamp;
import java.util.Properties;

import javax.servlet.ServletException;

import org.adempiere.util.ServerContext;
import org.compiere.model.MSysConfig;
import org.compiere.model.MSystem;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Language;
import org.compiere.util.Msg;

import com.trekglobal.vaadin.mobile.MobileEnv;
import com.trekglobal.vaadin.mobile.VEnv;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Viewport;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;

@Viewport("user-scalable=no,initial-scale=1.0")
@PreserveOnRefresh
public class WNavigatorUI extends UI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7582342143333788115L;
	private static final String DEFAULT_UI_PATH = "/m";
	private static final String VAADIN_THEME_DEFAULT = "mobiletheme";
    private static final String SESSION_CTX = "VaadinSessionContext";

	// 
	private Navigator navigator;

	// UI elements
	protected WLoginPanel loginPanel;
	protected WRolePanel  rolePanel;
	
	public static Properties getContext() {
        return (Properties) UI.getCurrent().getSession().getAttribute(SESSION_CTX);
    }

	@Override
	protected void init(VaadinRequest request) {
		setTheme(MSysConfig.getValue("VAADIN_THEME", VAADIN_THEME_DEFAULT));
		getPage().setTitle(MSysConfig.getValue(MSysConfig.ZK_BROWSER_TITLE, "iDempiere"));
		
		Properties ctx = new Properties();
		ServerContext.setCurrentInstance(ctx);
		String langSession = Env.getContext(ctx, Env.LANGUAGE);
		if (langSession == null || langSession.length() <= 0)
		{
			langSession = Language.getAD_Language(getLocale());
			Env.setContext(ctx, Env.LANGUAGE, langSession);
		}

		if (!MobileEnv.initWeb(VaadinServlet.getCurrent().getServletConfig()))
			try {
				throw new ServletException("WLogin.init");
			} catch (ServletException e1) {
				e1.printStackTrace();
			}

		VaadinSession.getCurrent().getSession().setMaxInactiveInterval(VEnv.TIMEOUT);
		
		//  Check DB connection
		if (!DB.isConnected())
		{
			String msg = Msg.getMsg(Env.getCtx(), "WLoginNoDB");
			if (msg.equals("WLoginNoDB"))
				msg = "No Database Connection";
			Notification.show(msg,
					Type.WARNING_MESSAGE);
		}
		
		getSession().setAttribute(SESSION_CTX, ctx);

		navigator = new Navigator(this, this);
		// Create and register the views
		navigator.addView(WLoginPanel.NAME, new WLoginPanel(this));
		navigator.setErrorView(new WLoginPanel(this));
	}

	public void loginOk(String userName, KeyNamePair[] clientsKNPairs) {
		createRolePanel(userName, clientsKNPairs);
	}

	protected void createRolePanel(String userName, KeyNamePair[] clientsKNPairs) {
		navigator.addView(WRolePanel.NAME, new WRolePanel(this, userName, clientsKNPairs));
		navigator.navigateTo(WRolePanel.NAME);
	}

	public void loginCompleted() {
		Properties ctx = Env.getCtx();

		String langLogin = Env.getContext(ctx, Env.LANGUAGE);
		MSystem system = MSystem.get(Env.getCtx());
		Env.setContext(ctx, "#System_Name", system.getName());

		// Validate language
		Language language = Language.getLanguage(langLogin);
		String locale = Env.getContext(ctx, VEnv.LOCALE);
		if (locale != null && locale.length() > 0 && !language.getLocale().toString().equals(locale)) {
			String adLanguage = language.getAD_Language();
			Language tmp = Language.getLanguage(locale);
			language = new Language(tmp.getName(), adLanguage, tmp.getLocale(), tmp.isDecimalPoint(),
					tmp.getDateFormat().toPattern(), tmp.getMediaSize());
		} else {
			Language tmp = language;
			language = new Language(tmp.getName(), tmp.getAD_Language(), tmp.getLocale(), tmp.isDecimalPoint(),
					tmp.getDateFormat().toPattern(), tmp.getMediaSize());
		}
		Env.verifyLanguage(ctx, language);
		Env.setContext(ctx, Env.LANGUAGE, language.getAD_Language()); //Bug

		getSession().setAttribute("Check_AD_User_ID", Env.getAD_User_ID(ctx));

		//	Don't Show Acct/Trl Tabs on HTML UI
		Env.setContext(ctx, "#ShowAcct", "N");
		Env.setContext(ctx, "#ShowTrl", "N");	
		Env.setContext(ctx, "#Date", new Timestamp(System.currentTimeMillis()));    //  JDBC format
		
		//update session context
		getSession().setAttribute("VaadinUISessionContext", ServerContext.getCurrentInstance());
		Env.setContext(ctx, "#UIClient", "vaadin");
		Env.setContext(ctx, "#LocalHttpAddr", VaadinServlet.getCurrent().getServletContext().getContextPath());

		navigator.addView(WMenuView.NAME, new WMenuView(this));
		navigator.navigateTo(WMenuView.NAME);
	}

	public void openWindow(int AD_Menu_ID) {
		navigator.addView(WWindowView.NAME, new WWindowView(this, AD_Menu_ID));
		navigator.navigateTo(WWindowView.NAME);
	}

	public void openProcessWindow(int AD_Menu_ID) {
		navigator.addView(WProcessView.NAME, new WProcessView(this, AD_Menu_ID));
		navigator.navigateTo(WProcessView.NAME);
	}

	public void openMainMenu() {
		loginCompleted();
	}

	public void onBackPressed() {
		JavaScript.getCurrent().execute("history.back()");
	}

	public void logout() {
		//Logout ad_session
		VEnv.logout();
		ServerContext.dispose();

		//clear context, invalidate session
		Env.getCtx().clear();

		// Close the VaadinServiceSession
		getPage().setLocation(DEFAULT_UI_PATH);
		getSession().close();
		VaadinService.reinitializeSession(VaadinService.getCurrentRequest());
	}
	
}
