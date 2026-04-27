# Hotel Nova: Professional Hotel Management System

Hotel Nova is a robust, desktop-based management system designed to streamline hotel operations. Developed with a focus on scalability and clean architecture, this project implements a clear separation of concerns, making it a reliable tool for both administrators and front-desk staff.
# System Architecture

This project adheres to professional software engineering standards, specifically utilizing MVC (Model-View-Controller) and DAO (Data Access Object) patterns. This ensures that the business logic, data access, and user interface remain decoupled and maintainable.
Visual Documentation

To display these diagrams in your repository, save the images I generated for you in a folder named /assets at the root of your project.

1. Class Diagram (UML)

2. Use Case Diagram (UML)
   🚀 Key Features

   Layered Architecture: Strict separation between Controllers, Services, DAO, and Models.

   Persistent Storage: SQLite integration with structured database initialization (DbInit).

   Role-Based Access: Secure login module with privileges for Recepcionists and Administrators.

   CRUD Operations: Full lifecycle management for Guests, Rooms, and Bookings.

   Activity Logging: Automated logging system to track system activity and errors.

   Responsive UI: Built with JavaFX for a fluid and professional user experience.

🛠️ Technology Stack

    Language: Java 17+

    UI Framework: JavaFX

    Build Tool: Apache Maven

    Database: SQLite

    Design Patterns: MVC, DAO, Singleton (for Config)

⚙️ Getting Started
Prerequisites

    JDK 17 or higher installed.

    Maven installed and configured in your system path.

Installation

    Clone the repository:
    Bash

    git clone https://github.com/sixorca00-collab/ASSESMENT-M5-Java.git

    Navigate to the project directory:
    Bash

    cd hotel-nova

Running the Application

The project is configured for seamless execution using Maven. Simply run the following command in your terminal:
Bash
# start
mvn javafx:run

This command will compile the project, resolve dependencies, and launch the Hotel Nova interface.
📖 Roadmap

    [x] Initial Architecture (MVC/DAO)

    [x] Database Configuration & Models

    [x] Authentication & Login Logic

    [x] Booking & Check-in/Check-out Module

    [ ] Advanced Reporting & Exporting (PDF/Excel)

    [ ] Automated UI Testing

👨‍💻 Author

[Juan Pablo Olarte Alvarez]
Junior Software Developer
Clan: Hamilton || C.C: 1021923969 || Email: olartealvarezjuanpablo28@gmail.com
Building scalable solutions with clean code.

