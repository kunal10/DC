Slip Days Used(This Assignment) : 1   Slip Days Used(Total) : 1

Problem 3) Broadcast config format:
    
Line 1: Directory where broadcast_config file has to be read from.   
dc/ 

Line 2: Space separated list of HostPortNo. NumProcesses MaxSimulationTime(Processes will be shutdown after this time)

Line 3: NumProcess x NumProcesses matrix D where D[i][j] is delay for channel between pi and pj.
5000 5 15

0 0 3 3 3
0 0 0 0 0
3 0 0 0 0 
3 0 0 0 0
3 0 0 0 0 

Next Line: Broadcast src followed by series of timestamps at which it broadcasts
One line for each process.
0 1
1 2 3

HOW TO RUN:
cd HW1/src
sh p3.sh
NOTE : This used HW1/src/broadcast_config. Modify it as per above specifications if you want to test some other cases.
 
LOGS : Generated in HW1/src/dc/


Problem 4) Unicast config format:
Line 1: Directory where unicast_config file has to be read from.   
dc/ 

Line 2: Space separated list of HostPortNo. NumProcesses MaxSimulationTime(Processes will be shutdown after this time)
5000 5 15

Line 3: NumProcess x NumProcesses matrix D where D[i][j] is delay for channel between pi and pj.
0 0 4 0 0
0 0 0 0 0
4 0 0 0 0
0 0 0 0 0
0 0 0 0 0

Next Line: Source Destination followed by series of timestamps comma separated (in ascending order) at which source sends messages to destination. One line per {src,dest} tuple.
0 2 1
0 1 2
1 2 3
3 4 1,2

HOW TO RUN:
cd HW1p4/src
sh p4.sh
NOTE : This uses HW1p4/src/unicast_config. Modify it as per above specifications if you want to test some other cases.
 
LOGS : Generated in HW1p4/src/dc/
