import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

class Maze {
    private class Cell{

        /**
         * This class is a custom cell class which stores which 'wall' each cell has, along with if it's been visited
         * in the recursive backtracking algorithm. Since each boolean is only changed in one way, the set variables
         * do not require parameters. Also, each cell is initialised in the same way, and so constructor parameters are
         * not required.
         */

        private boolean up,right,down,left,visited;
        Cell(){
            this.up=true;
            this.right=true;
            this.down=true;
            this.left=true;
            this.visited=false;
        }
        boolean isVisited() {
            return this.visited;
        }
        void setVisited() {
            this.visited = true;
        }
        boolean isLeft() {
            return this.left;
        }
        void setLeft() {
            this.left = false;
        }
        boolean isDown() {
            return this.down;
        }
        void setDown() {
            this.down = false;
        }
        boolean isRight() {
            return this.right;
        }
        void setRight() {
            this.right = false;
        }
        boolean isUp() {
            return this.up;
        }
        void setUp() {
            this.up = false;
        }
    }

    /**
     * This class is responsible for generating a maze for the solving and game classes.
     *  It takes the dimensions of the maze and creates an array of custom cell objects to store the properties of each cell,
     *  and then a recursive backtracking algorithm is used to create a maze.
     *  Loops are then added by adding random connections between cells to join together a previously perfect maze.
     *  There is also save/print functionality in the respective methods, which call a single outputMaze method which
     *  can either save or write. This reuses the code to select the appropriate character.
     */

    private final Cell[][] cells;
    private final ArrayList<int[]> stack=new ArrayList<>();
    private final float loopChance;
    Maze(int x, int y, float loopChance) {
        this.cells=new Cell[y][x]; //An array to store the cells is created with dimensions y*x (Rows then columns)
        this.loopChance=loopChance; //Local variables are saved
        for (int j=0;j<this.cells.length;j++){ //Looping through the cells array j-axis
            for (int i=0;i<this.cells[j].length;i++){ //Looping through the cells array i-axis
                this.cells[j][i]=new Cell(); //Cells are initialized
            }
        }
        generate(); //Calls maze to be generated
    }
    private void generate(){
        int[] current = new int[]{0, new Random().nextInt(this.cells[0].length)}; //The starting point is selected along the top row
        this.stack.add(new int[]{current[0], current[1]}); //This position is pushed to the path stack
        this.cells[current[0]][current[1]].setVisited(); //The starting cell is set to be visited
        int unvisited = this.cells.length * this.cells[0].length - 1; //The number of remaining cells is calculated
        while(unvisited >0&&this.stack.size()>0){ //While the stack isn't empty...
                boolean left = false, up = false, right = false, down = false; //Temporary booleans are created storing valid directions to travel
                int localUnvisited = 0; //Unvisited orthographic cells are counted...
                try { //Try catch loops are used to find valid cells, as the program will break if a null-pointer is encountered. If this occurs, that direction will be ignored
                    if (!this.cells[current[0]][current[1] - 1].isVisited()) { //Checking cell to the left
                        left = true;
                        localUnvisited++;
                    }
                }catch (Exception ignored){}
                try {
                    if (!this.cells[current[0] - 1][current[1]].isVisited()) { //Checking cell up
                        up = true;
                        localUnvisited++;
                    }
                }catch (Exception ignored){}
                try {
                    if (!this.cells[current[0]][current[1] + 1].isVisited()) { //Checking cell to the right
                        right = true;
                        localUnvisited++;
                    }
                }catch (Exception ignored){}
                try {
                    if (!this.cells[current[0] + 1][current[1]].isVisited()) { //Checking cell down
                        down = true;
                        localUnvisited++;
                    }
                }catch (Exception ignored){}
                if (localUnvisited>0){ //If there are orthographic unvisited cells to travel to...
                    int side; //A direction int is created
                    boolean valid=false; //A looping validation boolean is created
                    do{
                       side=new Random().nextInt(4); //A random direction is chosen (left, up, right, down : 0, 1, 2, 3)
                       switch (side){
                           case 0:
                               if (left){ //If the random direction is valid the loop is broken
                                   valid=true;
                               }
                               break;
                           case 1:
                               if (up){
                                   valid=true;
                               }
                               break;
                           case 2:
                               if (right){
                                   valid=true;
                               }
                               break;
                           case 3:
                               if (down){
                                   valid=true;
                               }
                               break;
                       }
                    }while (!valid);
                    switch (side) { //Based upon which direction is selected...
                        case 0: //Left
                                this.cells[current[0]][current[1]-1].setRight(); //The walls of the cell to left are disabled
                                this.cells[current[0]][current[1]].setLeft(); //The walls on the left of the current cell are disabled
                                current =new int[]{current[0], current[1]-1}; //The coordinates of the new cell are stored...
                                this.stack.add(new int[]{current[0], current[1]}); //And pushed to the path stack
                                this.cells[current[0]][current[1]].setVisited(); //The last cell is marked as visited
                                unvisited--; //And the unvisited count is decreased
                                break; //The switch is broken
                        case 1: //Up
                                this.cells[current[0]-1][current[1]].setDown();
                                this.cells[current[0]][current[1]].setUp();
                                current =new int[]{current[0]-1, current[1]};
                                this.stack.add(new int[]{current[0], current[1]});
                                this.cells[current[0]][current[1]].setVisited();
                                unvisited--;
                                break;
                        case 2: //Right
                                this.cells[current[0]][current[1]+1].setLeft();
                                this.cells[current[0]][current[1]].setRight();
                                current =new int[]{current[0], current[1]+1};
                                this.stack.add(new int[]{current[0], current[1]});
                                this.cells[current[0]][current[1]].setVisited();
                                unvisited--;
                                break;
                        case 3: //Down
                                this.cells[current[0]+1][current[1]].setUp();
                                this.cells[current[0]][current[1]].setDown();
                                current =new int[]{current[0]+1, current[1]};
                                this.stack.add(new int[]{current[0], current[1]});
                                this.cells[current[0]][current[1]].setVisited();
                                unvisited--;
                                break;
                }
            }else { //If no valid direction is found
                current =this.stack.get(this.stack.size()-1); //The path backtracks by a cell
                this.stack.remove(this.stack.size()-1); //And the 'blocked' cell is popped from the stack
            }
            //System.out.println(unvisited); //For debugging...
        }
        this.cells[0][new Random().nextInt(this.cells[0].length)].setUp(); //Once a full path is located a random start point is chosen on the top row
        this.cells[this.cells.length-1][new Random().nextInt(this.cells[this.cells.length-1].length)].setDown(); //Along with a random end point on the bottom row
        addLoops(); //A method is called to add loops to the maze, as the current path is a perfect maze
    }
    void printMaze(){
        outputMaze(false,null); //Output maze is called with the correct parameters
    }
    void writeMaze(File f){
        outputMaze(true,f); //Output maze is called with the correct parameters
    }
    private void outputMaze(boolean write, File f){
        BufferedWriter writer=null;
        if(write){ //If the user wants to write to a text file
            try {
                writer=new BufferedWriter(new FileWriter(f)); //The BufferedWriter is set
            }catch (Exception ignored){}
        }
        for (Cell[] j:this.cells){ //Looping through rows
            for (Cell i:j){ //Looping through columns
                if (i.isUp() && !i.isRight() && i.isDown() && !i.isLeft()) { //Each cell's type is checked...
                    if (!write) {
                        System.out.print("─"); //And the appropriate character is printed...
                    }else{
                        try {
                            writer.write("─"); //Or written, depending on how the class is called
                        }catch (Exception ignored){}
                    }
                } else if (!i.isUp() && i.isRight() && !i.isDown() && i.isLeft()) { //Etc...
                    if (!write) {
                        System.out.print("│");
                    }else{
                        try {
                            writer.write("│");
                        }catch (Exception ignored){}
                    }
                } else if (i.isUp() && !i.isRight() && !i.isDown() && i.isLeft()) {
                    if (!write) {
                        System.out.print("┌");
                    }else{
                        try {
                            writer.write("┌");
                        }catch (Exception ignored){}
                    }
                } else if (i.isUp() && !i.isRight() && !i.isDown() && !i.isLeft()) {
                    if (!write) {
                        System.out.print("┬");
                    }else{
                        try {
                            writer.write("┬");
                        }catch (Exception ignored){}
                    }
                } else if (i.isUp() && i.isRight() && !i.isDown() && !i.isLeft()) {
                    if (!write) {
                        System.out.print("┐");
                    }else{
                        try {
                            writer.write("┐");
                        }catch (Exception ignored){}
                    }
                } else if (!i.isUp() && !i.isRight() && !i.isDown() && i.isLeft()) {
                    if (!write) {
                        System.out.print("├");
                    }else{
                        try {
                            writer.write("├");
                        }catch (Exception ignored){}
                    }
                } else if (!i.isUp() && !i.isRight() && !i.isDown() && !i.isLeft()) {
                    if (!write) {
                        System.out.print("┼");
                    }else{
                        try {
                            writer.write("┼");
                        }catch (Exception ignored){}
                    }
                } else if (!i.isUp() && i.isRight() && !i.isDown() && !i.isLeft()) {
                    if (!write) {
                        System.out.print("┤");
                    }else{
                        try {
                            writer.write("┤");
                        }catch (Exception ignored){}
                    }
                } else if (!i.isUp() && !i.isRight() && i.isDown() && i.isLeft()) {
                    if (!write) {
                        System.out.print("└");
                    }else{
                        try {
                            writer.write("└");
                        }catch (Exception ignored){}
                    }
                } else if (!i.isUp() && !i.isRight() && i.isDown() && !i.isLeft()) {
                    if (!write) {
                        System.out.print("┴");
                    }else{
                        try {
                            writer.write("┴");
                        }catch (Exception ignored){}
                    }
                } else if (!i.isUp() && i.isRight() && i.isDown() && !i.isLeft()) {
                    if (!write) {
                        System.out.print("┘");
                    }else{
                        try {
                            writer.write("┘");
                        }catch (Exception ignored){}
                    }
                } else if(!i.isUp() && i.isRight() && i.isDown() && i.isLeft()){
                    if (!write) {
                        System.out.print("╵");
                    }else{
                        try {
                            writer.write("╵");
                        }catch (Exception ignored){}
                    }
                } else if(i.isUp() && !i.isRight() && i.isDown() && i.isLeft()){
                    if (!write) {
                        System.out.print("╶");
                    }else{
                        try {
                            writer.write("╶");
                        }catch (Exception ignored){}
                    }
                } else if(i.isUp() && i.isRight() && !i.isDown() && i.isLeft()){
                    if (!write) {
                        System.out.print("╷");
                    }else{
                        try {
                            writer.write("╷");
                        }catch (Exception ignored){}
                    }
                } else if(i.isUp() && i.isRight() && i.isDown() && !i.isLeft()){
                    if (!write) {
                        System.out.print("╴");
                    }else{
                        try {
                            writer.write("╴");
                        }catch (Exception ignored){}
                    }
                } else{
                    if (!write) {
                        System.out.print("'");
                    }else{
                        try {
                            writer.write("'");
                        }catch (Exception ignored){}
                    }
                }
            }
            if (!write) { //New lines are added
                System.out.println();
            }else{
                try {
                    writer.newLine();
                }catch (Exception ignored){}
            }
        }
        if(write){ //If the maze was being written to a text file
            try {
                writer.close(); //The BufferedWriter closes the file
            }catch (Exception ignored){}
        }
    }
    private void addLoops(){
        int n=(int)Math.floor((this.cells.length*this.cells[0].length)*this.loopChance); //The number of loops to add is calculated
        for (int i=0;i<n;i++){ //Looping until all are added
            int x=new Random().nextInt(this.cells[0].length-2)+1; //A random x coordinate...
            int y=new Random().nextInt(this.cells.length-2)+1; //And a random y coordinate are chosen
            int direction=new Random().nextInt(4); //Along with a direction to add the loop (Up, Right, Down, Left : 0, 1, 2, 3)
            switch (direction){ //Based upon the direction
                case 0:
                    this.cells[y][x].setUp(); //The walls of the random cell...
                    this.cells[y-1][x].setDown(); //Along with the new cell are set
                    break;
                case 1:
                    this.cells[y][x].setRight();
                    this.cells[y][x+1].setLeft();
                    break;
                case 2:
                    this.cells[y][x].setDown();
                    this.cells[y+1][x].setUp();
                    break;
                case 3:
                    this.cells[y][x].setLeft();
                    this.cells[y][x-1].setRight();
                    break;
            }
        }
    }
}