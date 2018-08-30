# RoomStateManager Eclipse Java project

This folder is the root directory of an Eclipse Java project which contains source code for the RoomStateManager, a class 
(and its ancillary classes) that automatically manages a studyroom state (open and closed) according to the study room opening 
hours by updating the room state into the SIB.

To build the project, run "gradlew build" in this directory. The output of the aforementioned command is the file 
RoomStateManager.jar. Bear in mind that to run this jar is dependent on the file libs/sofia_kp.jar. RoomStateManager.jar is 
already present in build/libs if you don't want to build the project yourself.
