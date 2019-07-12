#`Connection Pool`
![connection-pool-diagram](./connection-pool.png)
- Client send login info to server. If the number of threads is not full, then server will create a thread "ClientHandler" to handle request from client.
- Server check authentication with a sample database. If login success, server and client will go into "conversation"
- A conversation will continue until client send a message "quit" or when timeout.
- For each request/response from client/server, it may be in format : <format command> <content of command>
- All possible format of request/response are: login <username> <password>, quit, msg <message>, ddos (when server is full)...