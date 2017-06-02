package com.trekglobal.vaadin.ui;

import static org.compiere.model.SystemIDs.TREE_MENUPRIMARY;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.compiere.model.MTree;
import org.compiere.model.MTreeNode;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Util;

import com.trekglobal.vaadin.mobile.MobileSessionCtx;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;

public class WMenuView extends CssLayout implements IToolbarView, Button.ClickListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2917601740376013906L;
	public static final String NAME = "WMenu";
	private boolean initialized = false;
	private String windowTitle ="";

	protected MobileSessionCtx wsc;
	private WNavigatorUI loginPage;
	private Map<Button, MTreeNode> mapButtonNode = new HashMap<Button, MTreeNode>();
	private MTreeNode currentNode;
	
	//UI
	private WHeader header;
	private CssLayout content;

	public WMenuView(MobileSessionCtx wsc, WNavigatorUI loginPage) {
		this.loginPage = loginPage;
		this.wsc = wsc;
	}

	private void initComponents() {

		windowTitle = Util.cleanAmp(Msg.getMsg(Env.getCtx(),"Menu"));
		loginPage.getPage().setTitle(windowTitle);

		header = new WHeader(this, true, true);
		header.setLogoutButton();
		content = new CssLayout();

		int AD_Role_ID = Env.getAD_Role_ID(wsc.ctx);

		//  Load Menu Structure     ----------------------
		int AD_Tree_ID = getTreeId(AD_Role_ID);

		MTree tree = new MTree(wsc.ctx, AD_Tree_ID, false, false, null);
		//	Trim tree
		MTreeNode root = tree.getRoot();
		generateMenu(root);
	}
	
    private int getTreeId(int adRoleId) {
        int AD_Tree_ID = DB.getSQLValue(null,
                "SELECT COALESCE(r.AD_Tree_Menu_ID, ci.AD_Tree_Menu_ID)" 
                + "FROM AD_ClientInfo ci" 
                + " INNER JOIN AD_Role r ON (ci.AD_Client_ID=r.AD_Client_ID) "
                + "WHERE AD_Role_ID=?", adRoleId);
        if (AD_Tree_ID <= 0)
            AD_Tree_ID = TREE_MENUPRIMARY;    //  Menu
        return AD_Tree_ID;
    }
    
    private void generateMenu(MTreeNode rootNode) {
        Enumeration<?> nodeEnum = rootNode.children();
        currentNode = rootNode;

        while (nodeEnum.hasMoreElements()) {
            MTreeNode mChildNode = (MTreeNode)nodeEnum.nextElement();
    		Button button = null;

            if (mChildNode.getChildCount() != 0) {
            	button = new Button(mChildNode.getName());
    			button.setIcon(VaadinIcons.FOLDER);
    			button.setDescription(mChildNode.getDescription());
    			button.addStyleName("bx-menu-button");
            }
            else {
            	button = new Button(mChildNode.getName());
    			button.addStyleName("bx-menu-button");

                if (mChildNode.isReport()) {
                	button.setIcon(VaadinIcons.FILE_TABLE);
                }
                else if (mChildNode.isProcess()) {
                	button.setIcon(VaadinIcons.COG);
                }
                else if (mChildNode.isWindow()) {
                	button.setIcon(VaadinIcons.BROWSER);
                }
                else if (mChildNode.isWorkFlow() || mChildNode.isForm() || mChildNode.isTask()) {
                	continue;
                }
            }
            
            if (button != null) {
            	button.addClickListener(this);
            	mapButtonNode.put(button, mChildNode);
            	content.addComponent(button);
            }
        }
    }

	private void init() {
		createUI();
	}

	protected void createUI() {
		setWidth("100%");

		// Enable Responsive CSS selectors for the component
		Responsive.makeResponsive(this);
		Responsive.makeResponsive(content);
		content.setSizeFull();

		addComponents(header, content);
	}
	
	private void regenerateMenu(MTreeNode rootNode) {
		content.removeAllComponents();
		generateMenu(rootNode);
		
		header.updateTitle(rootNode.getName());
		
		if (rootNode.getLevel() != 0) {
			header.setHomeButton();
			header.setBackButton();
		} else  {
			header.setLogoutButton();
			header.hideLeftButton();
		}
	}
	
	public void logout() {
		loginPage.logout();
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
	public void buttonClick(ClickEvent event) {
		
		MTreeNode node = mapButtonNode.get(event.getButton());
		if (node.getChildCount() != 0) {
			regenerateMenu(node);
		} else if (node.isWindow()) {
			loginPage.openWindow(node.getNode_ID());
		} else if (node.isProcess()) {
			loginPage.openProcessWindow(node.getNode_ID());
		}
	}

	@Override
	public void onLeftButtonPressed() {
		//If it's not the root
		if (currentNode != null && currentNode.getLevel() > 0 && currentNode.getParent() != null) {
			regenerateMenu((MTreeNode) currentNode.getParent());
		}
	}

	@Override
	public void onRightButtonPressed() {
		if (currentNode != null && currentNode.getLevel() > 0 && currentNode.getParent() != null)
			regenerateMenu((MTreeNode) currentNode.getRoot());
		else
			logout();
	}

	@Override
	public String getWindowTitle() {
		return windowTitle;
	}

}
