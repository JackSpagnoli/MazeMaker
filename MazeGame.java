import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

class MazeGame{
    /**
     * This class allows the user to move a character through the maze attempting to find the exit.
     * It makes an object of MazeSolver to run in a separate thread, allowing it to solve the maze in the background.
     */

    private class Stack{


        int[][] stack=new int[0][];
        Stack(){}
        void push(int[] value){
            if (this.stack.length==0){
                this.stack=new int[][]{value};
            }else{
                int[][] temp=this.stack;
                this.stack=new int[temp.length+1][];
                this.stack[this.stack.length-1]=value;
                System.arraycopy(temp,0,this.stack,0,temp.length);
            }
        }
        int length(){
            return this.stack.length;
        }

    }
    private char[][] maze=new char[1][];
    private boolean[][] seen;
    private character player;
    private int[] endCo;
    private final Stack route=new Stack();
    private final MazeSolver m;
    MazeGame(File maze) { //Constructor takes the file containing the maze as a parameter
        this.m=new MazeSolver(maze); //Creates an object of the mazeSolver to run in the background
        Thread solver=new Thread(this.m); //Creates a thread of that object
        solver.start(); //Starts thread to start it solving
        while (!this.m.finishedReading){ //To stop collisions I make the game wait until the mazeSolver declares that it has finished reading
            try {
                Thread.sleep(200); //Wait 200 millis to reduce wasted checks
            }catch (Exception ignored){}
        }
        readMaze(maze); //Read in the maze
        this.m.canSave = true; //Tell the solver that it can save its solved maze, as this class no longer needs to use it
        setUp(); //Sets up the game
        play(); //PLays the game
    }
    private void play(){
        this.seen=new boolean[this.maze.length][this.maze[0].length]; //Sets the seen array to be of the relevant size and all false (Hidden)
        boolean inPlay=true; //Used to track if the game is being played
        this.route.push(new int[]{this.player.getPosi(),this.player.getPosj()}); //The starting position is pushed to the route stack
        while (inPlay){ //While the game is being player
            this.player.setStandingOn(this.maze[this.player.getPosj()][this.player.getPosi()]); //The player's position is updated
            updateSeen(this.player.getPosi(),this.player.getPosj()); //The seen array is updated based upon the new position

            playerMove(); //The player is asked to declare a move

            this.route.push(new int[]{this.player.getPosi(),this.player.getPosj()}); //The new location is pushed to a stack

            inPlay=!(this.endCo[0]==this.player.getPosi()&&this.endCo[1]==this.player.getPosj()); //Updates inPlay to check if the player has reached the end of the maze
        }
        printMaze(); //The maze is printed
        if (this.endCo[0]==this.player.getPosi()&&this.endCo[1]==this.player.getPosj()){ //If the player is at the end of the maze
            System.out.println("Exit found!"); //A message is printed
        }
        while (true){ //Makes the program wait for the maze to be solved...
            if (m.isSolved()) { //Checks if the maze solver object has finished
                System.out.println("Best route is of length " + m.getRouteDistance() + ":"); //Prints the length of the solved route
                this.m.printTracedMaze(); //The solved maze is printed
                System.out.println("Your route was of length " + this.route.length()); //The length of the player's route is printed
                System.out.println("You uncovered " + getSeenPercentage() + "% of the maze"); //The percentage of the maze uncovered is printed
                return; //Return breaks the infinite loop
            }
        }
    }
    private double getSeenPercentage(){ //Finds the percentage of the maze that the player uncovered
        int count = 0;
        for (int j=0;j<this.maze.length;j++){
            for (int i=0;i<this.maze[j].length;i++){
                if (this.seen[j][i]){
                    count++; //Loops through the maze array and keeps a count of how many cells are uncovered
                }
            }
        }
        return (double)(count*100) / (double)(this.maze.length * this.maze[0].length); //A simple percentage is calculated

    }
    private boolean[] getScan(int i, int j){ //This function returns a boolean array showing which directions the program needs to uncover cells
        switch (this.maze[j][i]){ //Based upon the character at the coordinate
            case '┌':
                return new boolean[]{false,true,true,false}; // Returns a boolean array of the 4 orthographic directions, true showing that the player can move in that direction, false meaning they cannot
            case '┬':
                return new boolean[]{false,true,true,true};
            case '┐':
                return new boolean[]{false,false,true,true};
            case '├':
                return new boolean[]{true,true,true,false};
            case '┼':
                return new boolean[]{true,true,true,true};
            case '┤':
                return new boolean[]{true,false,true,true};
            case '└':
                return new boolean[]{true,true,false,false};
            case '┴':
                return new boolean[]{true,true,false,true};
            case '┘':
                return new boolean[]{true,false,false,true};
            case '╵':
                return new boolean[]{true,false,false,false};
            case '╶':
                return new boolean[]{false,true,false,false};
            case '╷':
                return new boolean[]{false,false,true,false};
            case '╴':
                return new boolean[]{false,false,false,true};
        }
        return new boolean[]{false,false,false,false};
    }
    private void updateSeen(int posi, int posj){ //Updates the array storing if each cell of the maze has been seen
        this.seen[posj][posi]=true; //Sets the player's current position to be seen
        boolean[] scan=getScan(posi, posj); //U,R,D,L
        try {
            if (scan[0]) { //If the player can move up...
                for (int j = posj - 1; (this.maze[j][posi] == '│' || getScan(posi,j)[2]) && j >= 0; j--) { //The program checks up through all 'corridor' cells unit another junction is found
                    this.seen[j][posi] = true; //All of them being set to seen along the way
                }
            }
        }catch (Exception ignored){}
        try{
            if (scan[1]){
                for (int i=posi+1;(this.maze[posj][i]=='─' || getScan(i,posj)[3])&&i<=this.maze[0].length-1;i++){
                    this.seen[posj][i]=true;
                }
            }
        }catch (Exception ignored){}
        try {
            if (scan[2]) {
                for (int j = posj + 1; (this.maze[j][posi] == '│' || getScan(posi,j)[0]) && j <= this.maze.length; j++) {
                    this.seen[j][posi] = true;
                }
            }
        }catch (Exception ignored){}
        try {
            if (scan[3]) {
                for (int i = posi - 1; (this.maze[posj][i] == '─' || getScan(i,posj)[1]) && i >= 0; i--) {
                    this.seen[posj][i] = true;
                }
            }
        }catch (Exception ignored){}
    }
    private void playerMove(){
        boolean[] moves=this.player.moves(this.maze.length,this.maze[0].length); //Gets a boolean array showing the directions that the player can travel
        char input; //A character to store the player's input
        do {
            printMaze(); //The maze is printed
            printOptions(moves); //The player's options are printed
            input=nextChar(moves); //Takes an input from the player
            switch (input){ //Based upon the input...
                case 'A':
                    this.player.move(0); //The player is moved in the relevant direction
                    return;
                case 'D':
                    this.player.move(1);
                    return;
                case 'W':
                    this.player.move(2);
                    return;
                case 'S':
                    this.player.move(3);
                    return;
            }
        }while(input!='E');
    }
    private void printMaze(){
        for (int j=0;j<this.maze.length;j++){ //Loops through rows
            for (int i=0;i<this.maze[j].length;i++){ //Loops through columns
                if (Arrays.equals(new int[]{j,i},new int[]{this.player.getPosj(),this.player.getPosi()})){ //If the player is at that cell
                    System.out.print('P'); //The player character is printed
                }else{ //If not...
                    if (this.seen[j][i]) { //If the cell has been seen...
                        System.out.print(this.maze[j][i]); //The cell character is printed
                    }else{ //If not...
                        if (j==this.endCo[1] && i==this.endCo[0]){ //If it's the exit cell...
                            System.out.print("E"); //The exit character is printed
                        }else { //If not
                            System.out.print("."); //A placeholder is printed
                        }
                    }
                }
            }
            System.out.println(); //Line breaks are added after each row
        }
    }
    private void printOptions(boolean[] moves){ //Based upon the directions the player can move the relevant options are printed
        System.out.println("You can move:");
        if (moves[0]){
            System.out.println("Left (A)");
        }
        if (moves[1]){
            System.out.println("Right (D)");
        }
        if (moves[2]){
            System.out.println("Up (W)");
        }
        if (moves[3]){
            System.out.println("Down (S)");
        }
        System.out.println("Exit (E)");
    }
    private char nextChar(boolean[] moves){
        do {
            String input=new Scanner(System.in).nextLine(); //The player's input is taken
            try {
                if (input.toUpperCase().charAt(0) == 'A' && moves[0]) { //If a move in that direction is permitted...
                    return 'A'; //The character is returned
                }
                else if ((input.toUpperCase().charAt(0))=='D'&&moves[1]){
                    return 'D';
                }
                else if (input.toUpperCase().charAt(0)=='W'&&moves[2]){
                    return 'W';
                }
                else if (input.toUpperCase().charAt(0)=='S'&&moves[3]){
                    return 'S';
                }
                if (input.toUpperCase().charAt(0)=='E'){ //If the player wants to exit the maze
                    System.exit(0); //The program exits
                }
            }catch (Exception ignored){}
        }while (true); //This loops indefinitely to ensure that a valid direction is selected
    }
    private void setUp(){
        for (int i=0;i<this.maze[0].length;i++){ //Setting up player character by looping through the top row
            if (this.maze[0][i]=='├'||this.maze[0][i]=='┼'||this.maze[0][i]=='┤'||this.maze[0][i]=='└'||this.maze[0][i]=='┴'||this.maze[0][i]=='┘'){ //If an up-direction is found that must be the start point
                this.player=new character(i); //So the player object is initialised using the x-coordinate
                i=this.maze[0].length; //And the searching loop is broken
            }
        }
        for (int i=0;i<this.maze[this.maze.length-1].length;i++){ //Looking through the bottom row to find the end of the maze
            if (this.maze[this.maze.length-1][i]=='├'||this.maze[this.maze.length-1][i]=='┼'||this.maze[this.maze.length-1][i]=='┤'||this.maze[this.maze.length-1][i]=='┌'||this.maze[this.maze.length-1][i]=='┬'||this.maze[this.maze.length-1][i]=='┐'||this.maze[this.maze.length-1][i]=='│'){
                this.endCo=new int[]{i,this.maze.length-1}; //If the cell has a downward connection its coordinates are set as the end coordinates
                i=this.maze[this.maze.length-1].length; //The searching loop is broken
            }
        }
    }
    private void readMaze(File file){ //Reads in the maze to the char array storing it
        try {
            BufferedReader read=new BufferedReader(new FileReader(file)); //Creates a buffered reader to read the file
            this.maze[0]=read.readLine().toCharArray(); //Reads in the first line as a character array
            //noinspection InfiniteLoopStatement
            while(true){ //Going indefinitely
                this.maze=Functions.addOn(this.maze,read.readLine().toCharArray()); //Calls the addon function to append the next row to the array
            }
        }catch (Exception ignored){} //Once the end of the file is hit, a null is read in. This causes the addon function to throw an exception, breaking the loop
    }
    void saveMaze(File file){
        BufferedWriter write;
        try{
            write=new BufferedWriter(new FileWriter(file));
            for (char[] row:this.maze){
                for (char c:row){
                    write.write(c);
                }
                write.newLine();
            }
            write.close();
        }catch (Exception ignored){}
    }
}
class character {
    private int posi, posj;
    private char standingOn;
    character(int posi) {
        this.posi = posi;
        this.posj = 0;
    }
    int getPosi() {
        return posi;
    }
    int getPosj() {
        return posj;
    }
    void setStandingOn(char standingOn) {
        this.standingOn = standingOn;
    }
    boolean[] moves(int jLimit, int iLimit) {
        switch (this.standingOn) {
            case '─':
                return new boolean[]{(this.posi != 0), (this.posi != iLimit - 1), false, false};
            case '│':
                return new boolean[]{false, false, (this.posj != 0), (this.posj != jLimit - 1)};
            case '┌':
                return new boolean[]{false, (this.posi != iLimit - 1), false, (this.posj != jLimit - 1)};
            case '┬':
                return new boolean[]{(this.posi != 0), (this.posi != iLimit - 1), false, (this.posj != jLimit - 1)};
            case '┐':
                return new boolean[]{(this.posi != 0), false, false, (this.posj != jLimit - 1)};
            case '├':
                return new boolean[]{false, (this.posi != iLimit - 1), (this.posj != 0), (this.posj != jLimit - 1)};
            case '┼':
                return new boolean[]{(this.posi != 0), (this.posi != iLimit - 1), (this.posj != 0), (this.posj != jLimit - 1)};
            case '┤':
                return new boolean[]{(this.posi != 0), false, (this.posj != 0), (this.posj != jLimit - 1)};
            case '└':
                return new boolean[]{false, (this.posi != iLimit - 1), (this.posj != 0), false};
            case '┴':
                return new boolean[]{(this.posi != 0), (this.posi != iLimit - 1), (this.posj != 0), false};
            case '┘':
                return new boolean[]{(this.posi != 0), false, (this.posj != 0), false};
            case '╵':
                return new boolean[]{false, false, (this.posj != 0), false};
            case '╶':
                return new boolean[]{false, (this.posi != iLimit - 1), false, false};
            case '╷':
                return new boolean[]{false, false, false, (this.posj != jLimit - 1)};
            case '╴':
                return new boolean[]{(this.posi != 0), false, false, false};
        }
        return new boolean[]{false, false, false, false};
    }
    void move(int d) {
        switch (d) {
            case 0:
                this.posi--;
                return;
            case 1:
                this.posi++;
                return;
            case 2:
                this.posj--;
                return;
            case 3:
                this.posj++;
        }
    }
}