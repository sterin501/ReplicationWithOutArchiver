/******************************************************************************/
/*                                                                            */
/*  Stellent, Incorporated Confidential and Proprietary                       */
/*                                                                            */
/*  This computer program contains valuable, confidential and proprietary     */
/*  information.  Disclosure, use, or reproduction without the written        */
/*  authorization of Stellent is prohibited.  This unpublished                */
/*  work by Stellent is protected by the laws of the United States            */
/*  and other countries.  If publication of the computer program should occur,*/
/*  the following notice shall apply:                                         */
/*                                                                            */
/*  Copyright (c) 1997-2001 IntraNet Solutions, Incorporated.  All rights	  */
/*	reserved.																  */
/*  Copyright (c) 2001-2007 Stellent, Incorporated.  All rights reserved.     */
/*                                                                            */
/******************************************************************************/

package pushtoTargetRIDC;

import static pushtoTargetRIDC.Utils.trace;
import static pushtoTargetRIDC.Utils.traceVerbose;


import intradoc.common.*;
import intradoc.data.*;
import intradoc.server.*;
import intradoc.shared.*;

/**
 * Another way to execute arbitraty Java Code is through filters.  There
 * are many hooks in the server that check to see if the user wishes to execute
 * additional Java code before performing the standard functions.  
 * 
 * Common spots for filters include validation of data before checkin,
 * executing special code upon server startup, and execution of special
 * code at the beginning of a workflow.
 * 
 * This class is for code that needs to be executed on a scheduled basis, but
 * more frequently than a system event. It therefore hooks into the 
 * 'checkScheduledEvents' hook, and will be executed about every five minutes
 */
public class CustomFrequentEvent  extends ServiceHandler implements FilterImplementor
{
	/**
	 * Just a quick scheuled event
	 */
	public int doFilter(Workspace ws, DataBinder eventData, ExecutionContext cxt)
		throws DataException, ServiceException
	{
		// 
		

                  long  currentTimeMillis =   System.currentTimeMillis();

                    if (  (currentTimeMillis/(1000*60))%5 == 0)
                            {

                                traceVerbose("executing action...");
                               if ( SharedObjects.getEnvironmentValue("EnableReplication").equals("true"))



                                    {
                                              intradoc.data.DataBinder binder = new DataBinder();
                                              binder.getLocalData().clear();
                                              binder.putLocal("CommitStatus", "0");

                                                 binder.putLocal("UserDateFormat","iso8601");

                                                
                                              binder.putLocal("IdcService","GET_PUSH_RIDC");
                                              this.executeService(binder, "GET_PUSH_RIDC",ws);


                                    }


                            }
                      
                        
		
		// filter executed correctly.  Return FINISHED.
		return FINISHED;
	}
	
	
private void executeService(final  intradoc.data.DataBinder  serviceBinder, final String serviceName , Workspace workspace ) throws intradoc.common.ServiceException {
                       traceVerbose("Start executeService");
                        

                       try {

                                 

			serviceBinder.setEnvironmentValue("REMOTE_USER", "weblogic");
			final ServiceManager smg = new ServiceManager();
			smg.init(serviceBinder, workspace);
			smg.processCommand();
			smg.clear();
			traceVerbose("Successfully executed IdcService " + serviceName);




                             } catch (final Exception e ) {
                                           trace("Something went wrong executing service " + serviceName);
                                          e.printStackTrace(System.out);
                                           throw new intradoc.common.ServiceException("Something went wrong executing service " + serviceName, e);
                            
                               } finally {
                                         traceVerbose("End executeService");
                                          }



                      


   
                    }


	
}
