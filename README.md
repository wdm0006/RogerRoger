RogerRoger
==========

![screenshot](https://raw.githubusercontent.com/wdm0006/RogerRoger/master/docs/screenshot.png)

A simple API to serve out health information for a running system. Is built as a single runtime that would exist on each
host, configured to ping various local resources, persist their states into a data store of some kind, and then serve
metrics out of that datastore upon query. 

Currently, there is only one data store supported: 

 * Elasticsearch 2.3.4
 
Currently there are two "services" which can be setup to monitor stats:

 * Elasticsearch 2.3.4 service
 * Top service
 
There are two configurable scheduled background tasks (build with akka), that any runtime can have configured:

 * SelfReferencingPing: hits the localhost endpoints to get and persist data from all available services
 * Cleanup: deletes data older than X from the data store
 * (Planned) RandomPing: hits the endpoints to get and persist data from all available services from a random member of the cluster (found via the data store) 
 
Mostly a vehicle for learning about Scala.  At any given time, it may not work, and some things are over or under engineered just for the sake of learning, fair warning.

TODO:

 * Add endpoint that returns possible endpoints for querying data (es and top right now)
 * Add html page to render the local configuration for the runtime
 * RandomPing scheduler
 * Fix lineplots page (nothing is rendering)
 * Add RabbitMQ service
 * Add PostgreSQL service
 * Add PostgreSQL data store