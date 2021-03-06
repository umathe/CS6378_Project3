Raman Sathiapalan, Anshika Singh, Usuma Thet
CS 6378.001 - Advanced Operating Systems - F18
Project 3
Due: December 4, 2018
------------------------------------------

Code compilation instructions (Windows):
-----
1. Open PuTTY. Reference the configuration file and open the same number of instances as the number of nodes. 
   Connect each node with their respective their host names and ports. 

2. For each PuTTY instance, login with your UTD netID and password.

3. Create SampleInput.txt file in PuTTY and copy the contents of the configuration file. Save SampleInput.txt.

4. Create ComparatorForTS.java, NewMutexCode.java, and Requests.java files in PuTTY and copy the contents of the submitted java files respectively.

5. Compile code using the following commands: javac ComparatorForTS.java, javac NewMutexCode.java, and javac Requests.java

6. Run code in each PuTTY instance. Use the following command: java NewMutexCode

------------------------------------------

Code compilation instructions (Mac/Linux): 
-----
1. Save project folder on DC machine. 

2. Save config file (SampleInput.txt) inside the project folder.

3. Setup passwordless login. Create a folder "launcher" on the home machine with the scripts launcher.sh and cleanup.sh, with required changes based on filepaths. 

4. Save config file in "launcher" folder. 

5. Compile code in DC machine using the following commands: javac ComparatorForTS.java, javac NewMutexCode.java, and javac Requests.java

6. In the home machine, run the launcher script (command: ./launcher.sh) and cleanup script (./cleanup.sh).

------------------------------------------

Performance Analysis:
-----
The system throughput (milliseconds) is captured for each node after all required number of requests have been completed. Each test
instance will have varying number of nodes, inter-request delay (milliseconds), cs-execution time (milliseconds), and number of requests
to test various system parameters' effect on the performance of the program. 

Assumption:
-----
System throughput was chosen as the single variable to measure performance because the others provided would not produce valid comparisons due to how the program was implemented and for
the following reasons:
1. Message complexity is an invalid measure since the context of the message is hardcoded in the program. There are unique messages depending on its type and usage such as for REQUEST and GRANT.
   However, they will always follow a similar syntax save for the node ID that generates the request and its timestamp. Performance of the program will not have any effect on the context of these
   messages or vice versa.
2. Response time is also considered an invalid measure. This is because the inter-request delay (milliseconds) variable provided directly influences the response time of the program while being
   utilized as a means to compare the performance of the program. In this regard, inter-request delay will practically be measured twice for the overall comparison which can skew the results and bias
   any conclusions made about the data. 

Results:
-----
Please reference the Project3-Performance Analysis workbook for the graph and raw data. The number of requests were kept constant at
1000. As such, only inter-request delay, cs-execution time, and number of nodes were chosen as the system parameters for testing.

After analysis was done, it was discovered that cs-execution time had the most direct effect on overall system performance. Conclusions
may naturally be made that there was a linear increase in the system throughput due to the amount of time required to execute the
critial section for a constant number of requests. Though inter-request delay appeared to have some effect, it was minimal since by
nature of this protocol, if one node is delayed, another may jump in for execution.
