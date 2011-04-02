@echo on
set CLASSPATH=".\bin;.\RXTXcomm.jar;.\lib\bluecove.jar;.\lib\charts4j.jar;.\lib\BrowserLauncher.jar"

start java -classpath %CLASSPATH% zephyropen.device.DeviceServer brad
start java -classpath %CLASSPATH% zephyropen.swing.gui.viewer.DeviceViewer brad





