# Qurorum based consensus

NoSql databases are amazing! NoSql databases typically have a simple interface that allows storing data for a key and retrieving
data for a key. API's look something like below, 
- PUT(Key, Value) 
- GET(Key)

To provide reliability guarantee, these databases need to replicate data of a partition
across different servers. A simple architecture looks looks something like https://github.com/hareeshl/toy_quorum_replication/blob/346bf139ca25f877ec17cdaea62f4e32c1e7ba9e/IMG_8058.jpg.

As a consequence, this system is eventually consistent i.e. Read immediately after a 
Write cannot guarantee retrieval of latest version of data as the node that received the Read might not be the one
that received the write and hence lagging behind. 

This package is a naive way to try to improve the situation with eventual consistency (maybe). In this implementation, 
after every PUT, each node publishes the data to other nodes in cluster. On a GET, the node receiving the request retrieves
 data from all other nodes in the cluster, builds a majority consensus and return back a version. 

To facilitate this, I have implemented 2 internal api's 
- FETCH: As part of GET operation, retrieve data from all nodes, build majority consensus, return value.
- PUBLISH: As part of PUT operation, after updating internal state, publish to all nodes in the cluster.

Other Notes:
- There is no notion of a Master/Leader/Server or Slave/Client nodes. I am taking a peer to peer approach where
any node in the group can receive a request for PUT or GET. Each node reaches a quorum and responds back to the
client.

- Typically majority consensus is built by storing a version number along with data. I have just used naive equality
in this implementation. A detailed discussion on this approach is available @ http://web.mit.edu/6.033/2005/wwwdocs/quorum_note.html
o
- Rebuilding state or adding new nodes to a cluster and building is not done but possible. 

Implementation Detail

- Server.java: This is the starting point.
- E2ETests.java: Behaves like a client. Each test sets up a cluster, does GET and PUT api's and validates correctness  

Instructions to run locally
- javac *.java && java Server <portnumber>

