[Setup]
AppPublisher=HelixSoft
AppName=Dr. Higgins
AppVerName=Dr. Higgins v0.99WIP
DefaultDirName={pf}\DrHiggins
DefaultGroupName=Dr. Higgins
Compression=lzma
SolidCompression=yes
OutputBaseFilename=DrHiggins_v0_99WIP_Setup
OutputDir=.

[Files]
Source: "higgins.bat"; DestDir: "{app}"
Source: "higgins.jar"; DestDir: "{app}"
Source: "lessons/spaans/*"; DestDir: "{app}/lessons/spaans"
Source: "lessons/spanish/*"; DestDir: "{app}/lessons/spanish"
Source: "lessons/finnish/*"; DestDir: "{app}/lessons/finnish"
Source: "lessons/dutch/*"; DestDir: "{app}/lessons/dutch"
Source: "doc/images/*"; DestDir: "{app}/doc/images"
Source: "doc/index.html"; DestDir: "{app}/doc"
Source: "lib/*.jar"; DestDir: "{app}/lib"
Source: "README.txt"; DestDir: "{app}"; Flags: isreadme
Source: "COPYING.txt"; DestDir: "{app}";

[Icons]
Name: "{group}\Dr. Higgins"; Filename: "{app}\higgins.bat"; WorkingDir: "{app}";
Name: "{group}\manual"; Filename: "{app}\doc\higgins_manual.html";
Name: "{group}\readme"; Filename: "{app}\README.txt";
