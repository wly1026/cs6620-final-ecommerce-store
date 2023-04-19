# Project 4 Instruction
1) Start the Coordinator first with selected port number
    ```
    java -jar coordinator.jar 2000
    ```
2) Start the ServerApp with 5 selected port number and the coordinator port number selected above, otherwise it will have connection error with the cooridnotor
    ```
    java -jar server.jar java -jar serverApp.jar 1234 2345 4567 5678 9999 2000
    ```
3) Run multiple clients after both the servers and the coordinator are connected. Type in 5 selected port number on the command line to let client connect with the servers
   ```
   java -jar client.jar 1234 2345 4567 5678 9999
   ```
4) The client will ask you to enter a command after some pre-poluated value input <br />
   Example to type in:
   ```
   put 1 2
   get 1
   delete 1
   ```
5) The client will get the return value back from server
6) Enter `bye` on the client side to end the connection
7) Unzip the project4.zip to get source code