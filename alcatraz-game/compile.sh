# javac -classpath include/class/ Sender.java

javac -cp .:include/spread-4.4.0.jar Sender.java
javac -cp .:include/spread-4.4.0.jar Receiver.java

javac -classpath .:include/spread-4.4.0.jar:include/alcatraz-lib.jar Test.java
javac -classpath .:include/spread-4.4.0.jar:include/alcatraz-lib.jar Player.java
