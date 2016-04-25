package pushtoTargetRIDC;

import static pushtoTargetRIDC.Utils.trace;
import static pushtoTargetRIDC.Utils.traceVerbose;
import static pushtoTargetRIDC.Utils.traceResultSet;
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



/*
Folder services to track
FLD_CREATE_FOLDER
FLD_MOVE
FLD_COPY
FLD_UNFILE
FLD_EDIT_FOLDER
FLD_DELETE


*/



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

//traceVerbose("DataBinder is " + m_binder.toString());

                                                 
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

    

                                         else if (rIdcService.equals("FLD_CREATE_FOLDER"))
                                               {

                                                //        traceVerbose("DataBinder is FLD_CREATE_FOLDER " + m_binder.toString());

                              
                                                                 String      fFolderGUID = m_binder.getLocal("fFolderGUID");
                                                                 String      fParentPath = m_binder.getLocal("fParentPath");
                                                                 String      fParentGUID = m_binder.getLocal("fParentGUID");
                                                                 String      fFolderName = m_binder.getLocal("fFolderName");
                                                                         
                                                                                 fParentPath = fParentPath+"/"+fFolderName;
                                                                      
                                                                 Long currentTimeMillis =   System.currentTimeMillis();
                                                                 int commitstatus = 0;

                                                          insertPullFolderridc(ws,fFolderGUID,fParentGUID,fParentPath,rIdcService,currentTimeMillis,commitstatus);

                                                              




                                              } // end of FLD_CREATE_FOLDER                         




                                         else if (rIdcService.equals("FLD_MOVE"))
                                               {

                                                    //     traceVerbose("DataBinder is FLD_MOVE " + m_binder.toString());

                       
                                                               
                                                             String      fParentGUID = m_binder.getLocal("destination");
                                                             String      fParentPath =  m_binder.getLocal("destination_display");

                                                             fParentGUID = removeEscape(fParentGUID);
                                                                 Long currentTimeMillis =   System.currentTimeMillis();
                                                                 int commitstatus = 0;
                                                          
                                                       intradoc.data.ResultSet  FoldersMoved = m_binder.getResultSet("FoldersMoved");


                                                                               for(FoldersMoved.first(); FoldersMoved.isRowPresent(); FoldersMoved.next())

                                                                                                    {

                                             

                                                                                 String      fFolderGUID = FoldersMoved.getStringValueByName("fFolderGUID");
                                                        insertPullFolderridc(ws,fFolderGUID,fParentGUID,fParentPath,rIdcService,currentTimeMillis,commitstatus);

                                                                  
                                                                                                    }





                                              } // end of FLD_MOVE                       

                                         else if (rIdcService.equals("FLD_COPY"))
                                               {
                                                     

                                                                   //      traceVerbose("Binder"                + m_binder.toString());
                                                                              String      fParentGUID =  m_binder.getLocal("destination");
                                                                      

                                                                               String      fParentPath =  m_binder.getLocal("destination_display");

                                                             fParentGUID = removeEscape(fParentGUID);
                                                            
                                                                  Long currentTimeMillis =   System.currentTimeMillis();
                                                                 int commitstatus = 0;

                                                                                     for (int i=1;m_binder.getLocal("item"+i)!=null;i++)
                                                                               {
                                                                           String      fFolderGUID =  m_binder.getLocal("item"+i);

                                                                                    fFolderGUID = removeEscape(fFolderGUID);
                                                                    insertPullFolderridc(ws,fFolderGUID,fParentGUID,fParentPath,rIdcService,currentTimeMillis,commitstatus);

                                                                                }


                                                              intradoc.data.ResultSet  FolderCopied = m_binder.getResultSet("ChildFolders");
  


                                                                                  for(FolderCopied.first(); FolderCopied.isRowPresent(); FolderCopied.next())
 
                                                                                                    {

                                             

                                                                                 String      fFolderGUID = FolderCopied.getStringValueByName("fFolderGUID");

                                                                                        fParentGUID =  m_binder.getLocal("fParentGUID");

                                                                                                rIdcService="FLD_COPY_REST";
                                                                                                    
                                                        insertPullFolderridc(ws,fFolderGUID,fParentGUID,fParentPath,rIdcService,currentTimeMillis,commitstatus);

                                                                  
                                                                                                    }               
                                                             

   

         




                                              } // end of FLD_COPY                        

                                         else if (rIdcService.equals("FLD_DELETE"))
                                               {
                                       
                                           //             traceVerbose("DataBinder is FLD_DELETE " + m_binder.toString());
                                                           
                                                                      // String      fFolderGUID =  m_binder.getLocal("item1");
                                                                      // fFolderGUID = removeEscape(fFolderGUID);
 
                                                                       String      fParentGUID = "Not";
                                                                       String      fParentPath =  "Not";
                                                                 Long currentTimeMillis =   System.currentTimeMillis();
                                                                 int commitstatus = 0;



                                                                                      for (int i=1;m_binder.getLocal("item"+i)!=null;i++)
                                                                               {
                                                                           String      fFolderGUID =  m_binder.getLocal("item"+i);

                                                                                    fFolderGUID = removeEscape(fFolderGUID);
                                                                    insertPullFolderridc(ws,fFolderGUID,fParentGUID,fParentPath,rIdcService,currentTimeMillis,commitstatus);

                                                                                }


                                                                       

                                    
                                              } // end of FLD_DELETE                       

                                        else if (rIdcService.equals("FLD_UNFILE"))
                                               {
                                                       // traceVerbose("DataBinder is FLD_UNFILE " + m_binder.toString());

                                                              // String      fFolderGUID = m_binder.getLocal("fFolderGUID");
                                                                String     fParentGUID = "NO";

                                                            // fFolderGUID = removeEscape(fFolderGUID);
                                                                 Long currentTimeMillis =   System.currentTimeMillis();
                                                                 int commitstatus = 0;
  

                                                          

                                                                            /*         for (int i=1;m_binder.getLocal("item"+i)!=null;i++)
                                                                               {
                                                                         String      fParentPath =  m_binder.getLocal("item"+i);

                                                                                    fParentPath = removeEscape(fParentPath);

                                                                                 traceVerbose(fParentPath);
                                                                               traceVerbose(fFolderGUID);
                                                                                    
                                                                 //   insertPullFolderridc(ws,fFolderGUID,fParentGUID,fParentPath,rIdcService,currentTimeMillis,commitstatus);

                                                                                }

                                                                                */

                                              intradoc.data.ResultSet  FolderCopied = m_binder.getResultSet("TaskList");


                                                                                                 for(FolderCopied.first(); FolderCopied.isRowPresent(); FolderCopied.next())
 
                                                                                                    {

                                             

                                                                                 String      fFolderGUID = FolderCopied.getStringValueByName("fItemIdentifier");


                                                                                                        fFolderGUID=removeEscape(fFolderGUID);


                                                                                String   fParentPath = FolderCopied.getStringValueByName("fPath");
                                                                                       
                                                        insertPullFolderridc(ws,fFolderGUID,fParentGUID,fParentPath,rIdcService,currentTimeMillis,commitstatus);

                                                                  
                                                                                                    }               
                                                             
                                           //  traceResultSet (FolderCopied);
                                                                  



                                                      //    insertPullFolderridc(ws,fFolderGUID,fParentGUID,fParentPath,rIdcService,currentTimeMillis,commitstatus);

                                                               
                                


                                              } // end of FLD_UNFILE                        
                                                      

                                         else if (rIdcService.equals("FLD_EDIT_FOLDER"))
                                               {


                                                       // traceVerbose("DataBinder is FLD_EDIT_FOLDE " + m_binder.toString());


 
                                                            String      fFolderGUID =  m_binder.getLocal("fFolderGUID");
                                                            
                                                                       String      fParentGUID = "Not";
                                                                       String      fParentPath =  "Not";
                                                                 Long currentTimeMillis =   System.currentTimeMillis();
                                                                 int commitstatus = 0;

                                                          insertPullFolderridc(ws,fFolderGUID,fParentGUID,fParentPath,rIdcService,currentTimeMillis,commitstatus);


                                                

                                              } // end of FLD_EDIT_FODLER                        



                                      else if ( rIdcService.equals("FLD_BROWSE_POPUP"))
                       {                                                  //  traceVerbose("DataBinder is FLD_BROWSE_POPUP" + m_binder.toString());
 

                                         
                                      // intradoc.data.ResultSet  FolderCopied = m_binder.getResultSet("ChildFolders");
                                      // traceResultSet(FolderCopied);
                      }


                else if ( rIdcService.equals("FLD_RETRIEVE_CHILD_FOLDERS"))
                       {                                                 //   traceVerbose("DataBinder is FLD_BROWSE_POPUP" + m_binder.toString());
 

                                         
                                      // intradoc.data.ResultSet  FolderCopied = m_binder.getResultSet("ChildFolders");
                                      // traceResultSet(FolderCopied);
                      }


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


protected void  insertPullFolderridc(Workspace ws,String fFolderGUID,String fParentGUID,String fParentPath,String rIdcService,long currentTimeMillis,int commitstatus) throws DataException


{


 String insertSql = "INSERT INTO PullFolderridc (rID,fFolderGUID,fParentGUID,fParentPath,RIDCSERVICE,RENTRYDATE,commitstatus) values(rid_seq.nextval,'"+fFolderGUID+"','"+fParentGUID+"','"+fParentPath+"','"+rIdcService+"',"+currentTimeMillis+","+commitstatus+")";


          ws.executeSQL(insertSql);

} // end of insertPullFolderridc


String removeEscape ( String st)

{

String fg[]= st.split("\\:+");

if ( fg.length > 1) 
return fg[1];

else
return "No folder found";


}

}  // end of Class




                                                                   /*  intradoc.data.ResultSet  ParentInfo = m_binder.getResultSet("ParentInfo");


                                                                               for(ParentInfo.first(); ParentInfo.isRowPresent(); ParentInfo.next())

                                                                                                    {

                                             
                                                                                                   //    String   value = ParentInfo.getStringValueByName(key);
                                                                                                         // traceVerbose (key + "  " + value);

                                                                                    
                                                                                            fParentPath = fParentPath+"/"+ParentInfo.getStringValueByName("fDisplayName");
                                                                                 
                                                                  
                                                                                                    }

                                                                          */

 //  clear ;java GetServerOutput  > /opt/app/code/custom/pullRIDC/pushtoTargetRIDC/binders/FLD_EDIT_FOLDER.txt

