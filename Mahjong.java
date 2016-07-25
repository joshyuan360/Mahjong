import java.io.*;
import hsa.*;
import java.awt.*;
import java.util.StringTokenizer;

/**
 * @author Joshua Yuan
 * December 19, 2014 Version 3.0
 * Allows the user to play three levels of Mahjong Solitaire.
 * Software includes a menu, 3 levels, instructions, and high scores.
 */
public class Mahjong
{
  private hsa.Console c;
  private String choice; // menu choice
  private String playerName;
  private char key; // key user has pressed
  /** All the tiles needed for the board. The tiles are stored in an order that is randomly-generated each time a new game is played. */
  private Image randTile[];
  /** Contains all the tile images. */
  private Image picture[] = new Image[NUM_IMAGES];
  private static final int NUM_IMAGES = 42;
  /** tileLoc [0][] stores the x-coordinates of the tiles, and tileLoc [1][] stores the y-coordinates of the tile. */
  private int tileLoc[] [];
  /** 1 if the user selects easy, 2 if the user selects medium, and 3 if the user selects hard. */
  private int level;
  /** Current score of the active game. */
  private int score;
  /** Number of tiles that should be used for generating the board. Depends on the user's selected level. */
  private int numTiles;
  /**
   * index [0] stores an int such that tileLoc [1 or 2][index [0]] displays the coordinates of the first tile the user 
   * selects. Similarly, index [1] stores an int such that tileLoc [0 or 1][index [1]] contains the coordinates of 
   * the second tile the user selects.
   */
  private int index[] = new int [2];
  
  private static final Font BUTTON_FONT = new Font ("Cooper Black", Font.PLAIN, 26);
  private static final Color BACKGROUND_COLOUR = new Color (162, 252, 162);
  private static final Color LIGHT_BLUE = new Color (0, 180, 255);
  private static final String FILE_NAME = "HighScores.dat";
    
  /**
   * Draws a thick rectangle. Invoked in selectTiles().
   * @param x x-value of the top left corner of the rectangle.
   * @param y y-value of the top left corner of the rectangle.
   * @param width Width of the rectangle.
   * @param height Height of the rectangle.
   * @param colour Colour of the rectangle.
   */
  private void drawThickRect (int x, int y, int width, int height, Color colour)
  {
    c.setColour (colour);
    for (int i = -3 ; i <= 3 ; i++)
      for (int j = -3 ; j <= 3 ; j++)
      c.drawRect (x + i, y + j, width, height);
  }
  
  
  /**
   * Allows the user to select "Level Selection", "Instructions", "High Scores", or "Exit"
   * using the '/' key.
   */
  private void mainMenu() {
    drawTitle();
    
    c.println("Press 'w' to move up and 's' to move down. Press '/' to enter.");
    c.setFont(BUTTON_FONT);
    c.drawString("LEVEL SELECTION", 275, 235);
    c.drawString("INSTRUCTIONS", 291, 285);
    c.drawString("HIGH SCORES", 303, 335);
    c.drawString("EXIT", 365, 385);
    
    for (int i = 250; i <= 350; i += 50) {
      drawThickRect(200, i, 400, 50, Color.white);   //ouputs four thick yellow rectangles
    }
    
    char key;
    int y = 200; // y-value of the current rectangle's top left corner
    do {
      drawThickRect(200, y, 400, 50, LIGHT_BLUE);
      if ((key = c.getChar()) == 'w' && y >= 250 || key == 's' && y <= 300) {
        drawThickRect(200, y, 400, 50, Color.white); //only erases red rectangle
        y += (key == 'w') ? -50 : 50;
      }
    }
    while (key != '/');
    choice = "" + (y / 50 - 3);
  }
  
  
  /**
   * Clears the screen, adds a green colour to the background, and displays the title.
   */
  private void drawTitle() {
    c.clear();
    
    c.setColour(BACKGROUND_COLOUR);
    c.fillRect(0, 0, 799, 599);
    
    c.setFont(new Font("Chiller", Font.BOLD, 50));
    c.setColour(Color.black);
    c.drawString("Mahjong", 325, 50);
    
    c.setCursor(5, 1);
    c.setTextBackgroundColour(BACKGROUND_COLOUR);
  }
  
  
  /**
   * Erases a line of text on the screen and moves the cursor back to the beginning of the erased string.
   * @param x Row number of the location where the string starts.
   * @param y Column number of the location where the sring starts.
   */
  private void eraseText(int x, int y) {
    c.setCursor (x, y);
    c.println ();
    c.setCursor (x, y);
  }
  
  
  /**
   * Pauses the program and waits for keyboard input.
   * @param message The message to be displayed while waiting for user input.
   */
  private void pauseProgram (String message)
  {
    c.println ("\n" + message + "\n");
    c.getChar ();
  }
  
  
  /**
   * Updates HighScores.dat file by inserting the last score into the appropriate location in the file, if necessary.
   */
  private void updateHighScores ()
  {
    final int MAX = 10;      // size of the line array
    String[] line = new String [MAX];
    
    try {
      BufferedReader input = new BufferedReader(new FileReader(FILE_NAME));
      String firstLine = input.readLine();
      
      // checks for a valid header, and resets the file if none is found
      if (firstLine == null || !firstLine.equals("Mahjong file verification header.")) {
        input.close();
        resetFile();
        input = new BufferedReader(new FileReader(FILE_NAME));
        input.readLine();
      }
      
      for (int i = 0; i < MAX; i++) { // initializes values for line[]
        line[i] = input.readLine();
        c.println(line[i]);
      }
      
      // Inserts the player's name and score if a line between 0 and 10 is null. 
      // If not, it inserts the information into the proper location and moves all the lines below one line down.
      for (int i = 0; i < MAX; i++) {
        // Inserts new info into the file and breaks the loop only if a line between the first and tenth line is empty.
        if (line[i] == null || line [i].equals ("")) { 
          line[i] = playerName + " " + score;
          break;
        }
        
        StringTokenizer tokenizer = new StringTokenizer(line[i]);
        for (int j = 1; j <= tokenizer.countTokens(); j++) { // runs tokenizer.nextToken () until it reaches the last token
          tokenizer.nextToken();
        }
        // Checks if the player's score is higher than one stored in a line in the file.
        if (score >= Integer.parseInt(tokenizer.nextToken())) {
          for (int j = 9; j > i; j--) { // moves all the lines (below the new line) one line down
            line[j] = line[j - 1];
          }
          line[i] = playerName + " " + score;
          break;
        }
      }
      
      input.close();
      
      PrintWriter output = new PrintWriter(new FileWriter(FILE_NAME));
      output.println("Mahjong file verification header.");
      for (int i = 0; i < MAX; i++) { // copies all the contents of the line array into the file
        if (line[i] != null) {
          output.println(line[i]);
        }
      }
      output.close();
    }
    catch (IOException e) {
    }
  }
  
  
  /**
   * Asks the player for his or her name and stores that string in the variable playerName.
   */
  private void askPlayerName() {
    drawTitle();
    c.print("Please enter your name: ");
    playerName = c.readLine();
    eraseText(5, 1);
  }
  
  
  /**
   * Imports the 42 images needed for the Mahjong board.
   */
  private void loadImages() {
    MediaTracker tracker = new MediaTracker(new Frame()); // allows the pictures to be tracked
    
    //initializes the values for the picture array
    for (int i = 0; i <= 8; i++) {
      picture[i] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/character" + (i + 1) + ".png")); //0 to 8 character
      picture[i + 9] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/bamboo" + (i + 1) + ".png")); //9 to 17 bamboo
      picture[i + 18] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/circle" + (i + 1) + ".png")); //18 to 26 circle
    }
    for (int i = 0; i <= 3; i++) {
      picture[i + 27] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/wind" + (i + 1) + ".png")); //27 to 30 wind
      picture[i + 31] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/season" + (i + 1) + ".png")); //31 to 34 season
      picture[i + 35] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/flower" + (i + 1) + ".png")); //35 to 38 flower
    }
    for (int i = 0; i <= 2; i++) {
      picture[i + 39] = Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/dragon" + (i + 1) + ".png")); //39 to 41 dragon
    }
    
    //Begin tracking of image loading
    for (int i = 0; i < NUM_IMAGES; i++) {
      tracker.addImage (picture [i], 0);  // Add the picture to the list of images to be tracked
    }
    try {
      tracker.waitForAll();
    }
    catch (InterruptedException e) {
    }
    if (tracker.isErrorAny()) { // if error occurs while loading images
      new Message("Error loading images. Please exit the program.", "ERROR");
      mainMenu();
    }
  }
  
  
  /**
   * Uses the Images array to generate a board based on the user's desired level. Tiles in Images are repeated as necessary, and the order of the tiles is randomized.
   * Code structure explanation:
   * Except for the last for loop, all the for loops are used to initialize values for the tile array.
   * The last for loop is used to output the tiles onto the screen.
   * The while loop is used to continually generate random numbers until randTile is fully initialized.
   * The first if structure changes the position of the 45th tile, if needed, depending on the level selected. It also lowers the number of tile on the board, if needed.
   * The second if structure prevents tile circles appearing if the user selects medium or hard.
   * The third if structure prevents more character tiles from appearing if the user selects hard.
   * The fourth if structure prevents the same value from the tile array from occuring more than once in the randTile array.
   * The fifth if structure outputs the 45th tile again for the second tile. This is necessary it would otherwise be covered by the tiles on top.
   */
  private void generateBoard() {
    int randIndex; // stores the images in a randomized order
    Image tile[]; // contains the images of the tile in a predefiend order, tile height 51 width 41
    
    // The array below stores the x and y coordinates of the tiles.
    // It is also used to reset the values stored in this.tileLoc, which changes as the user plays the game.
    int tileLoc[][] = {
      {
        155, 196, 237, 278, 319, 360, 401, 442, 483, 524, 565, 606, 237, 278, 319, 360, 401, 442, 483, 
        524, 196, 237, 278, 319, 360, 401, 442, 483, 524, 565, 114, 155, 196, 237, 278, 319, 360, 401, 
        442, 483, 524, 565, 606, 647, 688, 155, 196, 237, 278, 319, 360, 401, 442, 483, 524, 565, 606, 
        196, 237, 278, 319, 360, 401, 442, 483, 524, 565, 237, 278, 319, 360, 401, 442, 483, 524, 155, 
        196, 237, 278, 319, 360, 401, 442, 483, 524, 565, 606, 278 - 6, 319 - 6, 360 - 6, 401 - 6, 442 - 6, 
        483 - 6, 278 - 6, 319 - 6, 360 - 6, 401 - 6, 442 - 6, 483 - 6, 278 - 6, 319 - 6, 360 - 6, 401 - 6, 
        442 - 6, 483 - 6, 278 - 6, 319 - 6, 360 - 6, 401 - 6, 442 - 6, 483 - 6, 278 - 6, 319 - 6, 360 - 6, 
        401 - 6, 442 - 6, 483 - 6, 278 - 6, 319 - 6, 360 - 6, 401 - 6, 442 - 6, 483 - 6, 319 - 12, 360 - 12, 
        401 - 12, 442 - 12, 319 - 12, 360 - 12, 401 - 12, 442 - 12, 319 - 12, 360 - 12, 401 - 12, 442 - 12, 
        319 - 12, 360 - 12, 401 - 12, 442 - 12, 360 - 18, 401 - 18, 360 - 18, 401 - 18, 380 - 24
      },
      {
        97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 97, 148, 148, 148, 148, 148, 148, 148, 148, 199, 199, 
        199, 199, 199, 199, 199, 199, 199, 199, 274, 250, 250, 250, 250, 250, 250, 250, 250, 250, 250, 250, 
        250, 274, 274, 301, 301, 301, 301, 301, 301, 301, 301, 301, 301, 301, 301, 352, 352, 352, 352, 352, 
        352, 352, 352, 352, 352, 403, 403, 403, 403, 403, 403, 403, 403, 454, 454, 454, 454, 454, 454, 454, 
        454, 454, 454, 454, 454, 148 + 7, 148 + 7, 148 + 7, 148 + 7, 148 + 7, 148 + 7, 199 + 7, 199 + 7, 
        199 + 7, 199 + 7, 199 + 7, 199 + 7, 250 + 7, 250 + 7, 250 + 7, 250 + 7, 250 + 7, 250 + 7, 301 + 7, 
        301 + 7, 301 + 7, 301 + 7, 301 + 7, 301 + 7, 352 + 7, 352 + 7, 352 + 7, 352 + 7, 352 + 7, 352 + 7, 
        403 + 7, 403 + 7, 403 + 7, 403 + 7, 403 + 7, 403 + 7, 199 + 14, 199 + 14, 199 + 14, 199 + 14, 
        250 + 14, 250 + 14, 250 + 14, 250 + 14, 301 + 14, 301 + 14, 301 + 14, 301 + 14, 352 + 14, 352 + 14, 
        352 + 14, 352 + 14, 250 + 21, 250 + 21, 301 + 21, 301 + 21, 274 + 28
      }
    };
    
    if (level == 1) {
      numTiles = 86;
      tileLoc[0][44] = 565 + 41;
      tileLoc[1][44] = 454;
    } else if (level == 2) {
      numTiles = 122;
      tileLoc[0][44] = 483 - 6;
      tileLoc[1][44] = 403 + 7;
    } else {
      numTiles = 144;
    }
    
    this.tileLoc = tileLoc;
    tile = new Image[numTiles];
    randTile = new Image[numTiles];
    //initializes values for the tile array (in order)
    //repeated four times for all levels
    for (int i = 9; i <= 17; i++) {
      for (int j = 0; j <= 3; j++) {
        tile[4 * (i - 9) + j] = picture[i]; //bamboo repeated four times (picture 9 to 17)
      }
    }
    for (int i = 27; i <= 30; i++) {
      for (int j = 0; j <= 3; j++) {
        tile[4 * (i - 27) + j + 36] = picture[i]; //wind repeated four times (picture 27 to 30)
      }
    }
    for (int i = 39; i <= 41; i++) {
      for (int j = 0; j <= 3; j++) {
        tile[4 * (i - 39) + j + 52] = picture[i]; //dragon repeated four times (picture 39 to 41)
      }
    }
    for (int i = 0; i <= 8; i++) {
      for (int j = 0; j <= 1; j++) {
        tile[2 * i + j + 64] = picture[i]; //character repeated twice (picture 0 to 8)
      }
    }
    for (int i = 31; i <= 34; i++) {
      tile[i - 31 + 82] = picture[i]; //season not repeated (picture 35 to 38)
    }
    //end initilization of tile [0] to tile [85]
    
    //begin intialization from tile [86] to [121]
    if (level != 1) {
      for (int i = 18; i <= 26; i++) {
        for (int j = 0; j <= 3; j++) {
          tile[4 * (i - 18) + j + 86] = picture[i]; //circle repeated four times if the level is medium or hard
        }
      }
    }
    //end
    
    if (level == 3) {
      for (int i = 0; i <= 8; i++) {
        for (int j = 0; j <= 1; j++) {
          tile[2 * i + j + 122] = picture[i]; //character repeated twice again for the hard level (picture 0 to 8)
        }
      }
      for (int i = 35; i <= 38; i++) {
        tile[i - 35 + 140] = picture[i];
      }
    }
    //end tile array initialization
    
    //initializes values of randTile in a pseudo-random order so board changes each time the user plays
    int j = 0;
    while (j != numTiles) {
      randIndex = (int) (Math.random() * numTiles);
      if (tile[randIndex] != null) {
        randTile[j] = tile[randIndex];
        tile[randIndex] = null;
        j++;
      }
    }
    //end randTile initialization
    
    //tile output begins with a delay to simulate a player creating a board
    for (int i = 0; i < numTiles; i++) {
      c.drawImage(randTile[i], tileLoc[0][i], tileLoc[1][i], null);
    }
    if (level == 2) {
      c.drawImage(randTile[44], tileLoc[0][44], tileLoc[1][44], null);
    }
    //end tile output
    c.setCursor(4, 1);
    c.print("Score: 0");
  }
  
  
  /**
   * Updates the board by removing the two tiles that the user has matched.
   */
  private void updateBoard() {
    drawTitle();
    // Changes the x and y coordinates of the removed tiles to -100, so that they no longer appear on the screen.
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        tileLoc[i][index[j]] = -100;
      }
    }
    // Redraws the board
    for (int i = 0; i < numTiles; i++) {
      c.drawImage(randTile[i], tileLoc[0][i], tileLoc[1][i], null);
    }
    // Draw the 45th tile again if the user is playing the medium level game. Otherwise, it will be covered by the tiles on top.
    if (level == 2) {
      c.drawImage(randTile[44], tileLoc[0][44], tileLoc[1][44], null);
    }
  }
  
  
  /**
   * Method levelSelection allows the user to select easy, medium, or hard level, and stores their choice as an integer in the variable level.
   */
  private void levelSelection ()
  {
    int y = 225; // y-value of the selection rectangle's top left corner
    drawTitle();
    c.println("Press 'w' to move up and 's' to move down. Press '/' to enter.");
    
    c.setFont(BUTTON_FONT);
    c.drawString("EASY", 362, 260);
    c.drawString("MEDIUM", 342, 310);
    c.drawString("HARD", 358, 360);
    
    for (int i = 275; i <= 325; i += 50) { // outputs multiple thick rectangles
      drawThickRect(200, i, 400, 50, Color.white);   //ouputs four thick yellow rectangles
    }
    do {
      drawThickRect(200, y, 400, 50, LIGHT_BLUE);
      // only erases current rectangle if user moves rectangle up or down
      if ((key = c.getChar ()) == 'w' && y >= 275 || key == 's' && y <= 275) {
        drawThickRect (200, y, 400, 50, Color.white); //only erases red rectangle
        y += (key == 'w') ? -50 : 50;
      }
    }
    while (key != '/'); // updates rectangle based on user input until they press '/'
    level = (y - 175) / 50;
  }
  
  
  /**
   * Method tileRemovable uses the tile index of the tile the user is attempting to remove, 
   * as well as the tileLoc array, to determine if the tile the user wishes to remove is removable.
   * @param index The index of the tile that the user attempts to remove.
   * @param tileLoc The x and y coordinates of all the current tiles on the board.
   * @return <code>true</code> if the tile is removable, <code>false</code> otherwise.
   */
  private boolean tileRemovable(int index, int[][] tileLoc) {
    // Returns false if the very top tile is present 
    // and the tile the user wants to remove is one of the four directly below.
    if (level == 2 && index == 121 && tileLoc[0][44] != -100 || 
        level == 1 && index == 85 && tileLoc[0][44] != 100 || 
        index > 138 && index < 143 && tileLoc[0][143] != -100 || 
        index == 45 && tileLoc[0][30] != -100 || 
        index == 56 && tileLoc[0][43] != -100) { 
      return false;
    }

    // Returns true if it is the first or last tile (to prevent ArrayIndexOutOfBoundsException) 
    // and if there is no tile either left or right of the tile.
    if (index == 0 || index == numTiles - 1 || 
        tileLoc[0][index - 1] != tileLoc[0][index] - 41 || 
        tileLoc[0][index + 1] != tileLoc[0][index] + 41) {   
      return true;
    }
    return false;   //otherwise the tile is not removable
  }
  
  
  /**
   * Returns the array index of the highest tile above the bottom-most tile 
   * specified by <code>bottomX</code> and <code>bottomY</code>. 
   * @param bottomX Current x value of the tile at the bottom of the board.
   * @param bottomY Current y value of the tile at the bottom of the baord.
   * @param tileLoc Current x and y coordinates of all the tiles on the board.
   * @return the array index of the highest tile above the specified tile.
   */
  private int topTileIndex(int bottomX, int bottomY, int[] [] tileLoc) {
    for (int height = 4; height >= 0; height--) { //checks for tiles above the tile at bottomX, bottomY, and returns the tile index of the highest tile found.
      for (int index = 0; index < numTiles; index++) {
        if (tileLoc[0][index] == bottomX - 6 * height && tileLoc[1][index] == bottomY + 7 * height) {
          return index; //returns the index of the highest tile above the tile at bottomX, bottomY
        }
      }
    }
    return -1; //if there is no tile at or above (bottomX, bottomY), -1 is returned
  }
  
  
  /**
   * Determines if the two selected match, by comparing the indices of the selected tiles
   * with the indices of the randTile array.
   * @param index1 Array index of the first tile the user has removed.
   * @param index2 Array index of the second tile the user has removed.
   * @param randTile Stores the images in the randomized order.
   * @return <code>true</code> if the tiles match, <code>false</code> otherwise.
   */
  private boolean tilesMatch(int index1, int index2, Image[] randTile) {
    for (int i = 1; i < 5; i++) { // check if the first tile matches any of the season and flower tiles.
      for (int j = 1; j < 5; j++) {       // check if the second tile matches any of the season and flower tiles
        // check if the two tiles selected are both flowers or both seasons
        if (randTile[index1] == Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/season" + (i) + ".png")) && 
            randTile[index2] == Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/season" + (j) + ".png")) || 
            randTile[index1] == Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/flower" + (i) + ".png")) && 
            randTile[index2] == Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/flower" + (j) + ".png"))) {
          return true; //returns true of both tiles are seasons or flowers
        }
      }
    }
    if (randTile[index1] == randTile[index2]) { // check if two tiles are identical
      return true; //returns if both tiles are the exact same
    }
    return false; //returns false if the tiles do not match
  }
  
  
  /**
   * Uses the tileLoc array and the randTile array to determine if there are any more possible moves left.
   * @param tileLoc x and y coordinates of all the tiles on the board.
   * @param randTile Stores the images of the tiles in the randomized order.
   * @return <code>true</code> if there is at least one more possible move left, <code>false</code> otherwise.
   */
  private boolean moreMovesLeft (int[] [] tileLoc, Image[] randTile) {
    // loops compare all the tiles with each other
    // if statement checks if two tiles can be paired
    for (int i = 0; i < numTiles; i++) {
      for (int j = 0; j < numTiles; j++) {
        if (tileRemovable(i, tileLoc) && tileRemovable(j, tileLoc) && tilesMatch(i, j, randTile) && i != j && 
            tileLoc[0][i] != -100 && tileLoc[0][j] != -100 && 
            topTileIndex(tileLoc[0][i], tileLoc[1][i], tileLoc) == i && 
            topTileIndex(tileLoc[0][j], tileLoc[1][j], tileLoc) == j) {
/* uncomment the lines below if you want the program to show you which tiles to remove. */
//       c.setColour (Color.blue);
//       for (int x = -3 ; x <= 3 ; x++) {
//           c.drawRect (tileLoc [0] [i] + x, tileLoc [1] [i] + x, 41, 51);
//           c.drawRect (tileLoc [0] [j] + x, tileLoc [1] [j] + x, 41, 51);
//       }
          return true;
        }
      }
    }
    return false;
  }
  
  
  /**
   * Uses the current X coordinate of the tile at the bottom of the board, the array index of the current tile, 
   * the key which the user selects, and a boolean value for whether the top tile present
   * to return what the next X coordinate should be.
   * @param currentBottomX Current x coordinate of the tile at the bottom of the board.
   * @param index Array index of the tile that the user is currently on.
   * @param key The key that the user pressed.
   * @param topTilePresent <code>true</code> if the highest tile on the board is present, <code>false</code> otherwise.
   * @return The next X coordinate.
   */
  private int nextBottomX(int currentBottomX, int index, char key, boolean topTilePresent) {
    if (key == 'a' || key == 'A') {
      if ((index == 140 || index == 142) && topTilePresent) {
        return currentBottomX - 27;
      }
      if (index == 143) {
        return currentBottomX - 14; //should go up or down depending on their pathway
      }
      return currentBottomX - 41;
    }
    if (key == 'd' || key == 'D') {
      if ((index == 139 || index == 141) && topTilePresent) {
        return currentBottomX + 14;
      }
      if (index == 143) {
        return currentBottomX + 27;
      }
      return currentBottomX + 41;
    }
    if (key == 'w') {
      if ((index == 141 || index == 142) && topTilePresent) {
        return 374;
      }
    }
    if (key == 's') {
      if ((index == 139 || index == 140) && topTilePresent) {
        return 374;
      }
      if (index == 143) {
        return 360;
      }
    }
    return currentBottomX;
  }
  
  
  /**
   * Uses the current Y coordinate of the tile at the bottom of the board, the array index of the current tile, the key which the user selects, and a boolean value for whether the top tile present
   * to return what the next Y coordinate should be.
   * @param currentBottomY Current y coordinate of the tile at the bottom of the board.
   * @param index Array index of the tile that the user is currently on.
   * @param key The key that the user pressed.
   * @param topTilePresent <code>true</code> if the highest tile on the board is present, <code>false</code> otherwise.
   */
  private int nextBottomY(int currentBottomY, int index, char key, boolean topTilePresent) {
    // Except for the [if (topTilePresent)] statements, 
    // all of the other if structures in the method determine if the array index of the tile the user 
    // is trying to select matches the number written in the boolean condition. 
    // This allows the method to determine what the next x coordinate of the selection rectangle should be.
    // [if (topTilePresent)] alters the location of the selection rectangle 
    // to accomodate the top tile, but only if it is present.
    if (key == 'a' || key == 'A') {
      if (index == 31) return currentBottomY + 24;
      if (index == 43) return currentBottomY - 24;
      if (index == 45) return currentBottomY - 27;
      if (index == 143) return currentBottomY - 31;
      if (topTilePresent) {
        if (index == 140) return currentBottomY + 31;
        if (index == 142) return currentBottomY - 20;
      }
    }
    
    if (key == 'd' || key == 'D') {
      if (index == 30) return currentBottomY - 24;
      if (index == 42) return currentBottomY + 24;
      if (index == 56) return currentBottomY - 27;
      if (index == 143) return currentBottomY - 31;
      if (topTilePresent) {
        if (index == 139) return currentBottomY + 31;
        if (index == 141) return currentBottomY - 20;
      }
    }
    
    if (key == 'w' || key == 'W') {
      if (index == 30 || index == 43) return currentBottomY - 24;
      if ((index == 141 || index == 142) && topTilePresent) return 281;
      if (index == 143) return currentBottomY - 31;
      return currentBottomY - 51;
    }
    
    if (key == 's' || key == 'S') {
      if (index == 30 || index == 43) return currentBottomY + 27;
      if ((index == 139 || index == 140) && topTilePresent) return 281;
      if (index == 143) return 301;
      return currentBottomY + 51;
    }
    
    return currentBottomY;
  }
  
  
  /**
   * Method rowHasTiles uses the current Y coordinate and the tileLoc array to determine if a certain row has tiles.
   * @param bottomY Current y coordinate of the tile at the bottom of the board.
   * @param tileLoc Current x and y coordinates of all the tiles on the board.
   * @return <code>true</code> if the row specified contains tiles, <code>false</code> otherwise.
   */
  private boolean rowHasTiles(int bottomY, int[] [] tileLoc) {
    int row = (bottomY - 97) / 51; // the row the user is currently on
    int END_OF_ROW_TILE_INDEX[] [] = { // stores the array indices of the tiles that are at the end of each row
      {0, 12, 20, 30, 45, 57, 67, 75},
      {11, 19, 29, 44, 56, 66, 74, 86}};
    
    try {
      // loops from left-most tile to right-most tile
      for (int i = END_OF_ROW_TILE_INDEX[0][row]; i <= END_OF_ROW_TILE_INDEX[1][row]; i++) {
        if (tileLoc [0] [i] != -100) return true; // checks if tile is visible on the screen
      }
    }
    catch (ArrayIndexOutOfBoundsException e) {
    }
    
    return false;
  }
  
  
  /**
   * Erases a selection or highlighted rectangle on the board.
   * @param rectX x coordinate of the top left corner of the rectangle to be erased.
   * @param rectY y coordinate of the top right corner of the rectangle to be erased.
   */
  private void eraseRect(int rectX, int rectY) {
    drawThickRect(rectX, rectY, 41, 51, BACKGROUND_COLOUR); //erase selection rectangle
    for (int j = 0; j < numTiles; j++) { // redraws the board
      c.drawImage(randTile[j], tileLoc[0][j], tileLoc[1][j], null);
    }
    if (level == 2) { // redraw 45th tile if the user is playing the medium level
      c.drawImage(randTile[44], tileLoc[0][44], tileLoc[1][44], null);
    }
  }
  
  
  /**
   * Allows the user to select two tiles.
   * Code structure explanation (open source code file for formatting):
   * First for loop is used so that the if statement inside checks through the tile locations of all the tiles in the bottom layer of the bottom. Start: 0 Stop: 87
   * The second for loop is used to avoid code repetition: the first and second tile selection use the same code, and the index of those tiles are stored in index [i]. Start: 0 Stop: 2
   * The do while loop within the loop above is used to continually allow the user to navigate through the board and make attempts to remove a tile until they remove a tile that can be removed.
   *       Within the do while loop above:
   *          The first for loop is used to redraw all the tiles on the board. Start: 0 Stop: numTiles
   *          The do while loop below is used to continually move the selection rectangle up/down (depending on the key pressed) until the selection rectangle hits a row that has tiles.
   *          The while loop below is used to continually alternate between searching left and searching right for a tile on the row until a tile is found.
   *              Within the while loop above:
   *                  The first for loop is used to move the selection rectangle multiple times to the tile on the left. Start: 1 Stop: k+1
   *                  The second for loop is used to move the selection rectangle multiple times to the tile on the right. Start: 1 Stop: k+2
   * The first if structure checks if the tile at tileLoc [0][i], tileLoc [1][i] has not been removed.
   * The second if structure returns the method if the user presses m, allowing the user to return to the main menu.
   * The third if structure highlights the previously selected tile.
   * The ternary operator below is used to return true of the top tile is present.
   * The fourth if structure is used to stop the search for a tile if a tile is found.
   * The fifth if structure is used so that the enclosed error message only displays if they attempt to remove a non-removable tile.
   * The try-catch structure restores the previous value of bottomX and bottomY if the user tries to access a tile location that does not exist.
   * The if structure after the large do while loop checks for two errors: non-matching tiles selected, or the same tile selected twice.
   *      The if structure within the one above is used to customize the error message depending on the error.
   */
  private void selectTiles() {
    int rectX = 155, rectY = 97;
    int prevBottomX = 0, prevBottomY = 0;
    int bottomX = 0, bottomY = 0;
    
    index [0] = 0;
    for (int i = 0; i < 87; i++) {
      if (tileLoc[0][i] != -100 && tileLoc[1][i] != -100) {//finds the top left tile, and the coordinates of the highest tile in that location
        bottomX = prevBottomX = tileLoc[0][i];
        bottomY = prevBottomY = tileLoc[1][i];
        rectX = tileLoc[0][topTileIndex(bottomX, bottomY, tileLoc)];
        rectY = tileLoc[1][topTileIndex(bottomX, bottomY, tileLoc)];
        break;
      }
    }
    
    for (int i = 0; i < 2; i++) {
      c.setCursor(3, 1);
      c.print(i == 0 ? "Select the first tile... " : "Select the second tile...");
      do {
        drawThickRect(rectX, rectY, 41, 51, Color.black); // selection rectangle
        
        key = c.getChar();
        if (key == 'm') return; // returns to menu
        
        eraseRect(rectX, rectY);
        // end erase
        
        if (i == 1) {
          drawThickRect(tileLoc[0][index[0]], tileLoc[1][index[0]], 41, 51, Color.yellow);
          c.setColour(Color.black);
        }
        // end tile erase
        
        // finds the value of bottomX and bottomY based on the user's click
        bottomX = nextBottomX(bottomX, index[i], key, tileLoc[0][143] != -100 ? true : false);
        
        int j = 1;
        do {
          bottomY = nextBottomY(bottomY, index[i], key, tileLoc[0][143] != -100 ? true: false);
          j++;
        }
        while (!rowHasTiles (bottomY, tileLoc) && j <= 7);
        // end find value of bottomX and bottomY
        
        // if the user attempts to access a tile location that does not exist,
        // the code below brings the selection rectangle to the closest tile available, if possible
        int k = 1; // stores how far left/right the nearest tile is from the tile directly above/below the current tile.
        while (topTileIndex(bottomX, bottomY, tileLoc) == -1 && k <= 22) {
          for (j = 1; j <= k; j++) {
            bottomX = nextBottomX(bottomX, index[i], 'a', tileLoc[0][143] != -100 ? true : false);
          }
          if (topTileIndex(bottomX, bottomY, tileLoc) != -1) {
            break;
          }
          for (j = 1; j <= k + 1; j++) {
            bottomX = nextBottomX(bottomX, index[i], 'd', tileLoc[0][143] != -100 ? true : false);
          }
          k += 2;
        }
        // end errortrapping of selection rectangle
        
        // if they try to select but the tile is not removable then key = ' ', so the big do-while loop does not exist, and they must reselect a tile
        if ((key == '/') && !tileRemovable(index[i], tileLoc)) {
          new Message("The tile is not removable.", "Cannot Remove Tile");
          key = ' ';
        }
        // end
        
        // returns index of tile on top, if no tile there, then -1 returned
        index[i] = topTileIndex(bottomX, bottomY, tileLoc);
        
        // determines the new location of the rectangle to be drawn after the key click
        // if index [i] returns a -1, then a tile does not exist at or above (bottomX, bottomY), 
        // so the value of bottomX and bottomY returns to its previous value before the bad key press
        try {
          rectX = tileLoc[0][index[i]];
          rectY = tileLoc[1][index[i]];
          prevBottomX = bottomX; // prevBottomX: backup, if user tries to access a tile location that does not exist
          prevBottomY = bottomY;
        } catch (ArrayIndexOutOfBoundsException e) {
          bottomX = prevBottomX;
          bottomY = prevBottomY;
          index[i] = topTileIndex(prevBottomX, prevBottomY, tileLoc);
        }
      }
      while (key != '/');
    }
    
    if (index[0] == index[1] || !tilesMatch(index[0], index[1], randTile)) {
      if (index[0] == index[1]) {
        new Message("The two tiles selected must be different.", "Try Again");
      } else {
        new Message("The two tiles you selected do not match.", "Try Again");
      }
      eraseRect(tileLoc[0][index [0]], tileLoc[1][index [0]]);
      selectTiles();
    }
    
  }
  
  
  /**
   * Method scoreIncrease uses he index of the tile removced to determine how much the score should increase, 
   * based on the number of tiles left on the board, and the suit of the tile.
   * @param index Array index of the first tile that has been removed.
   */
  private int scoreIncrease(int index)
  {
    int numOfTiles = 0; // number of tiles remaining on the board
    
    for (int i = 0; i < numTiles; i++) { // checks through all tile locations
      if (tileLoc[0][i] != -100) { // determine if the tile at (tileLoc [0][i], tileLoc [1][i]) has been removed
        numOfTiles++;
      }
    }
    
    // conditional statements below check if the suit of the tiles removed match the suit of the
    // tile specified in the boolean expression
    for (int i = 0; i <= 8; i++) { // loops through the first to ninth character, circle, and bamboo
      if (randTile[index] == Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/character" + (i + 1) + ".png")))
        return 1 * (numOfTiles / 2 + 1);
      if (randTile[index] == Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/circle" + (i + 1) + ".png")))
        return 2 * (numOfTiles / 2 + 1);
      if (randTile[index] == Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/bamboo" + (i + 1) + ".png")))
        return 3 * (numOfTiles / 2 + 1);
    }
    
    for (int i = 0 ; i <= 3 ; i++) { // loops through the first to fourth wind, flower, and season
      if (randTile[index] == Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/wind" + (i + 1) + ".png")))
        return 4 * (numOfTiles / 2 + 1);
      if (randTile[index] == Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/flower" + (i + 1) + ".png")))
        return 6 * (numOfTiles / 2 + 1);
      if (randTile[index] == Toolkit.getDefaultToolkit().getImage(getClass().getResource("img/season" + (i + 1) + ".png")))
        return 7 * (numOfTiles / 2 + 1);
    }
    
    return 5 * (numOfTiles / 2 + 1); //returns 5 for a dragon tile match
  }
  
  
  /**
   * Updates the score and displays the new score.
   */
  private void updateScore() {
    c.setCursor(4, 1);
    score += scoreIncrease(index[0]);
    c.println("Score: " + score);
  }
  
  
  /**
   * Returns the number of tiles that are on the board (and have not been removed).
   * @param tileLoc x and y coordinates of the tiles.
   * @return the number of tiles that are on the board (and have not been removed).
   */
  private int numTilesRemaining (int[][] tileLoc) {
    int numOfTiles = 0; // increments each time a remaining tile is found
    for (int i = 0; i < numTiles; i++) {
      if (tileLoc[0][i] != -100) { // checks if tile is still on the board
        numOfTiles++;
      }
    }
    return numOfTiles;
  }
  
  
  /**
   * Displays a final message to the user after they have finished playing the game.
   */
  private void displayMessage ()
  {
    if (numTilesRemaining (tileLoc) == 0)
      new Message ("Congratulations! You have removed all the tiles. Press OK to return to the main menu.", "Game Over");
    else
      new Message ("No more moves left! Press OK to return to the main menu.", "Game Over");
  }
  
  
  /**
   * Controls the order of method execution for the Mahjong game.
   */
  public void display ()
  {
    score = 0;
    askPlayerName ();
    generateBoard ();
    // allows user to keep selecting tiles to remove until no more tiles can be removed
    while (moreMovesLeft (tileLoc, randTile))
    {
      selectTiles ();
      if (key == 'm') // allow user to go back to main menu if they press 'm'
        return;
      updateBoard ();
      updateScore ();
    }
    displayMessage ();
    updateHighScores ();
  }
  
  
  /**
   * Outputs two pages of instructions to the user about how to play the game and how scores are calculated.
   */
  private void instructions() {
    String[] suit = {"Character", "Circle", "Bamboo", "Wind", "Dragon", "Flower", "Season"};
    
    drawTitle();
    c.println("Game Instructions: \n\nThe object of the game is to remove all the tiles on the board by matching them together. " + 
               "To remove a tile, use  the 'w' and 's' keys to navigate to the desired row, and then use the 'a' and 'd' keys to select a specific tile. " + 
               "Press '/' to remove the tile. Pairs can only be removed if both tiles    satisfy the following conditions:");
    c.println("\n     a) There cannot be tiles both directly left and right of the tile.\n     " + 
               "b) There are no tiles directly above any part of the tile to be removed.\n     " +
               "c) The two tiles must be identical, unless they are both seasons or both flowers.");
    pauseProgram("Press any key to read the next page of instructions...");
    
    drawTitle();
    c.println("Scoring:\n\nYou are awarded points each time you remove a pair. " +
               "The amount of points you receive after removing a pair of tiles is calculated using the following formula:" + 
               "\n\n[number of pairs remaining (including the last pair removed)] x [multiplier]\n\nThe table below shows the multiplier for each suit:");
    
    c.setCursor(14, 26);
    c.print("Suit", 40);
    c.println("Multiplier");
   
    for (int i = 0 ; i < 7 ; i++) {
      c.setCursor (i + 16, 26);
      c.print (suit [i], 49);
      c.print (i + 1);
    }
    
    pauseProgram("\nPress any key to return to the main menu...");
  }
  
  /**
   * Resets HighScores.dat, or creates one if one does not exist.
   */
  private void resetFile() {
    PrintWriter output;
    try {
      output = new PrintWriter(new FileWriter(FILE_NAME));
      output.println("Mahjong file verification header.");
      for (int i = 1; i <= 10; i++) {
        output.println();
      }
      output.close();
    } catch (IOException e) {
    }
  }
  
  
  /**
   * Outputs the name and score of the 10 highest scoring games to the screen.
   */
  public void displayHighScores() {
    int column = 7; // value of the column of which the line is to be displayed on
    
    drawTitle();
    
    try {
      BufferedReader input = new BufferedReader(new FileReader(FILE_NAME));
      String line = input.readLine();  // the line that is returned by the readLine method in the BufferedReader class
      if (line == null || !line.equals ("Mahjong file verification header.")) {
        input.close();
        resetFile();
        new Message(FILE_NAME + " contains an invalid file header, so the file has been reset. Select high scores from the menu to try again.", "FILE ERROR");
        return;
      }
      
      c.setCursor(5, 26);
      c.print("Player Name", 45);
      c.println("Score");
      
      while (true) { // outputs lines from the file to the Console window until there are no more lines in the file to read
        line = input.readLine();
        if (line == null || line.equals("")) break;
        
        StringTokenizer tokenizer = new StringTokenizer(line);
        c.setCursor(column, 26);
        
        // tokenize the line and output the player's name until the player's entire name is printed
        for (int i = 1; i <= tokenizer.countTokens(); i++) {
          c.print (tokenizer.nextToken() + " ");
        }
        
        c.setCursor(column, 60);
        c.println(Integer.parseInt(tokenizer.nextToken()), 16);
        column++;
      }
    } catch (FileNotFoundException e) {
      resetFile();
      new Message(FILE_NAME + " could not be found, so it has been created. Select high scores from the menu to read the new file.", "FILE ERROR");
      return;
    } catch (IOException e) {
      new Message("The program is unable to display high scores.", "FILE ERROR");
      return;
    }
    
    pauseProgram("Press any key to return to the main menu...");
  }
  
  
  /**
   * Thanks the user for playing the game and closes the window after 10 seconds.
   */
  private void goodbye() {
    drawTitle();
    c.print("Thank you for playing Yuan Mahjong Solitaire.\n\nFor more information, visit www.joshuayuan.com\n\nThis window will close in ");
    for (int seconds = 10 ; seconds > 0 ; seconds--) {
      try {
        c.print (seconds + " second");
        c.println ((seconds == 1) ? "." : "s.");
        Thread.sleep (1000);
      } catch (InterruptedException e) {
      }
      
      eraseText(9, 27);
    }
    
    c.close();
  }
  
  
  /**
   * Class constructor.
   */
  public Mahjong() {
    c = new hsa.Console (30, 100, "Mahjong"); //height 600 width 800
  }
  
  
  /**
   * Mahjong.java main main; controls order of method execution.
   */
  public static void main(String[] args) {
    Mahjong m = new Mahjong();
    
    m.loadImages();
    do {
      m.mainMenu();
      if (m.choice.equals("1")) {
        m.levelSelection();
        m.display();
      } else if (m.choice.equals("2")) {
        m.instructions();
      } else if (m.choice.equals("3")) {
        m.displayHighScores();
      }
    }
    while (!m.choice.equals("4")); // repeat program until they choose to exit
    m.goodbye();
  }
}
