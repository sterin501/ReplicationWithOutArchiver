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


/**
 * Please check the read me of the component 
 * it is using Utils and UseRIDC class
 * written by Sterin Jacob
 */
public class ToGETPushRIDC extends ServiceHandler
{


boolean CommitOnlyReleased=false;
long   timetoCheckMillis;
Workspace  ws;


//List<String> ReleasedContent = new ArrayList<String>(); 



	public void toGetPushRIDCMain() throws DataException, intradoc.common.ServiceException ,intradoc.common.ParseStringException
	{
		    traceVerbose("Inside the filter toGetPushRIDCMain");







                     
// To check the date format , it can change from RIDC call and browser call
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





                       
                                     long  currentTimeMillis =   System.currentTimeMillis();


                                    
 
                                      int commitstatusFromBinder=0;


                                        // This if to set doCheckinMin to default value 5 
                                     if (m_binder.getLocal("doCheckinMin")!=null )
                                        timetoCheckMillis =  Long.parseLong(m_binder.getLocal("doCheckinMin")) * 60000; 
                                   
 
                                     else 
                                        timetoCheckMillis = 300000 ;





                                    timetoCheckMillis = currentTimeMillis -timetoCheckMillis; 

          

            String selectSQL = "select RID,dID,RIDCSERVICE,VAULTFILEPATH,dDocName from pullRIDC where Rentrydate >"+ timetoCheckMillis +"AND commitstatus=0 ORDER BY Rid asc" ;


                                       
                                       
                                                  // this if to set commit status to default value 
                                        if (m_binder.getLocal("CommitStatus")!=null )

                                             {

                                                       commitstatusFromBinder=Integer.parseInt(m_binder.getLocal("CommitStatus"));

 selectSQL = "select RID,dID,RIDCSERVICE,VAULTFILEPATH,dDocName from pullRIDC where Rentrydate >"+ timetoCheckMillis +"AND commitstatus="+commitstatusFromBinder+"  ORDER BY Rid asc" ;

                                             }

                                        

                                                    // this if to set SelectSQL for all the content with out entrydate 
                                                      if (m_binder.getLocal("doCheckAll")!=null )
                                                        
                                                      {


 selectSQL = "select RID,dID,RIDCSERVICE,VAULTFILEPATH,dDocName from pullRIDC where  commitstatus="+commitstatusFromBinder+" ORDER BY Rid asc" ;
                                                          
                                                      timetoCheckMillis=0;

                                                      }



                                         // All Binder Values are assigned , so that they call any Services ;
                                                               if ( SharedObjects.getEnvironmentValue("CommitOnlyReleased").equals("true"))
                        
                                                                                          {     
                                                                                               CommitOnlyReleased=true;
                                                                                               scanTablewithRelease();


                                                                                           }                                        
                                                     // Before fun begins need to clear all binder
                                       m_binder.getLocalData().clear();

                                      // Calling SQL statment

                                            ws =  m_workspace;

                            intradoc.data.ResultSet rsSQLpullRIDCTable  =  runSQLpullRIDCTable(ws,selectSQL );
                                           
                                             

                                           if ( verifyRow(rsSQLpullRIDCTable))
                                      
                                       {
                                           
                                             //toGetdIDforPush(createListResultSet  (rsSQLpullRIDCTable ));
                                                List<String> listdID  = createListResultSet  (rsSQLpullRIDCTable );
                                            
                                                List<String> ReleaseddID = new ArrayList<String>();  
                                       
                                                               if(CommitOnlyReleased)
                                                        ReleaseddID =  toCreateSearchList();

                                                       toGetdIDforPush(listdID,ReleaseddID);
                                        }

                      
                 
                  

                       


         } // end of toGetPushRIDCMain


public   List toCreateSearchList ()  throws intradoc.common.ServiceException  

{

List<String> ReleaseddID = new ArrayList<String>();  

String PRquery=SharedObjects.getEnvironmentValue("PRQuery");


PRquery=appendDate(PRquery,timetoCheckMillis);




  

        m_binder.getLocalData().clear();
        m_binder.putLocal("QueryText", PRquery);
        m_binder.putLocal("IdcService", "GET_SEARCH_RESULTS");
        this.executeService(m_binder, "GET_SEARCH_RESULTS");
        intradoc.data.ResultSet  SearchResults = m_binder.getResultSet("SearchResults");


       ReleaseddID=afterSearchList(SearchResults);

traceVerbose(ReleaseddID.toString());

return ReleaseddID;



} // end of toCreateSearchList


public   List createListResultSet (final intradoc.data.ResultSet rs)   

{

List<String> rsList = new ArrayList<String>();  



for(rs.first(); rs.isRowPresent(); rs.next())
  


   {


/* HashMap <String,String> map = new HashMap()
map.put("rid",value)
map.put("did",value)

*/




String dID =rs.getStringValueByName("dID");
String RID =rs.getStringValueByName("RID");
String RIDCSERVICE =rs.getStringValueByName("RIDCSERVICE");
String VAULTFILEPATH = rs.getStringValueByName("VAULTFILEPATH");
String dDocName      = rs.getStringValueByName("dDocName");


if (VAULTFILEPATH == null)
VAULTFILEPATH="0";

if (dDocName == null)
dDocName="0";

rsList.add(RID+"\t"+dID+"\t"+RIDCSERVICE+"\t"+VAULTFILEPATH+"\t"+dDocName);

   }

return rsList;


} // end of createListResultSet



private void toGetdIDforPush ( List<String>  rsSQLList ,  List<String> ReleaseddID )  throws intradoc.common.ServiceException ,intradoc.data.DataException


            {

traceVerbose("toGetdIDforPush");
                                       for(int i=0; i < rsSQLList.size(); i++) 

                                                      { 
 
                                                
                                                          String [] tokens = rsSQLList.get(i).split("\\t+");
                                                          String   RID  = tokens[0];
                                                          
                                                          String   dID  = tokens[1];

                                                          String   idcService = tokens[2] ;



                                                                 if ( idcService.equals ("CHECKIN_UNIVERSAL")|| idcService.equals ("CHECKIN_NEW"))
                                                                   
                                                                     {    
                                                                      
                                                                         String  VaultfilePath =  tokens[3];             
                                                                         String   dDocName  = tokens[4];
                                                                          traceVerbose (dID  + "  " + idcService);
                                                                          traceVerbose ("Staus of doPushCheckin "+  doPushCheckin(RID,dID,VaultfilePath,dDocName,ReleaseddID));
                                                                                    
  
                                                                     }

                                                                else  if ( idcService.equals ("CHECKOUT_OK"))
                                                                           {

                                                                           traceVerbose (dID  + "  " + idcService);

                                                                           traceVerbose ("Staus of doPushCheckout "+ doPushCheckout(RID,dID));


                                                                           }
                                                                else  if ( idcService.equals ("CHECKIN_SEL"))
                                                                           {

                                                                          traceVerbose (dID  + "  " + idcService);
                                                                           String  dDocName =  tokens[4];
                                                                            String  VaultfilePath =  tokens[3];
                                                                        traceVerbose ("Staus of doPushCheckSEL "+ doPushCheckSEL(RID,dID,VaultfilePath,dDocName,ReleaseddID));


 
                                                                           }

                                                                else  if ( idcService.contains ("UPDATE_DOCINFO"))
                                                                           {

                                                                           traceVerbose (dID  + "  " + idcService);

                                                                       traceVerbose ("Staus of doPushUpdateDocinfo "+     doPushUpdateDocinfo(RID,dID,ReleaseddID));

                                                                                
                                                                                   
                                                                           

                                                                           }

                                                                  else  if ( idcService.contains ("DELETE_REV"))
                                                                            {

                                                                          
                                                                         traceVerbose (dID  + "  " + idcService);

                                                                          traceVerbose ("Staus of doPushDelete "+        doPushDelete(RID,dID));
                                                                      
                                                                           }
                                                                      

                                                                 else  if ( idcService.equals  ("DELETE_DOC"))
                                                                             


                                                                           {

                                                                          String  dDocName =  tokens[4];
                                     
                                                                         traceVerbose (dDocName  + "  " + idcService);

                                                                            traceVerbose ("Staus of doPushDeleteDoc "+   doPushDeleteDoc(RID,dDocName));


                                                                                                                                                  
                                                                                  


                                                                           }
                                   
                                          } // end of For 1:


         
     


            }  // end of toGetdIDforPush




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
                                                                       insertpullRIDCTable (m_workspace,updateSQL);
                                                                       return "Updated with 200 status";
                                                                           } 
                                                 }  // end of  isThereHigherRevisionEligilbe
                                                         
                                } // end of CommitOnlyReleased if  
                                                                      
                                                                           
                                                                          
                                                                            
                                                                            traceVerbose("Valut path " + VaultfilePath);
                                                                          
                                                                  


            



                               try {

                                    

                String updateSQL= "UPDATE pullRIDC  SET commitstatus=2 WHERE rID="+RID ;

                                     insertpullRIDCTable (ws,updateSQL);


                                      m_binder.getLocalData().clear();
                                      m_binder.putLocal("dID", dID);
                                      this.executeService(m_binder, "DOC_INFO");
                 intradoc.data.ResultSet docInfo = m_binder.getResultSet("DOC_INFO");


                                                                                      
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
                             
                                                                                    intradoc.data.ResultSet targetDocInfo =   runSQLpullRIDCTable(m_workspace,selectSQL );

                                                                             
                                                                                             
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
                                            m_binder.getLocalData().clear();
                                            m_binder.putLocal("dID", dID);
                                            this.executeService(m_binder, "DOC_INFO");
                                            intradoc.data.ResultSet docInfo = m_binder.getResultSet("DOC_INFO");
                                                                                      
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


                                      m_binder.getLocalData().clear();
                                      m_binder.putLocal("dID", dID);
                                      this.executeService(m_binder, "DOC_INFO");
                                      intradoc.data.ResultSet docInfo = m_binder.getResultSet("DOC_INFO");      

                        
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




private void executeService(final  intradoc.data.DataBinder  serviceBinder, final String serviceName) throws intradoc.common.ServiceException {
                       traceVerbose("Start executeService");
                        

                       try {
                               trace("Calling service " + serviceName + ": " + serviceBinder.getLocalData().toString());
                               // Execute service
                               m_service.getRequestImplementor().executeServiceTopLevelSimple(serviceBinder, serviceName, m_service.getUserData());
                               trace("Finished calling service");


                             } catch (final DataException e) {
                                           trace("Something went wrong executing service " + serviceName);
                                          e.printStackTrace(System.out);
                                           throw new intradoc.common.ServiceException("Something went wrong executing service " + serviceName, e);
                            
                               } finally {
                                         traceVerbose("End executeService");
                                          }



                      



                  }  // end of executeService



private void scanTablewithRelease () throws DataException, intradoc.common.ServiceException

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


public boolean   checkPastdID (String RID,String dID) throws intradoc.data.DataException ,intradoc.common.ServiceException

{

  boolean check = false ;                                                 


                                                    

String    selectSQL = "select rid from pullridc where commitstatus IN (200,100) AND ridcservice IN('CHECKIN_NEW','CHECKIN_UNIVERSAL','CHECKIN_SEL') AND  dID="+dID;
 
                                                                intradoc.data.ResultSet rs200status  =  runSQLpullRIDCTable(m_workspace,selectSQL );

                                                                                      if (verifyRow(rs200status))
                                                                                          {

                                                                                    String updateSQL= "UPDATE pullRIDC  SET commitstatus=200 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (m_workspace,updateSQL);
                                                                                       check = true ; 

                                                                                          }


                                                                              
                                                                              selectSQL = "select rid from pullridc where commitstatus=1 AND did="+dID;
   
                                                                              intradoc.data.ResultSet rs1status  =  runSQLpullRIDCTable(m_workspace,selectSQL );


                                                                                         if (verifyRow(rs1status))
                                                                                                check = true ; 



return check;


} // end of check check PastdID 


public boolean   checkPastdDocName (String RID,String dDocName) throws intradoc.data.DataException ,intradoc.common.ServiceException

{

  boolean check = false ;                                                 


String    selectSQL = "select rid from pullridc where commitstatus IN (200,100) AND ridcservice IN('CHECKIN_NEW','CHECKIN_UNIVERSAL','CHECKIN_SEL') AND  dDocName='"+dDocName+"'";
 
                                                                intradoc.data.ResultSet rs200status  =  runSQLpullRIDCTable(m_workspace,selectSQL );

                                                                                      if (verifyRow(rs200status))
                                                                                          {

                                                                                    String updateSQL= "UPDATE pullRIDC  SET commitstatus=200 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (m_workspace,updateSQL);
                                                                                       check = true ; 

                                                                                          }


                                                                              
                                                                              selectSQL = "select rid from pullridc where commitstatus=1 AND dDocName='"+dDocName+"'";
   
                                                                              intradoc.data.ResultSet rs1status  =  runSQLpullRIDCTable(m_workspace,selectSQL );


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
 

} //end of cLass 
