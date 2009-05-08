/**
 * Copyright (C) 2006, Laboratorio di Valutazione delle Prestazioni - Politecnico di Milano

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package jmt.engine.NodeSections;

import jmt.engine.QueueNet.NetEvent;	// MF (used in processing())
import jmt.engine.QueueNet.NetMessage;
import jmt.engine.QueueNet.NetNode;
import jmt.engine.QueueNet.NodeSection;
import jmt.engine.log.JSimLogger;
import jmt.engine.log.LoggerParameters; // MF (common object)

import java.util.HashMap; // MF (for Indexer)
import java.io.File; 	  // MF (check if logfile exists)

/**
 * <p>Title: LogTunnel Extension</p>
 * <p>Description: This class extends the "tunnel" service-section
 * by adding logging (information about forward messages only).
 * Every message sent from the input section is forwarded to the
 * output section and every message sent the from output section
 * is backwarded to the input section.</p>
 *
 * @author Michael Fercu
 *         Date: 12-lug-2008
 *         Time: 14.04.02
 *
 */
public class LogTunnel extends ServiceTunnel /*ServiceSection*/ {

	private final boolean DEBUG = true;
	
	private char chDelimiter, decimalSeparator;
	private int intReplacePolicy;
	private boolean boolExecutionTimestamp;
	private String strTimestampValue;
	private LoggerParameters lp;
	private JSimLogger localLog;
	private final JSimLogger debugLog = JSimLogger.getLogger(JSimLogger.STD_LOGGER);

	HashMap classesIndexerFast;	      /* Holds {name of class},{array index for classTimeAccounting} */
	short   classesTimeAccountingSize;
	double  classesTimeAccounting[];  /* Holds [previously-accessed time] of all classes */
	double  classesTimePreviousAny;   /* Holds [previous   arrival time]  of any of the classes, so as not to have to search */

	boolean didInitialCheck;

	/**
	 *  Creates a new instance of LogTunnel; called by simulator engine.
	 */
	public LogTunnel(String argFN, String argFP, Boolean argBLN, Boolean argBTS, Boolean argBJID, Boolean argBJC, Boolean argBTSC, Boolean argBTAC, Integer numClasses)
	{
       super();
       /* Create an object to hold the Logger's parameters, with parameters from XMLReader */
       lp = new LoggerParameters(argFN, argFP, argBLN, argBTS, argBJID, argBJC, argBTSC, argBTAC);
       
       /* Reset the 'didInitialCheck' variable for initialCheck() */
       didInitialCheck = false;

       /* Initialize objects for statistics accounting, like, the time between two messages. (mf'08) */
       classesIndexerFast = new HashMap(numClasses.intValue()+2,1);
       classesTimeAccounting = new double[numClasses.intValue()+1];
       classesTimePreviousAny = 0.0F;
       classesTimeAccountingSize = 0;
       
       if (DEBUG) 
       {
    	   debugLog.debug("[LT] new LoggerParameters(" + argFP + "," + argFN + "," + argBLN +","+ argBTS +","+ argBJID +","+ argBJC +","+ argBTSC +","+ argBTAC + ");");
    	   debugLog.debug("[LT] constructed " + (lp.isEnabled ? "and en" : "but dis") + "abled for " + lp.name);
       }
	}

	
	/**
	 * Checks if the file is writable
	 */ 
	private void initialCheck() {
		Boolean append;
		String absfilepath = "";
		
		// Get global values (from the XML file) for the path, auto-replace, and delimiter 
		lp.path = getOwnerNode().getSimParameters().getLogPath();
		intReplacePolicy = new Integer(getOwnerNode().getSimParameters().getLogReplaceMode()).intValue();
		chDelimiter = (getOwnerNode().getSimParameters().getLogDelimiter().charAt(0));
		try
		{
			String ds = getOwnerNode().getSimParameters().getLogDecimalSeparator();
			if(ds.length() > 0) decimalSeparator = ds.charAt(0);
			else decimalSeparator = '.';
		}catch(Exception e) { debugLog.debug(e.toString()); }
		boolExecutionTimestamp = new Boolean(getOwnerNode().getSimParameters().getLogExecutionTimestamp()).booleanValue();
		strTimestampValue = getOwnerNode().getSimParameters().getTimestampValue();
		
		// Add an initial file-separator if none exists
       if ((lp.path != "") && (lp.path.endsWith(File.separator) == false) && (lp.path.endsWith(JSimLogger.FILESEPARATOR) == false))
    	       lp.path = lp.path + File.separator;

       if (lp.path == File.separator || lp.path == JSimLogger.FILESEPARATOR)
    	   debugLog.info("Warning: possible mis-set path for <" + lp.path + "><" + lp.name + ">  (filesystem root)");

	    // Enable the log if: the logger is enabled, and the logfile is usable.
   		if (lp.isEnabled() == true)
   			absfilepath = checkLogfileWriteableAndReturnAbsPath();
   		else
   			debugLog.info("Not logging '" + lp.name + "'.  (nothing to log?)");
   		
   		// When logging is possible, connect this stream to JSimLogger
   		if (lp.isEnabled() == true)
   		{
   			append = new Boolean(intReplacePolicy == LoggerParameters.LOGGER_AR_APPEND);

   	    	if (DEBUG)
   	    		debugLog.debug("[LT].initialCheck() assigned: <"+lp.path+"=" + absfilepath + "><repl: " + intReplacePolicy + "><" + chDelimiter + "><" + (append.booleanValue() ? "append" : "replace") + "> by :" );

   			if (lp.isGlobal() == true)
   			{
   				localLog = JSimLogger.changeAppenderParameters(this.getClass().getName(),"LOGTUNNEL",
   							lp.path, lp.name, append);
   				JSimLogger.getLogger(JSimLogger.LOGTUNNEL_LOGGER).setHeaderWrittenFlag(absfilepath, lp.name, false);
   			}
   			else
   			{
   				localLog = JSimLogger.makeNewFileLogger(this.getClass().getName(),
   							lp.path, lp.name, append );
   				JSimLogger.getLogger(JSimLogger.LOGTUNNEL_LOGGER).setHeaderWrittenFlag(absfilepath, lp.name, false);
   			}

   			// Now that the logger is connected, write the header
   			writeHeader(append.booleanValue());
   		}

	}
	
	
	/**
	 * Convenience function used by the LogTunnel constructor to check that the logfile is usable.
	 * If the file is not usable, the logger-properties are set to disabled.
	 * This convenience function is only called by the initial check.
	 */
	private final String checkLogfileWriteableAndReturnAbsPath()
    {
		boolean loggerInstanceIsEnabled = false;
		String absolutePath = "";

		try {
			if ((new File(lp.path)).mkdirs() == true)
				debugLog.debug("LogTunnel.checkLogfileWriteable(): Created (missing) directory structure for " + lp.name);

			File f = new File(lp.path + lp.name + "");

	    	if (f.exists() == true)
	    	{
	    		if (f.canWrite() == true)
	    		{
	    			if (f.length() >= 0)
	    			{
    				   loggerInstanceIsEnabled = true;
    				   absolutePath = f.getAbsolutePath();
	    		    }
	    			else
	    				debugLog.error("LogTunnel.checkLogfileWriteable(): The file " + f.getAbsolutePath() + " has filesize " + f.length() + ".");
	    		}
	    		else
	    			debugLog.error("LogTunnel.checkLogfileWriteable(): The file " + f.getAbsolutePath() + " has no write access.");
	    	}
	    	else
	    	{
	    		absolutePath = f.getAbsolutePath();
	    		loggerInstanceIsEnabled = true;
	    	}

	    	f = null;
		}
		catch (Exception e) {
 	    	   debugLog.error("LogTunnel.checkLogfileWriteable(): Exception generated checking file " + lp.path + lp.name + ", not logging to this file.  Detail: " + e.toString());
 	    	   loggerInstanceIsEnabled = false;
 	       }

		if (DEBUG)
			debugLog.debug("[LT].chkFileWriteable confirms that " + absolutePath + " is " + (loggerInstanceIsEnabled ? "good." : "bad.") );

		if (loggerInstanceIsEnabled == true)
			lp.enable();
		else
			lp.disable();
		
		return absolutePath;
    }

	/**
	 * Convenience function used by the LogTunnel constructor to write the logfile header.
	 */
	private void writeHeader(boolean append)
	{
	       try {
	    	 File f = new File(lp.path + lp.name + "");
	    	 JSimLogger j = JSimLogger.getLogger(JSimLogger.LOGTUNNEL_LOGGER);

	    	 if (append == false)
	    	 {
				if (lp.isGlobal() == true && j.getHeaderWrittenFlag(lp.path, lp.name) == 0)
				{
					// we're about to write the header, since it wasn't written before
					j.setHeaderWrittenFlag(lp.path, lp.name, true);
					
					// compose the header, and then write to the logfile
					String h = composeHeaderLine();
					localLog.info(h);

					debugLog.debug("Logging to " + lp.path + lp.name + " with flags " + lp.toString().substring(0,8) + " (replace global)");
				}
				else if (lp.isGlobal() == false)
				{
					String h = composeHeaderLine();
					localLog.info(h);

					debugLog.info("Logging to " + lp.path + lp.name + " with flags " + lp.toString().substring(0,8) + " (replace local)");
				}
				else //if (lp.isGlobal() && glbHeadrWritten == true) then no action required
				{
				   	debugLog.info("Logging to " + lp.path + lp.name + " with flags " + lp.toString().substring(0,8) + " (replace,aggregated)");
				}
	         }
	    	 else if ((append == true) && ((f.exists() == false) || f.length() == 0))
	    	 {
	    		 // if appending, or if writing to a new file, indicate we're about to write the header 
	    		if (lp.isGlobal() == true && j.getHeaderWrittenFlag(lp.path, lp.name) == 0)
	    			j.setHeaderWrittenFlag(lp.path, lp.name, true);

				String h = composeHeaderLine();
				localLog.info(h);
	
	    		 debugLog.info("Logging to " + lp.path + lp.name + " with flags " + lp.toString().substring(0,8) + " (append to new)");
	    	 }
	    	 else
	    		 debugLog.info("Logging to " + lp.path + lp.name + " with flags " + lp.toString().substring(0,8) + " (append).");
	       }
	       catch (Exception e) {
	    	   debugLog.error("Exception writing file header to "+lp.name+".  Disabling log.  Details: " + e.getStackTrace().toString()); 
	    	   lp.disable();
	       }
	}
	
	private final String composeHeaderLine()
	{
		/* compose header to write to the logfile */
		String s = "";
		if (lp.boolLoggername.booleanValue() == true)
			s += "LOGGERNAME" + chDelimiter;
		if (lp.boolTimeStamp.booleanValue() == true)
			s += "TIMESTAMP" + chDelimiter;
		if (lp.boolJobID.booleanValue() == true)
			s += "JOBID" + chDelimiter;
		if (lp.boolJobClass.booleanValue() == true)
			s += "JOBCLASS" + chDelimiter;
		if (lp.boolTimeSameClass.booleanValue() == true)
			s += "TIMEELAPSED_SAMECLASS" + chDelimiter;
		if (lp.boolTimeAnyClass.booleanValue() == true)
			s += "TIMEELAPSED_ANYCLASS" + chDelimiter;
		if (boolExecutionTimestamp == true)
			s += "EXECUTION_TIMESTAMP" + chDelimiter;

		if (s.charAt(s.length()-1) == chDelimiter) // remove trailing semicolon
			return s.substring(0, s.length()-1);
		else
			return s;
	}

    public void NodeLinked(NetNode node) {
    }

	protected int process(NetMessage message) throws jmt.common.exception.NetException {
       if (isMyOwnerNode(message.getSource())) {
    	   
			if (message.getEvent() == NetEvent.EVENT_START) {
				debugLog.debug("[LT] EVENT_START");
			}
			if (message.getEvent() == NetEvent.EVENT_ABORT) {
				debugLog.debug("[LT] EVENT_ABORT");
			}
			if (message.getEvent() == NetEvent.EVENT_STOP) {
				
				debugLog.debug("[LT] EVENT_STOP");
				JSimLogger.getLogger(JSimLogger.LOGTUNNEL_LOGGER).closeCustomAppender(this.getClass().getName(), lp.path, "LT"+lp.name);
			}
			
			if (message.getSourceSection() == NodeSection.INPUT) {
				sendForward(message.getEvent(), message.getData(), 0.0);

				/* write to log this forward-going message */
				if (message.getEvent() == NetEvent.EVENT_JOB)
				{

				  // MF08: this is an initial file and logger check
				  // it is done here because getOwnerNode() cannot be used in the constructor
				  if (didInitialCheck == false)
				  {
					didInitialCheck = true;
					if (lp.isEnabled == true)
						initialCheck();
				  }
				  
				  // 
				  if (lp.isEnabled == true) {
					/*
					 * TO DO:Consider changing LoggerParameters (and LoggerSectionPanel, XMLWriter, XMLReader)
					 *  to use primitive booleans instead of Boolean Objects to avoid conversion penalty.
					 */

					String s = "";

					/* compose one line to write to the logfile */
					if (lp.boolLoggername.booleanValue() == true)
					{
						s += message.getSource().getName();
						s += chDelimiter;
					}

					if (lp.boolTimeStamp.booleanValue() == true)
					{
						s += Double.toString(message.getTime()).replace('.', decimalSeparator).replace(',', decimalSeparator);
						s += chDelimiter;
					}

					if (lp.boolJobID.booleanValue() == true)
					{
						s += Integer.toString(message.getJob().getId());
						s += chDelimiter;
					}

					if (lp.boolJobClass.booleanValue() == true)
					{
						s += message.getJob().getJobClass().getName();
						s += chDelimiter;
					}

					if (lp.boolTimeSameClass.booleanValue() == true)
					{
						Integer idx = (Integer)classesIndexerFast.get(message.getJob().getJobClass().getName());

						if (idx != null)
							s += Double.toString(message.getTime() - classesTimeAccounting[idx.intValue()]).replace('.', decimalSeparator).replace(',', decimalSeparator);
						else
						{
							s += "0";
							idx = new Integer(classesTimeAccountingSize++);
							classesIndexerFast.put(message.getJob().getJobClass().getName(),new Integer(classesTimeAccountingSize));
						}
						classesTimeAccounting[idx.intValue()] = message.getTime();
						s += chDelimiter;
					}

					if (lp.boolTimeAnyClass.booleanValue() == true) {
						s += Double.toString(message.getTime() - classesTimePreviousAny).replace('.', decimalSeparator).replace(',', decimalSeparator);
						classesTimePreviousAny = message.getTime();
						s += chDelimiter;
					}
					if (boolExecutionTimestamp == true) {
						s += strTimestampValue;
					}

					/* the composed line is now written out to the logfile */
					int sl = s.length()-1;
					if (s.charAt(sl) == chDelimiter)
						localLog.info(s.substring(0, sl));
					else
						localLog.info(s);
				  }
				}
				/* end of line */

			}

			if (message.getSourceSection() == NodeSection.OUTPUT) {
				sendBackward(message.getEvent(), message.getData(), 0.0);

				//log.write(NetLog.LEVEL_ALL, message.getJob(), this, NetLog.ACK_JOB);
			}
			

			return MSG_PROCESSED;
		} else
			return MSG_NOT_PROCESSED;
	}

	protected void finalize() throws Throwable {
	    debugLog.debug("[LT].finalize() runs.");
	    super.finalize();
	}
}