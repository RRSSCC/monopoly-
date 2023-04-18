package model;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;

//Model
//提供了游戏的数据结构和算法
//为了能够遍历，所有位置对象都存储在一个集合*中。

public class Model extends Observable {

    private final ArrayList<Position> board; //地图，储存在arraylist中，每一个元素就是一个对象（对应地图上一个位置）

    private final Player playerOne;
    private final Player playerTwo;
    private Player currentPlayer;

    private final Random rand;
    private boolean firstTurn;
    private int diceRoll;

    public ArrayList<Position> getBoard() {
        return board;
    }

    public Player getPlayerOne() {
        return playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getDiceRoll(){
        return diceRoll;
    }


    //为Game类的构造函数，用于创建一个新游戏
    //步骤：
   //1   创建了一个新的游戏棋盘，包括40个不同的位置（通过调用Position类的构造函数）
   // 2  创建了两个玩家（playerOne和playerTwo），并将当前玩家设置为playerOne
   // 3  将firstTurn设置为true，表示游戏开始，当前是游戏的第一回合。（ 没有使用，一用就报错，不使用也能进行回合）

    public Model(){
        rand = new Random();

        //构造地图
        board = new ArrayList<>();
        //第一个位置
        int posn = 1;

        // 完善地图，每个部分有 4 或 5 个位置
        for (int secn=0; secn<12; secn++){
            if (secn == 0)
                board.add(new Position(posn));
            else if (secn>0 && secn<4)
                posn = addBoardSection(secn, posn, true);
            else if (secn == 4)
                posn = addBoardSection(secn, posn, false);
                // JAIL
            else if (secn == 5) {
                posn++;
                board.add(new Position(posn));
            }
            else if (secn>5 && secn<9)
                posn = addBoardSection(secn-1, posn, true);
            else if (secn == 9)
                posn = addBoardSection(secn-1, posn, false);

        }
        //地图最多就40个格子
        assert (posn < 41) : "The position number must not go beyond 40.";

        //构造玩家
        playerOne = new Player(true, board.iterator());
        playerTwo = new Player(false, board.iterator());
        currentPlayer = playerOne;
        firstTurn = true;
    }


    //向地图添加位置。每个部分表示带有三个编号属性的道路，以及一个或两个空位置。
    // secnNum ：部分编号，用字符表示
    // posnNum ：当前位置编号
    //is5Posns ：如果此部分包含 5 个位置，则为 true；否则此部分包含 4 个位置

    private int addBoardSection(int secnNum, int posnNum, boolean is5Posns){

        char[] roads    = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H' };

        int[][] prices  = { {50, 70}, {100, 120}, {150, 170}, {200, 220},
                {250, 270}, {300, 320}, {350, 370}, {400, 420} };

        //查找
        char roadName = roads[secnNum-1];
        int[] roadPrices = prices[secnNum-1];

        // 逐个位置创建地图，同时递增位置编号
        posnNum++;
        board.add(new Position(posnNum, roadName, 1, roadPrices[0]));
        posnNum++;
        board.add(new Position(posnNum));
        posnNum++;
        board.add(new Position(posnNum, roadName, 2, roadPrices[0]));
        posnNum++;
        board.add(new Position(posnNum, roadName, 3, roadPrices[1]));
        if (is5Posns) {
            posnNum++;
            board.add(new Position(posnNum));
            return posnNum;
        }

        return posnNum;
    }


    // 启动下一个回合，掷骰子并移动玩家计数器。
    //交换当前玩家以启动新回合，然后模拟两个 6 面骰子的掷骰结果，并将玩家移动到游戏版上相应的位置
    // 计算并收取租金。

    public String nextTurn(){
        return nextTurn(
                (rand.nextInt(6) + 1)
                        + (rand.nextInt(6) + 1)
        );
    }

    public String nextTurn(int roll){
        assert (roll > 0 && roll < 13) : "This is not a valid roll. Must be"
                + " equivalent to the total of two six-sided dice.";

        String posStr = "";
        diceRoll = roll;

        //交换当前玩家
        if (!firstTurn){
            currentPlayer = (currentPlayer != playerOne) ? playerOne : playerTwo;
        }

        // 移动
        Position prevPosn = currentPlayer.getPosition();
        Position nextPosn = currentPlayer.move(diceRoll, board.iterator());

        // 如果走到监狱则返回起始位置
        if (nextPosn.getNumber() == 21) {
            nextPosn = currentPlayer.move(20, board.iterator());
            posStr += "21 (JAIL)\nSent back to Posn ";
        }

        // 计算金钱
        String s = isCurrentPosnRentable() ? calculateRent() : "";

        // 更新view
        firstTurn = false;
        setChanged();
        notifyObservers();

        return currentPlayer.toString() + " rolls " + String.valueOf(diceRoll)
                + "\nMoving from Posn " + String.valueOf(prevPosn.getNumber())
                + " to Posn " + posStr + String.valueOf(nextPosn.getNumber()) + s;
    }


    //计算并收取当前玩家所降落的财产的所有者所欠的任何租金
    // 如果当前玩家不是该财产的所有者则要求当前玩家支付租金

    private String calculateRent(){
        assert (isCurrentPosnRentable()) : "This position does not require rent";

        Position posn = currentPlayer.getPosition();
        double rent = 0;

        //有一整条路
        if (posn.getOwner().ownsAllPrptsOnRoad(posn.getRoad())) {
            rent = 0.2 * posn.getPrice();

        } else {
            // 最低租金
            rent += 0.1 * posn.getPrice();
        }
        //如果简单的使用一个乘法计算平衡性会不好
        // 金额的计算方式从随便搜出来的网站上抄的

        // 租金交易
        currentPlayer.setMoney(-rent);
        if (currentPlayer != playerOne)
            playerOne.setMoney(rent);
        else
            playerTwo.setMoney(rent);

        return "\nThis position is owned by " + posn.getOwner().toString()
                + ", therefore " + currentPlayer.toString()
                + " is\ncharged \u00a3" + String.valueOf(rent) + "0 in rent.";
    }



    //如果property可以购买，则执行购买操作。
    //如果property可以改进，则执行改进操作。

    public String interactCurrentPrpt(){
        assert (isCurrentPosnBuyable() || isCurrentPosnImprovable()) :
                "This position is not buyable or improvable.";

        Position currentPosn = currentPlayer.getPosition();

        String s;
        if (currentPosn.getOwner() == null){
            s = currentPosn.buy(currentPlayer);
            setChanged();
            notifyObservers();
            return s;
        }
        s = currentPosn.buy(currentPlayer);
        return s;
    }

    //如果当前玩家需要支付租金，则为 tru
    private boolean isCurrentPosnRentable(){
        return  currentPlayer.getPosition().isProperty()
                && currentPlayer.getPosition().getOwner() != null
                && currentPlayer != currentPlayer.getPosition().getOwner();
    }

    //如果当前玩家可以购买他们的位置上的property，则为 true
    public boolean isCurrentPosnBuyable(){
        return  currentPlayer.getPosition().isProperty()
                && currentPlayer.getPosition().getOwner() == null;
    }

    //如果当前玩家可以扩建，则为 true
    public boolean isCurrentPosnImprovable(){
        return  currentPlayer.getPosition().isProperty()
                && (currentPlayer.ownsAllPrptsOnRoad(currentPlayer.getPosition().getRoad()) );
    }

    //没钱了就输了
    public boolean isGameOver() {
        return (playerOne.getMoney() <= 0 || playerTwo.getMoney() <= 0);
    }

    //p ：地图上的指定位置
    private String playersOnPosnToString(Position p){
        String s = "";
        if (!p.isProperty() && p.getNumber() != 1
                && p.getNumber() != 21)
            s += "\t\t\t";  //三个制表符，用于控制字符串在输出时对齐

        if (playerOne.getPosition().equals(p))
            s += "\t" + playerOne.toString();

        if (playerTwo.getPosition().equals(p))
            s += "\t" + playerTwo.toString();

        return s;
    }


    //overrride toString，将地图的状态转换为字符串用以输出
    //Posn: 位置编号
    //Name: 位置名称
    //Price: 地产价格
    //Owner: 地产所有者
    //Impvmts: 地产的房屋/酒店数量
    //PlayerCounters: 该位置上玩家的数量

    @Override
    public String toString(){
        String s = "Posn\tName\tPrice\tOwner\tImpvmts\tPlayerCounters\n";
        for (Position p: board)
            s += (p.toString() + playersOnPosnToString(p) + "\n");
        return s;
    }
}