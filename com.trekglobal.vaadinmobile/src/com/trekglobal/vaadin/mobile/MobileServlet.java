package com.trekglobal.vaadin.mobile;

import java.io.File;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.compiere.Adempiere;
import org.compiere.util.CLogger;
import org.compiere.util.Ini;

/**
 * @author druiz
 */
public class MobileServlet
{
	/**	Logging									*/
	private static CLogger logger = CLogger.getCLogger(MobileServlet.class);

	public static void init(ServletConfig servletConfig) throws ServletException {

		/** Initialise context for the current thread*/
		String propertyFile = Ini.getFileName(false);
		File file = new File(propertyFile);
		if (!file.exists()) {
			throw new IllegalStateException("idempiere.properties is not setup. PropertyFile="+propertyFile);
		}
		if (!Adempiere.isStarted()) {
			boolean started = Adempiere.startup(false);
			if(!started) {
				throw new ServletException("Could not start iDempiere");
			}
		}

		logger.log(Level.OFF, "Vaadin Web Client started successfully");
		/**
		 * End iDempiere Start
		 */
	}
}
