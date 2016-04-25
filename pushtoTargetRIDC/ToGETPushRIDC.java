package pushtoTargetRIDC;

import static pushtoTargetRIDC.Utils.*;
import static pushtoTargetRIDC.UseRIDC.*;
import pushtoTargetRIDC.DoPush.*;
import pushtoTargetRIDC.ToGetFolderDetailsAndPush.*;


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
 * Please check the read me of the component 
 * it is using Utils and UseRIDC class
 * written by Sterin Jacob
 */
public class ToGETPushRIDC extends ServiceHandler
{


boolean CommitOnlyReleased=false;
long   timetoCheckMillis;
Workspace  ws;


//List<String> ReleasedContent = new ArrayList<String>(); 



	public void toGetPushRIDCMain() throws DataException, intradoc.common.ServiceException ,intradoc.common.ParseStringException
	{
		    traceVerbose("Inside the filter toGetPushRIDCMain");


ws =  m_workspace;
String idctoken="";
//intradoc.data.Datam_m_binder orgm_m_binder=m_binder;

                                                        if (m_binder.getLocal("idcToken")!=null )
                                                        idctoken=(m_binder.getLocal("idcToken"));


// To check the date format , it can change from RIDC call and browser call
           if ( m_binder.getLocal("UserDateFormat") !=null &&  m_binder.getLocal("UserDateFormat").equals("iso8601"))

                  {

                   traceVerbose("Calling from RIDC no need of Date  Conversion");

                    Isiso8601=true;   // this is defined in Utils.java 

                    }

             else {

                  idf = m_binder.m_blDateFormat;  // sending dateformat to Utils.java 
                  Isiso8601=false;
                  traceVerbose("Calling from Browser , Date conversion is required ");


                   }



                                     scanTablewithRelease();

                                    

                                     long  currentTimeMillis =   System.currentTimeMillis();

                                      int commitstatusFromm_m_binder=0;
                                        // This if to set doCheckinMin to default value 5 
                                     if (m_binder.getLocal("doCheckinMin")!=null )
                                        timetoCheckMillis =  Long.parseLong(m_binder.getLocal("doCheckinMin")) * 60000; 
                                   
 
                                     else 
                                        timetoCheckMillis = 300000 ;





                                    timetoCheckMillis = currentTimeMillis -timetoCheckMillis; 

     String selectpart = "select rid, ridcservice ,RENTRYDATE from";          

     String wherepart = " ";

wherepart="where Rentrydate >"+ timetoCheckMillis +" AND commitstatus=0" ;



                                                    if (m_binder.getLocal("CommitStatus")!=null )

                                             {

                                                       commitstatusFromm_m_binder=Integer.parseInt(m_binder.getLocal("CommitStatus"));

wherepart = " where Rentrydate >"+ timetoCheckMillis +" AND commitstatus="+commitstatusFromm_m_binder ;



                                             }

                                        

                                                    // this if to set SelectSQL for all the content with out entrydate 
                                                      if (m_binder.getLocal("doCheckAll")!=null )
                                                        
                                                      {


 wherepart = " where  commitstatus="+commitstatusFromm_m_binder;
                                                          
                                                      timetoCheckMillis=0;

                                                      }


traceVerbose(wherepart);

String SelectSQL =  selectpart + " Pullridc " + wherepart + "  UNION " + selectpart + " Pullfolderridc " + wherepart + " ORDER BY Rid asc" ;

traceVerbose(SelectSQL);


// select rid, ridcservice  from Pullridc UNION select rid,ridcservice from Pullfolderridc; 
// select rid, ridcservice  from Pullridc UNION select rid,ridcservice from Pullfolderridc order by rid asc;
// select rid, ridcservice,Rentrydate,VAULTFILEPATH   from Pullridc  where Rentrydate > 1457022466521 AND   commitstatus=0  UNION select rid,ridcservice,Rentrydate,Pullfolderridc.Fparentguid  from Pullfolderridc  where Rentrydate > 1457022466521 AND   commitstatus=0 order by rid asc;



                                         // All Binder Values are assigned , so that they call any Services ;
                                                               if ( SharedObjects.getEnvironmentValue("CommitOnlyReleased").equals("true"))
                        
                                                                                          {     
                                                                                               CommitOnlyReleased=true;
                                                                                             


                                                                                           }                                        
                                                     // Before fun begins need to clear all m_binder
                                       m_binder.getLocalData().clear();

                                      // Calling SQL statment

                                           

                            intradoc.data.ResultSet rsSQLpullRIDCTable  =  runSQLpullRIDCTable(ws,SelectSQL );
                                           

                                      if ( verifyRow(rsSQLpullRIDCTable))
                                      
                                       {
                                           
                                        
                                                List<String> listdID  = createListAllResultSet  (rsSQLpullRIDCTable );


                                                  List<String> ReleaseddID = new ArrayList<String>();  
                                       
                                                               if(CommitOnlyReleased)
                                                        ReleaseddID =  toCreateSearchList();

                                                toGetdIDforFolderAndContent(listdID,ReleaseddID);

                                        }
  
                  
                                                               if(idctoken.length() > 2)
                                                          m_binder.putLocal("idcToken",idctoken);   



         } // end of toGetPushRIDCMain


public   List createListAllResultSet (final intradoc.data.ResultSet rs)   

{

List<String> rsList = new ArrayList<String>();  



for(rs.first(); rs.isRowPresent(); rs.next())
  


   {


String RID =rs.getStringValueByName("RID");
String RIDCSERVICE =rs.getStringValueByName("RIDCSERVICE");
String Rentrydate = rs.getStringValueByName("RENTRYDATE");


rsList.add(RID+"\t"+RIDCSERVICE+"\t"+Rentrydate);

   }

return rsList;


}



private void toGetdIDforFolderAndContent ( List<String>  rsSQLList ,List<String> ReleaseddID )  throws intradoc.common.ServiceException ,intradoc.data.DataException


            {

traceVerbose("toGetdIDforFolderAndContent");

DoPush  DP = new DoPush (ws,m_binder,m_service,CommitOnlyReleased);
ToGetFolderDetailsAndPush    TFD =  new  ToGetFolderDetailsAndPush(ws,m_binder,m_service);
String    Rentrydate="0";
String    lastRentrydate="0";

                                       for(int i=0; i < rsSQLList.size(); i++)

                                              {
                                                                                                      
                                                          String [] tokens = rsSQLList.get(i).split("\\t+");
                                                          String   RID  = tokens[0];
                                                                                                          
                                                          String   idcService = tokens[1] ;

                                                            

                                                                   if ( idcService.equals ("CHECKIN_UNIVERSAL")|| idcService.equals ("CHECKIN_NEW"))
                                                                   
                                                                             { 

                                                                              
                                                                                traceVerbose(idcService);
                                                                          traceVerbose("Status" +      DP.doPushCheckin(RID,ReleaseddID));


                                                                                   
                                                                             }

                                                                   else  if ( idcService.equals ("CHECKOUT_OK"))
                                                                           {
                                                                                traceVerbose(idcService);

                                                             traceVerbose("Status" +      DP.doPushCheckout(RID,ReleaseddID));                                                         


                                                                           }
                                                                else  if ( idcService.equals ("CHECKIN_SEL"))
                                                                           {

                                                   
                                                                                traceVerbose(idcService);
                                                                          traceVerbose("Status" +      DP.doPushCheckSEL(RID,ReleaseddID));
                                                                           }

                                                                else  if ( idcService.contains ("UPDATE_DOCINFO"))
                                                                           {

                                                                          
                                                                                   
                                                                                   traceVerbose(idcService);   
                                                                           traceVerbose("Status" +      DP.doPushUpdateDocinfo(RID,ReleaseddID));    

                                                                           }

                                                                  else  if ( idcService.contains ("DELETE_REV"))
                                                                            {

                                                                          
                                                                                    traceVerbose(idcService); 
                                                                      traceVerbose("Status" +      DP.doPushDelete(RID));    
                                                                      
                                                                            }
                                                                      

                                                                 else  if ( idcService.equals  ("DELETE_DOC"))
                                                                             


                                                                           {
                                                                                traceVerbose(idcService);
                                                                           traceVerbose("Status" +      DP.doPushDeleteDoc(RID));                        
                                                                        

                                                                           }
                                   
                                                           else   if ( idcService.equals ("FLD_CREATE_FOLDER"))
                                                                   
                                                                     {

                                                                                traceVerbose(idcService);

                                                                       traceVerbose("Status" +   TFD.fldCreateFolder(RID));

                                                                     }

                                                          else if ( idcService.equals ("FLD_MOVE"))
                                                                   
                                                                     {

                                                                                traceVerbose(idcService);

                                                                                 Rentrydate =  tokens[2] ;


                                                                                    if (lastRentrydate.equals(Rentrydate))

                                                                                       {

                                                                                        traceVerbose("Same Operation ");
                                                                                        continue;

                                                                                       }
                                                                                          
                                                                              else {



                                                                                       traceVerbose("Status" +   TFD.fldMoveFolder(RID,Rentrydate));
                                                                                       lastRentrydate=Rentrydate;

                                                                                     }


                                                                      

                                                                     }

                                                          else if ( idcService.equals ("FLD_EDIT_FOLDER"))
                                                                   
                                                                     {

                                                                                traceVerbose(idcService);

                                                                                traceVerbose("Status" +   TFD.fldEditFolder(RID));

                                                                     }



                                                          else if ( idcService.equals ("FLD_COPY"))
                                                                   
                                                                     {

                                                                                traceVerbose(idcService);

                                                                                 Rentrydate =  tokens[2] ;


                                                                                    if (lastRentrydate.equals(Rentrydate))

                                                                                       {

                                                                                        traceVerbose("Same Operation ");
                                                                                        continue;

                                                                                       }
                                                                                          
                                                                              else {



                                                                                       traceVerbose("Status" +   TFD.fldCopyFolder(RID,Rentrydate));
                                                                                       lastRentrydate=Rentrydate;

                                                                                     }

                                                                     }


                                                                    else if ( idcService.equals ("FLD_DELETE"))
                                                                   
                                                                     {

                                                                         
                                                                                traceVerbose(idcService);
                                                                                      Rentrydate =  tokens[2] ;


                                                                                    if (lastRentrydate.equals(Rentrydate))

                                                                                       {

                                                                                        traceVerbose("Same Operation ");
                                                                                        continue;

                                                                                       }
                                                                                          
                                                                              else {



                                                                                      traceVerbose("Status" +   TFD.fldDeleteFolder(RID,Rentrydate));
                                                                                       lastRentrydate=Rentrydate;

                                                                                     }
                                                                           

                                                                     }

                                                                    else if ( idcService.equals ("FLD_UNFILE"))
                                                                   
                                                                     {

                                                                                  
                                                                                           traceVerbose(idcService);
                                                                                           Rentrydate =  tokens[2] ;


                                                                                    if (lastRentrydate.equals(Rentrydate))

                                                                                       {

                                                                                        traceVerbose("Same Operation ");
                                                                                        continue;

                                                                                       }
                                                                                          
                                                                              else {



                                                                                       traceVerbose("Status" +   TFD.fldUnFile(RID,Rentrydate));
                                                                                       lastRentrydate=Rentrydate;

                                                                                     }

                                                                      
                                                                     }




                                              } 

   }   // end of toGetdIDforFolderAndContent 


private    void scanTablewithRelease () throws DataException, intradoc.common.ServiceException

{


String  selectSQL = "select RID,dID,dDocName from pullRIDC where  commitstatus=1 ORDER BY Rid asc" ;

List<String> rsList = new ArrayList<String>();  

intradoc.data.ResultSet rsNonReleaseContent  =  runSQLpullRIDCTable(m_workspace,selectSQL );

                                            for(rsNonReleaseContent.first(); rsNonReleaseContent.isRowPresent(); rsNonReleaseContent.next())

                                          {

                                               
                                                 
                                                    
                                              String         RID = rsNonReleaseContent.getStringValueByName("RID");
                                              String         dID =  rsNonReleaseContent.getStringValueByName("dID");
                                              String         dDocName  =  rsNonReleaseContent.getStringValueByName("dID");

                                                          if (dDocName ==null)
                                                                dDocName="0";
                                                                          
                                                          if (dID ==null)
                                                                dID="0";
                                                 rsList.add(RID+"\t"+dID+"\t"+dDocName);
                                                     
                                                 
                                   
                                          }


                                            for(int i=0; i < rsList.size(); i++) 

                                                      { 
                                                             
                                                
                                                          String [] tokens = rsList.get(i).split("\\t+");
                                                          String   RID  = tokens[0];
                                                          
                                                          String   dID  = tokens[1];
                                                          String   dDocName = tokens[2];

                                                              if(isdIDReleased(dID,RID))
                                                                
                                                                     {

                                                                      String updateSQL= "UPDATE pullRIDC  SET commitstatus=0 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (m_workspace,updateSQL);

                                                                     }



                                                      }

} // end of scanTablewithRelease


private boolean isdIDReleased (String dID,String RID) throws DataException, intradoc.common.ServiceException

{


            /*                          m_binder.getLocalData().clear();
                                      m_binder.putLocal("dID", dID);
                                      this.executeService(m_binder, "DOC_INFO");
                 intradoc.data.ResultSet docInfo = m_binder.getResultSet("DOC_INFO");

              */


 String  selectSQL = "Select dStatus,dDocName From Revisions where did="+dID ;

                   intradoc.data.ResultSet docInfo =runSQLpullRIDCTable(m_workspace,selectSQL );

boolean isReleasestatus=false;
                                      for(docInfo.first(); docInfo.isRowPresent(); docInfo.next())

                                      

                                          {

                                                  String     dStatus = docInfo.getStringValueByName("dStatus");

                                                               traceVerbose("Status of the dID   "+dID +"   is   "+dStatus);
                                                 
                                                            
                                                                     if (dStatus.equals("RELEASED"))
                                                                    
                                                                       {
                                                                                  isReleasestatus=true;
                                                                         
                                                                                  return isReleasestatus;

                                                                       }


                                                                   else if (dStatus.equals("EXPIRED")) 
                                                                      
                                                                      {
                                                                      String updateSQL= "UPDATE pullRIDC  SET commitstatus=100 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (m_workspace,updateSQL);
                                                                               return isReleasestatus;
                                                                             
                                                                      }

                                                                 else  
                        
                                                                     {

                                                                         
                                                                           return isReleasestatus;
                                                                     }

                                                                 
                                          }


  

                                                                           traceVerbose("  "+dID +"   is   "+"Deleted");

                                                                      String updateSQL= "UPDATE pullRIDC  SET commitstatus=100 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (m_workspace,updateSQL);


                                                                     




return isReleasestatus;

} // end of isdIDReleased

public   List toCreateSearchList ()  throws intradoc.common.ServiceException  

{

List<String> ReleaseddID = new ArrayList<String>();  

String PRquery=SharedObjects.getEnvironmentValue("PRQuery");

traceVerbose ( " PRQuery is " + PRquery);

PRquery=appendDate(PRquery,timetoCheckMillis);




  

        m_binder.getLocalData().clear();
        m_binder.putLocal("whereClause", PRquery);
        m_binder.putLocal("IdcService", "GET_DATARESULTSET");
        m_binder.putLocal("dataSource","Documents");
        m_binder.putLocal("resultName", "DOCUMENTS");
                                          
        this.executeService(m_binder, "GET_DATARESULTSET");
        intradoc.data.ResultSet  SearchResults = m_binder.getResultSet("DOCUMENTS");


       ReleaseddID=afterSearchList(SearchResults);

traceVerbose(ReleaseddID.toString());

return ReleaseddID;



} // end of toCreateSearchList

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



                      



                  } 

 

} //end of cLass 
