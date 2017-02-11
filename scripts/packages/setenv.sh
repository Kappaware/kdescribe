
# Set JAVA_HOME. Must be at least 1.7.
# If not set, will try to lookup a correct version.
# JAVA_HOME=/some/place/where/to/find/java

# Set the log configuration file
JOPTS="$JOPTS -Dlog4j.configuration=file:/etc/kdescribe/log4j.xml"



# Set zookeeper quorum
# OPTS="$OPTS --zookeeper zk1:2181,zk2:2181,zk3:2181"

# If kerberos is activated, you must add line like this:
# JOPTS="$JOPTS -Djava.security.auth.login.config=/etc/kdescribe/kafka_client_jaas.conf"
# (You can of course modify the kafka_client_jaas.conf file to adjust to your needs or target another existing one).

# But, keep in mind, you must also perform a kinit command, with a principal granting access to all the topics. For example:
#
# kinit -kt /etc/security/keytabs/kafka.service.keytab kafka/my.broker.host@MY.REALM.COM
#
#

