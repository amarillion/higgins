[Setup]
AppPublisher=HelixSoft
AppName=Dr. Higgins
AppVerName=Dr. Higgins v0.99d~WIP
DefaultDirName={pf}\DrHiggins
DefaultGroupName=Dr. Higgins
Compression=lzma
SolidCompression=yes
OutputBaseFilename=DrHiggins_v0_99d~WIP_Setup
OutputDir=.

[Files]
Source: "higgins.bat"; DestDir: "{app}"
Source: "higgins.jar"; DestDir: "{app}"
Source: "lessons/spaans/*"; DestDir: "{app}/lessons/spaans"
Source: "lessons/spanish/*"; DestDir: "{app}/lessons/spanish"
Source: "lessons/finnish/*"; DestDir: "{app}/lessons/finnish"
Source: "lessons/dutch/*"; DestDir: "{app}/lessons/dutch"
Source: "lessons/anglais/*"; DestDir: "{app}/lessons/anglais"
Source: "lessons/italiaans/*"; DestDir: "{app}/lessons/italiaans"
Source: "lessons/russian/*"; DestDir: "{app}/lessons/russian"
Source: "lessons/japanese/*"; DestDir: "{app}/lessons/japanese"
Source: "doc/images/*"; DestDir: "{app}/doc/images"
Source: "doc/index.html"; DestDir: "{app}/doc"
Source: "lib/*.jar"; DestDir: "{app}/lib"
Source: "README.txt"; DestDir: "{app}"; Flags: isreadme
Source: "COPYING.txt"; DestDir: "{app}";

[Icons]
Name: "{group}\Dr. Higgins"; Filename: "{app}\higgins.bat"; WorkingDir: "{app}"; Flags: runminimized
Name: "{group}\manual"; Filename: "{app}\doc\index.html";
Name: "{group}\readme"; Filename: "{app}\README.txt";
Name: "{group}\Lesson folder"; Filename: "{app}\lessons";
Name: "{commondesktop}\Dr. Higgins"; Filename: "{app}\higgins.bat"; WorkingDir: "{app}"; Flags: runminimized

[Languages]
Name: "en"; MessagesFile: "compiler:Default.isl"
Name: "nl"; MessagesFile: "compiler:Languages\Dutch.isl"
Name: "es"; MessagesFile: "compiler:Languages\Spanish.isl"
Name: "fr"; MessagesFile: "compiler:Languages\French.isl"
Name: "pl"; MessagesFile: "compiler:Languages\Polish.isl"

