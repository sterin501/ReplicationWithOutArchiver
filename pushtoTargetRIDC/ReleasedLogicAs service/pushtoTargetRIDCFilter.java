package pushtoTargetRIDC;

import static pushtoTargetRIDC.Utils.trace;
import static pushtoTargetRIDC.Utils.traceVerbose;
//import static pushtoTargetRIDC.Utils.traceResultSet;

import intradoc.common.*;
import intradoc.data.*;
import intradoc.server.*;
import intradoc.shared.*;
import intradoc.resource.*;
import intradoc.serialize.*;
import intradoc.server.utils.CompInstallUtils;
import intradoc.provider.*;




public class pushtoTargetRIDCFilter  extends ServiceHandler implements FilterImplementor  
{





     public int  doFilter(Workspace ws, DataBinder m_binder ,  ExecutionContext cxt)   throws DataException, ServiceException
              {


                
                                if (cxt  instanceof Service) {

                                                            String     rIdcService= m_binder.getLocal("IdcService");

                                                                                                  

                                                        if ( rIdcService == null )
                                                               return CONTINUE;


                                                     


     if (rIdcService.equals("CHECKIN_NEW") ||rIdcService.equals("CHECKIN_UNIVERSAL") ||  rIdcService.contains("CHECKOUT_OK") || rIdcService.contains("UPDATE_DOCINFO") || rIdcService.equals("CHECKIN_SEL"))
                                           
                                                    {
 

                                                    
                                                                       
                                                  
                                                      traceVerbose("Inside the filter FORGREP");

                                                 
                                                        String      dID = m_binder.getLocal("dID");

                                                                     if ( dID != null )  // this if will stop to add null values to table or failed checkin etc
                                                                          
                                                            
                                                                   {
                                                              Long currentTimeMillis =   System.currentTimeMillis();

                                                            traceVerbose("Current time in ms " + currentTimeMillis );  

                                                             String      VaultfilePath  = m_binder.getLocal("VaultfilePath");

                                                                  // traceVerbose("DataBinder is " + m_binder.toString());      

                                                             traceVerbose("dID is " + dID + " VaultfilePath " +  VaultfilePath );

                                                             int  commitstatus = 0;

                                                             

                                                             String      dDocName  = m_binder.getLocal("dDocName");
                                                            

                                                                              // To verify the RELEASED status 


                                                                   if ( SharedObjects.getEnvironmentValue("CommitOnlyReleased").equals("true"))
                        
                                                                                     {
                                                                                              String      dStatus   = m_binder.getLocal("dStatus");

                                                                                                  if (!dStatus.equals("RELEASED"))
                                                                                                       commitstatus = 1;


                                                                                      }

                                                                                

                                                          
                                                            insertpullRIDCTable(ws,dID,dDocName,currentTimeMillis,rIdcService,VaultfilePath,commitstatus);

                                                                return CONTINUE;

                                                                  } 
                                                        }
                                 
                                       
                                             else if (rIdcService.contains("DELETE_REV") )    

                                                          //    traceVerbose("DataBinder is " + m_binder.toString());    
                                                     {


            
                                                                    
                                                                 String      dID = m_binder.getLocal("prevID");

                                                                    if ( dID != null )
                                                               {  

                                                                         
                                                                  String SQLStatement = " select * from pullridc where did="+dID+" and Commitstatus=0";
                                                                  intradoc.data.ResultSet rs =  ws.createResultSetSQL(SQLStatement);
                                                                    
                                                                         if ( rs.hasRawObjects())
                                                                       {
                                                                               // we have to update it 

                                                                 String  updateSQL= "UPDATE pullRIDC  SET commitstatus=100 WHERE did="+dID ;
                                                                                  ws.executeSQL(updateSQL);


                                                                       }

                                                               else 

                                                                        {

                                                                 int  commitstatus = 0;

                                                                 String dDocName = null;
                                                                 String      VaultfilePath = null ;
                                                                 Long currentTimeMillis =   System.currentTimeMillis();

                                                                insertpullRIDCTable(ws,dID,dDocName,currentTimeMillis,rIdcService,VaultfilePath,commitstatus);

                                                                         }

                                                               }


                                                    }  // end of last else if 




                                      }    // end of cxt check if       
                                   
                                     
                return 1; 
              } // end of doFilter 
















	protected void insertpullRIDCTable (Workspace ws,String dID,String dDocName, long currentTimeMillis ,String rIdcService,String  VaultfilePath,  int  commitstatus ) throws DataException
	


          {

		// insert the proper row into the database table


    //       String insertSql = "INSERT INTO pullRIDC(rID,dID,rEntryDate,rActionDate,rIdcService) values(rid_seq.nextval,"+dID+","+currentTimeMillis+","+currentTimeMillis+",'"+rIdcService+"')";

 String insertSql = "INSERT INTO pullRIDC(rID,dID,dDocName,rEntryDate,rActionDate,rIdcService,VaultfilePath,commitstatus) values(rid_seq.nextval,"+dID+",'"+dDocName+"',"+currentTimeMillis+","+currentTimeMillis+",'"+rIdcService+"','"+VaultfilePath+"',"+commitstatus+")";



		ws.executeSQL(insertSql);
	} // end of updatepullRIDCTable





}  // end of Class
