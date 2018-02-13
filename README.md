# CollabHubWebServer
Collaboration Over GitHub (COG)

This repository hosts the implementation of the UserCollaboration Engine and its various components. 
The CollabHubWebServer codebase consists of the implementation of the following:

•	GitHubCollaborator Component: Implemented using Java, this component clones the remote GitHub repository at regular intervals (CloneRemoteRepo.java), and invokes the User ActivityContext Analyzer Component which is responsible for creating the project dependency graph.

•	User ActivityContext Analyzer Component: Creates the project dependency graph (CreateDependencyGraph.java) and stores it with the server. On receiving current artifact content from the collaborating client, creates the current artifact dependency graph (CreateUserArtifactGraph.java) and compares it with the current project graph (CompareGraphs.java) stored with the server. Information regarding each conflicting node is sent forward to the InconsistencyCommunicator Component.

•	InconsistencyCommunicator Component: This component converts conflict messages into messages to be sent to collaborating clients (using rules for identifying clients listed) (InconsistencyCommunicator.java). These messages are stored in the MySQL database which are then retrieved and display by all collaborating clients.

This repository also hosts the implementation of the COGWebServer and COGWebClient. The folder src\main\webapp contains the code for Java Server Pages used in COGWebClient and the HTML file COGAdmin.html contains the code for the configuration page which is used to configure a project with the COG server and to invoke COGWebClient.



