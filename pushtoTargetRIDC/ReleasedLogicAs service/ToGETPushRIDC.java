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
 * This is the code that can be executed by any Idc service that has a 
 * service type of "AcmeMail.AcmeMailService".
 */
public class ToGETPushRIDC extends ServiceHandler
{


boolean CommitOnlyReleased=false;




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


                                      long   timetoCheckMillis;
 
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
                selectSQL = "select RID,dID,RIDCSERVICE,VAULTFILEPATH,dDocName from pullRIDC where  commitstatus="+commitstatusFromBinder+" ORDER BY Rid asc" ;
                                        



                                         // All Binder Values are assigned , so that they call any Services ;
                                                               if ( SharedObjects.getEnvironmentValue("CommitOnlyReleased").equals("true"))
                        
                                                                                          {     
                                                                                               CommitOnlyReleased=true;
                                                                                               scanTablewithRelease();


                                                                                           }                                        




                                      

                            intradoc.data.ResultSet rsSQLpullRIDCTable  =  runSQLpullRIDCTable(m_workspace,selectSQL );
                                           
                                             

                                           if ( rsSQLpullRIDCTable.hasRawObjects())
                                      
                                       {
                                           
                                             //toGetdIDforPush(createListResultSet  (rsSQLpullRIDCTable ));

                                               List<String> listdID  = new ArrayList<String>();  
                                                            listdID = createListResultSet  (rsSQLpullRIDCTable );
                                                 


                                             List<String> afterSearch = new ArrayList<String>();  
                                       
                                                               if(CommitOnlyReleased)
                                                   afterSearch =  toCreateSearchList(timetoCheckMillis );


                                                
                                                       toGetdIDforPush(listdID,afterSearch);

                                        }

                      
                 
                  

                       


         } // end of toGetPushRIDCMain


public   List toCreateSearchList (long timetoCheckMillis)  throws intradoc.common.ServiceException  

{

List<String> afterSearch = new ArrayList<String>();  

String PRquery=SharedObjects.getEnvironmentValue("PRQuery");


PRquery=appendDate(PRquery,timetoCheckMillis);




  

        m_binder.getLocalData().clear();
        m_binder.putLocal("QueryText", PRquery);
        m_binder.putLocal("IdcService", "GET_SEARCH_RESULTS");
        this.executeService(m_binder, "GET_SEARCH_RESULTS");
        intradoc.data.ResultSet  SearchResults = m_binder.getResultSet("SearchResults");


       afterSearch=afterSearchList(SearchResults);

traceVerbose(afterSearch.toString());

return afterSearch;



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


//private void toGetdIDforPush(intradoc.data.ResultSet rsPullRIDCTable) throws intradoc.common.ServiceException ,intradoc.data.DataException

private void toGetdIDforPush ( List<String>  rsSQLList ,  List<String> afterSearch )  throws intradoc.common.ServiceException ,intradoc.data.DataException


            {

traceVerbose("toGetdIDforPush");
                                       for(int i=0; i < rsSQLList.size(); i++) 

                                                      { 
 
                                                
                                                          String [] tokens = rsSQLList.get(i).split("\\t+");
                                                          String   RID  = tokens[0];
                                                          
                                                          String   dID  = tokens[1];

                                                            
                                                             // verify Released status content 
                                                           if(CommitOnlyReleased)

                                                            {  
                                                                      if (!verifydID( afterSearch,dID)) 
                                                               {
                                                                        traceVerbose("Not Matching  dID " + dID);
                                                                        String  updateSQL= "UPDATE pullRIDC  SET commitstatus=200 WHERE RID="+RID ;
                                                                        insertpullRIDCTable (m_workspace,updateSQL);
                                                                        continue;


                                                               } 
                                                         
                                                            }
                                                              

                                                              
                                                              
                      

                                                   
                                                     String   idcService = tokens[2] ;

                                                                 if ( idcService.equals ("CHECKIN_UNIVERSAL")|| idcService.equals ("CHECKIN_NEW"))
                                                                    {    
                                                                              
                                                                           String  VaultfilePath =  tokens[3];
                                                                           traceVerbose (dID  + "  " + idcService);
                                                                            
                                                                            traceVerbose("Valut path " + VaultfilePath);
                                                                          
                                                                           doPushCheckin (dID,RID,VaultfilePath);   

                                                                                    
  
                                                                     } // end of IF checkin

                                                  
                                                                else  if ( idcService.equals ("CHECKOUT_OK"))
                                                                           {

                                                                           traceVerbose (dID  + "  " + idcService);

                                                                                    String updateSQL= "UPDATE pullRIDC  SET commitstatus=13 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (m_workspace,updateSQL);
                                                                        
                                                                            


                                                                        String selectSQL = "select TARGETDID from pullRIDC where dID="+dID+" and TARGETDID is not null" ;    
                                                                              
                                                                                       
                                                                                    intradoc.data.ResultSet targetDocInfo =   runSQLpullRIDCTable(m_workspace,selectSQL );

                                                                             
                                                                                             
                                                                                         String targetdidNew = getTargetID(targetDocInfo);

                                                                                            

                                                                                       if ( targetdidNew.length () == 0 )
                                                                                      {

                                                                                     updateSQL= "UPDATE pullRIDC  SET commitstatus=14 WHERE rID="+RID ;

                                                                                    insertpullRIDCTable (m_workspace,updateSQL); 
                                                                                     traceVerbose ( " null value for targetID ");
                                                                       

                                                                                       }

                                                                                       else 

                                                                                  {

                                                                                    traceVerbose("Checking out with TargetID " + targetdidNew);

                                                                                    doPushCheckout(dID,RID,targetdidNew);

                                                                                  }


                                                                           }
                                                                else  if ( idcService.equals ("CHECKIN_SEL"))
                                                                           {


                                                                           traceVerbose (dID  + "  " + idcService);
                                                                            
                                                                           String  dDocName =  tokens[4];

                                                                            String updateSQL= "UPDATE pullRIDC  SET commitstatus=29 WHERE rID="+RID ;

                                                                            insertpullRIDCTable (m_workspace,updateSQL);            
                                                                              
                                            
                                    String selectSQL = "select max (TARGETDID) from pullRIDC where dDocName='"+dDocName+"' and TARGETDID is not null" ;
                             
                                                                                    intradoc.data.ResultSet targetDocInfo =   runSQLpullRIDCTable(m_workspace,selectSQL );

                                                                             
                                                                                             
                                                                                         String targetdidNew = getTargetID(targetDocInfo);

                                                                                            if ( targetdidNew.length() == 0 )
                                                                                       {

                                                                                      traceVerbose("TargetdID is missing Changing to CHECKIN_UNIVERSAL");
                                                                                           
                                                                                            idcService="CHECKIN_UNIVERSAL";
                                                                                            String  VaultfilePath =  tokens[3];
                                                                                            doPushCheckin (dID,RID,VaultfilePath);   
                                                                                           

                                                                                       }

                                                                                          else 

                                                                                      {     
                                                                                             traceVerbose("Target did is " + targetdidNew);

                                                                                             String  VaultfilePath =  tokens[3];
                                                                                             traceVerbose (dID  + "  " + idcService);

                                                                                             doPushCheckSEL(dID,RID,VaultfilePath,targetdidNew);

                                                                                      }


                                                                           }

                                                                else  if ( idcService.contains ("UPDATE_DOCINFO"))
                                                                           {

                                                                           traceVerbose (dID  + "  " + idcService);
                                                    
                                                                          String updateSQL= "UPDATE pullRIDC  SET commitstatus=5 WHERE rID="+RID ;

                                                                            insertpullRIDCTable (m_workspace,updateSQL);   
                                                                   
                                                                            
                                                                           String selectSQL = "select TARGETDID from pullRIDC where dID="+dID+" and TARGETDID is not null" ;    
                                                                              
                                                                                       
                                                                                    intradoc.data.ResultSet targetDocInfo =   runSQLpullRIDCTable(m_workspace,selectSQL );

                                                                                      // traceResultSet(targetDocInfo);
                                                                                             
                                                                                         String targetdidNew = getTargetID(targetDocInfo);
                                                                                       

                                                                                    doPushUpdateDocinfo(dID,RID,targetdidNew);
                                                                                   
                                                                           

                                                                           }

                                                                  else  if ( idcService.contains ("DELETE_REV"))
                                                                            {

                                                                          
                                                                         traceVerbose (dID  + "  " + idcService);

                                                                       String updateSQL= "UPDATE pullRIDC  SET commitstatus=61 WHERE rID="+RID ;

                                                                       insertpullRIDCTable (m_workspace,updateSQL);                                                                  


                                                                                                                                                                                                                      

                                                                       String selectSQL = "select TARGETDID from pullRIDC where dID="+dID+" and TARGETDID is not null" ;    
                                                                            
                                                                                     
                                                                       intradoc.data.ResultSet targetDocInfo =   runSQLpullRIDCTable(m_workspace,selectSQL );

                                                                       
                                                                                             
                                                                         String targetdidNew = getTargetID(targetDocInfo);

                                                                                  if ( targetdidNew.length() == 0 )
                                                                                      {

                                                                        updateSQL= "UPDATE pullRIDC  SET commitstatus=62 WHERE rID="+RID ;

                                                                       insertpullRIDCTable (m_workspace,updateSQL); 
                                                                           traceVerbose ( " null value for targetID ");
                                                                       

                                                                                       }

                                                                             else {



                                                                                      traceVerbose("Deleting  TargetID " + targetdidNew);

                                                                                    doPushDelete(dID,RID,targetdidNew);

                                                                                   }

                                                                           }
      
                                     
                                        //  traceVerbose ("Going to Next Row of toGetdIDforPush  ---");           
                                   
                                          } // end of For 1:


         
     


            }  // end of toGetdIDforPush




public void doPushCheckin (String dID,String RID,String VaultfilePath)  throws intradoc.common.ServiceException


{


                                            
traceVerbose (  "doPushCheckin");
                 

            



                               try {

                                    

                String updateSQL= "UPDATE pullRIDC  SET commitstatus=2 WHERE rID="+RID ;

                                     insertpullRIDCTable (m_workspace,updateSQL);


                                      m_binder.getLocalData().clear();
                                      m_binder.putLocal("dID", dID);
                                      this.executeService(m_binder, "DOC_INFO");
                 intradoc.data.ResultSet docInfo = m_binder.getResultSet("DOC_INFO");


                                                                                      
                      Long currentTimeMillis =   System.currentTimeMillis();

            updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus=3,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;

                               insertpullRIDCTable (m_workspace,updateSQL);

                                       UseRIDC UseRIDCObject = new UseRIDC () ;

                   

                              String ridcpushaction = UseRIDCObject.useRIDCCheckin(docInfo , VaultfilePath);

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

             updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETDID="+targetdid+" WHERE rID="+RID ;

                               insertpullRIDCTable (m_workspace,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception doPushCheckin "+e.getMessage());

                                        e.printStackTrace();
                                     
                                  }










}  // end of doPushCheckin



public void doPushCheckSEL (String dID,String RID,String VaultfilePath,String targetdidNew)  throws intradoc.common.ServiceException


{

traceVerbose ( " doPushCheckSEL " );
                                            


            



                               try {

                                          
                                            String updateSQL= "UPDATE pullRIDC  SET commitstatus=30 WHERE rID="+RID ;

                                            insertpullRIDCTable (m_workspace,updateSQL);
                                            m_binder.getLocalData().clear();
                                            m_binder.putLocal("dID", dID);
                                            this.executeService(m_binder, "DOC_INFO");
                                            intradoc.data.ResultSet docInfo = m_binder.getResultSet("DOC_INFO");
                                                                                      
                                  Long currentTimeMillis =   System.currentTimeMillis();

         updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus=31,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;

                               insertpullRIDCTable (m_workspace,updateSQL);

                                    UseRIDC UseRIDCObject = new UseRIDC () ;

                              String ridcpushaction = UseRIDCObject.useRIDCCheckinSEL(docInfo , VaultfilePath,targetdidNew);

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

    updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETDID="+targetdid+" WHERE rID="+RID ;


                               insertpullRIDCTable (m_workspace,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception Caught on doPushCheckSEL call "+e.getMessage());
                                     
                                  }










}  // end of doPushCheckinSEL



public void doPushCheckout (String dID,String RID,String targetdidNew)  throws intradoc.common.ServiceException

{


traceVerbose ("Doing doPushCheckout" );


                               try {
                                                                                      
                                   


                                                                                      
                      Long currentTimeMillis =   System.currentTimeMillis();

           String   updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus=15,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;

                               insertpullRIDCTable (m_workspace,updateSQL);

                                   UseRIDC UseRIDCObject = new UseRIDC () ;

                              String ridcpushaction = UseRIDCObject.useRIDCForCheckout(targetdidNew);

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

           updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETDID="+targetdid+" WHERE rID="+RID ;
                               insertpullRIDCTable (m_workspace,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception Caught on RIDC call "+e.getMessage());
                                     
                                  }


} // end of doPushCheckout 



public void doPushUpdateDocinfo (String dID,String RID,String targetdidNew)  throws intradoc.common.ServiceException

{

traceVerbose ("Doing doPushUpdateDocinfo" );



            



                               try {
                                                        

                                     String updateSQL= "UPDATE pullRIDC  SET commitstatus=6 WHERE rID="+RID ;

                                     insertpullRIDCTable (m_workspace,updateSQL);


                                      m_binder.getLocalData().clear();
                                      m_binder.putLocal("dID", dID);
                                      this.executeService(m_binder, "DOC_INFO");
                                      intradoc.data.ResultSet docInfo = m_binder.getResultSet("DOC_INFO");      

                        
                                  Long currentTimeMillis =   System.currentTimeMillis();

       updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus=7,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;


                               insertpullRIDCTable (m_workspace,updateSQL);

                                   UseRIDC UseRIDCObject = new UseRIDC () ;

                              String ridcpushaction = UseRIDCObject.useRIDCForMetaUpdate(docInfo,targetdidNew );

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

  updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETDID="+targetdid+" WHERE rID="+RID ;

                               insertpullRIDCTable (m_workspace,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception Caught on RIDC call "+e.getMessage());
                                     
                                  }



}



public void doPushDelete (String dID,String RID,String targetdidNew)  throws intradoc.common.ServiceException

{

traceVerbose ("Doing doPushDelete" );


 try {
                                                        

     

                        
                                  Long currentTimeMillis =   System.currentTimeMillis();

   String     updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus=63,RIDCPUSHACTION='Pushing' WHERE rID="+RID ;


                               insertpullRIDCTable (m_workspace,updateSQL);

                                  UseRIDC UseRIDCObject = new UseRIDC () ;

                              String ridcpushaction = UseRIDCObject.useRIDCForDelete(targetdidNew );

                            ridcpushaction=ridcpushaction.replaceAll("'","");

                            currentTimeMillis =   System.currentTimeMillis();

  updateSQL= "UPDATE pullRIDC  SET rActionDate="+currentTimeMillis+",commitstatus="+commitstatus+",RIDCPUSHACTION='"+ridcpushaction+"',TARGETDID="+targetdid+" WHERE rID="+RID ;

                               insertpullRIDCTable (m_workspace,updateSQL);
 

                                  } catch ( Exception e ){
                                                                                                 
                                 traceVerbose("Exception doPushDelete "+e.getStackTrace());
                                     
                                  }


}












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


String  selectSQL = "select RID,dID from pullRIDC where  commitstatus=1 ORDER BY Rid asc" ;

List<String> rsList = new ArrayList<String>();  

intradoc.data.ResultSet rsNonReleaseContent  =  runSQLpullRIDCTable(m_workspace,selectSQL );

                                            for(rsNonReleaseContent.first(); rsNonReleaseContent.isRowPresent(); rsNonReleaseContent.next())

                                          {

                                               
                                                 
                                                    
                                              String         RID = rsNonReleaseContent.getStringValueByName("RID");
                                              String         dID =  rsNonReleaseContent.getStringValueByName("dID");
                                                        

                                                 rsList.add(RID+"\t"+dID);
                                                     
                                                 
                                   
                                          }


                                            for(int i=0; i < rsList.size(); i++) 

                                                      { 
 
                                                
                                                          String [] tokens = rsList.get(i).split("\\t+");
                                                          String   RID  = tokens[0];
                                                          
                                                          String   dID  = tokens[1];

                                                              if(isdIDReleased(dID))
                                                                
                                                                     {

                                                                      String updateSQL= "UPDATE pullRIDC  SET commitstatus=0 WHERE rID="+RID ;

                                                                                      insertpullRIDCTable (m_workspace,updateSQL);

                                                                     }


                                                                 else 

                                                                     {
                                                                              traceVerbose (dID + " is not released so far ");

                                                                      }


                                                      }

} // end of scanTablewithRelease


private boolean isdIDReleased (String dID) throws DataException, intradoc.common.ServiceException

{


                                      m_binder.getLocalData().clear();
                                      m_binder.putLocal("dID", dID);
                                      this.executeService(m_binder, "DOC_INFO");
                 intradoc.data.ResultSet docInfo = m_binder.getResultSet("DOC_INFO");

boolean isReleasestatus=false;

                                  for(docInfo.first(); docInfo.isRowPresent(); docInfo.next())

                                          {

                                               
                                                 
                                                  String     dStatus = docInfo.getStringValueByName("dStatus");
                                                              
                                                                     if (dStatus.equals("RELEASED"))
                                                                            isReleasestatus=true;
                                                  
                                   
                                          }



return isReleasestatus;

} // end of isdIDReleased




} //end of cLass 