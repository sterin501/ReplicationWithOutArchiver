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
import intradoc.folders.*;





public class pushFolderUnFileFilter  extends ServiceHandler implements FilterImplementor  
{





     public int  doFilter(Workspace ws, DataBinder m_binder ,  ExecutionContext cxt)   throws DataException, ServiceException
              {
                        traceVerbose ("Inside pushFolderUnFileFilter");

                
                                if (cxt  instanceof Service) {

                                                            String     rIdcService= m_binder.getLocal("IdcService");

                                                 if (rIdcService != null && rIdcService.equals("FLD_UNFILE"))

                                                   {
                                                      // traceVerbose("SERVICE FROM filter " + rIdcService);

                                                                 // end of cxt check if       
                                   traceVerbose("DataBinder is FLD_EDIT_FOLDE " + m_binder.toString());


                            //  FolderItem item = (FolderItem) cxt.getCachedObject("folderItem");

                                     //   String collectionID = m_binder.getAllowMissing("folderChildren");

                                       //  String parentFolderGUID = FoldersUtils.getFolderGUIDForCollectionID(collectionID, ws);

                                         //          traceVerbose(parentFolderGUID);

                                  //  String parentFolderGUID = FoldersUtils.getFolderGUIDForCollectionID(collectionID, ws);

                            //  String dIDList = FoldersUtils.getRevisionIDs(item.get("dDocName"), ws);

                              //     traceVerbose("did " + dIDList);

                                                                 Long currentTimeMillis =   System.currentTimeMillis();
                                                                 int commitstatus = 0;
                                                             String      fFolderGUID=m_binder.getLocal("fFolderGUID");
                                                              String      fParentGUID="NOT";

                                               //  if ( dIDList.contains(","))

                                                 //                {

                                                   //                    String [] tokens = dIDList.split("\\,+");
                                                                  
                                                                            //  for ( String st : tokens)
                                                                    //insertPullFolderridc(ws,fFolderGUID,fParentGUID,st,rIdcService,currentTimeMillis,commitstatus);
 
                                                               //   }

                                                 // else 
                                                   //               {

                                                                     //    insertPullFolderridc(ws,fFolderGUID,fParentGUID,dIDList,rIdcService,currentTimeMillis,commitstatus);
                                                     //             }

                                                                        for (int i=1;m_binder.getLocal("item"+i)!=null;i++)
                                                                               {
                                                                         String      fParentPath =  m_binder.getLocal("item"+i);

                                                                                    fParentPath = removeEscape(fParentPath);

                                                                                 traceVerbose(fParentPath);
                                                                               traceVerbose(fFolderGUID);
                                                                                    
                                                                 //  insertPullFolderridc(ws,fFolderGUID,fParentGUID,fParentPath,rIdcService,currentTimeMillis,commitstatus);

                                                                                }



                                       
                                                   
                                                      } // end of rIdcService


                                                            }  // end of service if

                                     
                return 1; 
              } // end of doFilter 

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


} // end of class
