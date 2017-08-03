package com.trekglobal.vaadin.mobile;

import org.compiere.model.MSession;
import org.compiere.util.Env;

/**
 *  Vaadin Application Environment and utilities
 *
 *  @author Diego Ruiz
 */
public class VEnv {
	
	public static final String LOCALE = "#Locale";
	
	/** Timeout - 15 Minutes                    */
	public static final int         TIMEOUT     = 15*60;

	/**
	 * logout AD_Session
	 */
	public static void logout()
	{
		//String sessionID = Env.getContext(Env.getCtx(), "#AD_Session_ID");
		//windowCache.remove(sessionID);
		//	End Session
		MSession session = MSession.get(Env.getCtx(), false);	//	finish
		if (session != null)
			session.logout();
		//
	}
}
