import java.util.List;
import java.util.Random;

class Maze {
    private class Cell{
        private boolean up,right,down,left,visited;
        Cell(){
            this.up=true;
            this.right=true;
            this.down=true;
            this.left=true;
            this.visited=false;
        }
        boolean isVisited() {
            return visited;
        }
        void setVisited(boolean visited) {
            this.visited = visited;
        }
        boolean isLeft() {
            return left;
        }
        void setLeft(boolean left) {
            this.left = left;
        }
        boolean isDown() {
            return down;
        }
        void setDown(boolean down) {
            this.down = down;
        }
        boolean isRight() {
            return right;
        }
        void setRight(boolean right) {
            this.right = right;
        }
        boolean isUp() {
            return up;
        }
        void setUp(boolean up) {
            this.up = up;
        }
    }
    private Cell[][] cells;
    private List<int[]> stack;
    private int[] current;
    private int unvisited;
    Maze(int x, int y) {
        this.cells=new Cell[y][x];
        for (int j=0;j<this.cells.length;j++){
            for (int i=0;i<this.cells[j].length;i++){
                this.cells[j][i]=new Cell();
            }
        }
        generate();
    }
    void generate(){
        this.current=new int[]{0,new Random().nextInt(this.cells[0].length)};
        this.stack.add(new int[]{this.current[0],this.current[1]});
        this.cells[this.current[0]][this.current[1]].setVisited(true);
        this.unvisited=this.cells.length*this.cells[0].length-1;
        while(this.unvisited>0&&this.stack.size()>0){
                boolean left = false, up = false, right = false, down = false;
                int unvisited = 0;
                try {
                    if (!this.cells[this.current[0]][this.current[1] - 1].isVisited()) {
                        left = true;
                        unvisited++;
                    }
                }catch (Exception ignored){}
                try {
                    if (!this.cells[this.current[0] - 1][this.current[1]].isVisited()) {
                        up = true;
                        unvisited++;
                    }
                }catch (Exception ignored){}
                try {
                    if (!this.cells[this.current[0]][this.current[1] + 1].isVisited()) {
                        right = true;
                        unvisited++;
                    }
                }catch (Exception ignored){}
                try {
                    if (!this.cells[this.current[0] + 1][this.current[1]].isVisited()) {
                        down = true;
                        unvisited++;
                    }
                }catch (Exception ignored){}
                if (unvisited>0){
                int side = new Random().nextInt(unvisited);
                switch (side) {
                    case 0:
                        if (left){
                            this.cells[this.current[0]][this.current[1]-1].setRight(false);
                            this.cells[this.current[0]][this.current[1]].setLeft(false);
                            this.current=new int[]{this.current[0],this.current[1]-1};
                            this.stack.add(new int[]{this.current[0],this.current[1]});
                            this.cells[this.current[0]][this.current[1]].setVisited(true);
                            this.unvisited--;
                            break;
                        }
                    case 1:
                        if (up){
                            this.cells[this.current[0]-1][this.current[1]].setDown(false);
                            this.cells[this.current[0]][this.current[1]].setUp(false);
                            this.current=new int[]{this.current[0]-1,this.current[1]};
                            this.stack.add(new int[]{this.current[0],this.current[1]});
                            this.cells[this.current[0]][this.current[1]].setVisited(true);
                            this.unvisited--;
                            break;
                        }
                    case 2:
                        if (right){
                            this.cells[this.current[0]][this.current[1]+1].setLeft(false);
                            this.cells[this.current[0]][this.current[1]].setRight(false);
                            this.current=new int[]{this.current[0],this.current[1]+1};
                            this.stack.add(new int[]{this.current[0],this.current[1]});
                            this.cells[this.current[0]][this.current[1]].setVisited(true);
                            this.unvisited--;
                            break;
                        }
                    case 3:
                        if (down){
                            this.cells[this.current[0]+1][this.current[1]].setUp(false);
                            this.cells[this.current[0]][this.current[1]].setDown(false);
                            this.current=new int[]{this.current[0]+1,this.current[1]};
                            this.stack.add(new int[]{this.current[0],this.current[1]});
                            this.cells[this.current[0]][this.current[1]].setVisited(true);
                            this.unvisited--;
                            break;
                        }
                }
            }else {
                this.current=this.stack.get(this.stack.size()-1);
                this.stack.remove(this.stack.size()-1);
            }
        }
    }
    void printMaze(){

        for (int j=0;j<this.cells.length;j++){
            for (int i=0;i<this.cells[j].length;i++){ //Above
                if (!this.cells[j][i].isUp()){
                    System.out.print(".");
                }else {
                    System.out.print("O");
                }
            }
            System.out.println();
            for (int i=0;i<this.cells[j].length;i++){ //Row
                if (!this.cells[j][i].isLeft()){//Left
                    System.out.print(".");
                }else {
                    System.out.print("O");
                }
                System.out.print(".");//Centre
                if (!this.cells[j][i].isRight()){ //Right
                    System.out.print(".");
                }else {
                    System.out.print("O");
                }
            }
            System.out.println();
            for (int i=0;i<this.cells[j].length;i++){ //Below
                if (!this.cells[j][i].isDown()){
                    System.out.print(".");
                }else {
                    System.out.print("O");
                }
            }
        }
    }
}