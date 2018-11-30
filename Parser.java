/*
 * Raman Sathiapalan, Anshika Singh, Usuma Thet
 * CS 6378.001
 * Project 3
 * Due: December 4, 2018
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Parser {
	// Initialize local variables
	int num_nodes = 0;
	int temp_num_nodes = 0;
	static String[][] info_nodes;
	
	static int mean_inter_request_delay; // mean value for inter-request delay (in milliseconds)
	static int mean_cs_exe_time; // mean value for cs-execution time (in milliseconds)
	static int num_req; // number of requests each node should generate
	

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// MAIN
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		
		Parser main = new Parser();

		File config_file = new File("SampleInput.txt");
		info_nodes = main.ReadInput(config_file); // Run method.

		System.out.println(Arrays.deepToString(info_nodes)); // Print for testing. DELETE LATER		
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
						info_nodes[lineCount][2] = temp_splitarr[2].trim(); // port
						info_nodes[lineCount][3] = temp_neighbors.replace(info_nodes[lineCount][0], "").replaceAll(" +", " ").trim(); // all other nodes are neighbors 
												
						lineCount++;					
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return info_nodes;

	}
}