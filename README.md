# kdescribe

A tiny tool to display current Kafka cluster status:

* Brokers list

* Topic list

## Usage

kdescribe is provided as rpm packages (Sorry, only this packaging is currently provided. Contribution welcome), on the [release pages](https://github.com/Kappaware/kdescribe/releases).

Once installed, basic usage is the following:

    # kdescribe --zookeeper "zknode1.yourdomain.com,zknode2.yourdomain.com,zknode3.yourdomain.com"
    
Here all all the parameters

	Option (* = required)                     Description
	---------------------                     -----------
	--includeAll                              All topics, including systems ones
	--outputFormat <OutputFormat: json|yaml>  Output format (default: yaml)
	* --zookeeper <zk1:2181,ek2:2181>         Comma separated values of Zookeeper nodes


***
## Ansible integration

You will find an Ansible role [at this location](http://github.com/BROADSoftware/bsx-roles/tree/master/kappatools/kdescribe).

This role can be used as following;
	
	- hosts: zookeepers
	
	- hosts: cmd_node
	  vars:
        kdescribe_rpm_url: https://github.com/Kappaware/jdctopic/releases/download/v0.1.0/kdescribe-0.1.0-1.noarch.rpm
	  roles:
	  - kappatools/kdescribe
	  
> Note `- hosts: zookeepers` at the beginning, which force ansible to grab info about the hosts in the [zookeepers] group, to be able to fulfill this info into jdctopic configuration. Of course, such a group must be defined in the inventory. 


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
