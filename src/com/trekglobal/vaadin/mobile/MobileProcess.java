package com.trekglobal.vaadin.mobile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;

import org.adempiere.util.ServerContext;
import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MProcess;
import org.compiere.model.MProcessPara;
import org.compiere.print.ReportEngine;
import org.compiere.process.ProcessInfo;
import org.compiere.util.CLogger;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.compiere.util.Trx;

import com.trekglobal.vaadin.ui.WNavigatorUI;

/**
 * @author druiz
 */
public class MobileProcess {

	/**	Logger			*/
	protected CLogger log = CLogger.getCLogger(getClass());
	private MProcess process;
	private Properties ctx;
	private File pdfFile;
	private boolean processOK = false;

	public MobileProcess(MProcess process) {
		this.process = process;
		ctx = WNavigatorUI.getContext();
	}

	public String runProcess(int AD_Window_ID, int AD_Table_ID, int AD_Record_ID, 
			HashMap<String, String> parameters) throws IOException {
		
		ServerContext.setCurrentInstance(ctx);
		String processMessage = null; 
		
		if (process == null)
			return "Process Not Found";

		log.info("PI table id "+process.get_Table_ID());
		log.info("PI table name id "+process.get_TableName());
		log.info("PI table client id "+process.getAD_Client_ID());
		log.info("PI table process id "+process.getAD_Process_ID());
		log.info("PI  process class name "+process.getClassname());

		//	Create Process Instance
		MPInstance pInstance = fillParameter(parameters);

		ProcessInfo pi = new ProcessInfo(process.get_Translation("Name"), process.getAD_Process_ID(), AD_Table_ID, AD_Record_ID);		
		pi.setAD_User_ID(Env.getAD_User_ID(ctx));
		pi.setAD_Client_ID(Env.getAD_Client_ID(ctx));
		pi.setClassName(process.getClassname());
		log.info("PI client id "+pi.getAD_Client_ID());
		pi.setAD_PInstance_ID(pInstance.getAD_PInstance_ID());			

		//	Start
		if (process.isWorkflow()) {
			Trx trx = Trx.get(Trx.createTrxName("WebPrc"), true);
			try {	
				//WProcessCtl.process(this, AD_Window_ID, pi, trx, request);
				//processOK = process.processIt(pi, trx);			
				trx.commit();
				trx.close();
			} catch (Throwable t) {
				trx.rollback();
				trx.close();
			}
			
			if (pi.isError()) {
				processOK = false;
				processMessage = "Error:" + pi.getSummary();
			} else {
				processOK = true;
				processMessage = "OK: Workflow Started";
			}
		}

		String jasper = process.getJasperReport();
		if (process.isJavaProcess()) {
			if (jasper!=null) {
				pi.setPrintPreview(false);
				pi.setIsBatch(true);
			}
			
			Trx trx = Trx.get(Trx.createTrxName("WebPrc"), true);
			try {
				processOK = process.processIt(pi, trx);
				trx.commit();
				trx.close();
			} catch (Throwable t) {
				trx.rollback();
				trx.close();
			}
			
			if (!processOK || pi.isError()) {
				processMessage = "Error:" + pi.getSummary();
				processOK = false;
			} else {
				
				if (jasper != null) {
					//StringBuilder fileName = new StringBuilder(process.get_Translation("Name"));
					/*response.setHeader("Content-Disposition", "inline; filename="+fileName.toString()+ ".pdf" );
					String error = MobileUtil.streamFile(response, pi.getPDFReport());
					if (error == null)
						return;
					doc = MobileDoc.create(error);
					wsc.ctx.put("AD_PInstance_ID=" + pInstance.getAD_PInstance_ID(), "ok");*/
				} else {
					processMessage = pi.getSummary();
					processOK = true;
				}
			}
		}

		//	Report
		if (process.isReport()) {
			if (jasper == null) {
				ReportEngine re = ReportEngine.get(ctx, pi);
				
				if (re == null) {
					processMessage = "Could not start ReportEngine";
					processOK = false;
				} else {
					try {
						File file = File.createTempFile(getFileName(), ".pdf");
						
						if (re.createPDF(file)) {
							processMessage = pi.getSummary();
							pdfFile = file;
							processOK = true;
							ctx.put("AD_PInstance_ID=" + pInstance.getAD_PInstance_ID(), "ok");
						} else {
							processMessage = "Could not create Report";
							processOK = false;
						}
					} catch (Exception e) {
						processMessage = "Could not create Report";
						processOK = false;
					}
				}
			}
		}
		
		return processMessage;
	}	//runProcess

	private MPInstance fillParameter(HashMap<String, String> parameters) {

		ServerContext.setCurrentInstance(ctx);
		MPInstance pInstance = new MPInstance(process, 0);
		MPInstancePara[] iParams = pInstance.getParameters();

		for (MPInstancePara iPara : iParams) {
			String key = iPara.getParameterName();
			MProcessPara pPara = process.getParameter(key);
			if (pPara == null) {
				log.log(Level.SEVERE, "Parameter not found: " + key);
				continue;
			}

			String valueString = parameters.get(key);
			log.fine("fillParameter - " + key + " = " + valueString);

			Object value = valueString;
			if (valueString != null && valueString.length() == 0)
				value = null;
			//	No Value
			if (value == null) {
				//	if (pPara.isMandatory())
				//		log.log(Level.WARNING,"fillParameter - " + key 
				//			+ " - empty - mandatory!");
			} else {
				//	Convert to Type
				try {
					if (DisplayType.isNumeric(pPara.getAD_Reference_ID()) 
							|| DisplayType.isID(pPara.getAD_Reference_ID())) {
						BigDecimal bd = null;
						if (value instanceof BigDecimal)
							bd = (BigDecimal)value;
						else if (value instanceof Integer)
							bd = new BigDecimal (((Integer)value).intValue());
						else
							bd = new BigDecimal (value.toString());

						if ((DisplayType.isID(pPara.getAD_Reference_ID()) && "-1".equals(value))
								|| (DisplayType.Locator == pPara.getAD_Reference_ID() && "0".equals(value)))      // empty selection
							iPara.setP_Number((BigDecimal) null);
						else
							iPara.setP_Number(bd);
						
						log.fine("fillParameter - " + key
								+ " = " + valueString + " (=" + bd + "=)");
					} else if (DisplayType.isDate(pPara.getAD_Reference_ID())) {
						Timestamp ts = null;
						if (value instanceof Timestamp)
							ts = (Timestamp)value;
						else {
							SimpleDateFormat df = DisplayType.getDateFormat(pPara.getAD_Reference_ID());
							ts = new Timestamp(df.parse(value.toString()).getTime());
						}
						iPara.setP_Date(ts);
						log.fine("fillParameter - " + key
								+ " = " + valueString + " (=" + ts + "=)");
					} else {

						if (pPara.getAD_Reference_ID() == DisplayType.YesNo)
							value = "true".equalsIgnoreCase(value.toString()) ? "Y" : "N";

						iPara.setP_String(value.toString());
					}
					//
					iPara.setInfo(value.toString());
					iPara.saveEx();
				} catch (Exception e) {
					log.warning("fillParameter - " + key
							+ " = " + valueString + " (" + value
							+ ") " + value.getClass().getName()
							+ " - " + e.getLocalizedMessage());
				}

				// Range To
				key += "_2";
				valueString = parameters.get(key);
				value = valueString;
				if (valueString != null && valueString.length() == 0)
					value = null;
				if (value != null) {
					//	Convert to Type
					try {
						if (DisplayType.isNumeric(pPara.getAD_Reference_ID()) 
								|| DisplayType.isID(pPara.getAD_Reference_ID())) {
							BigDecimal bd = null;
							if (value instanceof BigDecimal)
								bd = (BigDecimal)value;
							else if (value instanceof Integer)
								bd = new BigDecimal (((Integer)value).intValue());
							else
								bd = new BigDecimal (value.toString());

							if ((DisplayType.isID(pPara.getAD_Reference_ID()) && "-1".equals(value))
									|| (DisplayType.Locator == pPara.getAD_Reference_ID() && "0".equals(value)))      // empty selection
								iPara.setP_Number_To((BigDecimal) null);
							else
								iPara.setP_Number_To(bd);

							log.fine("fillParameter - " + key
									+ " = " + valueString + " (=" + bd + "=)");
						} else if (DisplayType.isDate(pPara.getAD_Reference_ID())) {
							Timestamp ts = null;
							if (value instanceof Timestamp)
								ts = (Timestamp)value;
							else {
								SimpleDateFormat df = DisplayType.getDateFormat(pPara.getAD_Reference_ID());
								ts = new Timestamp(df.parse(value.toString()).getTime());
							}
							iPara.setP_Date_To(ts);
							log.fine("fillParameter - " + key
									+ " = " + valueString + " (=" + ts + "=)");
						} else {

							if (pPara.getAD_Reference_ID() == DisplayType.YesNo)
								value = "true".equalsIgnoreCase(value.toString()) ? "Y" : "N";

							iPara.setP_String_To(value.toString());
						}
						//
						iPara.setInfo_To(value.toString());
						iPara.saveEx();
					} catch (Exception e) {
						log.warning("fillParameter - " + key
								+ " = " + valueString + " (" + value
								+ ") " + value.getClass().getName()
								+ " - " + e.getLocalizedMessage());
					}
				}
			}	//	not null
		}	//	instance parameter loop

		return pInstance;
	}	//	fillParameter
	
	public boolean getProcessOK() {
		return processOK;
	}
	
	public File getPdfFile() {
		return pdfFile;
	}
	
	public String getFileName() {
		return process.get_Translation("Name");
	}

}
