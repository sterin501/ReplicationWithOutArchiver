package pushtoTargetRIDC;

import java.util.*;
import java.io.*;
import intradoc.common.*;
import intradoc.data.*;
import intradoc.server.*;
import intradoc.shared.*;
import oracle.stellent.ridc.*;
import oracle.stellent.ridc.model.*;
import oracle.stellent.ridc.protocol.*;
import oracle.stellent.ridc.protocol.intradoc.*;
import oracle.stellent.ridc.common.log.*;
import oracle.stellent.ridc.model.serialize.*;
import oracle.stellent.ridc.protocol.http.*;

/**
 * This is the code that can be executed by any Idc service that has a 
 * service type of "AcmeMail.AcmeMailService".
 */
public class ToGETPushRIDC extends ServiceHandler
{


String targetdid;
int commitstatus=1;

	
	/**
	 * This code will create a html page based on the data in the databinder,
	 * and email it to the list of addresses specified by the user.
	 */
	public void toGetPushRIDCMain() throws DataException, intradoc.common.ServiceException
	{
		    traceVerbose("Inside the filter toGetPushRIDCMain");

                       
                              long  currentTimeMillis =   System.currentTimeMillis();


                                    long   timetoCheckMillis;
                                        
                                    if (m_binder.getLocal("doCheckinMin")!=null )
                                        timetoCheckMillis =  Long.parseLong(m_binder.getLocal("doCheckinMin")) * 60000;
 
                                     else 
                                         timetoCheckMillis = 300000 ;

                                    timetoCheckMillis = currentTimeMillis -timetoCheckMillis; 

          

                  String selectSQL = "select RID,dID,RIDCSERVICE,VAULTFILEPATH from pullRIDC where Rentrydate >"+ timetoCheckMillis +"AND LASTACTION='New Entry' ORDER BY Rid asc" ;

                            intradoc.data.ResultSet rsSQLpullRIDCTable  =  runSQLpullRIDCTable(m_workspace,selectSQL );

                                             

                                           if ( rsSQLpullRIDCTable.next())
                                         createListResultSet  (rsSQLpullRIDCTable  );

                      
                 
                   traceVerbose("Coming Back to  toGetPushRIDCMain" );

                       


         } // end of toGetPushRIDCMain


public   List createListResultSet (intradoc.data.ResultSet rs)

{

List<String> rsList = new ArrayList<String>();  ;


for(rs.first(); rs.isRowPresent(); rs.next())
  


   {


String RID =rs.getStringValueByName("RID");
String dID =rs.getStringValueByName("dID");
String RIDCSERVICE =rs.getStringValueByName("RIDCSERVICE");
String VAULTFILEPATH = rs.getStringValueByName("VAULTFILEPATH");



if (VAULTFILEPATH == null)
VAULTFILEPATH=0;

rsList.add(RID+"\t"+dID+"\t"+RIDCSERVICE+"\t"+VAULTFILEPATH);

   }

return rsList;


} // end of createListResultSet


//private void toGetdIDforPush(intradoc.data.ResultSet rsPullRIDCTable) throws intradoc.common.ServiceException ,intradoc.data.DataException

private void toGetdIDforPush ( List<String>  rsSQLList )  throws intradoc.common.ServiceException ,intradoc.data.DataException


            {

                                  String   dstatus = "status is Before LOOP " +  rsSQLpullRIDCTable.isRowPresent();
                               
                                             traceVerbose(dstatus);

                                            

                                       for(rsSQLpullRIDCTable.first(); rsSQLpullRIDCTable.isRowPresent(); rsSQLpullRIDCTable.next())

                                          {

                                                
                                                   
                                                     String   idcService = rsSQLpullRIDCTable.getStringValueByName("RIDCSERVICE");
                                                     
                                                     String   dID  = rsSQLpullRIDCTable.getStringValueByName("dID");
                                                     String   RID  = rsSQLpullRIDCTable.getStringValueByName("RID");
                                           //        
                                                   
                                                       
                                                                 dstatus = "status is before if " +  rsSQLpullRIDCTable.isRowPresent();
                               
                                                                            traceVerbose(dstatus);
                                                   

                                                                 if ( idcService.equals ("CHECKIN_UNIVERSAL")|| idcService.equals ("CHECKIN_NEW"))
                                                                    {    
                                                                              
                                                                           String  VaultfilePath =  rsSQLpullRIDCTable.getStringValueByName("VAULTFILEPATH");
                                                                           traceVerbose (dID  + "  " + idcService);
                                                                            
                                                                          
                                                                            doPushCheckin (dID,RID,VaultfilePath);   

                                                                            traceVerbose("Coming Back to  toGetdIDforPush" );
                                                                            dstatus = "status is after funcall " +  rsSQLpullRIDCTable.isRowPresent();
                               
                                                                            traceVerbose(dstatus);
                                                   
                                                                
  
                                                                     } // end of IF checkin

                                                  
                                                                else  if ( idcService.equals ("CHECKOUT_OK"))
                                                                           {

                                                                                    doPushCheckout(dID,RID);


                                                                           }
                                                                else  if ( idcService.contains ("UPDATE_DOCINFO"))
                                                                           {

                                                                           String selectSQL = "select TARGETDID from pullRIDC where dID="+dID+" and TARGETDID is not null" ;    
                                                                              
                                                                                       
                                                                                    intradoc.data.ResultSet targetDocInfo =   runSQLpullRIDCTable(m_workspace,selectSQL );

                                                                                      // traceResultSet(targetDocInfo);
                                                                                             
                                                                                         String targetdidNew = getTargetID(targetDocInfo);
                                                                                       

                                                                                    doPushUpdateDocinfo(dID,RID,targetdidNew);
                                                                                   
                                                                           

                                                                           }

                                                                  else  if ( idcService.contains ("DELETE_REV"))
                                                                            {
                                                                              doPushDelete(dID,RID);

                                                                           }
      
                                     
                                        //  traceVerbose ("Going to Next Row of toGetdIDforPush  ---");           
                                   
                                          } // end of For 1:


                                                                            dstatus = "status is End of LOOP " +  rsSQLpullRIDCTable.isRowPresent();
                               
                                                                            traceVerbose(dstatus);

     


            }  // end of toGetdIDforPush




public void doPushCheckin (String dID,String RID,String VaultfilePath)  throws intradoc.common.ServiceException


{


                                            

                 m_binder.getLocalData().clear();
                 m_binder.putLocal("dID", dID);
                 this.executeService(m_binder, "DOC_INFO");
                 intradoc.data.ResultSet docInfo = m_binder.getResultSet("DOC_INFO");

            



                               try {
                                                                                      
                                  Long currentTimeMillis =   System.currentTimeMillis();

           String updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",LASTACTION='Pushing',RIDCPUSHACTION='Pushing' WHERE rID="+RID ;

                               insertpullRIDCTable (m_workspace,updateSQL);

                              String ridcpushaction = useRIDCCheckin(docInfo , VaultfilePath);

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

             updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",LASTACTION='Pushed',RIDCPUSHACTION='"+ridcpushaction+"',COMMITSTATUS="+commitstatus+",TARGETDID="+targetdid+" WHERE rID="+RID ;

                               insertpullRIDCTable (m_workspace,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception Caught on RIDC call "+e.getMessage());
                                     
                                  }










}  // end of doPushCheckin




public void doPushCheckout (String dID,String RID)  throws intradoc.common.ServiceException

{

traceVerbose ("Doing doPushCheckout" );


} // end of doPushCheckout 



public void doPushUpdateDocinfo (String dID,String RID,String targetdidNew)  throws intradoc.common.ServiceException

{

traceVerbose ("Doing doPushUpdateDocinfo" );

                 m_binder.getLocalData().clear();
                 m_binder.putLocal("dID", dID);
                 this.executeService(m_binder, "DOC_INFO");
                 intradoc.data.ResultSet docInfo = m_binder.getResultSet("DOC_INFO");

            



                               try {
                                                                                      
                                  Long currentTimeMillis =   System.currentTimeMillis();

           String updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",LASTACTION='Pushing',RIDCPUSHACTION='Pushing' WHERE rID="+RID ;

                               insertpullRIDCTable (m_workspace,updateSQL);

                              String ridcpushaction = useRIDCForMetaUpdate(docInfo,targetdidNew );

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

             updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",LASTACTION='Pushed',RIDCPUSHACTION='"+ridcpushaction+"',COMMITSTATUS="+commitstatus+",TARGETDID="+targetdid+" WHERE rID="+RID ;

                               insertpullRIDCTable (m_workspace,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception Caught on SQL call "+e.getMessage());
                                     
                                  }



}



public void doPushDelete (String dID,String RID)  throws intradoc.common.ServiceException

{

traceVerbose ("Doing doPushDelete" );


}


public String  useRIDCCheckin  (intradoc.data.ResultSet docInfo , String  VaultfilePath) throws Exception 

{

IdcClientManager manager = new IdcClientManager ();
traceVerbose ("in useRIDCCheckin class ");



String status="" ;

oracle.stellent.ridc.model.DataBinder  responseData = null;
                    
		try{
                               		IdcClient idcClient = manager.createClient ("http://10.184.36.144:16218/cs/idcplg");

                       IdcContext userContext = new IdcContext ("weblogic","welcome1");

			// Create an HdaBinderSerializer; this is not necessary, but it allows us to serialize the request and response data binders
			HdaBinderSerializer serializer = new HdaBinderSerializer ("UTF-8", idcClient.getDataFactory ());
			
			// Create a new binder for submitting a search
			oracle.stellent.ridc.model.DataBinder  dataBinder = idcClient.createBinder();

                             traceVerbose ("in useRIDC class  NO CATCH SO FAR");


                                     dataBinder = completedataBinder( dataBinder, docInfo);
                                       
        
                                dataBinder.addFile("primaryFile", new File(VaultfilePath));
                                dataBinder.putLocal("IdcService", "CHECKIN_UNIVERSAL");

                                ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
  
             responseData = response.getResponseAsBinder();


                                  //    traceVerbose(responseData.getLocal("StatusMessage")); 

                                   //   status = responseData.getLocal("StatusMessage");
        

                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=0;

		   }catch (IOException ioe){
			traceVerbose(ioe.getMessage());
                         status = ioe.getMessage();
		}finally {
                                   if ( responseData != null)    
                              // traceVerbose(responseData.getLocal("StatusMessage")); 
                               status = responseData.getLocal("StatusMessage");

                             targetdid = responseData.getLocal("dID");
                            
                }

return status ;

} // end of useRIDCCheckin 


public String  useRIDCForMetaUpdate  (intradoc.data.ResultSet docInfo,String targetdidNew ) throws Exception 


{

IdcClientManager manager = new IdcClientManager ();
traceVerbose ("in useRIDCForMetaUpdate class ");



String status="" ;

oracle.stellent.ridc.model.DataBinder  responseData = null;
                    
		try{
                       
                       IdcClient idcClient = manager.createClient ("http://10.184.36.144:16218/cs/idcplg");

                       IdcContext userContext = new IdcContext ("weblogic","welcome1");

			// Create an HdaBinderSerializer; this is not necessary, but it allows us to serialize the request and response data binders
			HdaBinderSerializer serializer = new HdaBinderSerializer ("UTF-8", idcClient.getDataFactory ());
			
			// Create a new binder for submitting a search
			oracle.stellent.ridc.model.DataBinder  dataBinder = idcClient.createBinder();

                           

                                     dataBinder = completedataBinder( dataBinder, docInfo);
          
        
                                dataBinder.putLocal("IdcService", "UPDATE_DOCINFO");
                                 
                                 dataBinder.putLocal("dID", targetdidNew);

                                ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
  
                                  responseData = response.getResponseAsBinder();


                                  //    traceVerbose(responseData.getLocal("StatusMessage")); 

                                   //   status = responseData.getLocal("StatusMessage");
        

                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=0;

		   }finally {
                                   if ( responseData != null)    
                             
                                     {  
                                                            int statuscode=1;
                                               if( responseData.getLocal("StatusCode") != null)
                                             statuscode = Integer.parseInt(responseData.getLocal("StatusCode"));           
                                          
                                                      if ( statuscode < 0 )
                                                       {
                                                                  status = responseData.getLocal("StatusMessage");
                                                              
                                                       }
                                                      else
                                                       {
                                                             status = "Update is fine" ;
                                                             commitstatus=1;
                                                             targetdid = responseData.getLocal("dID");
                                                       }



                                      }
                }

return status ;





} // end of useRIDCForMetaUpdate

 

public oracle.stellent.ridc.model.DataBinder completedataBinder   (oracle.stellent.ridc.model.DataBinder  dataBinder , intradoc.data.ResultSet rs )

{

                               
                                      int columsize = rs.getNumFields(); 


                                       for(rs.first(); rs.isRowPresent(); rs.next())

                                          {

                                                for (int j=0;j< columsize ; j++)

                                                  {
                                                     String   key = rs.getFieldName(j);
                                                     String   value = rs.getStringValueByName(key);
                                                     
                                                                                 if ( key.toLowerCase().contains("date")  )
                                                                                     {

                                                                                    

                                                                                                if ( key.equals("dCreateDate") || key.equals("dOutDate") )
                                                                                                    {
                                                                                                        //System.out.println("Skipping");
                                                                                                    }
                                                                                              
                                                                                                     else {   

                                                                                              value = value.substring(0, value.length()-1);

                                                                               
                                                                                              //traceVerbose(key + " --- " + value );



                                                                                                            }
                                                                                       }

                                                                                   else {

                                                                                            dataBinder.putLocal(key,value);  
                                                                                        } 
                                                    

                                                  }
                               
                                        
                                   
                                          }
                   
return dataBinder;


}  //end of  completedataBinder



	
	protected void insertpullRIDCTable (Workspace ws,String updateSQL ) throws DataException
	


          {

		// insert the proper row into the database table








		ws.executeSQL(updateSQL);
	} // end of insertpullRIDCTable



private static void trace(final String message)

              {
                 
                  Report.trace("PushRIDC", message, null);
              }        


 private static void traceVerbose(final String message) 

                  {
                
                if (Report.m_verbose) 
                         {
                        trace(message);
                         }
                   }  // end of TraceVerbose



	protected intradoc.data.ResultSet runSQLpullRIDCTable (Workspace ws,String SQlstatement) throws DataException ,intradoc.common.ServiceException
	


          {

		


                              
                                             

                           intradoc.data.ResultSet rs =  ws.createResultSetSQL(SQlstatement);
              
                               
                                  return rs;
                             

		
	} // end of updatepullRIDCTable

private void traceResultSet (final intradoc.data.ResultSet rs)


            {

                                
                                    int rowCount = 0;
                                    int columsize = rs.getNumFields(); 


                                       for(rs.first(); rs.isRowPresent(); rs.next())

                                          {

                                                for (int j=0;j< columsize ; j++)

                                                  {
                                                     String   key = rs.getFieldName(j);
                                                     String   value = rs.getStringValueByName(key);
                                                     traceVerbose (key + "  " + value);

                                                  }
                                          
                                          rowCount++; 
                                          traceVerbose ("Going to Next Row of traceResultSet---");           
                                   
                                          }


            } // end of traceResultSet 



private String  getTargetID (final intradoc.data.ResultSet rs)


            {

                          String newValue="";      
                             
                                    int columsize = rs.getNumFields(); 


                                       for(rs.first(); rs.isRowPresent(); rs.next())

                                          {

                                                for (int j=0;j< columsize ; j++)

                                                  {
                                                     String   key = rs.getFieldName(j);
                                                     newValue = rs.getStringValueByName(key);
                                                    // traceVerbose (key + "  " + value);

                                                      

                                                  }
                                          
                               
                                                
                                   
                                          }


       return newValue;


            }  // end of getTargetID



private void executeService(final  intradoc.data.DataBinder  serviceBinder, final String serviceName) throws intradoc.common.ServiceException {
                       traceVerbose("Start executeService");
                        

                       try {
                               trace("Calling service " + serviceName + ": " + serviceBinder.getLocalData().toString());
                               // Execute service
                               m_service.getRequestImplementor().executeServiceTopLevelSimple(serviceBinder, serviceName, m_service.getUserData());
                               trace("Finished calling service");


                             } catch (final DataException e) {
                                           trace("Something went wrong executing service " + serviceName);
                                          e.printStackTrace(System.out);
                                           throw new intradoc.common.ServiceException("Something went wrong executing service " + serviceName, e);
                            
                               } finally {
                                         traceVerbose("End executeService");
                                          }



                      



                  }  // end of executeService






} //end of cLass 
