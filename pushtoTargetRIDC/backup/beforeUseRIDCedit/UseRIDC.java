package pushtoTargetRIDC;

import static pushtoTargetRIDC.Utils.*;


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


public class UseRIDC {


static String targetdid;
static int commitstatus=1;
static oracle.stellent.ridc.model.DataBinder  dataBinder;
static IdcContext userContext ;
static IdcClient idcClient;
static IdcClientManager manager;
static List<String> listoftargetGUIDs;

public UseRIDC ( )


{

                     


          try {
 
                         manager = new IdcClientManager ();

                         idcClient = manager.createClient (SharedObjects.getEnvironmentValue("TargetServer"));

                         userContext = new IdcContext (SharedObjects.getEnvironmentValue("TargetUsername"),SharedObjects.getEnvironmentValue("TargetPassword"));

			// Create an HdaBinderSerializer; this is not necessary, but it allows us to serialize the request and response data binders
			HdaBinderSerializer serializer = new HdaBinderSerializer ("UTF-8", idcClient.getDataFactory ());
			
			// Create a new binder for submitting a search
			   dataBinder = idcClient.createBinder();

                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                        commitstatus=-1;

		   }


}  // end of Constructor 

public  String  useRIDCCheckin  (intradoc.data.ResultSet docInfo , String  VaultfilePath) throws Exception 

{


traceVerbose ("in useRIDCCheckin class ");
String status="" ;
oracle.stellent.ridc.model.DataBinder  responseData = null;
commitstatus=4;
                    
		try{
                                      



                                 dataBinder = completedataBinder( dataBinder, docInfo);
                                  
        
                                dataBinder.addFile("primaryFile", new File(VaultfilePath));
                                dataBinder.putLocal("IdcService", "CHECKIN_UNIVERSAL");

                                ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
  
                                responseData = response.getResponseAsBinder();


        

                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=3;

		   }catch (IOException ioe){
			traceVerbose(ioe.getMessage());
                         status = ioe.getMessage();
		}finally {
                                   if ( responseData != null)    
                            
                                     { 
                                         status = responseData.getLocal("StatusMessage");

                                         targetdid = responseData.getLocal("dID");
                                         traceVerbose ( " dID from responce is " + targetdid);
                                    }
                }

return status ;

} // end of useRIDCCheckin 

public  String  useRIDCCheckinSEL  (intradoc.data.ResultSet docInfo , String  VaultfilePath, String targetdidNew ) throws Exception 

{

traceVerbose ("in useRIDCCheckinSEL class ");
String status="" ;
oracle.stellent.ridc.model.DataBinder  responseData = null;
commitstatus=32;
                    
		try{                           

                                dataBinder = completedataBinder( dataBinder, docInfo);
                                   
        
                                dataBinder.addFile("primaryFile", new File(VaultfilePath));
                                dataBinder.putLocal("IdcService", "CHECKIN_SEL");
                                dataBinder.putLocal("dID", targetdidNew);

                                traceVerbose("Before Target did is " + targetdidNew );

                                 ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);

                                 
  
                                 responseData = response.getResponseAsBinder();


                                  //    traceVerbose(responseData.getLocal("StatusMessage")); 

                                   //   status = responseData.getLocal("StatusMessage");
        

                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=31;

		   }catch (IOException ioe){
			traceVerbose(ioe.getMessage());
                         status = ioe.getMessage();
		}finally {
                                   if ( responseData != null)    
                            
                                     { 
                                         status = responseData.getLocal("StatusMessage");

                                         targetdid = responseData.getLocal("dID");
                                          traceVerbose ( "After  dID from responce is " + targetdid);
                                    }
                }

return status ;

} // end of useRIDCCheckinSEL 


public  String  useRIDCForMetaUpdate  (intradoc.data.ResultSet docInfo,String targetdidNew ) throws Exception 


{

traceVerbose ("in useRIDCForMetaUpdate class ");
String status="" ;
oracle.stellent.ridc.model.DataBinder  responseData = null;
                    
		try{
                                              
                         

                                     dataBinder = completedataBinder( dataBinder, docInfo);
          
        
                                     dataBinder.putLocal("IdcService", "UPDATE_DOCINFO");
                                 
                                     dataBinder.putLocal("dID", targetdidNew);

                                     ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
  
                                     responseData = response.getResponseAsBinder();



                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=7;

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
                                                             commitstatus=8;
                                                             targetdid = responseData.getLocal("dID");
                                                       }



                                      }
                }

return status ;





} // end of useRIDCForMetaUpdate


public  String  useRIDCForCheckout  ( String targetdidNew ) throws Exception 


{

traceVerbose ("in useRIDCForCheckout class ");
String status="" ;
oracle.stellent.ridc.model.DataBinder  responseData = null;
                    
		try{
                        
                       
        
                                  dataBinder.putLocal("IdcService", "CHECKOUT");
                                 
                                  dataBinder.putLocal("dID", targetdidNew);

                                  ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
  
                                  responseData = response.getResponseAsBinder();

                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=15;

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
                                                             status = "Checkout is   fine" ;
                                                             commitstatus=16;
                                                             targetdid = responseData.getLocal("dID");
                                                       }



                                      }
                }

return status ;





}  // end of useRIDCForCheckout



public  String  useRIDCForDelete  ( String targetdidNew ) throws Exception 


{
traceVerbose ("in useRIDCForDelete class ");
String status="" ;
oracle.stellent.ridc.model.DataBinder  responseData = null;
                    
		try{
                       
          
        
                                 dataBinder.putLocal("IdcService", "DELETE_REV");
                                 
                                 dataBinder.putLocal("dID", targetdidNew);

                                 ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
  
                                  responseData = response.getResponseAsBinder();

                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=63;

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
                                                             status = "Deleted " + targetdidNew ;
                                                             commitstatus=64;
                                                             targetdid = responseData.getLocal("prevID");
                                                       }



                                      }
                }

return status ;





}  // end of useRIDCForDelete

 



public  String  useRIDCForDeleteDoc  ( String dDocName ) throws Exception 


{
traceVerbose ("in useRIDCForDeleteDoc class ");
String status="" ;
oracle.stellent.ridc.model.DataBinder  responseData = null;
                    
		try{
                       
          
        
                                 dataBinder.putLocal("IdcService", "DELETE_DOC");
                                 
                                 dataBinder.putLocal("dDocName", dDocName);

                                 ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
  
                                  responseData = response.getResponseAsBinder();

                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=63;

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
                                                             status = "Deleted " + dDocName ;
                                                             commitstatus=64;
                                                             targetdid = responseData.getLocal("prevID");
                                                       }



                                      }
                }

return status ;





}  // end of useRIDCForDeleteDoc


public String useRIDCfldCreateFolder  ( intradoc.data.ResultSet FolderInfo,String fParentGUID ) throws Exception 


{

traceVerbose ("in useRIDCForMetaUpdate class ");
String status="" ;
oracle.stellent.ridc.model.DataBinder  responseData = null;
                    
		try{
                                              
                         

                                     dataBinder = completedataBinder( dataBinder,FolderInfo);
          
        
                                     dataBinder.putLocal("IdcService", "FLD_CREATE_FOLDER");
                                 
                                     dataBinder.putLocal("fParentGUID", fParentGUID);

                                     ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
  
                                     responseData = response.getResponseAsBinder();



                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=303;

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
                                                             status = "Folder Created" ;
                                                             commitstatus=304;
                                                             targetdid = responseData.getLocal("fFolderGUID");
                                                       }



                                      }
                }

return status ;




} // end of useRIDCfldCreateFolder



//public String useRIDCfldMove  ( String targetffFolderGUID ,String targetffParentGUID ) throws Exception 

public String useRIDCfldMove  ( List<String>  targetffFolderGUID,String targetffParentGUID ) throws Exception 

{

traceVerbose ("in useRIDCfldMove class ");
String status="" ;
oracle.stellent.ridc.model.DataBinder  responseData = null;
                    
		try{
                                              
                         

                                        
        
                                     dataBinder.putLocal("IdcService", "FLD_MOVE");
                                 
                                     dataBinder.putLocal("destination", ("fFolderGUID:"+targetffParentGUID));

                                                int itemscount=1;
                                                  
                                                for ( int k=0; k < targetffFolderGUID.size();k++)

                                                      {

                                                     String item = "item"+itemscount;

                                     dataBinder.putLocal(item, ("fFolderGUID:"+targetffFolderGUID.get(k)));
                                                     itemscount++;

                                                      }

                                     ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
  
                                     responseData = response.getResponseAsBinder();



                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=315;

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
                                                             status = "Folder Moved" ;
                                                             commitstatus=316;
                                                             targetdid = responseData.getLocal("fFolderGUID");
                                                       }



                                      }
                }

return status ;



} // end of useRIDCfldMove


public String useRIDCfldCopy  ( List<String>  targetffFolderGUID,String targetffParentGUID ) throws Exception 

{

traceVerbose ("in useRIDCfldCopy class ");
String status="" ;
oracle.stellent.ridc.model.DataBinder  responseData = null;
                    
		try{
                                              
                         

                                        
        
                                     dataBinder.putLocal("IdcService", "FLD_COPY");
                                 
                                     dataBinder.putLocal("destination", ("fFolderGUID:"+targetffParentGUID));

                                                int itemscount=1;
                                                  
                                                for ( int k=0; k < targetffFolderGUID.size();k++)

                                                      {

                                                     String item = "item"+itemscount;

                                     dataBinder.putLocal(item, ("fFolderGUID:"+targetffFolderGUID.get(k)));
                                                     itemscount++;

                                                      }

                                     ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
  
                                     responseData = response.getResponseAsBinder();

                                        

                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=335;

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
                                                             status = "Folder Copied" ;
                                                             commitstatus=336;
                                                            oracle.stellent.ridc.model.DataResultSet resultSet = responseData.getResultSet("TaskList");

                                                                     listoftargetGUIDs =  new ArrayList<String>();

                                                                                  for (DataObject dataObject : resultSet.getRows ()) {

                                                                      // String      fFolderGUID = dataObject.get("fFolderGUID");

                                                                          traceVerbose("in RIDC class , Responce TaskList"+ dataObject.get("fItemIdentifier"));
                                                                                            
                                                                          listoftargetGUIDs.add(dataObject.get("fItemIdentifier"));
                                                                                     
                                                                                                                         }
                                                     
                                                        }



                                      }
                }

return status ;



}

public String useRIDCfldEditFolder  ( intradoc.data.ResultSet FolderInfo,String targetffFolderGUID ) throws Exception 


{

traceVerbose ("in useRIDCfldEditFolder class ");
String status="" ;
oracle.stellent.ridc.model.DataBinder  responseData = null;
                    
		try{
                                              
                         

                                     dataBinder = completedataBinder( dataBinder,FolderInfo);
          
        
                                     dataBinder.putLocal("IdcService", "FLD_EDIT_FOLDER");
                                 
                                     dataBinder.putLocal("fFolderGUID", targetffFolderGUID);

                                     ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
  
                                     responseData = response.getResponseAsBinder();



                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=323;

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
                                                             status = "Folder Created" ;
                                                             commitstatus=324;
                                                             targetdid = responseData.getLocal("fFolderGUID");
                                                       }



                                      }
                }

return status ;




} // end of useRIDCfldCreateFolder


public String useRIDCfldDelete  ( List<String>  targetffFolderGUID ) throws Exception 

{

traceVerbose ("in useRIDCfldDelete class ");
String status="" ;
oracle.stellent.ridc.model.DataBinder  responseData = null;
                    
		try{
                                              
                         

                                        
        
                                     dataBinder.putLocal("IdcService", "FLD_DELETE");
                                 
                                  
                                                int itemscount=1;
                                                  
                                                for ( int k=0; k < targetffFolderGUID.size();k++)

                                                      {

                                                     String item = "item"+itemscount;

                                     dataBinder.putLocal(item, ("fFolderGUID:"+targetffFolderGUID.get(k)));
                                                     itemscount++;

                                                      }

                                     ServiceResponse response = idcClient.sendRequest(userContext,dataBinder);
  
                                     responseData = response.getResponseAsBinder();



                   } catch (IdcClientException ice){
		   	traceVerbose("IDC exception  "+ ice.getMessage());
                         status = ice.getMessage();
                         commitstatus=359;

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
                                                             status = "Folder deleted" ;
                                                             commitstatus=360;
                                                             targetdid = "Not";
                                                       }



                                      }
                }

return status ;



} // end of useRIDCfldMove


public static  oracle.stellent.ridc.model.DataBinder completedataBinder   (oracle.stellent.ridc.model.DataBinder  dataBinder , intradoc.data.ResultSet rs )

{

                               
                                      int columsize = rs.getNumFields(); 


                                       for(rs.first(); rs.isRowPresent(); rs.next())

                                          {   
                                                      

                                                for (int j=0;j< columsize ; j++)

                                                  {
                                                     String   key = rs.getFieldName(j);
                                                     String   value = rs.getStringValueByName(key);


                                                                


                                                           if ( value.length() == 0 )
                                                               {

                                                                        dataBinder.putLocal(key,"");  

                                                                       //  traceVerbose("Key is " + key);
                                                                }

                                                     
                                                                                 if ( key.toLowerCase().contains("date")  )
                                                                                     {

                                                                                    

                                             if ( key.equals("dCreateDate") || key.equals("dOutDate") || key.equals("dDocLastModifiedDate")|| key.equals("dDocCreatedDate"))
                                                                                                    {
                                                                                                        //System.out.println("Skipping");
                                                                                                                continue;
                                                                                                    }
                                                                                              
                                                                                                     else {   

                                                                                              //      traceVerbose(key + " --- " + value );

                                                                                              value = convertDate(value);

                                                                                              

                                                                               
                                                                                              traceVerbose(key + " --- " + value );

                                                                                               dataBinder.putLocal(key,value);  

                                                                                                         

                                                                                                            }
                                                                                       } 

                                                       else  if ( key.equals("fParentGUID"))


                                                                                       {

                                                                                     

                                                                                       }

                                                                                   else {

                                                                                            dataBinder.putLocal(key,value);  

                                                                                                   
                                                                                        }  // else of date 
                                                    

                                                                                            



                                                  } // end of inner loop 
                               
                                                    
                                   
                                          } // end of outer loop
                   
return dataBinder;


}  //end of  completedataBinder


}  // end of class 
