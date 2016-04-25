package pushtoTargetRIDC;

import static pushtoTargetRIDC.Utils.*;
import static pushtoTargetRIDC.UseRIDC.*;
import intradoc.common.*;
import intradoc.data.*;
import intradoc.server.*;
import intradoc.shared.*;
import java.util.*;


public class PullRIDCConsole extends ServiceHandler
{



public void PullRIDCConsoleHandler() throws DataException, intradoc.common.ServiceException


{



 traceVerbose("Inside the filter PullRIDCConsoleHandler ");


// Calling 

                  scanTablewithRelease();

                       

 long   timetoCheckMillis;
 String doCheckinMin="5";
int commitstatusFromBinder=0;

String idctoken="";
  long  currentTimeMillis =   System.currentTimeMillis();

                                      int commitstatusFromm_m_binder=0;
                                        // This if to set doCheckinMin to default value 5 
                                    if (m_binder.getLocal("doCheckinMin")!=null )
                                       
                                          {
                                             
                                              doCheckinMin=m_binder.getLocal("doCheckinMin");
                                             timetoCheckMillis =  Long.parseLong(doCheckinMin) * 60000;

                                          }
 
                                     else 
                                        
                                           {
                                                  timetoCheckMillis = 300000 ;
                                                  
                                            }

                                    timetoCheckMillis = currentTimeMillis -timetoCheckMillis; 


 

                                                      if (m_binder.getLocal("CommitStatus")!=null )
                                                        commitstatusFromBinder=Integer.parseInt(m_binder.getLocal("CommitStatus"));

                                                        if (m_binder.getLocal("idcToken")!=null )
                                                        idctoken=m_binder.getLocal("idcToken");
 

     String selectpart = "select rid, ridcservice ,RENTRYDATE from";          

     String wherepart = " where Rentrydate >"+ timetoCheckMillis +" AND commitstatus=0" ;

traceVerbose(wherepart);

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




String SelectSQL =  selectpart + " Pullridc " + wherepart + "  UNION " + selectpart + " Pullfolderridc " + wherepart + " ORDER BY Rid asc" ;

traceVerbose(SelectSQL);




                                              
                                                     // Before fun begins need to clear all m_binder
                                       m_binder.getLocalData().clear();

                                      // Calling SQL statment

                                           

                            intradoc.data.ResultSet rsSQLpullRIDCTable  =  runSQLpullRIDCTable(m_workspace,SelectSQL );



                                 







                                         DataResultSet myDataResultSet = new DataResultSet();

                                         myDataResultSet.copy(rsSQLpullRIDCTable);


                            
                                     

                                           
                                         
                                                   m_binder.putLocal("FromDate",String.valueOf(timetoCheckMillis));

                                                   m_binder.putLocal("CurrentTime",String.valueOf(currentTimeMillis));

                                           
                                                   
                                                   m_binder.putLocal("doCheckinMin",doCheckinMin);

                                                   m_binder.putLocal("CommitStatus",String.valueOf(commitstatusFromBinder));



                                                               if(idctoken.length() > 2)
                                                          m_binder.putLocal("idcToken",idctoken);   
                                    

                                                  m_binder.addResultSet("ResultSetRIDC", myDataResultSet);
                                      
   








} // end of PullRIDCConsoleHandler


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



} // end of Class
