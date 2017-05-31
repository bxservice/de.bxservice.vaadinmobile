package com.trekglobal.vaadin.ui;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

@WebServlet(urlPatterns = {"/*", "/VAADIN/*"},
asyncSupported = true)
@VaadinServletConfiguration(ui=WNavigatorUI.class, productionMode=false)
public class MobileVaadinServlet extends VaadinServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -791829446766865105L;

}