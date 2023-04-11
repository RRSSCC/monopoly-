package model;

import java.util.ArrayList;
import java.util.Iterator;


//Player 类表示 玩家
// 游戏只有两个玩家
// 玩家可以购买财产，如果他们拥有一整套财产，他们可以扩建（改善）。
public class Player {
    private final boolean playerOne;
    private final ArrayList<Position> properties = new ArrayList<>();
    private Position position;
    private Iterator<Position> positionIter;   //表示该玩家在游戏中前进时所使用的迭代器，它用于遍历所有的位置。
    private double money;

    public boolean isPlayerOne() {
        return playerOne;
    }

    public Position getPosition() {
        return position;
    }

    public double getMoney() {
        return money;
    }

    //每个玩家银行账户中有 2000欧。
    // Player 对象由model构造。
    //isPlayerOne：判断玩家， 如果这个玩家是玩家 1，则为 true；否则，这是玩家 2。

    protected Player(boolean isPlayerOne, Iterator<Position> posIter){
        playerOne = isPlayerOne;
        money = 2000.00;
        positionIter = posIter;        // posIter ：一个generator，用于监视玩家的位置
        position = positionIter.next();
    }

    protected Player( boolean isPlayerOne,
                      Iterator<Position> posIter,
                      int startingMoney ){
        this(isPlayerOne, posIter);
        money = startingMoney;
    }


  //移动方法
  // 此方法使用generator访问地图的 Position ，并沿着它们迭代 num 次。
  // 如果玩家到达游戏版的末尾，他们将使用新的迭代器循环回游戏版的开头。
  //如果玩家到达游戏版的末尾，将使用新的迭代器重置玩家的迭代器
  //结束时return 玩家移动后到达的位置

    protected Position move(int num, Iterator<Position> boardIter){
        for (int i=0; i<num; i++) {
            if (!positionIter.hasNext())
                positionIter = boardIter;

            position = positionIter.next();
        }
        return position;
    }


//如果玩家拥有某个集/路的所有 3 个财产，则返回 true。

    public boolean ownsAllPrptsOnRoad(char road){
        int count = 0;

        if (!properties.isEmpty())
            for(Position p: properties)
                if (p.getRoad() == road)
                    count ++;

        return (count == 3);
    }


//给玩家指定金额的钱
    protected void setMoney(double money) {
        this.money += money;
    }


//给玩家指定的财产
    protected void addProperty(Position p){
        assert(p.isProperty()) : "Not a property";
        properties.add(p);
    }


//返回玩家的字符串（P1，P2）
    @Override
    public String toString(){
        return playerOne ? "[P1]" : "[P2]";
    }
}
