$env:JAVA_HOME = "C:\Users\Kose\.gradle\jdks\eclipse_adoptium-21-amd64-windows.2"
$env:PATH = "$env:JAVA_HOME\bin;c:\Users\Kose\.gemini\antigravity\scratch\java AI\tools\apache-maven-3.9.6\bin;" + $env:PATH
$settingsFile = "c:\Users\Kose\.gemini\antigravity\scratch\java AI\tools\settings.xml"
& "c:\Users\Kose\.gemini\antigravity\scratch\java AI\tools\apache-maven-3.9.6\bin\mvn.cmd" -s "$settingsFile" $args
