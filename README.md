A. Installation :

1. Install like normal component

2. Installation only required on source not in target 

B. Configration


Config File : pushtotargetridc_environment.cfg  

Location : <domain>/ucm/cs/custom/pushtoTargetRIDC	

1. Make sure that source host/IP details added in target config.cfg 

Variable : SocketHostAddressSecurityFilter


2. Update  pushtotargetridc_environment.cfg   of source :


TargetServer=idc://10.184.36.140:4997   ( idc port is recommend, http can be used  )
TargetUsername=
TargetPassword=

PRQuery  ( Query which eligible for replication in source )

Example :

PRQuery=dSecurityGroup='Public'


CommitOnlyReleased= ( it will commit only released content , if DONE status content want to replicate change to false)





3. Try to replicate one content and (folder) manually

This step is important . There might be chance that some of the metadata and values not in target and replication will fail

4. If every thing works fine, then enable in cfg file of component. Replication will do on every 5 min

EnableReplication=true


Services :

GET_PUSH_RIDC : To search and commit to target server
PULLRIDC_CONSOLE : Used to diplay acitve contents and commit using GET_PUSH_RIDC
PULLRIDC_REPORT  : Display pullridc table 
PULLRIDC_FOLDER_REPORT :  Display pullridcfolder table 
REPLICATE_ONTHEGO_CONTENT : Replicate content manually . 
REPLICATE_ONTHEGO_FOLDER  : Replicate folders and contents inside manually 




Parameters :
doCheckinMin : Check the Content in Last <>mins 
CommitStatus : Every content set by status , it search or commit by status
doCheckAll   : it will check all the contents with out entrydate in the .


Trace Section : PushRIDC


Action          CommitStauts : 

New entry       0

Not Released    1

Checkin Process

DocInfo          2
Failed in RIDC   3
Sucess           4 
 


Update Process 

TargetID search  5
Docinfo          6
Failed in RIDC   7
Sucess           8


Check out 

TargetID search  13 
Failed in RIDC   15
Sucess           16


Checkin SEL      
TargetID search  29
Docinfo          30
Failed in RIDC   31
Sucess           32


Delete  

TargetID search  61
TargetID null    62
Failed in RIDC   63
Sucess           64 

Special Status : 

Delete the revision before Replication : 100
Connection issue while Replication     : -1
Search query is not matching           : 200



Folder : FLD_CREATE_FOLDER

TargetFolderGUID search        301
folderinfo                     302
Failed in RIDC                 303
Sucess                         304

FLD_MOVE


TargetFolderGUID search        313
folderinfo                     314
Failed in RIDC                 315
Sucess                         316




FLD_EDIT

TargetFolderGUID search        321
folderinfo                     322
Failed in RIDC                 323
Sucess                         324


FLD_COPY

TargetFolderGUID search        333
folderinfo                     334
Failed in RIDC                 335
Sucess                         336


FLD_DELETE

TargetFolderGUID search        357
folderinfo                     358
Failed in RIDC                 359
Sucess                         360


FLD_UNFILE

TargetFolderGUID search        377
folderinfo                     378
Failed in RIDC                 379
Sucess                         380











