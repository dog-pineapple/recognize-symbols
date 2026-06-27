A web application for automatic recognition of musical notes from sheet music images. 
The system uses a Convolutional Neural Network (CNN) to identify the name, octave and duration. 
The result is visualised on a virtual piano with MIDI sound support.

Stack:
- Spring Boot — main server, REST API, user management
- Flask — microservice for CNN inference
- PostgreSQL — database 
