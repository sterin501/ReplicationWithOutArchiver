package pushtoTargetRIDC;

import static pushtoTargetRIDC.Utils.*;
import static pushtoTargetRIDC.UseRIDC.*;
import intradoc.common.*;
import intradoc.data.*;
import intradoc.server.*;
import intradoc.shared.*;



public class PullRIDCConsole extends ServiceHandler
{



public void PullRIDCConsoleHandler() throws DataException, intradoc.common.ServiceException


{



 traceVerbose("Inside the filter PullRIDCConsoleHandler ");

                 

                       
                             long  currentTimeMillis =   System.currentTimeMillis();


                                    long   timetoCheckMillis;

                                      String doCheckinMin="5";
                              
                                                int commitstatusFromBinder=0;
                                        
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
                                         

                                                  m_binder.addResultSet("ResultSetRIDC", myDataResultSet);
                                      
   








} // end of PullRIDCConsoleHandler

} // end of Class
