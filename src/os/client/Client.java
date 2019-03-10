package os.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
	Socket requestSocket;
	ObjectOutputStream out;
	ObjectInputStream in;
	String message = "";
	String option = "";
	String ipaddress;
	Scanner input;

	Client() {
	}

	void run() {
		input = new Scanner(System.in);
		try {
			// 1. creating a socket to connect to the server
			System.out.println("Please Enter your IP Address");
			ipaddress = input.next();
			requestSocket = new Socket(ipaddress, 2004);
			System.out.println("Connected to " + ipaddress + " in port 2004");
			// 2. get Input and Output streams
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());

			do {
				// List of options 1-2 for the user
				message = (String) in.readObject();
				System.out.println(message);
				message = input.next();
				sendMessage(message);

				// Dealing with registration
				if (message.equals("1")) {
					// Dealing with information message to user
					message = (String) in.readObject();
					System.out.println(message);

					// Dealing with name
					message = (String) in.readObject();
					System.out.println(message);
					// Needed two of these otherwise the program would just skip and send a blank
					// line
					message = input.nextLine();
					message = input.nextLine();
					sendMessage(message);

					// Dealing with Employee ID
					message = (String) in.readObject();
					System.out.println(message);
					message = input.next();
					sendMessage(message);

					// Dealing with Email
					message = (String) in.readObject();
					System.out.println(message);
					message = input.next();
					sendMessage(message.toLowerCase());

					// Dealing with Department
					message = (String) in.readObject();
					System.out.println(message);
					// Needed two of these otherwise the program would just skip and send a blank
					// line
					message = input.nextLine();
					message = input.nextLine();
					sendMessage(message);

					// Result of registration, either match was found or user was registered
					message = (String) in.readObject();
					System.out.println(message);
				}

				// Dealing with login
				else if (message.equals("2")) {
					// Dealing with information message to user
					message = (String) in.readObject();
					System.out.println(message);

					// Entering employee Username
					message = (String) in.readObject();
					System.out.println(message);
					// Needed two of these otherwise the program would just skip and send a blank
					// line
					message = input.nextLine();
					message = input.nextLine();
					sendMessage(message);

					// Entering employee ID
					message = (String) in.readObject();
					System.out.println(message);
					message = input.next();
					sendMessage(message);

					// Result of trying to login
					message = (String) in.readObject();
					System.out.println(message);

					// Logged in users will be able to access this block of code
					if (message.contains("Login successful")) {
						// Entering which option (3-7 they wish to choose)
						message = (String) in.readObject();
						System.out.println(message);
						option = input.next();
						sendMessage(option);

						do {
							// Dealing with adding a bug
							if (option.equals("3")) {
								// Just an information message confirming the users choice
								message = (String) in.readObject();
								System.out.println(message);

								// Telling the user to enter the application name + sending name to server
								message = (String) in.readObject();
								System.out.println(message);
								// Needed two of these otherwise the program would just skip and send a blank
								// line
								message = input.nextLine();
								message = input.nextLine();
								sendMessage(message);

								// Telling the user to enter the description + sending desc to server
								message = (String) in.readObject();
								System.out.println(message);
								message = input.nextLine();
								sendMessage(message);

								do {
									// Telling the user to Enter 1/2/3 for the status + sending to serv
									message = (String) in.readObject();
									System.out.println(message);
									message = input.next();
									sendMessage(message);
								} while (!message.equalsIgnoreCase("1") && !message.equalsIgnoreCase("2")
										&& !message.equalsIgnoreCase("3"));
							} // Option 3

							// Dealing with assigning a bug to a user
							if (option.equals("4")) {
								// Telling the user what option they've pressed + sending to serv
								message = (String) in.readObject();
								System.out.println(message);

								// Which bug id they want to send + sending it to serv
								message = (String) in.readObject();
								System.out.println(message);
								message = input.next();
								sendMessage(message);

								// Result of the ID they sent, either it matches or it doesn't
								message = (String) in.readObject();
								System.out.println(message);

								// If theres a match go in here to assign the ID to a user or just keep going if
								// there was no match
								if (message.contains("Bug ID Matched")) {
									// Asking the client to send back the User ID they wanna assign to the bug
									message = (String) in.readObject();
									System.out.println(message);
									message = input.next();
									sendMessage(message);

									// Information message to the client
									message = (String) in.readObject();
									System.out.println(message);
								}

							}

							// Dealing with displaying all non assigned bugs
							if (option.equals("5")) {
								// Telling the user what option they've pressed + sending to serv
								message = (String) in.readObject();
								System.out.println(message);

								// Returning bugs minus assigned bugs
								message = (String) in.readObject();
								System.out.println(message);
							}

							// Dealing with displaying ALL bugs
							if (option.equals("6")) {
								// Telling the user what option they've pressed + sending to serv
								message = (String) in.readObject();
								System.out.println(message);

								// Returning bugs
								message = (String) in.readObject();
								System.out.println(message);
							}

							// Update a bug
							if (option.equals("7")) {
								// Telling the user what option they've pressed + sending to serv
								message = (String) in.readObject();
								System.out.println(message);

								// Prompting the user to enter the Bug ID they want to edit
								message = (String) in.readObject();
								System.out.println(message);
								message = input.next();
								sendMessage(message);

								// Result of the ID they sent, either it matches or it doesn't
								message = (String) in.readObject();
								System.out.println(message);

								if (message.contains("Bug ID Matched")) {
									do {
										// Telling the user to Enter 1/2/3 for the option they want + sending to serv
										message = (String) in.readObject();
										System.out.println(message);
										message = input.next();
										sendMessage(message);
									} while (!message.equalsIgnoreCase("1") && !message.equalsIgnoreCase("2")
											&& !message.equalsIgnoreCase("3"));

									// Letting the user know which option 1/2/3 they chose
									message = (String) in.readObject();
									System.out.println(message);

									// If the user chose to change the status
									if (message.contains("Status")) {
										do {
											// Telling the user to Enter 1/2/3 for the status + sending to serv
											message = (String) in.readObject();
											System.out.println(message);
											message = input.next();
											sendMessage(message);
										} while (!message.equalsIgnoreCase("1") && !message.equalsIgnoreCase("2")
												&& !message.equalsIgnoreCase("3"));

										// Telling the user the status was updated
										message = (String) in.readObject();
										System.out.println(message);
									}

									// If the user chose to change the description
									if (message.contains("Description")) {
										// Telling the user to Enter a new Description
										message = (String) in.readObject();
										System.out.println(message);
										message = input.nextLine();
										message = input.nextLine();
										sendMessage(message);

										// Letting the user know the description was updated
										message = (String) in.readObject();
										System.out.println(message);
									}

									// If the user wants to chanage the assigned employee
									if (message.contains("Employee")) {

										// Letting the user know if there even was an employee assigned to the bug
										message = (String) in.readObject();
										System.out.println(message);

										// Let the user enter + change the assigned worker
										if (message.contains("The bug ID has someone assigned")) {
											// Sending the user they want to assign to the server
											message = (String) in.readObject();
											System.out.println(message);
											message = input.nextLine();
											message = input.nextLine();
											sendMessage(message);

											// Telling the client if the user was found or not
											message = (String) in.readObject();
											System.out.println(message);

											// Then client can progress and change the assigned user later
											if (message.contains("has been found in the System")) {
												// Just telling the client the user was replaced with another on the bug
												// id
												message = (String) in.readObject();
												System.out.println(message);
											}

										} // Bug had someone assigned prior if
									} // Edit assigned employee if
								} // Bug ID matched if
							}

							// Just checking the user actually enters a number corresponding to one of the
							// options
							if (!option.equals("3") && !option.equals("4") && !option.equals("5") && !option.equals("6")
									&& !option.equals("7") && !option.equalsIgnoreCase("n")) {
								System.out.println("Please enter ONLY 3-8 or N");
							}
							// As long as they do not wish to exit ...
							if (!option.equalsIgnoreCase("n")) {
								// Entering which option (3-7 they wish to choose)
								message = (String) in.readObject();
								System.out.println(message);
								option = input.next();
								sendMessage(option);

							}
							// Will log out the user and only permit them to view options 1 + 2 until logged
							// in again
						} while (!option.equalsIgnoreCase("N"));
						System.out.println("Logging Out ...");
					}
				} // Else if (option=2)

				// Reading in "Press y to continue or x to terminate" message + sending back
				message = (String) in.readObject();
				System.out.println(message);
				message = input.next();
				sendMessage(message);

			} while (!message.equalsIgnoreCase("x"));
			System.out.println("Ending Client Connection ...");
		}

		catch (UnknownHostException unknownHost) {
			System.err.println("You are trying to connect to an unknown host!");
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// 4: Closing connection
			try {
				in.close();
				out.close();
				requestSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	// Sending mesasges to the server
	void sendMessage(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
			System.out.println("Sending to server => " + msg);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	// Main method
	public static void main(String args[]) {
		Client client = new Client();
		client.run();
	}
} // Class Client