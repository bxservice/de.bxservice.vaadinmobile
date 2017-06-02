package com.trekglobal.vaadin.ui;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.compiere.model.MRole;
import org.compiere.model.MUser;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;
import org.compiere.util.Language;
import org.compiere.util.Login;
import org.compiere.util.Msg;
import org.compiere.util.Util;

import com.trekglobal.vaadin.mobile.MobileSessionCtx;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

public class WRolePanel extends CssLayout implements IToolbarView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3524898093282885838L;
	
    public static final String NAME = "WRole";
	private boolean initialized = false;
	private String windowTitle ="";

	protected MobileSessionCtx wsc;

	private WNavigatorUI loginPage;
	protected Login login;

	private WHeader header;
	private CssLayout content;
	private Label clientLabel;
	private Label roleLabel;
	private Label orgLabel;
	private NativeSelect<KeyNamePair> clientSelector;
	private NativeSelect<KeyNamePair> roleSelector;
	private NativeSelect<KeyNamePair> orgSelector;
	private Button okButton;
	
	protected String		userName;
	protected KeyNamePair[]	clients;
	private boolean roleChangedByUser = true;

	public WRolePanel(MobileSessionCtx wsc, WNavigatorUI loginPage, String userName, KeyNamePair[] clientsKNPairs) {

		this.loginPage = loginPage;
		this.wsc = wsc;
		this.userName = userName;
		clients = clientsKNPairs;
		
		initClient();
	}
	
	public void initClient() {
		login = new Login(wsc.ctx);
		
		if (clients.length == 1) {
        	Env.setContext(wsc.ctx, "#AD_Client_ID", clients[0].getID());
        }
	}
	
	private void initComponents() {

		windowTitle = Msg.getMsg(Env.getCtx(), "SelectRole");
		loginPage.getPage().setTitle(windowTitle);
		
    	Language language = Env.getLanguage(wsc.ctx);
    	
    	//Header
    	header = new WHeader(this, false, false);

		//Content
		content = new CssLayout();
		content.addStyleName("login-content");
		
		clientLabel = new Label(Msg.getMsg(language,"Client"));
		clientLabel.setWidth("50%");
		
		List<KeyNamePair> data = new ArrayList<>(Arrays.asList(clients));
		clientSelector = new NativeSelect<KeyNamePair>(null, data);
		clientSelector.setWidth("50%");
		clientSelector.addStyleName("bx-select");
		clientSelector.setEmptySelectionAllowed(false);
		clientSelector.setSelectedItem(data.get(0));
		
		if (clients != null && clients.length == 1) {
        	// don't show client if is just one
			clientLabel.setVisible(false);
			clientSelector.setVisible(false);
        } else {
        	clientLabel.setVisible(true);
        	clientSelector.setVisible(true);
        }

		roleLabel = new Label(Msg.getMsg(language,"Role"));
		roleLabel.setWidth("50%");
		
		orgLabel = new Label(Msg.getMsg(language,"Organization"));
		orgLabel.setWidth("50%");
		
		orgSelector = new NativeSelect<KeyNamePair>();
		orgSelector.setWidth("50%");
		orgSelector.addStyleName("bx-select");
		orgSelector.setEmptySelectionAllowed(false);

		okButton = new Button("Ok");
		okButton.addStyleName("bx-loginbutton");
		
        setUserID();
        updateRoleList();
        
		content.addComponents(clientLabel, clientSelector);
		content.addComponents(roleLabel, roleSelector);
		content.addComponents(orgLabel, orgSelector);
		content.addComponent(okButton);
	}
	
	private void init() {
		createUI();
		
		clientSelector.addValueChangeListener(event ->  {
			//Role changed by user flag to avoid calling the role selector value change recursively
			roleChangedByUser = false;
			updateRoleList();
			roleChangedByUser = true;
		});

    	okButton.addClickListener(e -> validateRoles());
		
	}
	
	protected void createUI() {
		setWidth("100%");

		// Enable Responsive CSS selectors for the component
		Responsive.makeResponsive(this);
		Responsive.makeResponsive(content);
		
		addComponents(header, content);		
	}
	
    private void setUserID() {
    	if (clientSelector.getSelectedItem() != null) {
        	Env.setContext(wsc.ctx, "#AD_Client_ID", (String) clientSelector.getSelectedItem().get().getID());
    	} else {
        	Env.setContext(wsc.ctx, "#AD_Client_ID", (String) null);
    	}
    	MUser user = MUser.get(wsc.ctx, userName);
    	if (user != null) {
    		Env.setContext(wsc.ctx, "#AD_User_ID", user.getAD_User_ID());
    		Env.setContext(wsc.ctx, "#AD_User_Name", user.getName());
    		Env.setContext(wsc.ctx, "#SalesRep_ID", user.getAD_User_ID());
    	}
    }
    
    private void updateRoleList() {
    	
    	KeyNamePair selectedClient = clientSelector.getSelectedItem().get(); 

    	if (selectedClient != null) {
        	//  initial role
            KeyNamePair clientKNPair = new KeyNamePair(new Integer(selectedClient.getID()), selectedClient.getName());
            KeyNamePair roleKNPairs[] = login.getRoles(userName, clientKNPair);
            
            if (roleKNPairs != null && roleKNPairs.length > 0) {
            	
            	NativeSelect<KeyNamePair> temp = roleSelector;
        		roleSelector = getSelectList(new ArrayList<>(Arrays.asList(roleKNPairs)));
        		setRoleSelectorListener();
        		
        		if (temp != null)
        			content.replaceComponent(temp, roleSelector);
        	
            }

            //force reload of default role
            MRole.getDefault(wsc.ctx, true);
            
    		// If we have only one role, we can hide the combobox - metas-2009_0021_AP1_G94
    		/*if (clients.length == 1 && roleSelector. == 1 && ! MSysConfig.getBooleanValue(MSysConfig.ALogin_ShowOneRole, true))
    		{
    			lstRole.setSelectedIndex(0);
    			lblRole.setVisible(false);
    			lstRole.setVisible(false);
    		}
    		else
    		{
    			lblRole.setVisible(true);
    			lstRole.setVisible(true);
    		}*/
        }
        setUserID();
        updateOrganisationList();
    }
    
    private NativeSelect<KeyNamePair> getSelectList(ArrayList<KeyNamePair> data) {
    	NativeSelect<KeyNamePair> selector = new NativeSelect<KeyNamePair>(null, data);
    	selector.setWidth("50%");
    	selector.addStyleName("bx-select");
    	selector.setEmptySelectionAllowed(false);
    	selector.setSelectedItem(data.get(0));
    	return selector;
    }
    
    private void setRoleSelectorListener() {
		roleSelector.addValueChangeListener(event -> {
			if (roleChangedByUser) {
				setUserID();
				updateOrganisationList();				
			}
		});
    }
    
    private void updateOrganisationList() {
    	
    	KeyNamePair selectedRole = roleSelector.getSelectedItem().get(); 

        if (selectedRole != null) {

            KeyNamePair RoleKNPair = new KeyNamePair(new Integer(selectedRole.getID()), selectedRole.getName());
            KeyNamePair orgKNPairs[] = login.getOrgs(RoleKNPair);
            if (orgKNPairs != null && orgKNPairs.length > 0)
            {
            	NativeSelect<KeyNamePair> temp = orgSelector;
            	orgSelector = getSelectList(new ArrayList<>(Arrays.asList(orgKNPairs)));
            	
        		if (temp != null)
        			content.replaceComponent(temp, orgSelector);
            }
        }
    }
    
    public void validateRoles() {
    	
    	KeyNamePair selectedClient = clientSelector.getSelectedItem().get();
    	KeyNamePair selectedRole   = roleSelector.getSelectedItem().get();
    	KeyNamePair selectedOrg    = orgSelector.getSelectedItem().get();

        if (selectedClient == null || selectedClient.getID() == null) {
        	showFillMandatoryErrorMessage(clientLabel.getValue());
        } else if(selectedRole == null || selectedRole.getID() == null) {
        	showFillMandatoryErrorMessage(roleLabel.getValue());
        } else if(selectedOrg == null || selectedOrg.getID() == null) {
        	showFillMandatoryErrorMessage(orgLabel.getValue());
        }
        
        int orgId = 0;
        orgId = Integer.parseInt((String)selectedOrg.getID());
        KeyNamePair orgKNPair = new KeyNamePair(orgId, selectedOrg.getName());

		String msg = login.loadPreferences(orgKNPair, null, null, null);

		//	Don't Show Acct/Trl Tabs on HTML UI
		Env.setContext(wsc.ctx, "#ShowAcct", "N");
		Env.setContext(wsc.ctx, "#ShowTrl", "N");	
		Env.setContext(wsc.ctx, "#Date", new Timestamp(System.currentTimeMillis()));    //  JDBC format
		
		//No error on load preferences
        if (Util.isEmpty(msg)) {
            msg = login.validateLogin(orgKNPair);
        }
        if (!Util.isEmpty(msg)) {
			Env.getCtx().clear();
			Notification.show(Msg.getMsg(wsc.ctx, "RoleInconsistent"),
					Type.ERROR_MESSAGE);
			
			return;
		}
		
        loginPage.loginCompleted();

    }
    
    private void showFillMandatoryErrorMessage(String reason) {
		Notification.show(Msg.getMsg(wsc.ctx, "FillMandatory") + reason,
				Type.ERROR_MESSAGE);
    }

	@Override
	public void enter(ViewChangeEvent event) {

		//Avoid problem of double initialization
	    if (initialized)
	        return;
	    
		initComponents();
		init();
		initialized = true;
	}

	@Override
	public void onLeftButtonPressed() {		
	}

	@Override
	public void onRightButtonPressed() {
	}

	@Override
	public String getWindowTitle() {
		return windowTitle;
	}

}
