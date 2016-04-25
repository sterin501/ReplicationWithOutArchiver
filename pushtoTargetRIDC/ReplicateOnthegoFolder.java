package pushtoTargetRIDC;

import static pushtoTargetRIDC.Utils.*;
import static pushtoTargetRIDC.UseRIDC.*;


import java.util.*;
import java.io.*;
import intradoc.common.*;
import intradoc.data.*;
import intradoc.server.*;
import intradoc.shared.*;
	
//import intradoc.filestore.*;


/**
 * Please check the read me of the component 
 * it is using Utils and UseRIDC class
 * written by Sterin Jacob
 */
public class ReplicateOnthegoFolder extends ServiceHandler
{


 int contentCounter=0;
 String listofContentsinFolder="";
 String listofdID="";
 String parentofBasefolder="";
 String Basefolder="";
 intradoc.data.Workspace  ws;
intradoc.data.DataBinder binder;
intradoc.server.Service mservice;


List<String> finalFolders  = new ArrayList<String>();

	public void ReplicateOnthegoFolderHandler() throws DataException, intradoc.common.ServiceException ,intradoc.common.ParseStringException,java.lang.Exception
	


        {
	
         traceVerbose("Inside     ReplicateOnthegoFolderHandler");  
            ws =m_workspace;
            binder = m_binder;



            
         

         String Rquery = m_binder.getLocal("Rquery");

         String  ListdID = m_binder.getLocal("listofdID");
 
         String  ListdDocName = m_binder.getLocal("ListdDocName");

         traceVerbose("Rquery is "+ Rquery); 

         traceVerbose("Rquery is "+ ListdID);           

         String idctoken="";
         String FfolderGUID="";

                                                          if (m_binder.getLocal("idcToken")!=null )
                                                        idctoken=(m_binder.getLocal("idcToken"));


                                if (Rquery !=null)

                     {
                               
                         Basefolder= getFfolderGUID(Rquery);

                     }


                        if (Basefolder.length() > 1 )

                     {

                                 
                                  
                                 
                      

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

                                           folderfind(Basefolder);

                    traceVerbose("Total Folders Under " + Basefolder + "  : " +finalFolders.size());
                   traceVerbose("Total Contents Under " + Basefolder + " : " +contentCounter);

                   traceVerbose("dID "+listofdID);

                                   
                                 m_binder.putLocal("ShowResult","yes");
                                 m_binder.putLocal("FfolderGUID",FfolderGUID);
                             

                               List<String> validFolders = getValidFoldertoReplicate();
                                                   if ( validFolders.size() > 0)
                                                         {
                                                                   replicateFolderOnthego(validFolders);

                                                         }

                                                   else { 

                                 m_binder.putLocal("ShowResult","yes");
                                 m_binder.putLocal("FfolderGUID","Parent of Folder is replicated.");

                                 m_binder.putLocal("listofContentsinFolder",listofContentsinFolder);

                                     m_binder.putLocal("listofdID",listofdID);

                                         

                                                        }



                      } // end of Basefolder if 


      if ( ListdID !=null )

              {

                  traceVerbose("Replicate on the go for content will be called " + ListdID);

                     List<intradoc.data.DataBinder>  dIDtoreplicate = verifyTheTargetIDinPullridc(ListdID);
                        
                   try {


                         replicateAndUpdatePullridc(dIDtoreplicate);



                       }


                       catch ( Exception e ) {

                                         traceVerbose("Exception replicateAndUpdatePullridc "+e.getMessage());

                                    e.printStackTrace();
                                             }


              }

                  

                                     





                                                              if(idctoken.length() > 2)
                                                          m_binder.putLocal("idcToken",idctoken);   


                                                        
  

        }  // end of handler method

public String  getFfolderGUID ( String path) throws DataException, intradoc.common.ServiceException ,intradoc.common.ParseStringException


{

                               m_binder.getLocalData().clear();
                               m_binder.putLocal("path",path);

                               m_binder.putLocal("IdcService", "FLD_BROWSE");
                              
                                          
                                this.executeService(m_binder, "FLD_BROWSE");

                                  
                                          
intradoc.data.ResultSet  FolderInfo = m_binder.getResultSet("FolderInfo");

String FfolderGUID="";



                                                                                  for(FolderInfo.first(); FolderInfo.isRowPresent(); FolderInfo.next())
 
                                                                                                    {

                                             

                                                                                       FfolderGUID = FolderInfo.getStringValueByName("fFolderGUID");
                                                                                       parentofBasefolder = FolderInfo.getStringValueByName("fParentGUID");

                                                                                                    }


  if ( FfolderGUID.equals("FLD_ROOT"))
parentofBasefolder="FLD_ROOT";

 m_binder.getLocalData().clear();
return FfolderGUID;



} // end of getFfolder

public  void folderfind (String    fFolderGUID)   throws  DataException, intradoc.common.ServiceException


{
         // traceVerbose("Inside folderfind with " + fFolderGUID);

//traceVerbose("----------------");


        List<String>    childFolders = runFLD_RETRIEVE_CHILD_FOLDERS(fFolderGUID);

                                        List<String>    subFolders = childFolders;


                                                 if  ( childFolders.size() > 0)
                                                      {
                                                                  //  traceVerbose("Inside childFolders "+ subFolders.size());
                                                              // String localfoolder="";

                                                                       // String []arr = new  String [2];
                                                              
                                                           for ( int j=0;j<subFolders.size();j++)
                                                                {
                                                                        
                                                                      

                                                                          traceVerbose(fFolderGUID+"-->"+subFolders.get(j)+"<--->"+listofContentsinFolder);  

                                                       // contentwithfolder="\n"+contentwithfolder+fFolderGUID+"-->"+subFolders.get(j)+"<--->"+listofContentsinFolder+"\n";
                                                              
                                                                  //    traceVerbose(fFolderGUID+"-->"+subFolders.get(j));
                                                           
                                                                finalFolders.add(fFolderGUID+"-->"+subFolders.get(j));
 
                                                                      
                                                                     folderfind(subFolders.get(j));
                                                                 }
                                                               


                                                      }


                                                 


} // end of folderfind

public  List<String> runFLD_RETRIEVE_CHILD_FOLDERS (String fFolderGUID ) throws  DataException, intradoc.common.ServiceException

{

//traceVerbose("Inside runFLD_RETRIEVE_CHILD_FOLDERS with " + fFolderGUID);



List<String> childFolders  = new ArrayList<String>(); 
listofContentsinFolder="";

 intradoc.data.DataBinder binder = new DataBinder();

                                         binder.getLocalData().clear();
                                      // binder.putLocal("IdcService", "FLD_RETRIEVE_CHILD_FOLDERS");
                                          m_binder.putLocal("IdcService", "FLD_BROWSE");

                                       binder.putLocal("fFolderGUID",fFolderGUID);         
                                                                         
                           this.executeService(binder, "FLD_BROWSE");

                                  
                                          
intradoc.data.ResultSet  FolderInfo = binder.getResultSet("ChildFolders");
intradoc.data.ResultSet  ChildFiles = binder.getResultSet("ChildFiles");    


                                                     if ( FolderInfo !=null)

                                                {
                                                                                               for(FolderInfo.first(); FolderInfo.isRowPresent(); FolderInfo.next())
 
                                                                                                    {

                                             

                                                                                       
                                                                                childFolders.add(FolderInfo.getStringValueByName("fFolderGUID"));

                                                                                                    }

                        

                                                   }
                    



         





                           if ( ChildFiles !=null)
                    
                          {   
                            for(ChildFiles.first(); ChildFiles.isRowPresent(); ChildFiles.next())
 
                            {
                                       
    

                           listofContentsinFolder=ChildFiles.getStringValueByName("dDocName")+","+listofContentsinFolder;
                           contentCounter++;

                              listofdID=ChildFiles.getStringValueByName("dID")+" "+listofdID;
                           }


                     }   


 
return childFolders;

                           

} // end of  runFLD_RETRIEVE_CHILD_FOLDERS


public  void  replicateFolderOnthego (List<String> ValidFolder )throws  DataException, intradoc.common.ServiceException,java.lang.Exception


{

 for ( int k=0;k <ValidFolder.size(); k++)


{

String ParentFolderofTarget ="FLD_ROOT";
                                                           String []        tokens = ValidFolder.get(k).split("\\-->+");

                                                           String Childfolder = tokens[1];
                                                           String ParentFolder = tokens[0];

                                                         //  traceVerbose("Creating " + Childfolder);
                                                           UseRIDC UseRIDCObject = new UseRIDC () ;


                                                                   if ( ParentFolder.contains("FLD_"))
                                        
                                                                   ParentFolderofTarget=ParentFolder;
                                                                    else 
                                                     ParentFolderofTarget = findGUIDInTarget("fFolderGUID",ParentFolder);


          

                                                    traceVerbose("Creating " + Childfolder + " PF " + ParentFolder + " PFT " + ParentFolderofTarget);

                                                           String ridcpushaction = UseRIDCObject.useRIDCfldCreateFolder(getFolderInfo(Childfolder),ParentFolderofTarget ); 
                                                                        ridcpushaction=ridcpushaction.replaceAll("'","");
                                                                    insertPullridcFolderTable(Childfolder,ParentFolder,UseRIDCObject.targetdid,ridcpushaction);

                                                     


}




}


//public  void  replicateFolderOnthego ()throws  DataException, intradoc.common.ServiceException

public  List   getValidFoldertoReplicate  () throws  DataException, intradoc.common.ServiceException,java.lang.Exception

{

traceVerbose("Inside getValidFoldertoReplicate");



List<String> ValidFolder  = new ArrayList<String>(); 



                                                     if ( parentofBasefolder.contains("FLD_"))
                                                                   traceVerbose(" FLD_ROOT ::No Need for targetID search");
                                                      else 

                                                      {
                                                                String targetffolderguid = findGUIDInTarget("fFolderGUID",parentofBasefolder);
                                                                              
                                                                                       if ( targetffolderguid.length () == 0 )
                                                                                      {
                                                                                      traceVerbose("Parent Folder is not replicate.Replicate One folder above");
                                                                                               return ValidFolder;     
                                                                                      }



                                                      }



                                                                      if ( Basefolder.contains("FLD_"))
                                                                   traceVerbose(" FLD_ROOT ::No Need for targetID search");
                                                   else 
                                                     {
                                                             
                                                            String targetbaseFolder = findGUIDInTarget("fFolderGUID",Basefolder);


                                                                 if (targetbaseFolder.length ()== 0)

                                                         {
                                                            traceVerbose("None of  sub Folders are  replicated :: Sending Base folder first"+Basefolder + " " +parentofBasefolder );
                                                                        UseRIDC UseRIDCObject = new UseRIDC () ;

                                                           String ridcpushaction = UseRIDCObject.useRIDCfldCreateFolder(getFolderInfo(Basefolder),parentofBasefolder ); 
                                                                        ridcpushaction=ridcpushaction.replaceAll("'","");
                                                                    insertPullridcFolderTable(Basefolder,parentofBasefolder,UseRIDCObject.targetdid,ridcpushaction);

  
                                                                                               return finalFolders;     


                                                           } 

                                                   }
                                                      



                                                                  for ( int k=0;k <finalFolders.size(); k++)



                                                     {

                                                           String []        tokens = finalFolders.get(k).split("\\-->+");
                                                                 
                                                                 
                                                               
                                                                         String targetbaseofFolder = findGUIDInTarget("fFolderGUID",tokens[1]);

                                                                              if (targetbaseofFolder.length () != 0)

                                                                                           {
                                                                                        traceVerbose("This Has to Replicate  " + tokens[1]);
                                                                                                
                                                                                           ValidFolder.add(finalFolders.get(k));   

                                                                                            } 



                                                            /*       if (folderaddsofar.contains(ParentFolder)  )
                                                                     traceVerbose(ParentFolder + " No need to create");

                                                                     else {

                                                                                 if (ParentFolder.equals(Basefolder)
                                                                                     {
                                                                                     traceVerbose("Sending to RIDC" + ParentFolder + " " +parentofBasefolder );      
                                                                                     folderaddsofar=folderaddsofar+" "+  ParentFolder; 
                                                                                
                                                                                        }
                                                                       else {
                                                                          traceVerbose("Sending to RIDC" + ParentFolder + " " +ChildFolder );
                                                                        folderaddsofar=folderaddsofar+" "+  ParentFolder;  
                                                                         folderaddsofar=folderaddsofar+" "+  ChildFolder; 
                                                                               }
                                                                          }

                                                            */

                                                     }


return ValidFolder;

} 

public intradoc.data.ResultSet  getFolderInfo ( String fFolderGUID) throws DataException, intradoc.common.ServiceException ,intradoc.common.ParseStringException


{

                               m_binder.getLocalData().clear();
                               m_binder.putLocal("fFolderGUID",fFolderGUID);

                               m_binder.putLocal("IdcService", "FLD_BROWSE");
                              
                                          
                                this.executeService(m_binder, "FLD_BROWSE");

                                  
                                          
//intradoc.data.ResultSet  FolderInfo =m_binder.getResultSet("FolderInfo");


return (m_binder.getResultSet("FolderInfo"));


}


public  String   findGUIDInTarget (String whichGUID,String fGUID ) throws intradoc.common.ServiceException ,intradoc.data.DataException


{

      String selectSQL = "select TARGETFFOLDERGUID from PullFolderridc where "+ whichGUID +"='"+fGUID+"' and (Targetffolderguid Is Not Null AND Targetffolderguid <> 'null')" ;    
                                                                              
       //   traceVerbose( selectSQL);                                                                           
                                                                                    intradoc.data.ResultSet targetFolderInfo =   runSQLpullRIDCTable(m_workspace,selectSQL );

                                                                      String targetffolderguid = getTargetID(targetFolderInfo);


return targetffolderguid;



}  // end of findGUIDInTarget


public void insertPullridcFolderTable (String fFolderGUID,String fParentGUID ,String targetdid,String ridcpushaction )  throws DataException

{

   long currentTimeMillis =  System.currentTimeMillis();

  int  commitstatus=600;

 String rIdcService="REPLICATE_ONTHEGO_FOLDER";



// String insertSql = "INSERT INTO PullFolderridc(rID,fFolderGUID,rEntryDate,rActionDate,rIdcService,VaultfilePath,commitstatus,TARGETFFOLDERGUID) values(rid_seq.nextval,"+dID+",'"+dDocName+"',"+currentTimeMillis+","+currentTimeMillis+",'"+rIdcService+"','"+VaultfilePath+"',"+commitstatus+",'"+targetdid+"')";


 String insertSql = "INSERT INTO PullFolderridc (rID,fFolderGUID,fParentGUID,RIDCSERVICE,RENTRYDATE,commitstatus,TARGETFFOLDERGUID) values(rid_seq.nextval,'"+fFolderGUID+"','"+fParentGUID+"','"+rIdcService+"',"+currentTimeMillis+","+commitstatus+",'"+targetdid+"')";


		m_workspace.executeSQL(insertSql);




}  

public List verifyTheTargetIDinPullridc ( String ListdID) throws DataException, intradoc.common.ServiceException ,intradoc.common.ParseStringException


{


 traceVerbose("Inside     verifyTheTargetIDinPullridc");  

  String [] tokens = ListdID.split("\\s+");

  List<intradoc.data.DataBinder> rsList = new ArrayList<intradoc.data.DataBinder>();  




                                          for ( String st : tokens)
                                                             {
                                                               // traceVerbose("dID is " + st);

                                                                        String selectSQL = "select TARGETDID from pullRIDC where dID="+st+" and TARGETDID is not null" ;    
                                                                              
                                                                                       
                                                                                    intradoc.data.ResultSet targetDocInfo =   runSQLpullRIDCTable(ws,selectSQL );






                                                                               String targetdidNew = getTargetID(targetDocInfo);
                                                                                       if ( targetdidNew.length () == 0 )
                                                                                      {

                                                                                          traceVerbose(st+"Not in target will be replicated");
                                                                                          rsList.add(computetheNativePath(st));

                                                                                        // computetheNativePath(st);
                                                                                       }


                                                                                 else {
                                                                                            traceVerbose(st+" in target as "+targetdidNew +" ..No Replication");
 
                                                                                       }


                                                              }


return rsList;

}


public intradoc.data.DataBinder computetheNativePath ( String dID) throws DataException, intradoc.common.ServiceException ,intradoc.common.ParseStringException

{

 traceVerbose("Inside     computetheNativePath");  

binder.getLocalData().clear();
binder.putLocal("dID", dID);

binder.putLocal("IdcService", "DOC_INFO");
                                          
                                this.executeService(binder,"DOC_INFO");

String filePath = m_service.m_fileUtils.computeRenditionPath(binder,"primaryFile",m_service.m_fileUtils.m_context);

traceVerbose(dID+"  filepath "+filePath);

intradoc.data.DataBinder newbinder,tempbinder = new DataBinder() ;

newbinder = binder;

binder=tempbinder;

newbinder.putLocal("ValtPath", filePath);



return newbinder;

      
}


public void  replicateAndUpdatePullridc(List<intradoc.data.DataBinder> dIDtoreplicate) throws DataException, intradoc.common.ServiceException ,intradoc.common.ParseStringException,java.lang.Exception

{



 traceVerbose("Inside     replicateAndUpdatePullridc");  


 UseRIDC UseRIDCObject = new UseRIDC (ws) ;

 int newcommitstaus=0;

                                                           for(int i=0; i < dIDtoreplicate.size(); i++)

                                              {
                                                       String ValtPath = dIDtoreplicate.get(i).getLocal("ValtPath");


                                                       String ridcpushaction = UseRIDCObject.useRIDCCheckin(dIDtoreplicate.get(i) , ValtPath);

                                                      ridcpushaction=ridcpushaction.replaceAll("'","");

                                                       newcommitstaus=UseRIDCObject.commitstatus;

                                                        if ( newcommitstaus !=4)
                                                             traceVerbose("Failed during RIDC");
                                                        else 
                                                            {

                                                               String dID = dIDtoreplicate.get(i).getLocal("dID");
                                                                String dDocName = dIDtoreplicate.get(i).getLocal("dDocName");

                                                          insertPullridcTable(dID,dDocName,UseRIDCObject.targetdid,ridcpushaction) ;
                                                             } 


                                                }




}  // end of replicateAndUpdatePullridc            


public void insertPullridcTable (String dID,String dDocName ,String targetdid,String ridcpushaction )  throws DataException

{

   long currentTimeMillis =  System.currentTimeMillis();

  int  commitstatus=500;

 String rIdcService="REPLICATE_ONTHEGO_CONTENT";

 String VaultfilePath="";

 String insertSql = "INSERT INTO pullRIDC(rID,dID,dDocName,rEntryDate,rActionDate,rIdcService,VaultfilePath,commitstatus,targetdid) values(rid_seq.nextval,"+dID+",'"+dDocName+"',"+currentTimeMillis+","+currentTimeMillis+",'"+rIdcService+"','"+VaultfilePath+"',"+commitstatus+",'"+targetdid+"')";



		ws.executeSQL(insertSql);




}  



private void executeService(final  intradoc.data.DataBinder  serviceBinder, final String serviceName) throws intradoc.common.ServiceException {
                      // traceVerbose("Start executeService");
                        

                       try {
                              // trace("Calling service " + serviceName + ": " + serviceBinder.getLocalData().toString());
                               // Execute service
                               m_service.getRequestImplementor().executeServiceTopLevelSimple(serviceBinder, serviceName, m_service.getUserData());
                             //  trace("Finished calling service");


                             } catch (final DataException e) {
                                           trace("Something went wrong executing service " + serviceName);
                                          e.printStackTrace(System.out);
                                           throw new intradoc.common.ServiceException("Something went wrong executing service " + serviceName, e);
                            
                               } finally {
                                      //   traceVerbose("End executeService");
                                          }



                      



                  } 

} // end of class 
