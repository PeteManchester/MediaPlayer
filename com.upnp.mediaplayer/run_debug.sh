export LD_LIBRARY_PATH=/usr/local/lib/
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005  -jar mediaplayer.jar