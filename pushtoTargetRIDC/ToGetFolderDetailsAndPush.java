package pushtoTargetRIDC;

import static pushtoTargetRIDC.Utils.*;
import static pushtoTargetRIDC.UseRIDC.*;

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


public class ToGetFolderDetailsAndPush 

{

intradoc.data.Workspace  ws;
intradoc.data.DataBinder binder;
intradoc.server.Service mservice;

public ToGetFolderDetailsAndPush(intradoc.data.Workspace  ws, intradoc.data.DataBinder binder,intradoc.server.Service mservice)

{

this.ws = ws ;
this.binder =  binder;
this.mservice = mservice;


}  // end of constuctor 




public String  fldCreateFolder (String RID) throws intradoc.common.ServiceException ,intradoc.data.DataException

{

traceVerbose (  "fldCreateFolder");

String SelectSQL="Select fFolderGUID,fParentGUID  from PullFolderridc where rid="+RID;


intradoc.data.ResultSet selectdIDDocName  =  runSQLpullRIDCTable(ws,SelectSQL );

String fFolderGUID="";
String fParentGUID="";


                                               for(selectdIDDocName.first(); selectdIDDocName.isRowPresent(); selectdIDDocName.next())

                                           {

                                            fFolderGUID=selectdIDDocName.getStringValueByName("fFolderGUID");
                                            fParentGUID=selectdIDDocName.getStringValueByName("fParentGUID");
                                                                                                                            
                                           }


                                                     if ( fParentGUID.equals("FLD_ROOT"))
                                                                   traceVerbose(" FLD_ROOT ::No Need for targetID search");
                                                      else 

                                                      {

                                                                  String targetffolderguid = findGUIDInTarget("fFolderGUID",fParentGUID);


                                                                                       if ( targetffolderguid.length () == 0 )
                                                                                      {
                                                                                               

                                                                             String        updateSQL= "UPDATE PullFolderridc  SET commitstatus=301 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (ws,updateSQL); 
                                                                                    
                                                                                             return "TARGETFFOLDERGUID is not availble";
                                                                       

                                                                                       }

                                                                                       else 

                                                                                  {

                                                                                    traceVerbose("Updating  with TargetID " + targetffolderguid);

                                                                                   fParentGUID=targetffolderguid;

                                                                                  }

                                                          } // end of FLD_ROOT search end 

 try {

                                    

                String updateSQL= "UPDATE PullFolderridc  SET commitstatus=302 WHERE rID="+RID ;

                                     insertpullRIDCTable (ws,updateSQL);


                                      binder.getLocalData().clear();
                                      binder.putLocal("fFolderGUID", fFolderGUID);
                                      this.executeService(binder, "FLD_BROWSE");
                 intradoc.data.ResultSet FolderInfo = binder.getResultSet("FolderInfo");


                                                                                      
                      Long currentTimeMillis =   System.currentTimeMillis();

            updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus=303,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;

                               insertpullRIDCTable (ws,updateSQL);

                                       UseRIDC UseRIDCObject = new UseRIDC () ;

                   

                              String ridcpushaction = UseRIDCObject.useRIDCfldCreateFolder(FolderInfo,fParentGUID );

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

    updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETFFOLDERGUID='"+targetdid+"' WHERE rID="+RID ;

                               insertpullRIDCTable (ws,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception doPushCheckin "+e.getMessage());

                                        e.printStackTrace();
                                     
                                  }

return "done";

} //end of fldCreateFolder



public String  fldMoveFolder (String RID,String Rentrydate) throws intradoc.common.ServiceException ,intradoc.data.DataException

{

traceVerbose (  "fldMoveFolder");


String    targetffParentGUID,targetffFolderGUID;

String SelectSQL="Select fFolderGUID,fParentGUID  from PullFolderridc where rid="+RID;


intradoc.data.ResultSet selectdIDDocName  =  runSQLpullRIDCTable(ws,SelectSQL );

String fFolderGUID="";
String fParentGUID="";


                                               for(selectdIDDocName.first(); selectdIDDocName.isRowPresent(); selectdIDDocName.next())

                                           {

                                            fFolderGUID=selectdIDDocName.getStringValueByName("fFolderGUID");
                                            fParentGUID=selectdIDDocName.getStringValueByName("fParentGUID");
                                                                                                                            
                                           }






                                                                                  if ( fParentGUID.equals("FLD_ROOT"))
                                                                                         
                                                                             {

                                                                   traceVerbose(" FLD_ROOT ::No Need for targetID search");

                                                                                    targetffParentGUID=fParentGUID;

                                                                            }

                                                                   else {

                                                                             targetffParentGUID = findGUIDInTarget("fFolderGUID",fParentGUID);

                                                                                    

                                                                                                 if ( targetffParentGUID.length () == 0 )
                                                                                        {
                                                                                   

                                                                             String        updateSQL= "UPDATE PullFolderridc  SET commitstatus=313 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (ws,updateSQL); 
                                                                                      
                                                                                             return "TARGETfParentGUID is not availble";
                                                                       

                                                                                       }

                                                                        } // end of FLD_ROOT check 

List<String> listoffolderguid = new ArrayList<String>();

String selectSQL= "select Ffolderguid from Pullfolderridc where Rentrydate="+Rentrydate;



intradoc.data.ResultSet folderstomove =   runSQLpullRIDCTable(ws,selectSQL );

    String targetffolderguid = getTargetfFolderGUIDs(folderstomove);



                     
                                     String [] tokens =targetffolderguid.split("\\t+");
                                    targetffFolderGUID="";
                                            for ( int k=0; k < tokens.length ; k++)
                                                 {
                                                            traceVerbose(" "+k+" "+tokens[k]);
                                                           targetffFolderGUID = findGUIDInTarget("fFolderGUID",tokens[k]);
                                                           // listoffolderguid.add(targetffFolderGUID);
                                                             traceVerbose("Folders to move "+ targetffFolderGUID);
                                                

                         
                  


                                                                        


                                                                                       if ( targetffFolderGUID.length () == 0 )
                                                                                      {
                                                                                       
                 String        updateSQL= "UPDATE PullFolderridc  SET commitstatus=314 where Rentrydate="+Rentrydate +"AND fFolderGUID='"+tokens[k]+"'" ;

                                                                                      insertpullRIDCTable (ws,updateSQL); 
                                                                                      
                                                                                           
                                                                       

                                                                                       }

                                                                                       else 

                                                                                  {
                                                                                            listoffolderguid.add(targetffFolderGUID);
                                                                                    traceVerbose("Updating  with TargetID " + targetffParentGUID +" " + targetffFolderGUID);

                                                                                  
                                                                                  }

                                                   } // end of loop 
                          

  try {

                                    



                                                                                      
                      Long currentTimeMillis =   System.currentTimeMillis();

 //    String       updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus=315,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;


String updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus=315,RIDCPUSHACTION='Pushing' WHERE Rentrydate="+Rentrydate +"AND commitstatus <> 314";

                               insertpullRIDCTable (ws,updateSQL);

                                       UseRIDC UseRIDCObject = new UseRIDC () ;

                   

                              String ridcpushaction = UseRIDCObject.useRIDCfldMove(listoffolderguid , targetffParentGUID);

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETFFOLDERGUID='"+targetdid+"' WHERE Rentrydate="+Rentrydate +"AND commitstatus <> 314";

                               insertpullRIDCTable (ws,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception doPushCheckin "+e.getMessage());

                                        e.printStackTrace();
                                     
                                  }





return "Done";


} // end of fldMoveFolder

public String  fldEditFolder (String RID) throws intradoc.common.ServiceException ,intradoc.data.DataException

{

traceVerbose (  "fldEditFolder");


   String SelectSQL="Select fFolderGUID,fParentGUID  from PullFolderridc where rid="+RID;


intradoc.data.ResultSet selectdIDDocName  =  runSQLpullRIDCTable(ws,SelectSQL );

String fFolderGUID="";



                                               for(selectdIDDocName.first(); selectdIDDocName.isRowPresent(); selectdIDDocName.next())

                                           {

                                            fFolderGUID=selectdIDDocName.getStringValueByName("fFolderGUID");
                                           
                                                                                                                            
                                           }
                                                                          

                                              


                                                                String targetffFolderGUID = findGUIDInTarget("fFolderGUID",fFolderGUID);


                                                                                       if ( targetffFolderGUID.length () == 0 )
                                                                                      {
                                                                                                

                                                                             String        updateSQL= "UPDATE PullFolderridc  SET commitstatus=322 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (ws,updateSQL); 
                                                                                      
                                                                                             return "TARGET FFOLDERGUID is not availble";
                                                                       

                                                                                       }

                                                                                       else 

                                                                                  {

                                                                                    traceVerbose("Updating  with TargetID " + targetffFolderGUID );

                                                                                  
                                                                                  }


 try {

                                    

                String updateSQL= "UPDATE PullFolderridc  SET commitstatus=321 WHERE rID="+RID ;

                                     insertpullRIDCTable (ws,updateSQL);


                                      binder.getLocalData().clear();
                                      binder.putLocal("fFolderGUID", fFolderGUID);
                                      this.executeService(binder, "FLD_BROWSE");
                 intradoc.data.ResultSet FolderInfo = binder.getResultSet("FolderInfo");


                                                                                      
                      Long currentTimeMillis =   System.currentTimeMillis();

            updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus=323,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;

                               insertpullRIDCTable (ws,updateSQL);

                                       UseRIDC UseRIDCObject = new UseRIDC () ;

                   

                              String ridcpushaction = UseRIDCObject.useRIDCfldEditFolder(FolderInfo , targetffFolderGUID);

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

    updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETFFOLDERGUID='"+targetdid+"' WHERE rID="+RID ;

                               insertpullRIDCTable (ws,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception doPushCheckin "+e.getMessage());

                                        e.printStackTrace();
                                     
                                  }


return "Done";


}  

/* 
This is most complicated method , 

Steps 
New steps add 1. Get fFolderGUID,fParentGUID from pullfolderidc table based on RID 

1.Verify FLD_ROOT as destination folder , if not find the target Destionation folder findGUIDInTarget method
2.Find the another folders to copy in the same FLD_COPY service from the binder . Select SQLquery1 will get it 
3. Loop tokens will help to find out source folders in Target 
4. useRIDCfldCopy by passing list of source folders and destionation (single one )in target 
5. SQLquery2 will get newly created folders in source server
6. loop3 will map newly created folders in source with newly created folders in targert based on loop2

*/



public String  fldCopyFolder (String RID,String Rentrydate) throws intradoc.common.ServiceException ,intradoc.data.DataException

{

traceVerbose (  "fldCopyFolder");


String    targetffParentGUID,targetffFolderGUID;
String SelectSQL="Select fFolderGUID,fParentGUID  from PullFolderridc where rid="+RID;


intradoc.data.ResultSet selectdIDDocName  =  runSQLpullRIDCTable(ws,SelectSQL );

String fFolderGUID="";
String fParentGUID="";


                                               for(selectdIDDocName.first(); selectdIDDocName.isRowPresent(); selectdIDDocName.next())

                                           {

                                            fFolderGUID=selectdIDDocName.getStringValueByName("fFolderGUID");
                                            fParentGUID=selectdIDDocName.getStringValueByName("fParentGUID");
                                                                                                                            
                                           }




                                                                                  if ( fParentGUID.equals("FLD_ROOT"))
                                                                                         
                                                                             {

                                                                   traceVerbose(" FLD_ROOT ::No Need for targetID search");

                                                                                    targetffParentGUID=fParentGUID;

                                                                            }

                                                                   else {

                                                                             targetffParentGUID = findGUIDInTarget("fFolderGUID",fParentGUID);

                                                                                    

                                                                                                 if ( targetffParentGUID.length () == 0 )
                                                                                        {
                                                                                   

                                                                             String        updateSQL= "UPDATE PullFolderridc  SET commitstatus=333 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (ws,updateSQL); 
                                                                                      
                                                                                             return "TARGETfParentGUID is not availble";
                                                                       

                                                                                       }

                                                                        } // end of FLD_ROOT check 

List<String> listoffolderguid = new ArrayList<String>();

String selectSQL= "select Ffolderguid from Pullfolderridc where RIDCSERVICE='FLD_COPY' AND Rentrydate="+Rentrydate;  //SQLquery1



intradoc.data.ResultSet folderstocopy =   runSQLpullRIDCTable(ws,selectSQL );

    String targetffolderguid = getTargetfFolderGUIDs(folderstocopy);



                     
                                     String [] tokens =targetffolderguid.split("\\t+");
                                    targetffFolderGUID="";
                                            for ( int k=0; k < tokens.length ; k++)
                                                 {
                                                            traceVerbose(" "+k+" "+tokens[k]);
                                                           targetffFolderGUID = findGUIDInTarget("fFolderGUID",tokens[k]);
                                                           // listoffolderguid.add(targetffFolderGUID);
                                                             traceVerbose("Folders to copy in the target "+ targetffFolderGUID);
                                                

                         
                  


                                                                        


                                                                                       if ( targetffFolderGUID.length () == 0 )
                                                                                      {
                                                                                       
                 String        updateSQL= "UPDATE PullFolderridc  SET commitstatus=334 where Rentrydate="+Rentrydate +"AND fFolderGUID='"+tokens[k]+"'" ;

                                                                                      insertpullRIDCTable (ws,updateSQL); 
                                                                                      
                                                                                           
                                                                       

                                                                                       }

                                                                                       else 

                                                                                  {
                                                                                            listoffolderguid.add(targetffFolderGUID);
                                                                                    traceVerbose("Updating  with TargetID " + targetffParentGUID +" " + targetffFolderGUID);

                                                                                  
                                                                                  }

                                                   } // end of loop 
                          

  try {

                                    



                                                                                      
                      Long currentTimeMillis =   System.currentTimeMillis();

 //    String       updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus=315,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;


String updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus=335,RIDCPUSHACTION='Pushing' WHERE Rentrydate="+Rentrydate +"AND commitstatus <> 334";

                               insertpullRIDCTable (ws,updateSQL);

                                       UseRIDC UseRIDCObject = new UseRIDC () ;

                   

                              String ridcpushaction = UseRIDCObject.useRIDCfldCopy(listoffolderguid , targetffParentGUID);

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"' WHERE Rentrydate="+Rentrydate +"AND commitstatus <> 334";

                               insertpullRIDCTable (ws,updateSQL);


 selectSQL= "select fFolderGUID from Pullfolderridc where  RIDCSERVICE='FLD_COPY_REST' AND  Rentrydate="+Rentrydate+" order by rid desc";  //SQLquery2

traceVerbose(selectSQL);
 
intradoc.data.ResultSet folderstocopytarget =   runSQLpullRIDCTable(ws,selectSQL );

                                                   List<String> listOfFLD_COPY_REST =  new ArrayList<String>();    
                                                  for(folderstocopytarget.first(); folderstocopytarget.isRowPresent(); folderstocopytarget.next())  //loop2

                                          {

                                               
                                                                                
                                                   traceVerbose("FLD_COPY_REST loop"+folderstocopytarget.getStringValueByName("fFolderGUID"));
                                                listOfFLD_COPY_REST.add(folderstocopytarget.getStringValueByName("fFolderGUID"));
                                   
                                          }

                                                  int listOfFLD_COPY_RESTcounter=0;
                                                for ( int k=0;k<UseRIDCObject.listoftargetGUIDs.size();k++)   // loop3 

                                               {
                                                       String sourcefFolderGUID=UseRIDCObject.listoftargetGUIDs.get(k);

                                                              traceVerbose("in the loop of tasklist result set "+ sourcefFolderGUID);

                                                               if ( sourcefFolderGUID.contains("fFolderGUID"))

                                                                    {

                                                                     sourcefFolderGUID=removeEscape(sourcefFolderGUID);
                                                                   k++;
                                                        String targetfFolderGUID=UseRIDCObject.listoftargetGUIDs.get(k);

                                                                       targetfFolderGUID=removeEscape(targetfFolderGUID);
                                            traceVerbose("From oldSource Target "+sourcefFolderGUID+"  From Target   "+targetfFolderGUID);


                                                   commitstatus=340;

                                                   ridcpushaction="New TargetID generated";

updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETFFOLDERGUID='"+targetfFolderGUID+"' WHERE Rentrydate="+Rentrydate +"  AND FFOLDERGUID='"+listOfFLD_COPY_REST.get(listOfFLD_COPY_RESTcounter)+"'";

                                               traceVerbose("SQL for update " + updateSQL);
                                                             insertpullRIDCTable (ws,updateSQL);

                                                                    listOfFLD_COPY_RESTcounter++;

                                                                     }
                                                               else {
                                                                     k++;
                                                                     continue;
                                                                     }

                                               }


 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception doPushCheckin "+e.getMessage());

                                        e.printStackTrace();
                                     
                                  }





return "Done";


} // end of fldCopyFolder






public String  fldDeleteFolder (String RID,String Rentrydate) throws intradoc.common.ServiceException ,intradoc.data.DataException

{

traceVerbose (  "fldDeleteFolder");



String    targetffParentGUID,targetffFolderGUID;

String SelectSQL="Select fFolderGUID  from PullFolderridc where rid="+RID;


intradoc.data.ResultSet selectdIDDocName  =  runSQLpullRIDCTable(ws,SelectSQL );

String fFolderGUID="";



                                               for(selectdIDDocName.first(); selectdIDDocName.isRowPresent(); selectdIDDocName.next())

                                           {

                                            fFolderGUID=selectdIDDocName.getStringValueByName("fFolderGUID");
                                   
                                                                                                                            
                                           }

                                                          
List<String> listoffolderguid = new ArrayList<String>();

String selectSQL= "select Ffolderguid from Pullfolderridc where Rentrydate="+Rentrydate;



intradoc.data.ResultSet folderstodelete =   runSQLpullRIDCTable(ws,selectSQL );

    String targetffolderguid = getTargetfFolderGUIDs(folderstodelete);



                     
                                     String [] tokens =targetffolderguid.split("\\t+");
                                    targetffFolderGUID="";
                                            for ( int k=0; k < tokens.length ; k++)
                                                 {
                                                            traceVerbose(" "+k+" "+tokens[k]);
                                                           targetffFolderGUID = findGUIDInTarget("fFolderGUID",tokens[k]);
                                                           // listoffolderguid.add(targetffFolderGUID);
                                                             traceVerbose("Folders to delete "+ targetffFolderGUID);
                                                

                         
                  


                                                                        


                                                                                       if ( targetffFolderGUID.length () == 0 )
                                                                                      {
                                                                                       
                 String        updateSQL= "UPDATE PullFolderridc  SET commitstatus=358 where Rentrydate="+Rentrydate +"AND fFolderGUID='"+tokens[k]+"'" ;

                                                                                      insertpullRIDCTable (ws,updateSQL); 
                                                                                      
                                                                                           
                                                                       

                                                                                       }

                                                                                       else 

                                                                                  {
                                                                                            listoffolderguid.add(targetffFolderGUID);
                                                                                    traceVerbose("Updating  with TargetID  " + targetffFolderGUID);

                                                                                  
                                                                                  }

                                                   } // end of loop 

  try {

                                    



                                                                                      
                      Long currentTimeMillis =   System.currentTimeMillis();



String updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus=359,RIDCPUSHACTION='Pushing' WHERE Rentrydate="+Rentrydate +"AND commitstatus <> 314";

                               insertpullRIDCTable (ws,updateSQL);

                                       UseRIDC UseRIDCObject = new UseRIDC () ;

                   

                              String ridcpushaction = UseRIDCObject.useRIDCfldDelete(listoffolderguid);

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETFFOLDERGUID='"+targetdid+"' WHERE Rentrydate="+Rentrydate +"AND commitstatus <> 358";

                               insertpullRIDCTable (ws,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception flddelete "+e.getMessage());

                                        e.printStackTrace();
                                     
                                  }





return "Done";


} // end of fldDeleteFolder

public String  fldUnFile (String RID,String Rentrydate) throws intradoc.common.ServiceException ,intradoc.data.DataException

{

traceVerbose (  "fldUnFile");


String SelectSQL="Select FPARENTPATH,fFolderGUID  from PullFolderridc where Rentrydate="+Rentrydate;

traceVerbose ( SelectSQL);

intradoc.data.ResultSet selectdIDDocName  =  runSQLpullRIDCTable(ws,SelectSQL );

List<String> listofitems = new ArrayList<String>();



                                               for(selectdIDDocName.first(); selectdIDDocName.isRowPresent(); selectdIDDocName.next())

                                           {

                                                listofitems.add(selectdIDDocName.getStringValueByName("FPARENTPATH"));
                                            
                                                                                                                            
                                           }


                                                 

                                                                                       
                 String        updateSQL= "UPDATE PullFolderridc  SET commitstatus=377 where RID="+RID ;

                                                                                   
                          

  try {

                            






                                       UseRIDC UseRIDCObject = new UseRIDC () ;

                   

                              String ridcpushaction = UseRIDCObject.fldFolderUnFile(listofitems);

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                        long    currentTimeMillis =   System.currentTimeMillis();

updateSQL= "UPDATE PullFolderridc  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"' where RID="+RID;

                               insertpullRIDCTable (ws,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception flddelete "+e.getMessage());

                                        e.printStackTrace();
                                     
                                  }





return "Done";


} // end of fldUnFile



public  String   findGUIDInTarget (String whichGUID,String fGUID ) throws intradoc.common.ServiceException ,intradoc.data.DataException


{

      String selectSQL = "select TARGETFFOLDERGUID from PullFolderridc where "+ whichGUID +"='"+fGUID+"' and (Targetffolderguid Is Not Null AND Targetffolderguid <> 'null')" ;    
                                                                              
           traceVerbose( selectSQL);                                                                           
                                                                                    intradoc.data.ResultSet targetFolderInfo =   runSQLpullRIDCTable(ws,selectSQL );

                                                                      String targetffolderguid = getTargetID(targetFolderInfo);


return targetffolderguid;



}  // end of findGUIDInTarget


public String  getTargetfFolderGUIDs(final intradoc.data.ResultSet rs) throws intradoc.common.ServiceException ,intradoc.data.DataException


{

String newValue="";
                                      for(rs.first(); rs.isRowPresent(); rs.next())

                                          {

                                               
                                                    // newValue = rs.getStringValueByName("Ffolderguid");
                                                    

                                                      newValue=rs.getStringValueByName("fFolderGUID")+"\t"+newValue;

                                                 
                                                  //      traceVerbose("in the loop"+newValue);
                               
                                                
                                   
                                          }


return newValue;

}  // end of getTargetfFolderGUIDs


private String removeEscape ( String st)

{

String fg[]= st.split("\\:+");

return fg[1];


}


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



} //end of class 
