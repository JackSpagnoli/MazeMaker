import java.io.File;
import java.util.Scanner;
class Terminal {
    public static void main(String[] args){
        mainMenu();
    }
    private static void mainMenu(){
        do{
            switch (getChoice(new String[]{"Welcome to the maze generator/solver/player thing", "You can generate a maze to solve or play in the play/solve menus"}, new String[]{"1. Generate a maze", "2. Solve a maze", "3. Play a maze", "4. Exit the program"})){
                case 1:
                    generateMaze(true);
                    break;
                case 2:
                    solveMaze();
                    break;
                case 3:
                    playGame();
                    break;
                case 4:
                    return;
            }
        }while(getChoice(new String[]{"Would you like to exit the program?"}, new String[]{"1. Yes", "2. No"}) == 2);
    }
    private static void generateMaze(boolean allowSave){
        int y, x;
        do {
            System.out.println("Enter the height of the maze (>1)");
            y = getInt();
        }while (y<2);
        do {
            System.out.println("Enter the width of the maze (>1)");
            x = getInt();
        }while (x<2);
        float loops = 0;
        if (getChoice(new String[] {"Would you like loops in your maze?"}, new String[]{"1. Yes","2. No"}) == 1){
            do {
                System.out.println("Enter the chance of loops in the maze (0 - 1)");
                loops = getFloat();
            }while (loops<=0 || loops > 1);
        }
        Maze m = new Maze(x, y , loops);
        if(allowSave) {
            switch (getChoice(new String[]{"Would you like your maze saved to a text file or printed to the screen?"}, new String[]{"1. Print to screen", "2. Write to a text file", "3. Both"})) {
                case 1:
                    m.printMaze();
                    return;
                case 3:
                    m.printMaze();
                case 2:
                    System.out.println("Please enter the file name you would like to save to");
                    String temp;
                    do {
                        temp = getString();
                    } while (temp.length() == 0);
                    m.writeMaze(new File(temp));
            }
        }else{
            m.writeMaze(new File("Temp.temp"));
        }
    }
    private static void solveMaze(){
        boolean generated = getChoice(new String[]{"Would you like to generate a new maze or solve one from a text file?"}, new String[]{"1. Generate a new maze", "2. Solve from a text file"})==1;
        String file = "Temp.temp";
        if (generated) generateMaze(false);
        else{
            do {
                System.out.println("Enter the name of the text file you wish to load");
                file = getString();
            }while (!new File(file).exists());
        }
        long t1=System.nanoTime();
        MazeSolver m = new MazeSolver(new File(file));
        double time=((System.nanoTime()-t1)*Math.pow(10,-9));

        switch (getChoice(new String[]{"Would you like your maze saved to a text file or printed to the screen?"}, new String[]{"1. Print to screen", "2. Write to a text file", "3. Both"})) {
            case 1:
                m.printTracedMaze();
                break;
            case 3:
                m.printTracedMaze();
            case 2:
                System.out.println("Please enter the file name you would like to save to");
                String temp;
                do {
                    temp = getString();
                } while (temp.length() == 0);
                m.saveTracedMaze(new File(temp));
        }
        if(getChoice(new String[]{"Would you like to see statistics about the maze solver?"}, new String[]{"1. Yes", "2. No"}) == 1){
            System.out.println("Time taken: " + time + "s");
            System.out.println("By converting the maze into a graph the program made the problem " + (100-(((double)m.getVertexLength() / ((m.getDimenstions()[0] * (double)m.getDimenstions()[1])))*100)) + "% simpler");
            System.out.println("This saved approximately " + ((time/((double)m.getVertexLength() / ((double)m.getDimenstions()[0] * (double)m.getDimenstions()[1]))) - time) + " seconds");
        }
    }
    private static void playGame(){
        boolean generated = getChoice(new String[]{"Would you like to generate a new maze or play one from a text file?"}, new String[]{"1. Generate a new maze", "2. Play from a text file"})==1;
        String file = "Temp.temp";
        if (generated) generateMaze(false);
        else{
            do {
                System.out.println("Enter the name of the text file you wish to load");
                file = getString();
            }while (!new File(file).exists());
        }
        MazeGame m = new MazeGame(new File(file));
        if (generated){
            if (getChoice(new String[]{"Would you like the save the maze you just played?"}, new String[]{"1. Yes", "2. No"}) == 1){
                System.out.println("Please enter the file name you would like to save to");
                do {
                    file = getString();
                } while (file.length() == 0);
                m.saveMaze(new File(file));
            }
        }
    }
    private static int getInt(){
        try{
            return new Scanner(System.in).nextInt();
        }catch (Exception e){
            System.out.println("Please enter a number");
            return getInt();
        }
    }
    private static float getFloat(){
        try{
            return new Scanner(System.in).nextFloat();
        }catch (Exception e){
            System.out.println("Please enter a number");
            return getFloat();
        }
    }
    private static int getChoice(String[] prompt, String[] choices){
        for (String s : prompt){
            System.out.println(s);
        }
        for (String s : choices){
            System.out.println(s);
        }
        int temp;
        do {
            temp = getInt();
        }while (temp < 1 || temp > choices.length);
        return temp;
    }
    private static String getString(){
        return new Scanner(System.in).nextLine();
    }
}