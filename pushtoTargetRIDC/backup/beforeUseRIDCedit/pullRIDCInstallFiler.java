package pushtoTargetRIDC;

import static pushtoTargetRIDC.Utils.*;
import java.util.*;
import java.io.*;
import intradoc.common.*;
import intradoc.data.*;
import intradoc.server.*;
import intradoc.shared.*;







public class pullRIDCInstallFiler  implements FilterImplementor 


{

  pullRIDCInstallFiler.Version current = new pullRIDCInstallFiler.Version(1, 0.1F, 0, 1, new GregorianCalendar(2008, 5, 15).getTime());
                private static String compName = "pushtoTargetRIDC";

                public pullRIDCInstallFiler.Version getCurrentVersion()
                   {
                    return this.current;
                   }


	public int doFilter(Workspace ws, DataBinder binder, ExecutionContext cxt)
		throws DataException, ServiceException
	{


                 if (getSignificantVersion() <= 6)
		{
			return CONTINUE;
		}
		String param = null;
		Object paramObj = cxt.getCachedObject("filterParameter");
		

                if (paramObj == null || (paramObj instanceof String) == false)
		{
			return CONTINUE;
		}
		param = (String)paramObj;

             Service s = null;
		IdcExtendedLoader loader = null;

		if (cxt instanceof IdcExtendedLoader)
		{
			loader = (IdcExtendedLoader) cxt;
			if (ws == null)
			{
				ws = loader.getLoaderWorkspace();
			}
		}
		else  if (cxt instanceof Service)
		{
			s = (Service)cxt;
			loader = (IdcExtendedLoader)ComponentClassFactory.createClassInstance("IdcExtendedLoader",
								"intradoc.server.IdcExtendedLoader", "!csCustomInitializerConstructionError");
		}



         if (param.equals("extraAfterServicesLoadInit"))
                {
                   // traceVerbose("services Entered extraAfterServiceLoadInit");               
                   
                    DataResultSet enabledComponents = SharedObjects.getTable("Components");
                    String status = ResultSetUtils.findValue(enabledComponents, "name", "pushtoTargetRIDC", "status");                    
                   
                  //  traceVerbose("pushtoTargetRIDC Value for component status " + status );

                    // String personalFoldersInstalled = loader.getDBConfigValue("ComponentInstall", compName, String.valueOf(getCurrentVersion().getVersionMax()));
                    String pushtoTargetRIDCInstalled = loader.getDBConfigValue("ComponentInstall", compName,"3");
                  //  traceVerbose("pushtoTargetRIDC personalFoldersInstalled = " + pushtoTargetRIDCInstalled);




 
                        


                
                
                                        if ( pushtoTargetRIDCInstalled == null && (status.equals("Enabled") ) )
                    {
                        

                         traceVerbose("pullridc table not founf , Inserting tables ");

                      

                           
                   String     updateSQL="CREATE SEQUENCE rid_seq START WITH     1000 INCREMENT BY   1 NOCACHE NOCYCLE";
                            insertpullRIDCTable (ws,updateSQL);


                                  updateSQL="create table pullridc  (RID NUMBER(7), DID NUMBER(8), RENTRYDATE NUMBER(20),RACTIONDATE NUMBER(20), RIDCSERVICE VARCHAR2(100), VAULTFILEPATH VARCHAR2(256), RIDCPUSHACTION VARCHAR2(256), TARGETDID NUMBER(8), COMMITSTATUS NUMBER(3), DDOCNAME VARCHAR2(20) ,  CONSTRAINT    PK_RID    PRIMARY KEY (rID)) ";
    

                             insertpullRIDCTable (ws,updateSQL);
 

                       traceVerbose("pushtoTargetRIDCInstalled Inside the condition to insert data to CONFIG table"); 
                        loader.setDBConfigValue("ComponentInstall",compName, "3", "3");
 


 
                               
 


                    }



               }
 


         return CONTINUE;

        }  // end of doFiler

protected int getSignificantVersion()
	{
		String strVersion=SystemUtils.getProductVersionInfo();
		int nIndex=strVersion.indexOf(".");
		if (nIndex != -1)
		{
			strVersion=strVersion.substring(0,nIndex);
		}
		return(Integer.valueOf(strVersion).intValue());
	}
        



      public class Version
        {
              int m_nVersionMax;
              float m_fVersionMin;
              int m_nRevision;
              int m_nPointRevision;
              Date m_dBuildDate;

          public Version(int versionmax, float versionmin, int revision, int pointrevision, Date builddate)
            {
               this.m_nVersionMax = versionmax;
               this.m_fVersionMin = versionmin;
               this.m_nRevision = revision;
               this.m_nPointRevision = pointrevision;
               this.m_dBuildDate = builddate;
            }

          public boolean equals(Version v)
            {
                 return (v.getVersionMax() == getVersionMax()) && (v.getVersionMin() == getVersionMin()) && (v.getRevision() == getRevision()) && (v.getPointRevision() == getPointRevision()) && (v.getBuildDate().equals(getBuildDate()));
            }

          public int getVersionMax()
            { 
               // traceVerbose("Inside getVersion Max function"); 
                return this.m_nVersionMax;
            }

           public float getVersionMin()
            {
                return this.m_fVersionMin;
            }

           public int getRevision()
            {
                 return this.m_nRevision;
            }

           public int getPointRevision()
            {
                 return this.m_nPointRevision;
            }

           public Date getBuildDate()
            {
                 return this.m_dBuildDate;
            }

           public String getBuildYearMonthDayStr()
            {
                Calendar c = new GregorianCalendar();
                c.setTime(this.m_dBuildDate);

                return c.get(1) + "_" + c.get(2) + "_" + c.get(5);
           }
      } // end of inner class  Version


} // end of class 


/*


for Folderrepliaction , you need this to impilment


Create Table PullFolderridc  (rid Number(7), Ffolderguid Varchar2(32), Rentrydate Number(20),Ractiondate Number(20), ridcservice Varchar2(32), Fparentguid Varchar2(32),


ridcpushaction Varchar2(256),targetFFOLDERGUID VARCHAR2(32),  Commitstatus Number(3), Fparentpath Varchar2(512) ,  

   PRIMARY KEY (rid));

*/


