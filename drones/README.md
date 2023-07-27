# Delivery Drones Assignment System

## Beforehand notes

When launching the server, I also create a client to show the activity working (without having to run multiple files).

I strongly recommend to change the `waitTime` of AssignmentThread.java (line 20) to something lower than 60000 milliseconds for testing the application in a small time.

For readability I added a small sleep time in the subactivity that handles the assignments and drone states so the text flows slowly and it doesn't write dozens of lines almost instatly. It can be configured in the line 21 of AssignmentThread.java (it is the `sleepTime`) or reduced to 0 to avoid this sleep.

## Overview

This Java application implements a Delivery Drones Assignment System that efficiently assigns deliveries to a set of delivery drones. The system comprises two complex sub-activities: one for continuously monitoring the position of drones and the other for analyzing and assigning deliveries to available drones. Additionally, the system includes a graphical user interface (GUI) developed using the SWING library to start and monitor the main activity with proper logs of ongoing processes. To facilitate drone position updates, the application employs a server application accessed through a TCP connection.

## Features

- Efficient delivery assignment: The application intelligently assigns deliveries to the first available drone in the stationary state, streamlining the delivery process.

- Real-time position monitoring: The system continuously monitors the position of drones, ensuring up-to-date information is available for efficient assignment.

- Graceful handling of unavailable drones: If no drone is available for assignment, the system waits for one minute before retrying, ensuring that deliveries are eventually assigned.

- User-friendly GUI: The graphical user interface allows users to start the main activity and provides real-time logs, providing a clear view of the ongoing process.

- Server application for drone position updates: The system uses a server application accessed through a TCP connection to keep track of the drones' positions.

## How to Use

Run the Application: To run the application, execute the main class, which initializes the server and the delivery drones assignment system.

Enter Inputs: The GUI will prompt you to provide the necessary inputs, including the set of delivery drones and deliveries to be assigned.

Monitoring and Assignment: Once the activity starts, the system will begin continuously monitoring the drones' positions. Simultaneously, the deliveries will be analyzed and assigned to the first available drone.

View Logs: The GUI will display proper logs of the ongoing processes, showing the assignment events, drone positions, and any waiting times in case of unavailability.

Completing Deliveries: Once all deliveries are completed, the assignment sub-activity will end, and an interrupt event will be sent to the position monitoring sub-activity, terminating it.

## Implementation Details:

- Classes: The Java application includes all classes implementing the requirements, following the studied methodology.
- Graphical User Interface: The GUI is developed using the SWING library, allowing users to start the main activity and monitor the processes with informative logs.
- Server Application: The system includes a server application accessed through a TCP connection, which receives drone IDs and returns their positions. This ensures real-time updates on drone positions.
- The application has been thoroughly tested to handle various scenarios, ensuring stability and reliability. Proper exception handling is implemented to handle unexpected situations gracefully.

