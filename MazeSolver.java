import java.io.*;

class MazeSolver implements Runnable{

    /**
     * This class is responsible for solving the mazes generated using Maze.java. It takes the path of a file, reads
     * it in to a temporary array. This maze is then read over to create a tree of custom vertices, with their information
     * stored in the vertices array. Route is used to store the ID's of the vertices in the shortest path. RouteDistance
     * stores the number of cells in the shortest path. file stores the text file the maze is read from. Solved is
     * for when the class is used with the game class, as in this case this class is a second thread. Solved is checked
     * by the game class before route information is requested.
     */

    private char[][] maze=new char[1][],tracedMaze; //[y][x]
    private Vertex[] vertices=new Vertex[2];
    private int[] route;
    private int routeDistance=0;
    private File file;
    private boolean solved=false;
    boolean finishedReading=false, canSave = false;

    public void run(){ //When the class is used as a thread...
        readMaze(this.file); //The file is read in
        this.finishedReading =true; //Tells the game that it has finished with the text file
        solve(false); //This maze is solved
        while(this.canSave){
            try{
                Thread.sleep(200);
            }catch (Exception ignored){}
        }
        saveTracedMaze(new File("Solution"+this.file.getName())); //The traced maze is saved
        this.solved=true; //The alert variable is set to true
    }
    MazeSolver(File file, boolean ignored){  //A constructor for when the class is used with multithreading, and so it shouldn't solve immediately after creation
        this.file=file; //All it does is set the file
    }
    MazeSolver(File file) { //A general constructor, which jumps straight into working out the shortest path
        readMaze(file);
        solve(true);
    }
    private void solve(boolean statistics){ //The method to solve the maze
        findVertices(statistics); //The method to populate the vertices is called
        calculateMatches(statistics); //The method which calculates which vertex links to which is called
        calculateDistances(statistics); //The method which finds the distance between each vertex is called
        AStar n=new AStar(this.vertices, statistics); //An object of the AStar class is called and the tree is passed
        this.route=n.getPath(); //The route is set once it is completed
        this.route=invert(this.route); //The route is then inverted, as AStar uses a stack to store the route as it is found
        traceRoute(); //The method to set the characters in the Maze array to the traced characters is called
        findRouteDistance(); //The method to calculate the distance of the shortest path is called
    }
    private int[] invert(int[] array){ //This method flips the order of the route array
        int[] temp=new int[array.length];
        for (int i=0;i<array.length;i++){
            temp[temp.length-1-i]=array[i];
        }
        return temp;
    }
    private void calculateDistances(boolean statistics){ //Sets the distance between the vertices and the end point
        for (Vertex v:this.vertices){ //Looping through vertices
            if (statistics){
                System.out.println("Calculating distance for vertex " + v.getId());
            }


            v.setDistance(Math.sqrt(Math.pow(this.vertices[1].y-v.y,2)+Math.pow(this.vertices[1].x-v.x,2))); //Using pythagoras
        }
    }
    private void calculateMatches(boolean statistics){ // Sets distance between vertices
        for (Vertex v:this.vertices){ //For each vertex
            if (statistics) {
                System.out.println("Calculating matches for vertex " + v.getId());
            }
            //Check right
            if (v.rightMatch&&v.rightMatchDistance==Integer.MAX_VALUE) { //If the match distance hasn't been set...
                for (int i = v.x + 1; i < this.maze[v.y].length; i++) { //Sets a temporary x coordinate, which is increased until it hits a cell containing a vertex
                    if (this.maze[v.y][i] == '┬' || this.maze[v.y][i] == '┐' || this.maze[v.y][i] == '┼' || this.maze[v.y][i] == '┤' || this.maze[v.y][i] == '┴' || this.maze[v.y][i] == '┘' || this.maze[v.y][i] == '╴') { //Checks if it's found a cell containing a vertex (In this case a cell with a left connection)
                        int node2 = where(i, v.y); //Temporarily stores the ID of the second node by calling the where() function
                        int distance = this.vertices[node2].x-v.x; //Calculates the distance in the x-axis
                        v.rightMatchDistance=distance; //Sets the relevant distance in the vertex
                        v.rightMatchVertex=node2; //Along with the ID of the vertex it is matching to
                        this.vertices[node2].leftMatchDistance=distance; //The same is done with the vertex being matched to
                        this.vertices[node2].leftMatchVertex=v.id;
                        i=this.maze[v.y].length; //Breaks loop
                    }
                }
            }
            //Check left
            if (v.leftMatch&&v.leftMatchDistance==Integer.MAX_VALUE) {
                for (int i = v.x -1; i >= 0; i--) {
                    if (this.maze[v.y][i] == '┬' || this.maze[v.y][i] == '┼' || this.maze[v.y][i] == '┴' || this.maze[v.y][i] == '┌' || this.maze[v.y][i] == '├' || this.maze[v.y][i] == '└' ||  this.maze[v.y][i] == '╶') {
                        int node2 = where(i, v.y);
                        int distance = v.x-this.vertices[node2].x;
                        v.leftMatchDistance=distance;
                        v.leftMatchVertex=node2;
                        this.vertices[node2].rightMatchDistance=distance;
                        this.vertices[node2].rightMatchVertex=v.id;
                        i=-1;
                    }
                }
            }
            //Check up
            if (v.UpMatch && v.upMatchDistance == Integer.MAX_VALUE) {
                if (v.id>0) {
                    for (int i = v.y - 1; i >= 0; i--) {
                        if (this.maze[i][v.x] == '┬' || this.maze[i][v.x] == '┼' || this.maze[i][v.x] == '┐' || this.maze[i][v.x] == '┌' || this.maze[i][v.x] == '├' || this.maze[i][v.x] == '┤' || this.maze[i][v.x] == '╷') {
                            int node2 = where(v.x, i);
                            int distance = v.y - this.vertices[node2].y;
                            v.upMatchDistance=distance;
                            v.upMatchVertex=node2;
                            this.vertices[node2].downMatchDistance=distance;
                            this.vertices[node2].downMatchVertex=v.id;
                            i=-1;
                        }
                    }
                }
            }
            //Check down
            if (v.DownMatch && v.downMatchDistance==Integer.MAX_VALUE) {
                if (v.id<this.vertices.length-1) {
                    for (int i = v.y + 1; i < this.maze.length; i++) {
                        if (this.maze[i][v.x] == '┴' || this.maze[i][v.x] == '┼' || this.maze[i][v.x] == '┘' || this.maze[i][v.x] == '└' || this.maze[i][v.x] == '├' || this.maze[i][v.x] == '┤' || this.maze[i][v.x] == '╵') {
                            int node2 = where(v.x, i);
                            int distance = this.vertices[node2].y - v.y;
                            v.downMatchDistance=distance;
                            v.downMatchVertex=node2;
                            this.vertices[node2].upMatchDistance=distance;
                            this.vertices[node2].upMatchVertex=v.id;
                            i=this.maze.length;
                        }
                    }
                }
            }
        }
    }
    private int where(int x,int y){ //This function returns the ID of the vertex at the passed coordinates
        for (int i=0;i<this.vertices.length;i++){ //Looking through each vertex
            if (this.vertices[i].x==x&&this.vertices[i].y==y){ //And checking if the coordinates match
                return i; //If so returning the ID
            }
        }
        return -2; //If not returning a -2 in place of a null
    }
    private void findVertices(boolean statistics){ //This method sets up the vertex array
        for (int j=0;j<this.maze.length;j++){ //Checking though the y-axis
            for (int i=0;i<this.maze[j].length;i++){ //And the x-axis
                switch (this.maze[j][i]){ //Checking based upon the character at each point
                    case '┌':
                        if (!(j==this.maze.length-1)){ //If the character has a downward connection it checks if it is the end cell by checking if it's on the bottom row
                            this.vertices = append(this.vertices, new Vertex(this.vertices.length, i, j, false, true, false, true)); //A relevant vertex is appended to the array of vertices
                        }else {
                            this.vertices[1]=new Vertex(1, i, j, false, true, false, true);
                        }
                        break;
                    case '┬':
                        if (!(j==this.maze.length-1)) {
                            this.vertices = append(this.vertices, new Vertex(this.vertices.length, i, j, true, true, false, true));
                        }else {
                            this.vertices[1]=new Vertex(1, i, j, true, true, false, true);
                        }
                        break;
                    case '┐':
                        if (!(j==this.maze.length-1)) {
                            this.vertices = append(this.vertices, new Vertex(this.vertices.length, i, j, true, false, false, true));
                        }else {
                            this.vertices[1]=new Vertex(1, i, j, true, false, false, true);
                        }
                        break;
                    case '├':
                        if (j==0) {
                            this.vertices[0]=new Vertex(0, i, j, false, true, true, true);
                        }else if (j==this.maze.length-1){
                            this.vertices[1]=new Vertex(1, i, j, false, true, true, true);
                        }else {
                            this.vertices = append(this.vertices, new Vertex(this.vertices.length, i, j, false, true, true, true));
                        }
                        break;
                    case '┼':
                        if (j==0){
                            this.vertices[0]=new Vertex(0, i, j, true, true, true, true);
                        }else if(j==this.maze.length-1){
                            this.vertices[1]=new Vertex(1, i, j, true, true, true, true);
                        }else {
                            this.vertices = append(this.vertices, new Vertex(this.vertices.length, i, j, true, true, true, true));
                        }
                        break;
                    case '┤':
                        if (j==0){
                            this.vertices[0]=new Vertex(0, i, j, true, false, true, true);
                        }else if (j==this.maze.length-1){
                            this.vertices[1]=new Vertex(1, i, j, true, false, true, true);
                        }else {
                            this.vertices = append(this.vertices, new Vertex(this.vertices.length, i, j, true, false, true, true));
                        }
                        break;
                    case '└':
                        if (j==0){
                            this.vertices[0]=new Vertex(0, i, j, false, true, true, false);
                        }else {
                            this.vertices = append(this.vertices, new Vertex(this.vertices.length, i, j, false, true, true, false));
                        }
                        break;
                    case '┴':
                        if (j==0){
                            this.vertices[0]=new Vertex(0, i, j, true, true, true, false);
                        }else {
                            this.vertices = append(this.vertices, new Vertex(this.vertices.length, i, j, true, true, true, false));
                        }
                        break;
                    case '┘':
                        if (j==0){
                            this.vertices[0]=new Vertex(0, i, j, true, false, true, false);
                        }else {
                            this.vertices = append(this.vertices, new Vertex(this.vertices.length, i, j, true, false, true, false));
                        }
                        break;
                    case '╵':
                        this.vertices=append(this.vertices,new Vertex(this.vertices.length,i,j,false,false,true,false));
                        break;
                    case '╶':
                        this.vertices=append(this.vertices,new Vertex(this.vertices.length,i,j,false,true,false,false));
                        break;
                    case '╷':
                        this.vertices=append(this.vertices,new Vertex(this.vertices.length,i,j,false,false,false,true));
                        break;
                    case '╴':
                        this.vertices=append(this.vertices,new Vertex(this.vertices.length,i,j,true,false,false,false));
                        break;
                }
                if (statistics){
                    System.out.println(this.vertices.length + " vertices found");
                }
            }
        }
    }
    private void readMaze(File file){ //Reads in maze from a file
        try {
            BufferedReader read=new BufferedReader(new FileReader(file)); //Creates a buffered reader to read in the file
            this.maze[0]=read.readLine().toCharArray(); //Sets the initial row
            while(true){
                this.maze=Functions.addOn(this.maze,read.readLine().toCharArray()); //Appends the next row of the file
            }
        }catch (Exception ignored){}
    }
    private Vertex[] append(Vertex[] array, Vertex addition){ //Adds a vertex onto a vertex array
        if (array.length>0) { //If there's content in the array
            Vertex[] temp = new Vertex[array.length + 1]; //Temp array
            System.arraycopy(array, 0, temp, 0, array.length); //Copies over into temp array
            temp[temp.length - 1] = addition; //Adds on final vertex
            return temp; //Returns complete array
        } else { //If not...
            return new Vertex[]{addition}; //Return a newly initialised array
        }
    }
    void printTracedMaze(){ //Prints the traced maze
        for (char[] row:this.tracedMaze){
            for (char i:row){
                System.out.print(i);
            }
            System.out.println();
        }
    }
    void saveTracedMaze(File file){  //Writes the traced maze to a text file
        BufferedWriter write;
        try{
            write=new BufferedWriter(new FileWriter(file));
            for (char[] row:this.tracedMaze){
                for (char c:row){
                    write.write(c);
                }
                write.newLine();
            }
            write.close();
        }catch (Exception ignored){}
    }
    private void traceRoute(){
        this.tracedMaze=this.maze; //Sets the tracedMaze array initially equal to the existing maze array
        int entry=0; //The route always starts facing upwards (U,R,D,L : 0,1,2,3)
        for (int i=0;i<this.route.length;i++){ //For each vertex in the route...
            int x=this.vertices[this.route[i]].x; //Calculating the difference in x and y for each vertex to find which direction they are in
            int y=this.vertices[this.route[i]].y;
            int next; //Initialising the next direction variable
            try { //Try in case of the considered vertex is at the end of the route
                if (x-this.vertices[this.route[i+1]].x<0){ //If dx<0, the next vertex must be to the right, as a larger x - current x < 0
                    next=1; //So next is set to 1 (R)
                }else if (x-this.vertices[this.route[i+1]].x>0){ //Likewise checking if the next vertex is to the left
                    next=3;
                }else if (y-this.vertices[this.route[i+1]].y>0){ //Above...
                    next=0;
                }else{ //Else, meaning below...
                    next=2;
                }
            }catch (Exception e){ //If the last vertex is hit this block is called, in which case we need the direction to be down to trace the exit
                next=2; //And so next is set to 2
            }
            if (next==0){ //If the next direction is up...
                switch (this.tracedMaze[y][x]){ //Considering the character of the current cell...
                    case '├': //For instance...
                        if (entry==1){ //It can be entered from 2 directions...
                            this.tracedMaze[y][x]='┡'; //From the right this is the correct shading (Right and Up shaded)
                        }else{ //If entered from the bottom
                            this.tracedMaze[y][x]='┠'; //This is the correct shading
                        }
                        break;
                    case '┼': //Etc...
                        if (entry==1){
                            this.tracedMaze[y][x]='╄';
                        }else if (entry==2){
                            this.tracedMaze[y][x]='╂';
                        }else{
                            this.tracedMaze[y][x]='╃';
                        }
                        break;
                    case '┤':
                        if (entry==2){
                            this.tracedMaze[y][x]='┨';
                        }else {
                            this.tracedMaze[y][x]='┩';
                        }
                        break;
                    case '└':
                        this.tracedMaze[y][x]='┗';
                        break;
                    case '┴':
                        if (entry==1){
                            this.tracedMaze[y][x]='┺';
                        }else{
                            this.tracedMaze[y][x]='┹';
                        }
                        break;
                    case '┘':
                        this.tracedMaze[y][x]='┛';
                        break;
                }
                entry=2; //Entry is then set to next, as this turn results in facing downwards
                y--; //So the y coordinated is reduced in order to trace the corridors between the vertices
                try { //Try in case an edge is hit
                    while(this.tracedMaze[y][x]=='│'){ //Since these cells are not vertices, they need tracing where appropriate. As we've traced the next vertex coming from the top, we need to backtrace along the path taken to get here...
                        this.tracedMaze[y][x]='┃'; //And so corridors are set
                        y--; //And y is decreased further
                    }
                }catch (Exception ignored){}
            }else if (next==1){ //Likewise for a turn ending facing right...
                switch (this.tracedMaze[y][x]){
                    case '┌':
                        this.tracedMaze[y][x]='┏';
                        break;
                    case '┬':
                        if (entry==2){
                            this.tracedMaze[y][x]='┲';
                        }else{
                            this.tracedMaze[y][x]='┯';
                        }
                        break;
                    case '└':
                        this.tracedMaze[y][x]='┗';
                        break;
                    case '├':
                        if (entry==0){
                            this.tracedMaze[y][x]='┡';
                        }else{
                            this.tracedMaze[y][x]='┢';
                        }
                        break;
                    case '┼':
                        if (entry==0){
                            this.tracedMaze[y][x]='╄';
                        }else if (entry==2){
                            this.tracedMaze[y][x]='╆';
                        }else{
                            this.tracedMaze[y][x]='┿';
                        }
                        break;
                    case '┴':
                        if (entry==0){
                            this.tracedMaze[y][x]='┺';
                        }else {
                            this.tracedMaze[y][x]='┷';
                        }
                        break;
                }
                entry=3;
                x++;
                try {
                    while(this.tracedMaze[y][x]=='─'){ //Except this time we're going along not up to trace the corridors between vertices
                        this.tracedMaze[y][x]='━';
                        x++;
                    }
                }catch (Exception ignored){}
            }else if (next==2){ //Likewise...
                switch (this.tracedMaze[y][x]){
                    case '┌':
                        this.tracedMaze[y][x]='┏';
                        break;
                    case '┬':
                        if (entry==1){
                            this.tracedMaze[y][x]='┲';
                        }else{
                            this.tracedMaze[y][x]='┱';
                        }
                        break;
                    case '┐':
                        this.tracedMaze[y][x]='┓';
                        break;
                    case '├':
                        if (entry==0){
                            this.tracedMaze[y][x]='┠';
                        }else {
                            this.tracedMaze[y][x]='┢';
                        }
                        break;
                    case '┼':
                        if (entry==0){
                            this.tracedMaze[y][x]='╂';
                        }else if (entry==1){
                            this.tracedMaze[y][x]='╆';
                        }else{
                            this.tracedMaze[y][x]='╅';
                        }
                        break;
                    case '┤':
                        if (entry == 0) {
                            this.tracedMaze[y][x]='┨';
                        }else {
                            this.tracedMaze[y][x]='┪';
                        }
                        break;
                }
                entry=0;
                y++;
                try {
                    while(this.tracedMaze[y][x]=='│'){
                        this.tracedMaze[y][x]='┃';
                        y++;
                    }
                }catch (Exception ignored){}
            }else{ //Likewise...
                switch (this.tracedMaze[y][x]){
                    case '┐':
                        this.tracedMaze[y][x]='┓';
                        break;
                    case '┘':
                        this.tracedMaze[y][x]='┛';
                        break;
                    case '┬':
                        if (entry==1){
                            this.tracedMaze[y][x]='┯';
                        }else {
                            this.tracedMaze[y][x]='┱';
                        }
                        break;
                    case '┤':
                        if (entry==0){
                            this.tracedMaze[y][x]='┩';
                        }else{
                            this.tracedMaze[y][x]='┪';
                        }
                        break;
                    case '┴':
                        if (entry==0){
                            this.tracedMaze[y][x]='┹';
                        }else{
                            this.tracedMaze[y][x]='┷';
                        }
                        break;
                    case '┼':
                        if (entry==0){
                            this.tracedMaze[y][x]='╃';
                        }else if (entry==1){
                            this.tracedMaze[y][x]='┿';
                        }else{
                            this.tracedMaze[y][x]='╅';
                        }
                        break;
                }
                entry=1;
                x--;
                while(this.tracedMaze[y][x]=='─'){
                    this.tracedMaze[y][x]='━';
                    x--;
                }
            }
        }
    }
    private void findRouteDistance(){
        for (int i=0;i<this.route.length-1;i++){ //For each vertex...
            this.routeDistance+=Math.abs(this.vertices[this.route[i]].x-this.vertices[this.route[i+1]].x); //The distance is increased by the difference in x
            this.routeDistance+=Math.abs(this.vertices[this.route[i]].y-this.vertices[this.route[i+1]].y); //And the difference in y
        }
        this.routeDistance++; //It is increased by 1 to account for the start and end which would otherwise be ignored
    }
    int getRouteDistance(){
        return this.routeDistance;
    }
    int[] getDimenstions(){
        return new int[]{this.maze.length, this.maze[0].length}; //Returns a coordinate array of y by x
    }
    int getVertexLength(){
        return this.vertices.length;
    }
    boolean isSolved() { //For use when multithreading
        return solved;
    }
}
class AStar{
    /**
     * This class holds the A* algorithm used to solve the maze. The algorithm is different to a general algorithm as it
     * only looks at orthographic directions (And so an adjacency matrix is unnecessary), and it keeps track of how many
     * vertices are modified when each node is considered so that the sorting method only looks for however many vertices
     * have been changed (As up to 4 are changed, however sometimes only 1 or 2 are. In this case it would waste computational time
     * to look through every vertex once the 1 or 2 are sorted)
     */

    private int[] path; //An array to store the list of vertices visited
    class Stack{ // Custom stack class to store the vertices not yet considered
        private int[] stack=new int[]{}; //The size of the stack is dynamic
        Stack(){}
        void push(int ID){ //Simple push method
            int[] temp=this.stack;
            this.stack=new int[temp.length+1];
            System.arraycopy(temp, 0, this.stack, 0, temp.length);
            this.stack[this.stack.length-1]=ID;
        }
        int pop(){ //Simple pop functions
            int temp=this.stack[0];
            int[] temp2=this.stack;
            this.stack=new int[this.stack.length-1];
            System.arraycopy(temp2, 1, this.stack, 0, this.stack.length);
            return temp;
        }
        int length(){
            return this.stack.length;
        }
        void sort(Vertex[] vertices, int changed){ //Since the stack needs to be sorted for A* to be efficient, the stack can call a custom shuttleSort method
            if (this.stack.length>1) {
                this.stack = Sort.shuttleSort(this.stack,vertices,changed);
            }
        }
    }
    AStar(Vertex[] vertices, boolean statistics) {
        vertices = complete(vertices, statistics); //Calls the complete method to find the route
        backTrace(vertices); //Backtraces the route to the path array
    }
    private Vertex[] complete(Vertex[] vertices, boolean statistics) { //This method performs the A* algorithm on the network drawn from the maze
        Stack pile = new Stack(); //A stack is created to hold the vertices under consideration
        pile.push(vertices[0].getId()); //The start vertex is pushed first
        for (int i = 2; i < vertices.length; i++) { //Then every other vertex except the end vertex is pushed (As the end vertex will have a distance of 0)
            pile.push(vertices[i].getId());
        }

        while (pile.length() > 0) { //While there are still nodes requiring consideration
            if(statistics) {
                System.out.println("Pile is now " + pile.length() + " vertices long");
            }
            int changed=0; //This int stores the number of vertices which have had their distance changed (Keeps sorting efficient as only modified vertices are moved)
            int current; //Int to store the vertex currently under consideration
            try { //Try in case stack is empty
                do {
                    current = pile.pop(); //The top of the stack is popped...
                } while (vertices[current].currentBest > vertices[1].currentBest); //While the distance to them is larger than the best distance to the end (And so can be ignored)
                // Each of the orthographic directions are checked in turn. This ensures that only relevant connections are checked,
                // and removes the need for holding a large adjacency matrix (Which would result in many more redundant comparisons)

                //Check Right
                if (vertices[current].rightMatchVertex!=-1) { //If the current vertex has a right-connection...
                    /*
                    The next if statement checks if the node to the right of the current node requires modifying.
                    It checks if the distance to the current vertex + the distance from the current vertex to the vertex to the right (Distance to
                    vertex to the right via the current vertex) is less than the current known shortest distance to the right vertex. If this
                    is not the case considering routing via the current vertex is irrelevant.
                    It also checks if the distance to the right vertex via the current vertex is less than the current known best distance to
                    the end vertex. In this is not the case it would not need checking as it could not improve the current best route
                    */

                    if ((vertices[current].currentBest)
                            + (vertices[current].rightMatchDistance)
                            < (vertices[vertices[current].rightMatchVertex].currentBest)
                            && (vertices[current].currentBest)
                            + (vertices[current].rightMatchDistance)
                            < vertices[1].currentBest) { //If this is the case...

                        vertices[vertices[current].rightMatchVertex].currentBest=vertices[current].currentBest
                                + vertices[current].rightMatchDistance; //The currentBest distance to the vertex on the right is set to the distance via
                                //the current vertex

                        vertices[vertices[current].rightMatchVertex].trace=vertices[current].id; //The 'trace' vertex of the right vertex is set to the
                                //current vertex so that it paths via the current vertex when backtracking

                        vertices[vertices[current].rightMatchVertex].combined=(vertices[current].currentBest
                                + vertices[current].rightMatchDistance
                                + (vertices[vertices[current].rightMatchVertex].distance)); //The combined distance (Used to sort the stack) is set
                                //to be the current best distance to the right vertex + the orthographic distance from that vertex to the end of the maze

                        changed++; //Since a vertex has been changed and so requires resorting, changed is increased
                    }
                }
                //The same is then done for each direction...
                //Check Left
                if (vertices[current].leftMatchVertex!=-1) {
                    if ((vertices[current].currentBest)
                            + (vertices[current].leftMatchDistance)
                            < (vertices[vertices[current].leftMatchVertex].currentBest)
                            && (vertices[current].currentBest)
                            + (vertices[current].leftMatchDistance)
                            < vertices[1].currentBest) {
                        vertices[vertices[current].leftMatchVertex].currentBest = (vertices[current].currentBest)
                                + (vertices[current].leftMatchDistance);
                        vertices[vertices[current].leftMatchVertex].trace=vertices[current].id;
                        vertices[vertices[current].leftMatchVertex].combined=(vertices[current].currentBest)
                                + (vertices[current].leftMatchDistance)
                                + (vertices[vertices[current].leftMatchVertex].distance);
                        changed++;
                    }
                }
                //Check Down
                if (vertices[current].downMatchVertex!=-1) {
                    if ((vertices[current].currentBest)
                            + (vertices[current].downMatchDistance)
                            < (vertices[vertices[current].downMatchVertex].currentBest)
                            && (vertices[current].currentBest)
                            + (vertices[current].downMatchDistance)
                            < vertices[1].currentBest) {
                        vertices[vertices[current].downMatchVertex].currentBest=(vertices[current].currentBest)
                                + (vertices[current].downMatchDistance);
                        vertices[vertices[current].downMatchVertex].trace=vertices[current].id;
                        vertices[vertices[current].downMatchVertex].combined=(vertices[current].currentBest)
                                + (vertices[current].downMatchDistance)
                                + (vertices[vertices[current].downMatchVertex].distance);
                        changed++;
                    }
                }
                //Check Up
                if (vertices[current].upMatchVertex!=-1 && vertices[current].id!=0) {
                    if ((vertices[current].currentBest)
                            + (vertices[current].upMatchDistance)
                            < (vertices[vertices[current].upMatchVertex].currentBest)
                            && (vertices[current].currentBest)
                            + (vertices[current].upMatchDistance)
                            < vertices[1].currentBest) {
                        vertices[vertices[current].upMatchVertex].currentBest=(vertices[current].currentBest)
                                + (vertices[current].upMatchDistance);
                        vertices[vertices[current].upMatchVertex].trace=vertices[current].id;
                        vertices[vertices[current].upMatchVertex].combined=(vertices[current].currentBest)
                                + (vertices[current].upMatchDistance)
                                + (vertices[vertices[current].upMatchVertex].distance);
                        changed++;
                    }
                }
                //Once all vertices have been updated based upon the current vertex...
                pile.sort(vertices, changed); //The stack is sorted so that the next vertex for consideration is found
            } catch (Exception ignored) {}
        }
        return vertices; //Once complete the vertices array is returned
    }
    private void backTrace(Vertex[] vertices){ //This method populates the path array
        this.path=new int[]{1}; //It first adds the end vertex to the array...
        while(this.path[this.path.length-1]!=0){ //And while the final vertex in the array isn't the starting node (And so the path is complete)...
            this.path=Functions.addOn(this.path,vertices[this.path[this.path.length-1]].getTrace()); //The 'trace' vertex of each vertex in the route is added,
            // where the trace vertex is a pointer to the vertex taken to get to the current vertex in the shortest distance
        }
    }
    int[] getPath() {
        return this.path;
    }
}
class Sort {
    static int[] shuttleSort(int[] array, Vertex[] vertices, int changedNum){ //The stack of ID's is passed as int[] array, the array of vertices is passed to allow
         //comparisons, and the number of changed vertices is passed to limit unnecessary comparisons
        for (int i=1, n=0;i<array.length && n<changedNum;i++){ //i stores the current pointer, n stores the number changed
            boolean changed=false; //A boolean to check if the currently considered vertex has been sorted is initialised to false
            for (int j=i;j>0;j--){ //Shuttle sort moves down the array...
                if (vertices[array[j]].getCombined()<vertices[array[j-1]].getCombined()){ //If the combined distance of 2 need swapping...
                    int temp=array[j]; //The ID's of them in the array are swapped
                    array[j]=array[j-1];
                    array[j-1]=temp;
                    changed=true; //And changed is set to true
                }else{ //If not...
                    j=0; //The loop is broken
                }
            }
            if (changed){ //If the vertex considered needed moving in the array...
                n++; //The number changed is increased
            }
        } //If all vertices potentially requiring moving have been moved the loop breaks...
        return array; //And the sorted array is returned
    }
}
class Vertex{
    /**
     * This class stores a custom vertex used for shortest path finding
     * It differs from a general vertex object as it only stores information about orthographic connections (As the maze
     * is built out of square cells), and so while looking more complicated it saves a lot of memory on not requiring
     * and adjacency matrix
     */
    final boolean leftMatch, rightMatch, UpMatch, DownMatch; //These store if there is a connection to each side
    final int x, y, id; //These store its coordinates and ID
    int rightMatchDistance = Integer.MAX_VALUE, leftMatchDistance = Integer.MAX_VALUE, upMatchDistance= Integer.MAX_VALUE, downMatchDistance=Integer.MAX_VALUE; //These store
     //the current shortest distance known to get to that vertex (Initialised as int.max_value so that they are considered)
    int trace, rightMatchVertex, leftMatchVertex, upMatchVertex, downMatchVertex; //Trace is the vertex taken to get the the current
     //vertex in the shortest distance, and the MatchVertices store the ID of the vertex to each side
    double combined, distance, currentBest; //Combined stores a metric used to sort the stack (Best distance + orthographic distance left)
     //while distance stores the orthographic distance to the end and currentBest stores the currently known shortest distance to reach that vertex
    Vertex(int id,int x, int y,boolean leftMatch, boolean rightMatch, boolean upMatch, boolean downMatch) { //A constructor used in the first pass (Where vertices are discovered)
        this.id=id;
        this.leftMatch=leftMatch;
        this.rightMatch=rightMatch;
        this.UpMatch=upMatch;
        this.DownMatch=downMatch;
        this.rightMatchVertex=-1;
        this.leftMatchVertex=-1;
        this.upMatchVertex=-1;
        this.downMatchVertex=-1;
        this.x=x;
        this.y=y;
        this.currentBest=Double.MAX_VALUE;
        this.combined=Double.MAX_VALUE;
        if (this.id==0){ //If the vertex is the starting vertex...
            this.currentBest=0; //currentBest and combined are set to 0 so that the connected vertices are considered
            this.combined=0;
        }
    }
    int getId() {
        return this.id;
    }
    double getCombined() {
        return this.combined;
    }
    void setDistance(double distance) {
        this.distance = distance;
    }
    int getTrace() {
        return this.trace;
    }
}
class Functions{
    static int[] addOn(int[] array, int value){ //This function adds an additional integer onto an array
        if(array==null){
            return new int[]{value};
        }
        else {
            int[] temp = array;
            array = new int[array.length + 1];
            System.arraycopy(temp, 0, array, 0, array.length - 1);
            array[array.length - 1] = value;
            return array;
        }
    }
    static char[][] addOn(char[][] array, char[] addition){ //Likewise with a char array (Used when reading in the maze)
        if (array.length>0) {
            char[][] temp = new char[array.length + 1][array[0].length];
            System.arraycopy(array, 0, temp, 0, array.length);
            temp[temp.length - 1] = addition;
            return temp;
        } else {
            return new char[][]{addition};
        }
    }
}