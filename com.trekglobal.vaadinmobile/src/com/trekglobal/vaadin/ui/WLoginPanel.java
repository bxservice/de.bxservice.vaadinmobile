package com.trekglobal.vaadin.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.adempiere.webui.util.UserPreference;
import org.compiere.model.MSysConfig;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Language;
import org.compiere.util.Login;
import org.compiere.util.Msg;
import org.compiere.util.Util;

import com.trekglobal.vaadin.mobile.VEnv;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Responsive;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class WLoginPanel extends AbstractToolbarView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6809008316849723519L;
	
    public static final String NAME = "";

	private VerticalLayout loginForm;
	private String selectedLanguage = "";
	private Label languageLabel;
	private TextField txtUserId;
	private PasswordField pwdField;
	private NativeSelect<String> languageSelector;
	private Button okButton;
	
	protected boolean email_login = MSysConfig.getBooleanValue(MSysConfig.USE_EMAIL_FOR_LOGIN, false);
	
	public WLoginPanel(WNavigatorUI loginPage) {
		super(loginPage);
	}
	
	protected void initComponents() {

    	String AD_Language = Env.getContext(ctx, Env.LANGUAGE);
		windowTitle = Msg.getMsg(AD_Language, "Login");
		String usrText = Msg.getMsg(AD_Language, "User");
		if (email_login)
			usrText = Msg.getMsg(AD_Language, "EMail");

		String pwdText = Msg.getMsg(AD_Language, "Password");
		String lngText = Msg.translate(AD_Language, "AD_Language");

		//Header
		header = new WHeader(this, true, true);
		header.setAboutButton();

		//Content
		content = new CssLayout();
		content.addStyleName("login-content");
		
		loginForm = new VerticalLayout();
		loginForm.addStyleName("bx-loginform");
		
		Image logo = new Image();
		String zk_theme_value=MSysConfig.getValue("ZK_THEME");
		String urldef = "/webui/theme/"+zk_theme_value+"/images/login-logo.png";
		String imgdef = MSysConfig.getValue(MSysConfig.ZK_LOGO_LARGE, urldef);
		
		logo.setStyleName("bx-login-logo");
		logo.setSource(new ExternalResource(imgdef));
		content.addComponent(logo);
		
		//usrLabel.setId(P_USERNAME + "L");
		txtUserId = new TextField();
		txtUserId.setPlaceholder(usrText);
		txtUserId.addStyleName("bx-loginfield");
		loginForm.addComponent(txtUserId);

		pwdField = new PasswordField();
		pwdField.setPlaceholder(pwdText);
		//pwd.setId(P_PASSWORD + "L");
		pwdField.addStyleName("bx-loginpassword");
		loginForm.addComponent(pwdField);
		
		
		languageLabel = new Label(lngText);
		languageLabel.setId(Env.LANGUAGE + "L");
		languageLabel.setWidth("50%");

		Env.getLoginLanguages(); // to fill the s_language array on Language
		List<String> data = new ArrayList<>();
		ArrayList<String> supported = Env.getLoginLanguages();
		String[] availableLanguages = Language.getNames();
		for (String langName : availableLanguages) {
			Language language = Language.getLanguage(langName);
			if (!supported.contains(language.getAD_Language()))
				continue;
			data.add(langName);
		}

		languageSelector = new NativeSelect<>(null, data);

		languageSelector.setEmptySelectionAllowed(false);
		languageSelector.setSelectedItem(data.get(0));
		languageChanged(data.get(0));

		languageSelector.setWidth("50%");
		languageSelector.addStyleName("bx-select");

		okButton = new Button("Ok");
		okButton.addStyleName("bx-loginbutton");
		
		content.addComponent(loginForm);
		content.addComponent(languageLabel);
		content.addComponent(languageSelector);
		content.addComponent(okButton);

    }
    
	protected void init() {
    	createUI();
    	
    	okButton.addClickListener(e -> validateLogin());
		languageSelector.addValueChangeListener(event -> languageChanged(event.getValue()));
    }
    
	protected void createUI() {
		setWidth("100%");

		// Enable Responsive CSS selectors for the component
		Responsive.makeResponsive(this);
		Responsive.makeResponsive(content);
		
		addComponents(header, content);		
	}
	
	private void languageChanged(String langName) {
		Language language = findLanguage(langName);
		selectedLanguage = langName;

		if (email_login)
			txtUserId.setPlaceholder(Msg.getMsg(language, "EMail"));
		else
			txtUserId.setPlaceholder(Msg.getMsg(language, "User"));
		pwdField.setPlaceholder(Msg.getMsg(language, "Password"));
		languageLabel.setValue(Msg.getMsg(language, "Language"));
	}

	private Language findLanguage(String langName) {
		Language tmp = Language.getLanguage(langName);
		Language language = new Language(tmp.getName(), tmp.getAD_Language(), tmp.getLocale(), tmp.isDecimalPoint(),
				tmp.getDateFormat().toPattern(), tmp.getMediaSize());
		
    	Env.verifyLanguage(ctx, language);
    	Env.setContext(ctx, Env.LANGUAGE, language.getAD_Language());
    	Env.setContext(ctx, VEnv.LOCALE, language.getLocale().toString());

		return language;
	}

	/**
	 *  validates user name and password when logging in
	 *
	 **/
	public void validateLogin() {

    	syncCtx();
		Login login = new Login(ctx);
		String userId = txtUserId.getValue();
        String userPassword = pwdField.getValue();
        
		KeyNamePair[] clients = null;
		
		VaadinSession currSess = getUI().getSession();
		clients = login.getClients(userId, userPassword);
		
		if (clients == null || clients.length == 0)
        {
        	String loginErrMsg = login.getLoginErrMsg();
        	if (Util.isEmpty(loginErrMsg))
        		loginErrMsg = Msg.getMsg(ctx, "FailedLogin", true);
        	
        	if (loginErrMsg != null)
    			Notification.show(loginErrMsg,
    					Type.ERROR_MESSAGE);
        } else {
    		Language language = findLanguage(selectedLanguage);

        	Env.setContext(ctx, UserPreference.LANGUAGE_NAME, language.getName()); // Elaine 2009/02/06
    		Env.setContext(ctx, "#UIClient", "vaadin");

            if (!login.isPasswordExpired())
            	loginPage.loginOk(userId, clients);

            Locale locale = language.getLocale();
            currSess.setLocale(locale);
        }

	}
	
	@Override
	public void onRightButtonPressed() {
		loginPage.openAboutPage();
	}

	@Override
	public void enter(ViewChangeEvent event) {
		initView();
	}

}
