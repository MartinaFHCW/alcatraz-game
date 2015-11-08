Java RMI Calculator

In order to run this example, execute the commands in the following order:

1.) compile.bat
2.) run-registry.bat
3.) run-server.bat
4.) run-client.bat

-- or the corresponding *.sh files in Unix shells

The previous version defines the necessary CLASSPATH environment variable for 
the registry in order to allow the registry to access classes required to 
register the server, i.e., the Calculator interface. This will is most often 
also the case if the registry is started within a program by using 
LocateRegistry.createRegistry().

Alternatively, the dynamic code downloading feature of Java can be used in order 
to give the registry access to the required code. This feature can be tried by 
executing the following steps:

1.) compile.bat (not necessary if already compiled)
2.) run-registry-without-classpath.bat
3.) run-server-with-code-download.bat
4.) run-client.bat

Please check "run-registry.bat" vs. "run-registry-without-classpath.bat" and 
"run-server.bat" vs. "run-server-with-code-download.bat" in order to view the 
differences.
