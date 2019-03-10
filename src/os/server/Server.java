package os.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {
	public static void main(String[] args) throws Exception {
		ServerSocket m_ServerSocket = new ServerSocket(2004, 10);
		int id = 0;
		while (true) {
			Socket clientSocket = m_ServerSocket.accept();
			ClientServiceThread cliThread = new ClientServiceThread(clientSocket, id++);
			cliThread.start();
		}
	}
}

class ClientServiceThread extends Thread {
	Socket clientSocket;
	String message;
	int clientID = -1;
	boolean running = true;
	ObjectOutputStream out;
	ObjectInputStream in;
	// Used for registration/login
	private String userName;
	private String employeeID;
	private String email;
	private String department;
	private String employeeLoginId;
	private String employeeLoginUserName;
	// Assumed unique employee id until found otherwise
	private boolean unique = true;
	private int loggedIn;
	// Used for bug submission
	private String applicationName;
	private String bugDescription;
	private String bugStatus;
	// Used for searching for bug ID
	private String requestedBugId;
	private String requestedUserId;
	private String foundLine = "";
	private String newData = "";
	// Used for options 5 & 6
	private String nonAssignedBugs = "";
	private String allBugs = "";
	// For whatever reason these weren't working as booleans
	private int found;
	private int userIdFound;
	// Used for updating a bug status (option 7)
	private String updateChoice;
	private String newDescription;
	private String oldDescription;
	private String assignment;
	private String requestedUser;
	private String userExists;
	private String oldAssignedUser;
	// Used to generate unique random bug ID
	private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	ClientServiceThread(Socket s, int i) {
		clientSocket = s;
		clientID = i;
	}

	// Called to generate a unique bug ID
	public String generateString() {
		int maxLength = 9;
		Random random = new Random();
		StringBuilder builder = new StringBuilder(maxLength);

		// Looping 9 times, one for each char
		for (int i = 0; i < maxLength; i++) {
			builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
		}
		// Generates a random ID that has may have a quintillion different combinations
		// (1/64^9)
		return builder.toString();
	}

	// Used to get the OS platform
	public String getOperatingSystem() {
		String os = System.getProperty("os.name");
		// Returning the os eg - windows 10
		return os;
	}

	// Method for sending messages to the client
	void sendMessage(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
			System.out.println("Sending to client => " + msg);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	// Server ran
	public void run() {
		System.out.println(
				"Accepted Client : ID - " + clientID + " : Address - " + clientSocket.getInetAddress().getHostName());
		try {
			out = new ObjectOutputStream(clientSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(clientSocket.getInputStream());
			System.out.println("Accepted Client : ID - " + clientID + " : Address - "
					+ clientSocket.getInetAddress().getHostName());

			// While client does not enter "x", the block of code will execute
			do {
				// Firstly only presenting two options to the client until login is complete
				sendMessage("Press 1 to Register with the System\n" + "Press 2 to Log in to the System\n");
				message = (String) in.readObject();

				// Registration
				if (message.equalsIgnoreCase("1")) {
					unique = true;
					// Writing to the file
					BufferedWriter writer = new BufferedWriter(new FileWriter("Users.txt", true));
					// Reading the file for validation (Making sure email + id is unique)
					Scanner scanner = new Scanner(new File("Users.txt"));
					sendMessage("You have chosen to Register");

					sendMessage("Please enter your Name");
					userName = (String) in.readObject();

					sendMessage("Please enter your Employee ID");
					employeeID = (String) in.readObject();

					sendMessage("Please enter your Email");
					email = (String) in.readObject();

					sendMessage("Please enter your Department");
					department = (String) in.readObject();

					// Iterates through the users in the file to check to see if the newly entered
					// information is unique or not
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine();
						if (line.contains(email) || line.contains(employeeID)) {
							sendMessage(
									"*Your Email address and Employee ID must be unique*\n*Your account has not been registered*");
							// Making sure their info will not be written to the file when the while
							// terminates
							unique = false;
						}
					}
					// If there was NO MATCH write their information to the file
					if (unique != false) {
						// Sending info to file if no match & also notifying the user of their
						// registration
						writer.newLine();
						writer.write(("Username: " + userName + " | Employee ID: " + employeeID + " | Email: " + email
								+ " | Department: " + department));
						sendMessage("Welcome " + userName
								+ ", Your account is now registered with the system and you may Log in.");
					}

					// Closing files
					scanner.close();
					writer.close();
				} // Registration if ends

				// Login, option 3-8 contained within here since only logged in users may access
				// them
				else if (message.equalsIgnoreCase("2")) {
					// Logged in = false until user verified/logged in correctly
					loggedIn = 0;
					// Reading the file for validation (Making sure email + id is unique)
					Scanner scanner = new Scanner(new File("Users.txt"));

					sendMessage("You have chosen to Login");

					sendMessage("Please enter your User Name (Case Sensitive)");
					employeeLoginUserName = (String) in.readObject();

					sendMessage("Please enter your unique Employee ID");
					employeeLoginId = (String) in.readObject();

					// Scans through all lines (users) to see if the information entered matches a
					// specific line
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine();
						if (line.contains(employeeLoginId) && line.contains(employeeLoginUserName)) {
							// ID Matched
							loggedIn = 1;
							sendMessage("Login successful");
						}
					}
					// If theres no match it's gonna return the client to the menu to login/register
					// again, logic for this is handled client side
					if (loggedIn == 0) {
						sendMessage("*The Username & Employee ID Entered does not match any registered users*");
					}
					// Handles all the 3-8 Options for logged in users
					if (loggedIn == 1)
						do {
							sendMessage("\n<Welcome to the Bug Report System " + employeeLoginUserName + ">\n"
									+ "Press 3 to Add a bug record to the System\n"
									+ "Press 4 to Assign a bug record to a registered user\n"
									+ "Press 5 to View the bugs not assigned to any developer\n"
									+ "Press 6 to View ALL bugs in the system\n" + "Press 7 to Update a bug's details\n"
									+ "Press N to Log Out\n");
							message = (String) in.readObject();

							// Adding a bug to the Bug file
							if (message.equalsIgnoreCase("3")) {
								BufferedWriter bugWriter = new BufferedWriter(new FileWriter("Bugs.txt", true));
								bugStatus = "0";
								// Used for date + time + formatting
								DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
								LocalDateTime now = LocalDateTime.now();
								sendMessage("You have chosen to Add a bug record to the System");

								// Sending + reading in
								sendMessage("Enter the Application Name");
								applicationName = (String) in.readObject();

								// Sending + reading in
								sendMessage("Enter a Description of the Issue");
								bugDescription = (String) in.readObject();

								// Sending + reading in
								do {
									sendMessage("Enter 1 for Open 2 for Assigned 3 for Closed");
									bugStatus = (String) in.readObject();
								} while (!bugStatus.equalsIgnoreCase("1") && !bugStatus.equalsIgnoreCase("2")
										&& !bugStatus.equalsIgnoreCase("3"));

								// Switch assigning a status
								switch (bugStatus) {
								case "1":
									bugStatus = "Open";
									break;
								case "2":
									bugStatus = "Assigned";
									break;
								case "3":
									bugStatus = "Closed";
									break;
								default:
									// Shouldn't ever go into here anyway
									bugStatus = "NULL";
								}
								// Writing the bug to the file
								bugWriter.write(("Unique ID: " + generateString().toUpperCase()
										+ " | Application Name: " + applicationName + " | Date & Time: "
										+ dtf.format(now) + " | Platform: " + getOperatingSystem().toLowerCase()
										+ " | Description: " + bugDescription + " | Status: " + bugStatus + " |\n"));
								bugWriter.close();
							} // If (option 3)

							// Assigning a bug record to a registered user
							if (message.equalsIgnoreCase("4")) {
								Scanner bugScanner = new Scanner(new File("Bugs.txt"));
								found = 0;
								sendMessage("You've chosen to Assign a bug record to a registered user");

								sendMessage("Please enter the ID of the bug you want to assign");
								requestedBugId = (String) in.readObject();

								// If the bug ID entered matches an actual bug ID in the file found is true(1)
								while (bugScanner.hasNextLine()) {
									String line = bugScanner.nextLine();
									if (line.contains("Unique ID: " + requestedBugId)) {
										// Preserving the line it's found on in this variable
										foundLine = line;
										found = 1;
									}
								}
								// Tells the user no match and returns them to the menu
								if (found == 0) {
									sendMessage("*The bug ID entered does not match any on the System*");
									// If found is true
								} else if (found != 0) {
									// Make sure the user ID matches an ID in the users file
									Scanner userScanner = new Scanner(new File("Users.txt"));
									// Originally set to false(0)
									userIdFound = 0;
									// Bug ID is assumed matching if this current if statement is accessed
									sendMessage("Bug ID Matched");

									sendMessage("Please enter the ID of the user you want to assign to the BugID");
									requestedUserId = (String) in.readObject();

									// Checking to see if the user entered matches a registered user
									while (userScanner.hasNextLine()) {
										String line = userScanner.nextLine();
										if (line.contains(requestedUserId)) {
											sendMessage("*User <" + requestedUserId + "> Found*\nNow assigning User: "
													+ requestedUserId + " to Bug ID: " + requestedBugId);
											userIdFound = 1;
										}
									}

									// Just tell the user their attempt of matching the user ID failed + they get
									// returned to the menu
									if (userIdFound == 0) {
										sendMessage("*Sorry, User <" + requestedUserId + "> was not found*\n ");
									}

									// Assign the verified user to the end of the Bug, nothing sent to the user
									// everything in here is done in the file
									else if (userIdFound == 1) {
										// Gotta clear the var otherwise if the client comes back in here info will be
										// written to the file multiple times
										newData = "";
										Scanner appendBugFile = new Scanner(new File("Bugs.txt"));
										while (appendBugFile.hasNextLine()) {
											// Looping for each line
											String line = appendBugFile.nextLine();
											// If line is found that matches the bug ID the user requested then append
											// the registered username to the end
											if (line.contains("Unique ID: " + requestedBugId)) {
												line = line.replace(foundLine,
														foundLine + " User Responsible: " + requestedUserId + " |");
												newData += line + "\n";
											}
											// Otherwise just copy the line as it is to newData
											else {
												// newData will populate another file
												newData += line + "\n";
											}
										}
										// This got a bit messy, basically I wanted to clear the file and then put
										// the new information in it (newData) that contains the user responsible,
										// .append wasn't working. I spent a long time on this so it's working anyway
										// and I'm unlikely to change it due to time constraints

										// Making the bugs file blank temporarily
										BufferedWriter writeToBug = new BufferedWriter(new FileWriter("Bugs.txt"));
										writeToBug.write("");
										writeToBug.close();

										// Same file, just putting in the new data with user responsible appended
										BufferedWriter writeToNewBug = new BufferedWriter(
												new FileWriter("Bugs.txt", true));
										writeToNewBug.write(newData);
										writeToNewBug.close();
										appendBugFile.close();
									} // else if

									bugScanner.close();
									userScanner.close();
								} // else if
							} // Option 4

							// View bugs not assigned to a registered user
							if (message.equalsIgnoreCase("5")) {
								// Gotta clear it each time this option is selected
								nonAssignedBugs = "";
								sendMessage("You've chosen to view the bugs NOT assigned to a developer");
								Scanner scanThrough = new Scanner(new File("Bugs.txt"));

								while (scanThrough.hasNextLine()) {
									// Looping for each line
									String line = scanThrough.nextLine();
									// If the bug has a user responsible, ignore it don't send it back
									if (line.contains("User Responsible")) {
										// Do nothing
									}
									// Otherwise just copy the line as it is to the String + return it
									else {
										nonAssignedBugs += line + "\n";
									}
								}
								sendMessage(nonAssignedBugs);
								scanThrough.close();
							} // Option 5

							// View all the bugs
							if (message.equalsIgnoreCase("6")) {
								// Gotta clear it each time this option is selected
								allBugs = "";
								sendMessage("You've chosen to view all Bugs in the System");
								Scanner scanThrough = new Scanner(new File("Bugs.txt"));

								while (scanThrough.hasNextLine()) {
									// Looping for each line
									String line = scanThrough.nextLine();
									allBugs += line + "\n";
								}
								sendMessage(allBugs);
								scanThrough.close();
							}

							// Update a bug
							if (message.equalsIgnoreCase("7")) {
								// Again gotta check the ID entered by the user actually matches one in the file
								Scanner bugScanner = new Scanner(new File("Bugs.txt"));
								found = 0;
								newData = "";
								sendMessage("You've chosen to update a bug");

								// What bug id
								sendMessage("Please enter the Bug ID you wish to edit");
								requestedBugId = (String) in.readObject();

								while (bugScanner.hasNextLine()) {
									String line = bugScanner.nextLine();
									if (line.contains("Unique ID: " + requestedBugId)) {
										// Wanna copy that line where the ID was found (preserves found line)
										foundLine = line;
										found = 1;
									}
								}
								bugScanner.close();
								if (found == 0) {
									// Tells the user no match and returns them to the menu
									sendMessage(
											"*The bug ID entered does not match any on the System*\nReturning you to the Menu ...");
								} else if (found == 1) {
									newData = "";
									sendMessage("Bug ID Matched");
									do {
										sendMessage(
												"Enter 1 to Update Status 2 to update the Description 3 to change Assigned Employee");
										updateChoice = (String) in.readObject();
									} while (!updateChoice.equalsIgnoreCase("1") && !updateChoice.equalsIgnoreCase("2")
											&& !updateChoice.equalsIgnoreCase("3"));

									// 1 = User wants to update the status of the bug
									if (updateChoice.equals("1")) {
										sendMessage("You have chosen to Update a Status");
										// Making sure the user enters either 1/2/3 for the NEW status
										do {
											sendMessage("Enter 1 for Open 2 for Assigned 3 for Closed");
											bugStatus = (String) in.readObject();
										} while (!bugStatus.equalsIgnoreCase("1") && !bugStatus.equalsIgnoreCase("2")
												&& !bugStatus.equalsIgnoreCase("3"));

										// Switch for the three possibilites (1/2/3), each case deals with replacing the
										// original status with the requested one
										switch (bugStatus) {
										case "1":
											Scanner appendBugFile = new Scanner(new File("Bugs.txt"));
											bugStatus = "Open";
											// Doing the same thing I did with appending someone to the end, long winded
											// but it works
											while (appendBugFile.hasNextLine()) {
												// Looping for each line
												String line = appendBugFile.nextLine();
												// If line is found that matches the bug ID the user requested then
												// append the username to the end
												if (line.contains("Unique ID: " + requestedBugId)) {
													// Storing the replaced status along with the whole line in newData
													// It should only ever go into one of these
													line = line.replace(foundLine,
															foundLine.replace("Closed", bugStatus));
													line = line.replace(foundLine,
															foundLine.replace("Assigned", bugStatus));
													// newData += whatever it already had in it + this adjusted line
													newData += line + "\n";
												}
												// Otherwise just copy the line as it is to newData
												else {
													// newData will populate another file
													newData += line + "\n";
												}
											}
											// Making the bugs file blank temporarily
											BufferedWriter clearFile = new BufferedWriter(new FileWriter("Bugs.txt"));
											clearFile.write("");
											clearFile.close();

											// Same file, just putting in the new data with user responsible appended
											BufferedWriter changeStatus = new BufferedWriter(
													new FileWriter("Bugs.txt", true));
											changeStatus.write(newData);
											changeStatus.close();
											appendBugFile.close();
											sendMessage("Bug ID " + requestedBugId + " is now Set to " + bugStatus
													+ " ...");
											break;
										case "2":
											newData = "";
											Scanner appendBugFile2 = new Scanner(new File("Bugs.txt"));
											bugStatus = "Assigned";
											// Doing the same thing I did with appending someone to the end, long winded
											// but it works
											while (appendBugFile2.hasNextLine()) {
												// Looping for each line
												String line = appendBugFile2.nextLine();
												// If line is found that matches the bug ID the user requested then
												// append
												// the username to the end
												if (line.contains("Unique ID: " + requestedBugId)) {
													// Storing the replaced status along with the whole line in newData
													line = line.replace(foundLine,
															foundLine.replace("Open", bugStatus));
													line = line.replace(foundLine,
															foundLine.replace("Closed", bugStatus));
													newData += line + "\n";
												}
												// Otherwise just copy the line as it is to newData
												else {
													// newData will populate another file
													newData += line + "\n";
												}
											}
											// Making the bugs file blank temporarily
											BufferedWriter clearFile2 = new BufferedWriter(new FileWriter("Bugs.txt"));
											clearFile2.write("");
											clearFile2.close();

											// Same file, just putting in the new data with user responsible appended
											BufferedWriter changeStatus2 = new BufferedWriter(
													new FileWriter("Bugs.txt", true));
											changeStatus2.write(newData);
											changeStatus2.close();
											appendBugFile2.close();
											sendMessage("Bug ID " + requestedBugId + " is now Set to " + bugStatus
													+ " ...");
											break;
										case "3":
											newData = "";
											Scanner appendBugFile3 = new Scanner(new File("Bugs.txt"));
											bugStatus = "Closed";
											// Doing the same thing I did with appending someone to the end, long winded
											// but it works
											while (appendBugFile3.hasNextLine()) {
												// Looping for each line
												String line = appendBugFile3.nextLine();
												// If line is found that matches the bug ID the user requested then
												// append
												// the username to the end
												if (line.contains("Unique ID: " + requestedBugId)) {
													// Storing the replaced status along with the whole line in newData
													line = line.replace(foundLine,
															foundLine.replace("Open", bugStatus));
													line = line.replace(foundLine,
															foundLine.replace("Assigned", bugStatus));
													newData += line + "\n";
												}
												// Otherwise just copy the line as it is to newData
												else {
													// newData will populate another file
													newData += line + "\n";
												}
											}
											// Making the bugs file blank temporarily
											BufferedWriter clearFile3 = new BufferedWriter(new FileWriter("Bugs.txt"));
											clearFile3.write("");
											clearFile3.close();

											// Same file, just putting in the new data with user responsible appended
											BufferedWriter changeStatus3 = new BufferedWriter(
													new FileWriter("Bugs.txt", true));
											changeStatus3.write(newData);
											changeStatus3.close();
											appendBugFile3.close();
											sendMessage("Bug ID " + requestedBugId + " is now Set to " + bugStatus
													+ " ...");
											break;
										default:
											// Shouldn't ever go into here anyway
											bugStatus = "NULL";
										}
									}
									if (updateChoice.equals("2")) {
										Scanner appendBugFile = new Scanner(new File("Bugs.txt"));
										sendMessage("You have chosen to Update a Description");

										sendMessage("Please enter a new Description for the Bug");
										newDescription = (String) in.readObject();

										while (appendBugFile.hasNextLine()) {
											// Looping for each line
											String line = appendBugFile.nextLine();
											// If line is found that matches the bug ID the user requested then append
											// the registered username to the end
											if (line.contains("Unique ID: " + requestedBugId)) {
												// I wanna take out the description, the description will always be what
												// is between these two strings
												String pattern1 = "Description: ";
												String pattern2 = " | Status:";
												String text = line;
												// Take out whats between the two strings
												Pattern p = Pattern.compile(
														Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
												Matcher m = p.matcher(text);
												while (m.find()) {
													oldDescription = m.group(1);
												}
												// Repalces oldDescription that was found from regex above with the one
												// the user entered
												line = line.replace(foundLine,
														foundLine.replace(oldDescription, newDescription));
												newData += line + "\n";
											}
											// Otherwise just copy the line as it is to newData
											else {
												// newData will populate another file
												newData += line + "\n";
											}
										}
										BufferedWriter clearFile3 = new BufferedWriter(new FileWriter("Bugs.txt"));
										clearFile3.write("");
										clearFile3.close();

										// Same file, just putting in the new data with user responsible appended
										BufferedWriter changeStatus = new BufferedWriter(
												new FileWriter("Bugs.txt", true));
										changeStatus.write(newData);
										changeStatus.close();
										sendMessage("Bug ID " + requestedBugId + " description is now updated ...");
										appendBugFile.close();
									}
									if (updateChoice.equals("3")) {
										Scanner empScanner = new Scanner(new File("Bugs.txt"));
										// Need to check if the bug was even assigned to anyone in the first place
										assignment = "0";
										sendMessage("You have chosen to Update an Assigned Employee");

										// Checking to see if the user entered matches a registered user
										while (empScanner.hasNextLine()) {
											String line = empScanner.nextLine();
											if (line.contains(requestedBugId) && (line.contains("User Responsible"))) {
												String pattern1 = "User Responsible: ";
												String pattern2 = " |";
												String text = line;

												// Take out whats between the two strings
												Pattern p = Pattern.compile(
														Pattern.quote(pattern1) + "(.*?)" + Pattern.quote(pattern2));
												Matcher m = p.matcher(text);
												while (m.find()) {
													oldAssignedUser = m.group(1);
												}
												System.out.println("OLD USER: " + oldAssignedUser);
												assignment = "1";
											}
										}
										empScanner.close();

										// There was already a user assigned to the bug
										if (assignment.equals("1")) {
											Scanner userScanner = new Scanner(new File("Users.txt"));
											userExists = "0";
											sendMessage(
													"The bug ID has someone assigned\nYou may now change the Assigned user");

											sendMessage(
													"Please enter the Registered User you wish to assign to the Bug");
											requestedUser = (String) in.readObject();

											// Gotta check the user exists in the users file
											while (userScanner.hasNextLine()) {
												String line = userScanner.nextLine();
												if (line.contains(requestedUser)) {
													userExists = "1";
												}
											}
											// User doesn't exist
											if (userExists.equals("0")) {
												sendMessage(
														"*The user " + requestedUser + " was NOT found in our System*");
											}

											// User exists
											if (userExists.equals("1")) {
												Scanner changeEngineer = new Scanner(new File("Bugs.txt"));
												sendMessage(
														"The user " + requestedUser + " has been found in the System");
												while (changeEngineer.hasNextLine()) {
													// Looping for each line
													String line = changeEngineer.nextLine();
													// If line is found that matches the bug ID the user requested then
													// change the user
													if (line.contains("Unique ID: " + requestedBugId)) {
														// Repalces oldDescription that was found from regex above with
														// the one the user entered
														line = line.replace(foundLine,
																foundLine.replace(oldAssignedUser, requestedUser));
														newData += line + "\n";
													}
													// Otherwise just copy the line as it is to newData
													else {
														// newData will populate another file
														newData += line + "\n";
													}
												}
												changeEngineer.close();
											}

											BufferedWriter clearFile = new BufferedWriter(new FileWriter("Bugs.txt"));
											clearFile.write("");
											clearFile.close();

											// Same file, just putting in the new data with user responsible appended
											BufferedWriter changeWorker = new BufferedWriter(
													new FileWriter("Bugs.txt", true));
											changeWorker.write(newData);
											changeWorker.close();
											sendMessage("User " + oldAssignedUser + " has been replaced with "
													+ requestedUser + " on the Bug ID " + requestedBugId + " ...");
											userScanner.close();
										}

										// There was no user assigned to the bug
										if (assignment.equals("0")) {
											sendMessage(
													"\n*The bug ID has nobody assigned to it*\nPlease use option 4 in the menu to assign an employee to it");
										}
									} // if user wants
								} // if bug ID matched
								bugScanner.close();
							} // message 7 (update bug condition)
								// User logs out if N is entered
						} while (!message.equalsIgnoreCase("N"));
					// Closing files
					scanner.close();
				} // Login ends

				sendMessage("Press Y Login/Register or X to terminate");
				message = (String) in.readObject();
			} while (!message.equalsIgnoreCase("x")); // Do while

			// Client terminated
			System.out.println(
					"Ending Client : ID - " + clientID + " : Address - " + clientSocket.getInetAddress().getHostName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	} // Run()
} // Class Server
