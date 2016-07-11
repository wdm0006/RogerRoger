RogerRoger
==========

A simple API to serve out health information for a running system. Optionally persists data into elasticsearch.  

Mostly a vehicle for learning about some new datastores and Scala.

TODO:

 * Add endpoint that returns possible endpoints for querying data (es and top right now)
 * Add cleanup scheduler that reaps data by query older than a certain date (TTL logic)
 * Add self referencing scheduler that pings the localhost endpoints on a schedule to populate data
 * Add html page to render the local configuration for the runtime