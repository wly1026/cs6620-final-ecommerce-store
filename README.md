# Final Project Instruction
## Install RabbitMQ
1) Follow the instruction to installed RabbitMQ
   https://www.rabbitmq.com/install-homebrew.html
2) Start the rabbitmq services
   ```
   brew services start rabbitmq
   ```
3) Check the message queue traffic: http://localhost:15672/#/queues/%2F/task_queue
   Enter "guest" for both username and password.

## Run the project
1) Start the coordinator first with selected port number
   ```
   java -cp 6650-Paxos-1.0-SNAPSHOT.jar coordinator.CoordinatorApp 2000
   ```
2) Start multiple ServerApp with selected port number and the coordinator port number selected above, otherwise it will have connection error with the cooridnotor
   ```
   java -cp 6650-Paxos-1.0-SNAPSHOT.jar server.serverApp 2000 1234
   ```
   ```
   java -cp 6650-Paxos-1.0-SNAPSHOT.jar server.serverApp 2000 2345
   ```
   ```
   java -cp 6650-Paxos-1.0-SNAPSHOT.jar server.serverApp 2000 4567
   ```
   ```
   java -cp 6650-Paxos-1.0-SNAPSHOT.jar server.serverApp 2000 5678
   ```
   ```
   java -cp 6650-Paxos-1.0-SNAPSHOT.jar server.serverApp 2000 9999
   ```
3) Use the following command if you want to recover a connected server
   ```
   java -cp 6650-Paxos-1.0-SNAPSHOT.jar server.serverApp 2000 <connected port>
   ```
3) Run multiple clients after both the servers and the coordinator are connected. Type in 5 selected port number on the command line to let client connect with the servers. "true" on the first args means init the client with prepolulate data. 
   ```
   java -jar client.jar true 1234 2345 4567 5678 9999
   ```
4) The client will ask you to enter a command after some pre-poluated value input <br />
   ```
   <customer name> <request action> <product name> <count>
   ```
   Example to type in:
   ```
   Andy add apple 1
   Ella delete juice 1
   Cindy remove coffee
   Andy get
   Ella checkout
   ```
5) The client will get the return value back from server
6) Enter `bye` on the client side to end the connection
7) Unzip the final_project.zip to get source code