package pushtoTargetRIDC;



import intradoc.common.*;
import intradoc.data.*;
import intradoc.server.*;
import intradoc.shared.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;


public class Utils {


static boolean Isiso8601;
static IdcDateFormat idf ;


public static  String  getTargetID (final intradoc.data.ResultSet rs)


            {

                          String newValue="";      
                             
                                    int columsize = rs.getNumFields(); 


                                       for(rs.first(); rs.isRowPresent(); rs.next())

                                          {

                                                for (int j=0;j< columsize ; j++)

                                                  {
                                                     String   key = rs.getFieldName(j);
                                                     newValue = rs.getStringValueByName(key);
                                                    // traceVerbose (key + "  " + value);

                                                      

                                                  }
                                          
                               
                                                
                                   
                                          }


       return newValue;


            }  // end of getTargetID





public static void trace(final String message)

              {
                 
                  Report.trace("PushRIDC", message, null);
              }        


 public static void traceVerbose(final String message) 

                  {
                
                if (Report.m_verbose) 
                         {
                        trace(message);
                         }
                   }  // end of TraceVerbose



	
public static void insertpullRIDCTable (Workspace ws,String updateSQL ) throws DataException
	


          {

		// insert the proper row into the database table








		ws.executeSQL(updateSQL);
	} // end of insertpullRIDCTable







public static intradoc.data.ResultSet runSQLpullRIDCTable (Workspace ws,String SQlstatement) throws DataException ,intradoc.common.ServiceException
	


          {

		


                              
                                             

                           intradoc.data.ResultSet rs =  ws.createResultSetSQL(SQlstatement);
              
                               
                                  return rs;
                             

		
	} // end of updatepullRIDCTable

public static void  traceResultSet (final intradoc.data.ResultSet rs)


            {

                                
                                    int rowCount = 0;
                                    int columsize = rs.getNumFields(); 


                                       for(rs.first(); rs.isRowPresent(); rs.next())

                                          {

                                                for (int j=0;j< columsize ; j++)

                                                  {
                                                     String   key = rs.getFieldName(j);
                                                     String   value = rs.getStringValueByName(key);
                                                     traceVerbose (key + "  " + value);

                                                  }
                                          
                                          rowCount++; 
                                          traceVerbose ("Going to Next Row of traceResultSet---");           
                                   
                                          }


            } // end of traceResultSet 



public static String convertDate (String value )


{

if ( Isiso8601 )

{

 if (value.length() > 0)
value = value.substring(0, value.length()-1);


traceVerbose("iso8601 date " + value);
return value; 


}

else {

if (value.length() == 0)
return value;
 
               try { 









//traceVerbose(" 	Date format  " + idf.toString());

//traceVerbose("toPattern()  " + idf.toPattern());

 //  IdcTimeZone itz = new  IdcTimeZone(0,"UTC");

  TimeZoneFormat izf = new  TimeZoneFormat() ;


  IdcTimeZone itz =  izf.parseTimeZoneDirect(idf.toPattern());

// traceVerbose("getTimeZone()  " + idf.getTimeZone());


 Date parseDateDirect = idf.parseDateDirect(value,itz,izf);

//Date parseDate    = idf.parseDate("2/11/16 5:10 PM",itz,izf);



// traceVerbose("parseDateDirect  " + parseDateDirect.toString());

//traceVerbose("parseDate  " + parseDate.toString());



DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");



TimeZone tz = TimeZone.getTimeZone("UTC");

df.setTimeZone(tz);

String newDate  = df.format(parseDateDirect);

String [] tokens = newDate.split("\\++");



newDate=tokens[0]+"Z";









return newDate;

   } catch ( java.text.ParseException e) {

traceVerbose("Bad  Date ");

return "Not Date";

   } 



}  // end of else 




} // end of convertDate






public static String appendDate (String PRquery , long timetoCheckMillis)


{


if ( Isiso8601 )

   {

Date dIndate = new Date(timetoCheckMillis);

traceVerbose ("dInDate" + dIndate);

DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");



//TimeZone tz = TimeZone.getTimeZone("UTC");

//df.setTimeZone(tz);

String newDate  = df.format(dIndate);

//String [] tokens = newDate.split("\\++");



//newDate=tokens[0]+"Z";


traceVerbose ("dInDate after conversion" + newDate);


//PRquery="dSecurityGroup='Public'";


//dInDate < {ts '2016-02-18 00:00:00.000'}) 

PRquery=PRquery+" AND dInDate >= {ts '"+newDate+"'}";

PRquery=PRquery.replaceAll("`","'");

//PRquery=PRquery+"<AND> dInDate < `"+newDate+"`";


 



return PRquery;


   } // end of if , it is required for RIDC call 


else 

{

  //  try {


LocaleResources LR = new LocaleResources();

traceVerbose("Dindate Conversion ");

       Date dIndate = new Date(timetoCheckMillis);


traceVerbose("Dindate Before  Conversion " + dIndate.toString());

      TimeZoneFormat izf = new  TimeZoneFormat() ;


//TimeZone tz = TimeZone.getTimeZone("UTC");





    //   IdcTimeZone itz = new  IdcTimeZone(0,"IST");

 


   //  IdcTimeZone itz =  izf.parseTimeZoneDirect(idf.toPattern());

      String newDate = idf.format(dIndate, LR.m_systemTimeZone);


java.sql.Timestamp sq =   new java.sql.Timestamp(dIndate.getTime());


//          traceVerbose ("dInDate after conversion" + newDate);

//           PRquery=PRquery+"<AND> dInDate >= `"+newDate+"`";

PRquery=PRquery+" AND dInDate >= {ts '"+sq+"'}";

PRquery=PRquery.replaceAll("`","'");



      return PRquery;
     
   /*     } catch ( java.text.ParseException e) {

         traceVerbose("Bad  Date ");

                return "Onda";

   } 


    */
}



} // end of appendDate



public static  List   afterSearchList (final intradoc.data.ResultSet rs)


            {


List<String> afterSearch = new ArrayList<String>(); 






                                       for(rs.first(); rs.isRowPresent(); rs.next())

                                          {


                                                   String   newValue = rs.getStringValueByName("dID");
                                                   
                                                    afterSearch.add(newValue);
                                                  //  traceVerbose("dID from search" + newValue);

                                                
                                                  traceVerbose("dID from search " + newValue + "   dInDate   " +rs.getStringValueByName("dInDate") );

                               
                                                
                                   
                                          }


       return afterSearch;


            }

public static boolean verifydID ( List<String> afterSearch , String dID)

{

boolean verify = false ;
for(int i=0; i < afterSearch.size(); i++) 

{

            if ( afterSearch.get(i).equals(dID))
                  {

                     verify = true ;
                     break;
                  }




}



return verify;


} // end of verifydID

public static boolean verifyRow (final intradoc.data.ResultSet rs )

{

boolean check=false;
                                       for(rs.first(); rs.isRowPresent(); rs.next())

                                          {


                                                    check=true;
                                                    break;
                                   
                                          }


return check;

} // end of verify1status

public static boolean isThereHigherRevisionEligilbe ( Workspace  ws,String dDocName,List<String> ReleaseddID ) throws intradoc.data.DataException ,intradoc.common.ServiceException

{

String selectSQL = "select  dID  from pullridc where commitstatus=0 AND ridcservice='CHECKIN_SEL' AND  dDocName='"+dDocName+"'";



 intradoc.data.ResultSet newRS = runSQLpullRIDCTable ( ws,selectSQL);

boolean verify = false ; 
              for(newRS.first(); newRS.isRowPresent(); newRS.next())

                                          {
                                                   String dID = newRS.getStringValueByName("dID");


                                                   traceVerbose("Higher version dID is "+ dID);

                                               for(int i=0; i < ReleaseddID.size(); i++) 

                                                       {
                                                           if ( ReleaseddID.get(i).equals(dID))
                                                                            
                                                                                {

                                                                                        verify = true ;
                                                                                              break;
                                                                                 }
                                                                
                                                        
                                                       }

                        
                                          }


return verify ;

} //end of isThereHigherRevisionEligilbe


} // end of class 
