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


public class DoPush 

{

intradoc.data.Workspace  ws;
intradoc.data.DataBinder binder;
boolean CommitOnlyReleased;
intradoc.server.Service mservice;

public DoPush(intradoc.data.Workspace  ws, intradoc.data.DataBinder binder,intradoc.server.Service mservice,boolean CommitOnlyReleased  )

{

this.ws = ws ;
this.binder =  binder;
this.mservice = mservice;
this.CommitOnlyReleased = CommitOnlyReleased;

}

public String  doPushCheckin (String RID,String dID,String VaultfilePath,String dDocName,List<String> ReleaseddID)throws intradoc.common.ServiceException ,intradoc.data.DataException

{


                                            
traceVerbose (  "doPushCheckin");
                 

                              if(CommitOnlyReleased)

                                   { 

                                           if ( isThereHigherRevisionEligilbe (ws,dDocName,ReleaseddID))

                                                    {

                                                        traceVerbose("One of the version is eligible and Replicating ! ");

                                                     }
                                            else {
                                                                                                
                                                          if (!verifydID( ReleaseddID,dID)) 
                                                                   
                                                                         {
                                                                                                        

                                                                       traceVerbose("Not Matching  dID " + dID);
                                                                       String  updateSQL= "UPDATE pullRIDC  SET commitstatus=200 WHERE RID="+RID ;
                                                                       insertpullRIDCTable (ws,updateSQL);
                                                                       return "Updated with 200 status";
                                                                           } 
                                                 }  // end of  isThereHigherRevisionEligilbe
                                                         
                                } // end of CommitOnlyReleased if  
                                                                      
                                                                           
                                                                          
                                                                            
                                                                            traceVerbose("Valut path " + VaultfilePath);
                                                                          
                                                                  


            



                               try {

                                    

                String updateSQL= "UPDATE pullRIDC  SET commitstatus=2 WHERE rID="+RID ;

                                     insertpullRIDCTable (ws,updateSQL);


                                      binder.getLocalData().clear();
                                      binder.putLocal("dID", dID);
                                      this.executeService(binder, "DOC_INFO");
                 intradoc.data.ResultSet docInfo = binder.getResultSet("DOC_INFO");


                                                                                      
                      Long currentTimeMillis =   System.currentTimeMillis();

            updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus=3,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;

                               insertpullRIDCTable (ws,updateSQL);

                                       UseRIDC UseRIDCObject = new UseRIDC () ;

                   

                              String ridcpushaction = UseRIDCObject.useRIDCCheckin(docInfo , VaultfilePath);

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

             updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETDID="+targetdid+" WHERE rID="+RID ;

                               insertpullRIDCTable (ws,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception doPushCheckin "+e.getMessage());

                                        e.printStackTrace();
                                     
                                  }








return "Done ";

}  // end of doPushCheckin


public String doPushCheckSEL (String RID,String dID,String VaultfilePath,String dDocName,List<String> ReleaseddID)throws intradoc.common.ServiceException ,intradoc.data.DataException


{

traceVerbose ( " doPushCheckSEL " );
                                            

                                                                                                             if(CommitOnlyReleased)

                                                                                                   {  
                                                                                                        if (!verifydID( ReleaseddID,dID)) 
                                                                                                             {
                                                                                                                           traceVerbose("Not Matching  dID " + dID);
                                                                                                            String  updateSQL= "UPDATE pullRIDC  SET commitstatus=200 WHERE RID="+RID ;
                                                                                                              insertpullRIDCTable (ws,updateSQL);
                                                                                                               return "Not Matching ";


                                                                                                              } 
                                                         
                                                                                                    }



                                                                            String updateSQL= "UPDATE pullRIDC  SET commitstatus=29 WHERE rID="+RID ;

                                                                            insertpullRIDCTable (ws,updateSQL);            
                                                                              
                                            
                                    String selectSQL = "select max (TARGETDID) from pullRIDC where dDocName='"+dDocName+"' and TARGETDID is not null" ;
                             
                                                                                    intradoc.data.ResultSet targetDocInfo =   runSQLpullRIDCTable(ws,selectSQL );

                                                                             
                                                                                             
                                                                                         String targetdidNew = getTargetID(targetDocInfo);

                                                                                            if ( targetdidNew.length() == 0 )
                                                                                       {

                                                                                      traceVerbose("TargetdID is missing Changing to CHECKIN_UNIVERSAL");
                                                                                           
                                                                                            
                                                                                     
                                                                                            doPushCheckin (RID,dID,VaultfilePath,dDocName,ReleaseddID); 
                                                                                            return "TargetdID is missing ";  
                                                                                           

                                                                                       }

                                                                                          else 

                                                                                      {     
                                                                                             traceVerbose("Target did is " + targetdidNew);
                                                                                                                                                                                  

                                                                                      }


            



                               try {

                                          
                                             updateSQL= "UPDATE pullRIDC  SET commitstatus=30 WHERE rID="+RID ;

                                            insertpullRIDCTable (ws,updateSQL);
                                            binder.getLocalData().clear();
                                            binder.putLocal("dID", dID);
                                            this.executeService(binder, "DOC_INFO");
                                            intradoc.data.ResultSet docInfo = binder.getResultSet("DOC_INFO");
                                                                                      
                                  Long currentTimeMillis =   System.currentTimeMillis();

         updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus=31,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;

                               insertpullRIDCTable (ws,updateSQL);

                                    UseRIDC UseRIDCObject = new UseRIDC () ;

                              String ridcpushaction = UseRIDCObject.useRIDCCheckinSEL(docInfo , VaultfilePath,targetdidNew);

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

    updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETDID="+targetdid+" WHERE rID="+RID ;


                               insertpullRIDCTable (ws,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception Caught on doPushCheckSEL call "+e.getMessage());
                                     
                                  }








return "Done";

}  // end of doPushCheckinSEL



public String  doPushCheckout (String RID,String dID) throws intradoc.common.ServiceException ,intradoc.data.DataException

{


traceVerbose ("Doing doPushCheckout" );




                                                                                  if(CommitOnlyReleased)
                                                                           
                                                                                     {    
                                                                                                if (checkPastdID(RID,dID))
                                                                                                    return "Not matching";
 


                                                                                     }
                                                                            


                                                                String         selectSQL = "select TARGETDID from pullRIDC where dID="+dID+" and TARGETDID is not null" ;    
                                                                              
                                                                                       
                                                                                    intradoc.data.ResultSet targetDocInfo =   runSQLpullRIDCTable(ws,selectSQL );

                                                                             
                                                                                             
                                                                                         String targetdidNew = getTargetID(targetDocInfo);

                                                                                            

                                                                                       if ( targetdidNew.length () == 0 )
                                                                                      {

                                                                          String    updateSQL= "UPDATE pullRIDC  SET commitstatus=14 WHERE rID="+RID ;

                                                                                    insertpullRIDCTable (ws,updateSQL); 
                                                                                     traceVerbose ( " null value for targetID ");
                                                                                             return "TargetID is not availble";
                                                                       

                                                                                       }

                                                                                       else 

                                                                                  {

                                                                                    traceVerbose("Checking out with TargetID " + targetdidNew);

                                                                                   

                                                                                  }


                               try {
                                                                                      
                                   


                                                                                      
                      Long currentTimeMillis =   System.currentTimeMillis();

           String   updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus=15,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;

                               insertpullRIDCTable (ws,updateSQL);

                                   UseRIDC UseRIDCObject = new UseRIDC () ;

                              String ridcpushaction = UseRIDCObject.useRIDCForCheckout(targetdidNew);

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

         updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETDID="+targetdid+" WHERE rID="+RID ;
                               insertpullRIDCTable (ws,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception Caught on RIDC call "+e.getMessage());
                                     
                                  }


return "Done";


} // end of doPushCheckout 



public String  doPushUpdateDocinfo (String RID,String dID,List<String> ReleaseddID)  throws intradoc.common.ServiceException ,intradoc.data.DataException

{

traceVerbose ("Doing doPushUpdateDocinfo" );

                                                                                           if(CommitOnlyReleased)
                                                                           
                                                                                     {
                                                                                                 if (!verifydID( ReleaseddID,dID)) 
                                                                                                             {
                                                                                                        
                                                                                                            traceVerbose("Not Matching  dID " + dID);
                                                                                                            String  updateSQL= "UPDATE pullRIDC  SET commitstatus=200 WHERE RID="+RID ;
                                                                                                            insertpullRIDCTable (ws,updateSQL);
                                                                                                            return "Not Matching ";


                                                                                                              } 
                                                         
 


                                                                                     }



                                                    
                                                                          String updateSQL= "UPDATE pullRIDC  SET commitstatus=5 WHERE rID="+RID ;

                                                                                                                                             
                                                                            
                                                                           String selectSQL = "select TARGETDID from pullRIDC where dID="+dID+" and TARGETDID is not null" ;    
                                                                              
                                                                                       
                                                                                    intradoc.data.ResultSet targetDocInfo =   runSQLpullRIDCTable(ws,selectSQL );

                                                                                      // traceResultSet(targetDocInfo);
                                                                                             
                                                                                         String targetdidNew = getTargetID(targetDocInfo);
                                                                                       if ( targetdidNew.length () == 0 )
                                                                                      {
                                                                                                     if (releasedInPastUpdatedNow(dID))
                                                                                                    return "It is eligible now only it will replicate in next cycle";

                                                                                       updateSQL= "UPDATE pullRIDC  SET commitstatus=14 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (ws,updateSQL); 
                                                                                      traceVerbose ( " null value for targetID ");
                                                                                             return "TargetID is not availble";
                                                                       

                                                                                       }

                                                                                       else 

                                                                                  {

                                                                                    traceVerbose("Updating  with TargetID " + targetdidNew);

                                                                                   

                                                                                  }

                                                                                    

            



                               try {
                                                        

                                      updateSQL= "UPDATE pullRIDC  SET commitstatus=6 WHERE rID="+RID ;

                                     insertpullRIDCTable (ws,updateSQL);


                                      binder.getLocalData().clear();
                                      binder.putLocal("dID", dID);
                                      this.executeService(binder, "DOC_INFO");
                                      intradoc.data.ResultSet docInfo = binder.getResultSet("DOC_INFO");      

                        
                                  Long currentTimeMillis =   System.currentTimeMillis();

       updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus=7,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;


                               insertpullRIDCTable (ws,updateSQL);

                                   UseRIDC UseRIDCObject = new UseRIDC () ;

                              String ridcpushaction = UseRIDCObject.useRIDCForMetaUpdate(docInfo,targetdidNew );

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

  updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETDID="+targetdid+" WHERE rID="+RID ;

                               insertpullRIDCTable (ws,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception Caught on RIDC call "+e.getMessage());
                                     
                                  }

return "Done";

}



public String  doPushDelete (String RID,String dID)  throws intradoc.common.ServiceException ,intradoc.data.DataException

{

traceVerbose ("Doing doPushDelete" );


                                                                                               if(CommitOnlyReleased)
                                                                            
                                                                                     {
                                                                                                if (checkPastdID(RID,dID))
                                                                                                    return "200 status found ";
 


                                                                                     }



                                                                       String updateSQL= "UPDATE pullRIDC  SET commitstatus=61 WHERE rID="+RID ;

                                                                       insertpullRIDCTable (ws,updateSQL);                                                                  


                                                                                                                                                                                                                      

                                                                       String selectSQL = "select TARGETDID from pullRIDC where dID="+dID+" and TARGETDID is not null" ;    
                                                                            
                                                                                     
                                                                       intradoc.data.ResultSet targetDocInfo =   runSQLpullRIDCTable(ws,selectSQL );

                                                                       
                                                                                             
                                                                         String targetdidNew = getTargetID(targetDocInfo);

                                                                                  if ( targetdidNew.length() == 0 )
                                                                                      {

                                                                           updateSQL= "UPDATE pullRIDC  SET commitstatus=62 WHERE rID="+RID ;

                                                                           insertpullRIDCTable (ws,updateSQL); 
                                                                           return  " null value for targetID ";
                                                                       

                                                                                       }

                                                                             else {



                                                                                      traceVerbose("Deleting  TargetID " + targetdidNew);

                                                                                    

                                                                                   }



 try {
                                                        

     

                        
                                  Long currentTimeMillis =   System.currentTimeMillis();

        updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus=63,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;


                               insertpullRIDCTable (ws,updateSQL);

                                  UseRIDC UseRIDCObject = new UseRIDC () ;

                              String ridcpushaction = UseRIDCObject.useRIDCForDelete(targetdidNew );

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

  updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETDID="+targetdid+" WHERE rID="+RID ;

                               insertpullRIDCTable (ws,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception doPushDelete "+e.getStackTrace());
                                     
          }


return "Done";

}  // end of doPushDelete






public String  doPushDeleteDoc (String RID,String dDocName)  throws intradoc.common.ServiceException ,intradoc.data.DataException


{

traceVerbose("in doPushDeleteDoc");





                                                                                           if(CommitOnlyReleased)
                                                                           
                                                                                     {
                                                                                                if (checkPastdDocName(RID,dDocName))
                                                                                                    return "Content in 200 found";
 


                                                                                     }


                                                                               

                                                                       String updateSQL= "UPDATE pullRIDC  SET commitstatus=61 WHERE rID="+RID ;

                                                                       insertpullRIDCTable (ws,updateSQL); 

 try {
                                                        

     

                        
                                  Long currentTimeMillis =   System.currentTimeMillis();

        updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus=63,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;


                               insertpullRIDCTable (ws,updateSQL);

                                  UseRIDC UseRIDCObject = new UseRIDC () ;

                              String ridcpushaction = UseRIDCObject.useRIDCForDeleteDoc(dDocName );

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

  updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',dDocName='"+dDocName+"' WHERE rID="+RID ;

                               insertpullRIDCTable (ws,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception doPushDeleteDoc "+e.getStackTrace());
                                           e.printStackTrace();
                                     
          }

return "Done";

} // end of doPushDeleteDoc



public boolean   checkPastdID (String RID,String dID) throws intradoc.data.DataException ,intradoc.common.ServiceException

{

  boolean check = false ;                                                 


                                                    

String    selectSQL = "select rid from pullridc where commitstatus IN (200,100) AND ridcservice IN('CHECKIN_NEW','CHECKIN_UNIVERSAL','CHECKIN_SEL') AND  dID="+dID;
 
                                                                intradoc.data.ResultSet rs200status  =  runSQLpullRIDCTable(ws,selectSQL );

                                                                                      if (verifyRow(rs200status))
                                                                                          {

                                                                                    String updateSQL= "UPDATE pullRIDC  SET commitstatus=200 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (ws,updateSQL);
                                                                                       check = true ; 

                                                                                          }


                                                                              
                                                                              selectSQL = "select rid from pullridc where commitstatus=1 AND did="+dID;
   
                                                                              intradoc.data.ResultSet rs1status  =  runSQLpullRIDCTable(ws,selectSQL );


                                                                                         if (verifyRow(rs1status))
                                                                                                check = true ; 



return check;


} // end of check check PastdID 


public boolean   checkPastdDocName (String RID,String dDocName) throws intradoc.data.DataException ,intradoc.common.ServiceException

{

  boolean check = false ;                                                 


String    selectSQL = "select rid from pullridc where commitstatus IN (200,100) AND ridcservice IN('CHECKIN_NEW','CHECKIN_UNIVERSAL','CHECKIN_SEL') AND  dDocName='"+dDocName+"'";
 
                                                                intradoc.data.ResultSet rs200status  =  runSQLpullRIDCTable(ws,selectSQL );

                                                                                      if (verifyRow(rs200status))
                                                                                          {

                                                                                    String updateSQL= "UPDATE pullRIDC  SET commitstatus=200 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (ws,updateSQL);
                                                                                       check = true ; 

                                                                                          }


                                                                              
                                                                              selectSQL = "select rid from pullridc where commitstatus=1 AND dDocName='"+dDocName+"'";
   
                                                                              intradoc.data.ResultSet rs1status  =  runSQLpullRIDCTable(ws,selectSQL );


                                                                                         if (verifyRow(rs1status))
                                                                                                check = true ; 



return check;


} // end of checkPastdDocName

public boolean releasedInPastUpdatedNow ( String dID ) throws intradoc.data.DataException ,intradoc.common.ServiceException

{

boolean check = false ;       
     
String    selectSQL = "select rid from pullridc where commitstatus IN (200,100) AND ridcservice IN('CHECKIN_NEW','CHECKIN_UNIVERSAL','CHECKIN_SEL') AND  dID="+dID;
                                           

                                         intradoc.data.ResultSet newRS  =  runSQLpullRIDCTable(ws,selectSQL );

                                              for(newRS.first(); newRS.isRowPresent(); newRS.next())

                                          {
                                                   String rid = newRS.getStringValueByName("RID");
                                                          traceVerbose("rid to update "+rid);

                                                    String updateSQL= "UPDATE pullRIDC  SET commitstatus=0 WHERE rID="+rid ;

                                                                                      insertpullRIDCTable (ws,updateSQL);
                                                                                       check = true ; 
                                                                                       return check;
                                               

                        
                                          }






return check ;



} // end of releasedInPastUpdatedNow
 


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




} // end of Class
