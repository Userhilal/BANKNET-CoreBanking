\# BANKNET Core Banking



BANKNET Core Banking is a desktop banking management application built with JavaFX, Hibernate, MySQL, and Docker.

The application allows bank administrators and clients to manage accounts, transactions, savings goals, notifications, messages, and account security features.



\## Features



\* Admin and client authentication

\* Client dashboard

\* Bank account management

\* Internal transfers

\* Transfers to third-party accounts

\* Savings goals management

\* Transaction limits

\* Notifications

\* Messaging system between clients and admin

\* Suspicious activity tracking

\* Account activation and blocking

\* Multilingual interface support: French, English, and Arabic

\* MySQL database integration using Hibernate

\* Docker-based MySQL setup



\## Technologies Used



\* Java 17

\* JavaFX

\* Hibernate ORM

\* MySQL 8

\* Maven

\* Docker

\* Docker Compose

\* BCrypt for password hashing

\* FXML and CSS for the user interface



\## Project Structure



```text

BANKNET-CoreBanking/

│

├── src/main/java/com/banknet/

│   ├── controller/      # JavaFX controllers

│   ├── dao/             # Data access layer

│   ├── model/           # Entity classes

│   ├── service/         # Business logic

│   ├── util/            # Utility classes

│   └── MainApp.java     # Application entry point

│

├── src/main/resources/

│   ├── com/banknet/view/    # FXML views and CSS

│   ├── com/banknet/i18n/    # Language files

│   └── hibernate.cfg.xml    # Hibernate configuration

│

├── docker-compose.yml

├── database\_setup.sql

├── pom.xml

└── README\_DOCKER.md

```



\## Prerequisites



Before running the project, make sure you have installed:



\* Java JDK 17

\* Maven

\* Docker Desktop

\* Git



\## Database Setup with Docker



Start the MySQL container:



```bash

docker-compose up -d

```



Check that the container is running:



```bash

docker-compose ps

```



The Docker configuration creates:



\* Database: `banknet\_db`

\* User: `bank\_user`

\* Password: `bank\_pass`

\* MySQL container: `banknet\_mysql`



\## Important MySQL Port Note



In `docker-compose.yml`, MySQL is exposed using:



```yaml

ports:

&#x20; - "3307:3306"

```



So if the application cannot connect to the database, make sure the Hibernate connection URL matches the Docker port.



You can either change Hibernate to:



```xml

jdbc:mysql://localhost:3307/banknet\_db

```



or change Docker to expose port `3306`:



```yaml

ports:

&#x20; - "3306:3306"

```



\## Run the Application



After starting MySQL, run the JavaFX application:



```bash

mvn clean compile javafx:run

```



\## Default Accounts



\### Administrator



```text

Login: admin

Password: admin123

```



\### Test Client



```text

Login: client1

Password: client123

CIN: AB123456

Initial balance: 1000.00

```



\## Useful Docker Commands



Stop the database container:



```bash

docker-compose down

```



Stop the container and delete stored data:



```bash

docker-compose down -v

```



View MySQL logs:



```bash

docker-compose logs mysql

```



Restart the database container:



```bash

docker-compose restart

```



\## Maven Commands



Compile the project:



```bash

mvn clean compile

```



Run the application:



```bash

mvn javafx:run

```



\## Notes



This project is designed for educational purposes and demonstrates the structure of a desktop banking application using JavaFX, Hibernate, and MySQL.



Default credentials are included only for testing. For real deployment, credentials must be changed and secured properly.



\## Author



Hind Hilal



