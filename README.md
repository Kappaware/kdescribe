# kdescribe

A tiny tool to display current Kafka cluster status:

* Brokers list

* Topic list

* Optionnally, topic info (Start/end offset and timestamp)


***
## Usage

kdescribe is provided as rpm packages (Sorry, only this packaging is currently provided. Contribution welcome), on the [release pages](https://github.com/Kappaware/kdescribe/releases).

Once installed, basic usage is the following:

    # kdescribe --zookeeper "zknode1.yourdomain.com,zknode2.yourdomain.com,zknode3.yourdomain.com"
    
Here all all the parameters

	Option (* = required)                          Description
	---------------------                          -----------
	--forceProperties                              Force unsafe properties
	--includeAll                                   All topics, including systems ones
	--outputFormat <OutputFormat: text|json|yaml>  Output format (default: text)
	--partitions                                   List topic partitions
	--property <prop=val>                          Consumer property (May be specified several times)
	--ts                                           List topic partitions with timestamp
	* --zookeeper <zk1:2181,ek2:2181>              Comma separated values of Zookeeper nodes

When using without `-partitions` or `-ts` option, kdescribe will only access to zookeeper to grab Kafka related information. If one of these option is activated, kdescribe will also use a Kafka Consumer to get topic start and end information. Note this may increase significantly response time.

***
## Kerberos support

If kerberos is activated, you will need to define a jaas configuration file as java option. 

This can easely be achieved by uncomment this following line in `/etc/kdescribe/setenv.sh`
    
    JOPTS="$JOPTS -Djava.security.auth.login.config=/etc/kdescribe/kafka_client_jaas.conf"
    
You can of course modify the `kafka_client_jaas.conf` file to adjust to your needs or target another existing one.

But, keep in mind, you must also perform a `kinit` command, with a principal granting access to all the topics before issuing kdescribe command. For example:

    # kinit -kt /etc/security/keytabs/kafka.service.keytab kafka/my.broker.host@MY.REALM.COM


***
## Ansible integration

You will find an Ansible role [at this location](http://github.com/BROADSoftware/bsx-roles/tree/master/kappatools/kdescribe).

This role can be used as following;
	
	- hosts: zookeepers
	
	- hosts: cmd_node
	  vars:
        kdescribe_rpm_url: https://github.com/Kappaware/kdescribe/releases/download/v0.2.0/kdescribe-0.2.0-1.noarch.rpm
	  roles:
	  - kappatools/kdescribe
	  
> Note `- hosts: zookeepers` at the beginning, which force ansible to grab info about the hosts in the `[zookeepers]` group, to be able to fulfill this info into jdctopic configuration. Of course, such a group must be defined in the inventory. In this case, zookeeper information will no longer be required on the command line.


***
## Build

Just clone this repository and then:

    $ gradlew rpm

This should build everything. You should be able to find generated packages in build/distribution folder.

***
## License

    Copyright (C) 2016 BROADSoftware

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
