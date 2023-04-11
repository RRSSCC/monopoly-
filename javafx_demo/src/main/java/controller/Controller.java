package controller;


import model.Model;
import model.Player;
import model.Position;
import view.View;

//Controller
//该类允许在view和model之间的方法。
// 当view需要更改model时，来自view的任何输入都会通过此类进行验证，然后传递给model。
// 该类也用于向view更新model状态

public class Controller {
    private Model model;
    private View view;

//描述位置的状态
    public enum posnState{
        BUYABLE,
        IMPROVABLE,
        NONE
    }

    public void setView(View view){
        this.view = view;
    }


    //构造controller
    //model为数据结构的模型
    public Controller(Model model){
        this.model = model;
    }


    //随机摇骰子启动下一个回合。
    // 生成一个随机的骰子点数，并将当前玩家移动到游戏版上的下一个位置。
    // return 移动位置和租金的字符串
    public String nextTurn(){
        return model.nextTurn();
    }


    //当前玩家将在一个位置上执行一个操作。
    // 可能为购买操作或改进操作，取决于model。

    public String interact(){
        return model.interactCurrentPrpt();
    }


    // id view中位置的 ID
    // num model中位置的编号
    //return 如果 ID 号与位置编号相同，则返回 true
    //用于确认
    public boolean isPosn(String id, int num){
        return id.equals(String.valueOf(num));
    }

    //isPlayerOne 用于判断玩家编号，如果玩家是 Player One，则为 true，否则为 false
    //如果 ID 号与位置编号相同，则返回 true
    //用于确认

    public boolean isPlayerOnPosn(String id, boolean isPlayerOne){
        Player p = isPlayerOne ? model.getPlayerOne()
                : model.getPlayerTwo();

        return isPosn(id, p.getPosition().getNumber());
    }


    //重新获得位置状态
    public posnState getCurrentPosnState(){
        if (model.isCurrentPosnBuyable())
            return posnState.BUYABLE;

        else if (model.isCurrentPosnImprovable())
            return posnState.IMPROVABLE;

        else
            return posnState.NONE;
    }


   // i 位置编号
   // 具有指定编号的位置
    public Position getPosition(int i){
        for (Position p : model.getBoard())
            if (p.getNumber() == i)
                return p;

        return null;
    }

}
