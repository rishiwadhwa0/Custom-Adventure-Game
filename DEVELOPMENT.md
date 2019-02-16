#### Fixes based on Adventure 1 Feedback:
- [x] Rename commandChecker to formatCommand and change the calls to the function
- [x] fix any other naming issues you see
- [] Check for invalid JSON by placing functions in other classes (i.e. assigning responsibilities to my objects)
- [] make tests more organized this time

#### Plan to accomplish changes for Adventure 2:
- [x] Edit World class to represent new JSON schema
- [x] Allow two new user-commands for this assignment and implement functionality to handle these commands
- [x] Write a new JSON file to work with
- [] Allow user to specify URL of filename and load JSON from there using command line
- [] Add a new feature (i.e. extending the game) - Create a monster which user has to defeat based on chance!!!

#### Updates:
1. I ran into an issue with checking the commands. My original logic structure made it difficult to introduce two new
commands, so I re-did the entire command checking sequence. Overall, it led to a nicer, modular implementation.