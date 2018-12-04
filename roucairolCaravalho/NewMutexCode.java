import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.concurrent.CountDownLatch;

public class MutexCode {

	/*
	 * Initialize local variables. Each dc machine (node) will have a unique
	 * combination of host name, port, and node neighbors
	 */
	static String nodeHostName = null;
	static int nodePortNumber = 0;
	static String nodeNeighbors = null;
	static String[] nodeNeighborsArray;
	static int nodeNumber = 0;

	static ArrayList<Socket> socClientsArray = new ArrayList<Socket>();
	static int[] neighborHopArray;
	
	static int num_nodes = 0;
	int temp_num_nodes = 1;
	static String[][] info_nodes;
	
	//mutex variables
	static int mean_inter_request_delay; // mean value for inter-request delay (in milliseconds)
	static int mean_cs_exe_time; // mean value for cs-execution time (in milliseconds)
	static int num_req; // number of requests each node should generate
	
	static PriorityQueue<Requests> requestsReceived = new PriorityQueue<Requests>(new ComparatorForTS());
	static ArrayList<Socket> repliesReceived = new ArrayList<Socket>();
	static ArrayList<Long> repliesET = new ArrayList<Long>();
	static boolean inCriticalSection = false;
	static boolean selfReq = false;
	static long selfReqTime = 0;
	static int msgCounter = 0;
	static int finalCounter;
	static long startTime, endTime, duration;
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// MAIN
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		MutexCode n1 = new MutexCode(); // Initialize class

		File config_file = new File("/home/010/r/rx/rxs131130/TestProj/CS6378_Project3-master/roucairolCaravalho/SampleInput.txt");
		// Check if configuration file is available
		if (config_file.exists() == true) {
			System.out.println("Configuration file for input found.");
		} else {
			System.out.println("Configuration file for input not found.");
			System.exit(0); // Terminate code
		}

		// Run ReadInput function. Outputs cleaned configuration file contents in 2d
		// array		
		
		String[][] info_nodes = n1.ReadInput(config_file);

		System.out.println(Arrays.deepToString(info_nodes)); // Print for testing. DELETE

		try {
			// Capture host name of dc machines (node)
			nodeHostName = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		/*
		 * Identify node # running code. Configuration file information is partially
		 * extracted according to the node number.
		 */
      		for (int i = 0; i < info_nodes.length; i++) {
			if (info_nodes[i][1].equals(nodeHostName)) { // Match node based on host name
				nodeNumber = Integer.parseInt(info_nodes[i][0]);
				nodePortNumber = Integer.parseInt(info_nodes[i][2]);
				nodeHostName = info_nodes[i][1];
				nodeNeighbors = info_nodes[i][3];
				break;
			}
		}

		/*
		 * Check if node information is initialized. Exit code if dc machine (node) not
		 * identified in configuration file
		 */
		if (nodePortNumber == 0) {
			System.out.println(
					"\nCould not find host name in the configuration file or port number does not match. Exiting. . .");
			System.exit(0); // Terminate code
		} else {
			System.out.println("\nHost " + nodeHostName + " on port #" + nodePortNumber
					+ " initialized.\nWelcome, Node #" + nodeNumber + "!");

			System.out.println("\n--------------------------------------\n");
		}
		nodeNeighborsArray = nodeNeighbors.split(" ");
		finalCounter = num_req + (num_nodes-1);
		startTime = System.currentTimeMillis();
		
		final CountDownLatch latch = new CountDownLatch(2);
		final boolean[] serverReply = new boolean[1];
		serverReply[0] = false;
		final boolean[] clientCompletion = new boolean[1];
		clientCompletion[0] = false;
		
		/* Thread for executing server on this node */
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				neighborHopArray = new int[info_nodes.length];				
				serverReply[0] = n1.setServer(nodePortNumber, nodeNeighborsArray.length);
				latch.countDown();
			}
		});

		/* Thread for executing the client instances of this node */
		Thread t2 = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(2000);
					int connectingClient = 0;
					for(int i= 0; i<nodeNeighborsArray.length; i++) {
						for(int j = 0; j<info_nodes.length; j++) {
							if(info_nodes[j][0].equals(nodeNeighborsArray[i])) {
								connectingClient = connectingClient + n1.setClient(info_nodes[j][1], Integer.parseInt(info_nodes[j][2]));
							}
						}
					}
					System.out.println("All clients connected "+connectingClient);
					if(connectingClient == nodeNeighborsArray.length) {
						clientCompletion[0]=true;
					}
					latch.countDown();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		t1.start();
		t2.start();
		try {
			latch.await();
			/* If all the neighbors according to the config file were accepted by this node 
			 * Broadcast Messages to all neighbors and receive their messages */
			boolean continueAttempt = true;
			while(continueAttempt) {
				if(serverReply[0] == true && clientCompletion[0]==true){
					for(int i = 0; i<num_req; i++) {
						try {
							//Request to enter CS after certain delay
							Thread.sleep(mean_inter_request_delay);
							csEnter();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					doneMessage();
					continueAttempt = false;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// FUNCTIONS
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String[][] ReadInput(File config_file) {
		try {
			// Initialize function variables
			boolean firstlinepassed = false;
			int lineCount = 0;
			String[] temp_splitarr;
			String temp_neighbors = "";
			
			BufferedReader br = new BufferedReader(new FileReader(config_file));
			String temp_filerow;
			String temp_line;

			while ((temp_filerow = br.readLine()) != null) {

				// Handle # which denotes comments. Characters after # in line are ignored
				if (temp_filerow.contains("#")) {
					temp_filerow = temp_filerow.substring(0, temp_filerow.indexOf("#"));
				}
				temp_filerow = temp_filerow.trim(); // Handle leading and trailing white spaces

				if (!temp_filerow.isEmpty()) { // Ignore empty lines and lines beginning with #

					// first valid line of the configuration file contains four tokens
					if (firstlinepassed == false) {
						temp_splitarr = temp_filerow.trim().split(" +"); // line will consist of four tokens
						
						num_nodes = Integer.parseInt(temp_splitarr[0].trim()); // number of nodes
						info_nodes = new String[num_nodes][4];
						
						mean_inter_request_delay = Integer.parseInt(temp_splitarr[1].trim()); 
						mean_cs_exe_time = Integer.parseInt(temp_splitarr[2].trim());
						num_req = Integer.parseInt(temp_splitarr[3].trim()); // number of requests each node should generate
												
						for(int i = 0; i<num_nodes; i++) {
							temp_neighbors += i + " "; //temp var to capture node neighbors for later function						
						}
						firstlinepassed = true;

					} else { // first line has passed
						// Populate array containing node information
						temp_splitarr = temp_filerow.trim().split(" +"); // line will consist of three tokens

						// Assign node ID, host name, and port
						info_nodes[lineCount][0] = temp_splitarr[0].trim(); // node ID 
						info_nodes[lineCount][1] = temp_splitarr[1].trim() + ".utdallas.edu"; // host name
						//info_nodes[lineCount][1] = temp_splitarr[1].trim(); // host name
						info_nodes[lineCount][2] = temp_splitarr[2].trim(); // port
						info_nodes[lineCount][3] = temp_neighbors.replace(info_nodes[lineCount][0], "").replaceAll(" +", " ").trim(); // all other nodes are neighbors 
												
						lineCount++;					
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return info_nodes;
	}
	
	/* Method to create server on this node and accept clients*/
	public boolean setServer(int nodePortNumber, int nodeNeighborsNumber) {	
		ServerSocket ssoc = null;
		try {
			ssoc = new ServerSocket(nodePortNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int counter = 0;
		while(true) {
			try {				
				Socket soc = ssoc.accept();
				System.out.println("Server accepts "+soc);
				/* Array to store each client accepted */
				socClientsArray.add(soc);
				counter++;
				/* If all the neighbors according to the config file were accepted by this node 
				 * Broadcast Messages to all neighbors and receive their messages */
				if(counter == nodeNeighborsNumber){
					return true;
				}
			} catch (SocketException e1) {
				try{
					ssoc.close();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
		
	/* Client Method to connect to server and receive requests */
	public int setClient(String nodeHostName, int nodePortNumber) {
		final CountDownLatch latch = new CountDownLatch(1);
		Thread singleClientThread = new Thread(new Runnable(){
			public void run(){
				try {
					//Connect to server as a client
					Socket clientSocket = new Socket(nodeHostName, nodePortNumber);
					DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
					latch.countDown();
					//receive messages from the server after connection
					while(true && finalCounter > 0) {
						try {
							String msgRcvd = dis.readUTF();
							String[] msgSplit = msgRcvd.split(" ");
							//basic conditions to grant permission to a requesting node to enter the CS
							boolean grantCond1 = (inCriticalSection == false && selfReq == false);
							boolean grantCond2 = (inCriticalSection == false && selfReq == true && selfReqTime > Long.parseLong(msgSplit[2]));
							boolean grantCond3 = (inCriticalSection == false && selfReq == true && selfReqTime == Long.parseLong(msgSplit[2]) && Integer.parseInt(msgSplit[1]) < nodeNumber);
							System.out.println("msgRcvd "+msgRcvd);
							if(msgSplit[0].equals("Request")) {
								if(grantCond1 || grantCond2 || grantCond3) {
									DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
									long t = System.currentTimeMillis();
									dos.writeUTF("Granted "+ t+" "+nodeNumber);
								} else {
									Requests newReq = new Requests(clientSocket, Long.parseLong(msgSplit[2]));
									requestsReceived.add(newReq);  //Add in the priority queue if cannot grant permission
								}
							} else if(msgSplit[0].equals("Done")) {
								finalCounter--;
							}
						}  catch (EOFException e1) {
							dis.close();
							clientSocket.close();
						} 
					}
					dis.close();
					clientSocket.close();
					if(finalCounter == 0) {
						endTime = System.currentTimeMillis();
						duration = (endTime - startTime);
						System.out.println("Total Duration for node "+ nodeNumber+ " is "+duration);
					}
				} catch (SocketException e) {
					System.out.println(e);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		singleClientThread.start();
		try {
			latch.await();
		} catch (InterruptedException e){
			e.printStackTrace();
		}
		return 1;
	}
	
	/* Method to request permission to enter CS */
	public static void csEnter() {
		//if no requests received since the last time you entered CS then directly access the CS
		if(requestsReceived.size() == 0 && repliesReceived.size() == socClientsArray.size()) {
			mutexService(true, true);
		} else {						//	else ask permission from the neighbors to enter CS
			repliesReceived.clear();
			repliesET.clear();
			selfReq = true;
			selfReqTime = System.currentTimeMillis();
			for(Socket s:socClientsArray) {
				try {
					DataOutputStream dos = new DataOutputStream(s.getOutputStream());
					dos.writeUTF("Request "+ nodeNumber +" "+ selfReqTime);
					DataInputStream dis = new DataInputStream(s.getInputStream());
					String msgRcvd = dis.readUTF();
					System.out.println("Reply : " + msgRcvd);
					String[] msgRcvdArray = msgRcvd.split(" ");
					if(msgRcvdArray[0].equals("Granted")) {
						repliesReceived.add(s);
						repliesET.add((Long.parseLong(msgRcvdArray[1])));
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//if all neighbors grant permission then enter CS
			if(repliesReceived.size()==socClientsArray.size()) {
				mutexService(true, false);
			}
		}
	}
	
	/* Method to send reply done with CS */
	public synchronized static void csLeave(long t) {
		//on leaving CS, grant permission to all the requesting nodes
		ArrayList<Requests> clearPQ = new ArrayList<Requests>();
		for(Requests s:requestsReceived) {
			clearPQ.add(s);
			DataOutputStream dos;
			try {
				System.out.println("Request in cs leave "+s.getTS());
				dos = new DataOutputStream((s.getRequest()).getOutputStream());
				dos.writeUTF("Granted "+t+" "+nodeNumber+" LEFT CS");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for(Requests s:clearPQ) {
			requestsReceived.remove(s);
		}
	}
	
	/* Handles Mutual Exclusion cases*/
	public synchronized static void mutexService(boolean inCS, boolean reEnter) {
		try {
			long t1 = System.currentTimeMillis();
			//cases to check whether collision occurs
			System.out.println("In CS "+nodeNumber + " " + msgCounter + " at time "+ t1);
			
			//sleep time in Critical Section
			Thread.sleep(mean_cs_exe_time);
			
			//After executing CS reset variables and Leave CS
			selfReq = false;
			selfReqTime = 0;
			inCriticalSection = false;
			msgCounter++;
			finalCounter--;
			if(finalCounter == 0) {
				endTime = System.currentTimeMillis();
				duration = (endTime - startTime);
				System.out.println("Total Duration for node "+ nodeNumber+ " is "+duration);
			}
			long t2 = System.currentTimeMillis();
			csLeave(t2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void doneMessage() {
		for(Socket s:socClientsArray) {
			try {
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());
				dos.writeUTF("Done "+ nodeNumber);
				dos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
