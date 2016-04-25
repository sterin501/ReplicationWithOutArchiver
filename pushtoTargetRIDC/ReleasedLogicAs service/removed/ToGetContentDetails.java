package pushtoTargetRIDC;

import static pushtoTargetRIDC.Utils.*;
import static pushtoTargetRIDC.UseRIDC.*;
import pushtoTargetRIDC.DoPush.*;
import pushtoTargetRIDC.ToGetFolderDetails.*;

import java.util.*;
import java.io.*;
import intradoc.common.*;
import intradoc.data.*;
import intradoc.server.*;
import intradoc.shared.*;


public class ToGetContentDetails 

{

intradoc.data.Workspace  ws;
intradoc.data.DataBinder binder;
boolean CommitOnlyReleased;
intradoc.server.Service mservice;
long   timetoCheckMillis;

public ToGetContentDetails(intradoc.data.Workspace  ws, intradoc.data.DataBinder binder,intradoc.server.Service mservice)

{

this.ws = ws ;
this.binder =  binder;
this.mservice = mservice;


}  // end of constuctor 



public void scanPullrridc () throws DataException, intradoc.common.ServiceException

{
                            

traceVerbose("scanPullrridc");



              long  currentTimeMillis =   System.currentTimeMillis();


                                    
 
                                      int commitstatusFromBinder=0;


                                        // This if to set doCheckinMin to default value 5 
                                     if (binder.getLocal("doCheckinMin")!=null )
                                        timetoCheckMillis =  Long.parseLong(binder.getLocal("doCheckinMin")) * 60000; 
                                   
 
                                     else 
                                        timetoCheckMillis = 300000 ;





                                    timetoCheckMillis = currentTimeMillis -timetoCheckMillis; 

          

            String selectSQL = "select RID,dID,RIDCSERVICE,VAULTFILEPATH,dDocName from pullRIDC where Rentrydate >"+ timetoCheckMillis +"AND commitstatus=0 ORDER BY Rid asc" ;


                                       
                                       
                                                  // this if to set commit status to default value 
                                        if (binder.getLocal("CommitStatus")!=null )

                                             {

                                                       commitstatusFromBinder=Integer.parseInt(binder.getLocal("CommitStatus"));

 selectSQL = "select RID,dID,RIDCSERVICE,VAULTFILEPATH,dDocName from pullRIDC where Rentrydate >"+ timetoCheckMillis +"AND commitstatus="+commitstatusFromBinder+"  ORDER BY Rid asc" ;

                                             }

                                        

                                                    // this if to set SelectSQL for all the content with out entrydate 
                                                      if (binder.getLocal("doCheckAll")!=null )
                                                        
                                                      {


 selectSQL = "select RID,dID,RIDCSERVICE,VAULTFILEPATH,dDocName from pullRIDC where  commitstatus="+commitstatusFromBinder+" ORDER BY Rid asc" ;
                                                          
                                                      timetoCheckMillis=0;

                                                      }



                                         // All Binder Values are assigned , so that they call any Services ;
                                                               if ( SharedObjects.getEnvironmentValue("CommitOnlyReleased").equals("true"))
                        
                                                                                          {     
                                                                                               CommitOnlyReleased=true;
                                                                                             


                                                                                           }                                        
                                                     // Before fun begins need to clear all binder
                                       binder.getLocalData().clear();

                                      // Calling SQL statment

                                           

                            intradoc.data.ResultSet rsSQLpullRIDCTable  =  runSQLpullRIDCTable(ws,selectSQL );
                                           
                                             

                                           if ( verifyRow(rsSQLpullRIDCTable))
                                      
                                       {
                                           
                                             //toGetdIDforPush(createListResultSet  (rsSQLpullRIDCTable ));
                                                List<String> listdID  = createListResultSet  (rsSQLpullRIDCTable );
                                            
                                                List<String> ReleaseddID = new ArrayList<String>();  
                                       
                                                               if(CommitOnlyReleased)
                                                        ReleaseddID =  toCreateSearchList();

                                                       toGetdIDforPush(listdID,ReleaseddID);
                                        }

                      

} // end of scanPullFolderridc


public   List toCreateSearchList ()  throws intradoc.common.ServiceException  

{

List<String> ReleaseddID = new ArrayList<String>();  

String PRquery=SharedObjects.getEnvironmentValue("PRQuery");

traceVerbose ( " PRQuery is " + PRquery);

PRquery=appendDate(PRquery,timetoCheckMillis);




  

        binder.getLocalData().clear();
        binder.putLocal("whereClause", PRquery);
        binder.putLocal("IdcService", "GET_DATARESULTSET");
        binder.putLocal("dataSource","Documents");
        binder.putLocal("resultName", "DOCUMENTS");
                                          
        this.executeService(binder, "GET_DATARESULTSET");
        intradoc.data.ResultSet  SearchResults = binder.getResultSet("DOCUMENTS");


       ReleaseddID=afterSearchList(SearchResults);

traceVerbose(ReleaseddID.toString());

return ReleaseddID;



} // end of toCreateSearchList


public   List createListResultSet (final intradoc.data.ResultSet rs)   

{

List<String> rsList = new ArrayList<String>();  



for(rs.first(); rs.isRowPresent(); rs.next())
  


   {


/* HashMap <String,String> map = new HashMap()
map.put("rid",value)
map.put("did",value)

*/




String dID =rs.getStringValueByName("dID");
String RID =rs.getStringValueByName("RID");
String RIDCSERVICE =rs.getStringValueByName("RIDCSERVICE");
String VAULTFILEPATH = rs.getStringValueByName("VAULTFILEPATH");
String dDocName      = rs.getStringValueByName("dDocName");


if (VAULTFILEPATH == null)
VAULTFILEPATH="0";

if (dDocName == null)
dDocName="0";

rsList.add(RID+"\t"+dID+"\t"+RIDCSERVICE+"\t"+VAULTFILEPATH+"\t"+dDocName);

   }

return rsList;


} // end of createListResultSet



private void toGetdIDforPush ( List<String>  rsSQLList ,  List<String> ReleaseddID )  throws intradoc.common.ServiceException ,intradoc.data.DataException


            {

traceVerbose("toGetdIDforPush");
DoPush  DP = new DoPush (ws,binder,mservice,CommitOnlyReleased);
                                       for(int i=0; i < rsSQLList.size(); i++) 

                                                      { 
 
                                                
                                                          String [] tokens = rsSQLList.get(i).split("\\t+");
                                                          String   RID  = tokens[0];
                                                          
                                                          String   dID  = tokens[1];

                                                          String   idcService = tokens[2] ;



                                                                 if ( idcService.equals ("CHECKIN_UNIVERSAL")|| idcService.equals ("CHECKIN_NEW"))
                                                                   
                                                                     {    
                                                                      
                                                                         String  VaultfilePath =  tokens[3];             
                                                                         String   dDocName  = tokens[4];
                                                                          traceVerbose (dID  + "  " + idcService);

                                                                       // DoPush  DP = new DoPush (ws,binder,m_service,CommitOnlyReleased);

                                                                        traceVerbose ("Staus of doPushCheckin "+ DP.doPushCheckin(RID,dID,VaultfilePath,dDocName,ReleaseddID));       


                                                                       //   traceVerbose ("Staus of doPushCheckin "+  doPushCheckin(RID,dID,VaultfilePath,dDocName,ReleaseddID));
                                                                                    
  
                                                                     }

                                                                else  if ( idcService.equals ("CHECKOUT_OK"))
                                                                           {

                                                                           traceVerbose (dID  + "  " + idcService);

                                                                           traceVerbose ("Staus of doPushCheckout "+ DP.doPushCheckout(RID,dID,ReleaseddID));


                                                                           }
                                                                else  if ( idcService.equals ("CHECKIN_SEL"))
                                                                           {

                                                                          traceVerbose (dID  + "  " + idcService);
                                                                           String  dDocName =  tokens[4];
                                                                            String  VaultfilePath =  tokens[3];
                                                                        traceVerbose ("Staus of doPushCheckSEL "+ DP.doPushCheckSEL(RID,dID,VaultfilePath,dDocName,ReleaseddID));


 
                                                                           }

                                                                else  if ( idcService.contains ("UPDATE_DOCINFO"))
                                                                           {

                                                                           traceVerbose (dID  + "  " + idcService);

                                                                       traceVerbose ("Staus of doPushUpdateDocinfo "+     DP.doPushUpdateDocinfo(RID,dID,ReleaseddID));

                                                                                
                                                                                   
                                                                           

                                                                           }

                                                                  else  if ( idcService.contains ("DELETE_REV"))
                                                                            {

                                                                          
                                                                         traceVerbose (dID  + "  " + idcService);

                                                                          traceVerbose ("Staus of doPushDelete "+        DP.doPushDelete(RID,dID));
                                                                      
                                                                           }
                                                                      

                                                                 else  if ( idcService.equals  ("DELETE_DOC"))
                                                                             


                                                                           {

                                                                          String  dDocName =  tokens[4];
                                     
                                                                         traceVerbose (dDocName  + "  " + idcService);

                                                                            traceVerbose ("Staus of doPushDeleteDoc "+  DP.doPushDeleteDoc(RID,dDocName));


                                                                                                                                                  
                                                                                  


                                                                           }
                                   
                                          } // end of For 1:


         
     


            }  // end of toGetdIDforPush








private void executeService(final  intradoc.data.DataBinder  serviceBinder, final String serviceName) throws intradoc.common.ServiceException {
                       traceVerbose("Start executeService");
                        

                       try {
                               trace("Calling service " + serviceName + ": " + serviceBinder.getLocalData().toString());
                               // Execute service
                               mservice.getRequestImplementor().executeServiceTopLevelSimple(serviceBinder, serviceName, mservice.getUserData());
                               trace("Finished calling service");


                             } catch (final DataException e) {
                                           trace("Something went wrong executing service " + serviceName);
                                          e.printStackTrace(System.out);
                                           throw new intradoc.common.ServiceException("Something went wrong executing service " + serviceName, e);
                            
                               } finally {
                                         traceVerbose("End executeService");
                                          }



                      



                  } 



} // end of class 
