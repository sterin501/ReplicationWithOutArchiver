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

                       
                             long  currentTimeMillis =   System.currentTimeMillis();


                                    long   timetoCheckMillis;

                                      String doCheckinMin="5";
                              
                                                int commitstatusFromBinder=0;

String idctoken="";
                                        
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
                                                        idctoken=(m_binder.getLocal("idcToken"));
 


                                           
          

      String selectSQL = "select RID,dID,RIDCSERVICE,dDocName from pullRIDC where Rentrydate >"+ timetoCheckMillis +"AND commitstatus="+commitstatusFromBinder+" ORDER BY Rid asc" ;



                                          if (m_binder.getLocal("doCheckAll")!=null )
             selectSQL = "select RID,dID,RIDCSERVICE,dDocName from pullRIDC where  commitstatus="+commitstatusFromBinder+" ORDER BY Rid asc" ;


                                         intradoc.data.ResultSet rsSQLpullRIDCTable  =  runSQLpullRIDCTable(m_workspace,selectSQL );

                                         DataResultSet myDataResultSet = new DataResultSet();

                                         myDataResultSet.copy(rsSQLpullRIDCTable);


                            
                                         m_binder.getLocalData().clear();

                                           
                                         
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
