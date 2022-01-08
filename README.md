# Qurorum based replication in NoSql

My idea for this project is to have a demo implementation to share the idea of Quorum based replication
used in NoSql databases. 

NoSql databases  are amazing! NoSql databases typically have a simple interface, 
- PUT(Key, Value) 
- GET(Key, Value)

They provide reliability guarantee, these databases need to replicate data of a partition (key range) 
across different servers responsible for serving data of a partition. It looks something like this: <TODO: Image Url>.

As a consequence, such a system is eventually consistent i.e. Read immediately after a 
Write cannot guarantee retrieval of latest version of data. 

What version of data should be returned for a key?

This is the problem I am trying to solve. I am taking a simple approach of each node voting with their version of
data they have and the node receiving the request making a decision based on the votes received.

Other Notes:
- There is no notion of a Master/Leader/Server or Slave/Client nodes. I am taking a peer to peer approach where
any node in the group can receive a request for PUT or GET. Each node reaches a quorum and responds back to the
client. 

Implementation Detail

- Server.java : This is the starting point. It starts off 2 Threads, one for receiving incoming requests and 
other for sending requests to other nodes in the cluster.

