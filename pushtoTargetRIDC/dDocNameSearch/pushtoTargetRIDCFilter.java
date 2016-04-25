package pushtoTargetRIDC;

import static pushtoTargetRIDC.Utils.trace;
import static pushtoTargetRIDC.Utils.traceVerbose;
import static pushtoTargetRIDC.Utils.verifyRow;
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
 

                                                    
                                                                       
                                                  
                                                      traceVerbose("Inside the filter ");

                                                 
                                                        String      dID = m_binder.getLocal("dID");

                                                                     if ( dID != null )  // this if will stop to add null values to table or failed checkin etc
                                                                          
                                                            
                                                                   {
                                                              Long currentTimeMillis =   System.currentTimeMillis();
                                                             String      VaultfilePath  = m_binder.getLocal("VaultfilePath");
                                                             traceVerbose("dID is " + dID + " VaultfilePath " +  VaultfilePath );

                                                             int  commitstatus = 0;
                                                             String      dDocName  = m_binder.getLocal("dDocName");
                                                            

                                                                              // To verify the RELEASED status 


                                                                   if ( SharedObjects.getEnvironmentValue("CommitOnlyReleased").equals("true"))
                        
                                                                                     {
                                                                                              String      dStatus   = m_binder.getLocal("dStatus");

                                                                                                  if ( dStatus !=null && !dStatus.equals("RELEASED"))
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
                                                                    
                                                                         if ( verifyRow (rs))
                                                                            {
                                                                               // we have to update it ,100 means got deleted before even try to replicate it 

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
    
                                         else if (rIdcService.equals("DELETE_DOC") ) 
                                                   
                                                    {
                                                                 String      dID = m_binder.getLocal("dID");
                                                                 String      dDocName = m_binder.getLocal("dDocName");

                                                                 traceVerbose("DELETE_DOC  did   " + dID + " dDocname" + dDocName);


                                                                     if ( dID != null && dDocName!= null)

                                                                          { 
                                                                            
                                                                  String SQLStatement = " select * from pullridc where dDocName='"+dDocName+"' and Commitstatus=0";
                                                                  intradoc.data.ResultSet rs =  ws.createResultSetSQL(SQLStatement);

                                                                     
                                                                            if ( verifyRow(rs))
                                                                             {
                                                                               // we have to update it ,100 means got deleted before even try to replicate it 

                                                                 String  updateSQL= "UPDATE pullRIDC  SET commitstatus=100 WHERE dDocName='"+dDocName+"'" ;
                                                                                  ws.executeSQL(updateSQL);


                                                                             }

                                                                         else 

                                                                             {


                                                                                 Long currentTimeMillis =   System.currentTimeMillis();
                                                                                 int  commitstatus = 0;
                                                                                 String      VaultfilePath = null ;

                                                                                insertpullRIDCTable(ws,dID,dDocName,currentTimeMillis,rIdcService,VaultfilePath,commitstatus);
                                                                      
                                                                              }

                                                                         }


                                                   } // end of DELETE_DOC

    



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
