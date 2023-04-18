package model;

//Position 类表示棋盘上的位置。
//每个位置都有一个位置编号。如果该位置是一个财产，它也会有一个道路名称和该道路上的编号。
// 它还有一个价格，可以被购买和改善。

public class Position {
    private final int number;
    private boolean property;
    private final char road;
    private final int roadNumber;
    private final double price;
    private Player owner = null;
    private boolean hotel = false;

    public int getNumber() {
        return number;
    }

    public boolean isProperty(){
        return property;
    }

    public char getRoad() {
        return road;
    }

    public double getPrice() {
        return price;
    }

    public Player getOwner() {
        return owner;
    }


    //构造一个带有位置编号、道路名称、道路上的编号和价格的 位置。
    //posnNum ：property的位置
    // roadName ：road名称
    // roadNum ：road中的PropertyNo
    // propertyPrice ：property的价格

    protected Position(int posnNum, char roadName,
                       int roadNum, int propertyPrice){
        number = posnNum;
        road = roadName;
        roadNumber = roadNum;
        price = propertyPrice;
        property = true;
    }


//构造一个带有位置编号的位置
    protected Position(int posnNum) {
        this(posnNum, ' ', 0, 0);//''代表开始没有owner
        property = false;
    }


  //返回字符串
    public String nameToString() {
        switch (number){
            case 1:    //初始位置
                return "GO";
            case 21:  //监狱，走到监狱则会返回初始位置
                return "JAIL";
        }

        return property ?
                String.valueOf(road) + String.valueOf(roadNumber) : "";
    }


    //玩家可以购买property，从银行卡中扣除相应的钱
    //buyer 购买者
    //购买前提：坐标位置是property且为空闲。return 交易的字符串表示形式
    protected String buy(Player buyer){
        assert (property)       : "This is not a property.";
        assert (owner == null)  : "This property already has an owner.";

        buyer.setMoney(-price);
        owner = buyer;
        buyer.addProperty(this);

        return "" + nameToString() + " has been bought by " + buyer.toString()
                + " for \u00a3" + String.valueOf(price) + "0";
    }


//override toString
//用于返回该位置在游戏中的字符串表示。
// 它将该位置的编号、名称、价格、所有者、改善等信息格式化为一个字符串并返回。

// o表示所有者（如果没有则为"[]"）
// s表示位置的编号和名称。如果该位置不是财产，则返回一个空字符串
// 如果该位置是GO或JAIL，则返回一个特殊的格式化字符串。

// 在返回字符串时，如果该位置是property，它将包括其价格、所有者和改善等级的信息。
    @Override
    public String toString(){
        String i = "", o = "";

        if (owner != null)
            o += " " + owner.toString();
        else
            o += " []";

        String s = String.valueOf(number) + ":\t " + nameToString();
        if (property)
            return s + "\t\u00a3" + String.valueOf(price) + "0\t" + o + "\t" + i;
        else if (number == 1 || number == 21)
            return s + "\t\t\t";
        else
            return s;
    }

}
