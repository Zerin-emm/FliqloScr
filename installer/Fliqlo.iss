; ============================================================================================
;  Fliqlo 网页版 Inno Setup 安装脚本
;  编译： "D:\Inno Setup 6.6.1\ISCC.exe" installer\Fliqlo.iss
;  前置： 无构建步骤，直接打包 Web 目录下的 fliqlo.html 与 Fliqlo.ico
;  产物： installer\Output\Fliqlo-Setup-1.6.1.exe
;  卸载： 删除安装目录（网页与图标）和快捷方式；浏览器 localStorage 里的设置不触碰
; ============================================================================================

#define MyAppName "Fliqlo"
#define MyAppVersion "1.6.1"
#define MyAppPublisher "9031"
#define MyAppUrl "https://fliqlo.com/"
#define MyAppFileName "fliqlo.html"
#define MyAppIconName "Fliqlo.ico"

[Setup]
AppId={{6F1B2E4A-9C3D-4A7E-B5D8-2E0F7C1A9B34}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppUrl}
AppSupportURL={#MyAppUrl}
AppUpdatesURL={#MyAppUrl}
DefaultDirName={localappdata}\Programs\{#MyAppName}
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
MinVersion=10.0
OutputDir=Output
OutputBaseFilename={#MyAppName}-Setup-{#MyAppVersion}
SetupIconFile=..\{#MyAppIconName}
UninstallDisplayIcon={app}\{#MyAppIconName}
UninstallDisplayName={#MyAppName}
Compression=lzma2/max
SolidCompression=yes
WizardStyle=modern dynamic windows11
CloseApplications=no

[Languages]
Name: "chinese"; MessagesFile: "compiler:Languages\Chinese.isl"

[Tasks]
Name: "desktopicon"; Description: "创建桌面快捷方式"; GroupDescription: "附加任务："

[Files]
Source: "..\{#MyAppFileName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\{#MyAppIconName}"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppFileName}"; IconFilename: "{app}\{#MyAppIconName}"; WorkingDir: "{app}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppFileName}"; IconFilename: "{app}\{#MyAppIconName}"; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppFileName}"; WorkingDir: "{app}"; Description: "立即运行 {#MyAppName}"; Flags: nowait postinstall skipifsilent shellexec

[UninstallDelete]
Type: filesandordirs; Name: "{app}"

[Code]
function InitializeSetup(): Boolean;
begin
  { 纯静态网页，没有前置检查；这里只留一条安装日志便于排查 }
  Log('Fliqlo web build: installing static page');
  Result := True;
end;
