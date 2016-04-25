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
public class ReplicateOnthegoContent extends ServiceHandler
{


intradoc.data.Workspace  ws;
intradoc.data.DataBinder binder;
intradoc.server.Service mservice;




	public void ReplicateOnthegoContentHandler() throws DataException, intradoc.common.ServiceException ,intradoc.common.ParseStringException
	


        {
	
         traceVerbose("Inside     ReplicateOnthegoContentHandler");  

         
            binder = m_binder;
            ws=m_workspace;
            mservice=m_service;


         String Rquery = binder.getLocal("Rquery");

         String  ListdID = binder.getLocal("ListdID");
 
         String  ListdDocName = binder.getLocal("ListdDocName");

         traceVerbose("Rquery is "+ Rquery); 

         String idctoken="";

                                                          if (binder.getLocal("idcToken")!=null )
                                                        idctoken=(binder.getLocal("idcToken"));


                                if (Rquery !=null)

                     {
                               binder.getLocalData().clear();
                               binder.putLocal("QueryText",Rquery);
                               binder.putLocal("IdcService", "GET_SEARCH_RESULTS");
                               binder.putLocal("SearchQueryFormat" ,"Universal");
                               binder.putLocal("ResultCount", "200");
                                          
                                this.executeService(binder, "GET_SEARCH_RESULTS");
                                  intradoc.data.ResultSet  SearchResults = binder.getResultSet("SearchResults");
   
                       

                                           DataResultSet myDataResultSet = new DataResultSet();

                                         myDataResultSet.copy(SearchResults);


                                             // traceResultSet(myDataResultSet);
                                           
                                                 binder.addResultSet("ResultSetRIDC", myDataResultSet);
                                                  binder.putLocal("ShowResult","yes");


                     }


                        if (ListdID !=null)

                     {


                      

                         if ( binder.getLocal("UserDateFormat") !=null &&  binder.getLocal("UserDateFormat").equals("iso8601"))

                            {
         
                           traceVerbose("Calling from RIDC no need of Date  Conversion");

                             Isiso8601=true;   // this is defined in Utils.java 

                             }

                          else {

                                 idf = binder.m_blDateFormat;  // sending dateformat to Utils.java 
                             Isiso8601=false;
                             traceVerbose("Calling from Browser , Date conversion is required ");


                              }



                      traceVerbose("ListdID is "+ListdID);
                      traceVerbose ("ListdDocName is "+ListdDocName);

                List<intradoc.data.DataBinder>  dIDtoreplicate = verifyTheTargetIDinPullridc(ListdID);
                        
                   try {


                         replicateAndUpdatePullridc(dIDtoreplicate);



                       }


                       catch ( Exception e ) {

                                         traceVerbose("Exception replicateAndUpdatePullridc "+e.getMessage());

                                    e.printStackTrace();
                                             }

                  

                  }  // end of ListdID

                                     





                                                              if(idctoken.length() > 2)
                                                          binder.putLocal("idcToken",idctoken);   


                                                        
  

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

String filePath = mservice.m_fileUtils.computeRenditionPath(binder,"primaryFile",mservice.m_fileUtils.m_context);

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



}
