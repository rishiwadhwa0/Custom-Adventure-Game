#### Fixes based on Adventure 1 Feedback:
- [x] Rename commandChecker to formatCommand and change the calls to the function
- [x] fix any other naming issues you see
- [X] Check for invalid JSON by placing functions in other classes (i.e. assigning responsibilities to my objects)
- [] make tests more organized this time

#### Plan to accomplish changes for Adventure 2:
- [x] Edit World class to represent new JSON schema
- [x] Allow two new user-commands for this assignment and implement functionality to handle these commands
- [x] Write a new JSON file to work with
- [X] Allow user to specify URL of filename and load JSON from there using command line
- [x] Add a new feature (i.e. extending the game) - Create a monster which user has to defeat based on chance!!!

#### Updates:
1. I ran into an issue with checking the commands. My original logic structure made it difficult to introduce two new
commands, so I re-did the entire command checking sequence. Overall, it led to a nicer, modular implementation.
2. Scanner.nextInt() consumes the integer, but not the "/n", so the next time you do Scanner.nextLine() it will take in
that "/n". To get circumvent this, just take in the next like ride after you take in the int to consume the "\n"

