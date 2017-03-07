import java.util.List;

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
    private List<Cell> stack;
    Maze(int x, int y) {
        this.cells=new Cell[y][x];
        generate();
    }
    void generate(){

    }
}