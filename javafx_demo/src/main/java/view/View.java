package view;

//使用的一些api的方法和作用
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import javafx.application.Application;
//------------------------------------------------------------------------------------------------------------------------------------
//单击、双击、鼠标移动，在JavaFX应用程序中使用动作事件，实现用户界面上各种组件的响应和交互
import javafx.event.ActionEvent;
//------------------------------------------------------------------------------------------------------------------------------------
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
//------------------------------------------------------------------------------------------------------------------------------------
//使用setOnMouseClicked()方法、setOnMousePressed()方法、setOnMouseReleased()方法等方法，
// 当用户进行相应的鼠标操作时，JavaFX会生成相应的鼠标事件，并将事件传递给注册的事件处理器进行处理
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
//------------------------------------------------------------------------------------------------------------------------------------
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
//------------------------------------------------------------------------------------------------------------------------------------
//绘制各种多边形
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
//------------------------------------------------------------------------------------------------------------------------------------
//通常用于表示一些有限的、固定的取值范围，例如星期几、颜色、状态等
import controller.Controller;
import controller.Controller.posnState;
import model.*;

//

//继承obsever是为了需要后面override方法：update(Observable o, Object arg)
public class View extends Application implements Observer {

    private Model model;   //model
    private Controller controller;  //controller

    //定义了一个Label控件messageLabel，并初始化为空白字符串
    private final Label messageLabel = new Label();
    //定义了一个Label控件posnInspectLabel，并初始化为空白字符串
    private final Label posnInspectLabel = new Label();
    //定义了两个Label控件，分别用于显示两个玩家的金钱数目
    private final Label playerOneMoneyLabel = new Label();
    private final Label playerTwoMoneyLabel = new Label();
    //定义了一个Label控件diceRollLabel，用于显示骰子的点数
    private final Label diceRollLabel = new Label();
    //定义了一个Label控件winnerLabel，并调用了makeInfoLabel()方法进行初始化
    // makeInfoLabel()：创建一个带有背景色和文本样式的Label控件
    private final Label winnerLabel = makeInfoLabel("    ", null, 1.6);

    private final Pane currentPlayerPane = new Pane();

    private final Button nextTurnBtn = new Button();
    private final Button interactBtn = new Button();

    private final ArrayList<Pane> propertiesPanes = new ArrayList<>();
    private final ArrayList<HBox> houseIcons = new ArrayList<>();
    private final ArrayList<HBox> playerCounterPanes = new ArrayList<>();
    //窗口大小
    private final double height = 700, width = height * 1.35;


    @Override
    public void start(Stage stage) throws Exception {
        model = new Model();
        controller = new Controller(model);
        controller.setView(this);

        HBox root = new HBox();
        root.setPadding(new Insets(10, 10, 10, 10));
        root.getChildren().addAll(
                makeGameBoard(),
                makeControlPanel()
        );


        stage.setResizable(false);
        stage.setTitle("Monopoly");
        stage.setScene(new Scene(root, width, height + 7));
        stage.show();

        model.addObserver(this);
        update(null, null);
    }

    //游戏中的每个位置显示正方形，沿着地图的边缘和角落排列
    private GridPane makeGameBoard() {
        GridPane boardGrid = new GridPane();
        int num = 1;

        // 起始位置（右下）
        boardGrid.add(makeBoardPosn(2, 2, num, false), 11, 11, 2, 2);

        // 下
        for (int i = 10; i > 1; i--) {
            num++;
            boardGrid.add(makeBoardPosn(1, 2, num, false), i, 11, 1, 2);
        }

        // 左下
        num++;
        boardGrid.add(makeBoardPosn(2, 2, num, false), 0, 11, 2, 2);

        // 右
        for (int i = 10; i > 1; i--) {
            num++;
            boardGrid.add(makeBoardPosn(2, 1, num, true), 0, i, 2, 1);
        }

        //左上
        num++;
        boardGrid.add(makeBoardPosn(2, 2, num, false), 0, 0, 2, 2);

        // 上
        for (int i = 2; i < 11; i++) {
            num++;
            boardGrid.add(makeBoardPosn(1, 2, num, false), i, 0, 1, 2);
        }

        // 右上
        num++;
        boardGrid.add(makeBoardPosn(2, 2, num, false), 11, 0, 2, 2);

        // 左
        for (int i = 2; i < 11; i++) {
            num++;
            boardGrid.add(makeBoardPosn(2, 1, num, true), 11, i, 2, 1);
        }

        // 中心
        Pane center = makeBoardPosn(9, 9, 0, true);
        double boxsize = center.getMinWidth();
        // +1防锯齿
        center.setMinSize(boxsize + 1, boxsize + 1);
        center.getChildren().clear();
        center.setOnMouseClicked(null);
        boardGrid.add(center, 2, 2, 9, 9);

        boardGrid.setBorder(makeBorder());
        return boardGrid;
    }

    //位置的形状基于其在地图上的位置，可以水平或垂直方向。
    //w 宽度倍增器
    //h 高度倍增器
    //i 该位置在游戏版上的索引
    //isHBox 如果游戏版位置在游戏版的侧面，则为 true

    private Pane makeBoardPosn(int w, int h, int i, boolean isHBox) {
        //true：HBox      false：VBox
        Pane posn = isHBox ? new HBox() : new VBox();

        // 大小
        double boxsize = ((double) height - 5.0) / 13.0;
        posn.setMinSize(w * boxsize, h * boxsize);
        posn.setMaxSize(w * boxsize, h * boxsize);
        posn.setBorder(makeBorder());
        posn.setPadding(new Insets(5, 6, 5, 6));

        // 填充内容
        VBox nameBox = makePosnNameBox(w, i, boxsize, isHBox);
        VBox infoBox = makePosnInfoBox(i, boxsize, isHBox, (w == 2 && h == 2));
        posn.getChildren().addAll(nameBox, infoBox);

        // make each property position inspectable
        if (i != 0 && controller.getPosition(i).isProperty()) {
            posn.setOnMouseClicked((MouseEvent e) -> {
                String[] s = controller.getPosition(i).toString().split("\t");
                posnInspectLabel.setText("\t  Inspect A Property\n"
                        + "Position Number:\t" + s[0].replace(":", "")
                        + "\nProperty Name:\t" + s[1].replace(" ", "")
                        + "\nProperty Price:\t\t" + s[2]
                        + "\nProperty Owner:\t" + s[3]
                        + "\nImprovements:\t" + s[4].replace(".0", "")
                );
            });

            //设置背景颜色
            posn.setId(String.valueOf(i));
            propertiesPanes.add(posn);
        }

        return posn;
    }

    // 创建每个位置的名称框，并在该框中显示该位置的名称
    // w 宽度倍增器
    //i 该位置在地图上的索引
    //boxsize 框的默认大小
    //isHBox 如果位置在地图边上，则为 true
    private VBox makePosnNameBox(int w, int i, double boxsize, boolean isHBox) {
        //创建过程：
        //创建一个VBox实例
        //设置框的最小尺寸为半个框的大小（boxsize*0.5）
        // 这个大小将被用来限制框的最小尺寸，以确保框不会太小而无法显示位置的名称。
        //根据isHBox参数设置名称框的填充。如果位置框是HBox（即在地图边）
        // 则设置框的填充为new Insets(17, 10, 10, 17)
        // 否则，设置填充为new Insets(10, 10, 10, 10)

        VBox posnNameBox = new VBox();
        posnNameBox.setMinSize(boxsize * 0.5, boxsize * 0.5);
        posnNameBox.setPadding(isHBox ? new Insets(17, 10, 10, 17)
                : new Insets(10, 10, 10, 10));

        // 定位
        for (Position p : model.getBoard())
            if (i == p.getNumber()) {
                //显示property、Go 和Jail
                Label roadLabel = new Label(
                        (p.isProperty() || p.getNumber() == 1
                                || p.getNumber() == 21) ?
                                p.nameToString() : ""
                );

                // format box
                roadLabel.setMaxWidth(w * boxsize - 14);
                roadLabel.setAlignment(Pos.BOTTOM_CENTER);
                roadLabel.setScaleX(2);
                roadLabel.setScaleY(2);
                posnNameBox.getChildren().add(roadLabel);
            }

        return posnNameBox;
    }


    //创建一个包含可显示给用户的任何信息 Pane，其中一些信息将经常更新
    //i ：该位置在游戏版上的索引
    //boxsize ：框的默认大小
    //isHBox ：如果位置为地图边，则为 true
    //isCorner ：如果位置为地图角，则为 true

    private VBox makePosnInfoBox(int i, double boxsize,
                                 boolean isHBox, boolean isCorner) {
        VBox posnInfoBox = new VBox();
        posnInfoBox.setMinSize(boxsize * 0.5, boxsize * 0.5);
        posnInfoBox.setPadding(isHBox ? new Insets(0, 0, 0, 15)
                : new Insets(5, 0, 5, 0));

        //在model中加载
        for (Position p : model.getBoard())
            if (i == p.getNumber()) {

                // property信息
                if (p.isProperty()) {
                    posnInfoBox.getChildren().add(new Label(
                            "£" + String.valueOf(p.getPrice()) + "0"
                    ));

                    HBox housesPane = new HBox(1);
                    housesPane.setPadding(isHBox ? new Insets(2, 0, 2, 0)
                            : new Insets(6, 0, 6, 0));
                    housesPane.setId(String.valueOf(i));
                    houseIcons.add(housesPane);
                    posnInfoBox.getChildren().add(housesPane);
                } else {
                    posnInfoBox.getChildren().add(new Label("  "));
                }

                // 显示玩家位置的pane
                HBox playersPane = new HBox();
                playersPane.setId(String.valueOf(i));
                if (isCorner)
                    playersPane.setTranslateX(30);
                playerCounterPanes.add(playersPane);
                posnInfoBox.getChildren().add(playersPane);
            }

        return posnInfoBox;
    }

    //显示给玩家的按钮
    private VBox makeControlPanel() {
        VBox controlPanel = new VBox(13);
        controlPanel.setPadding(new Insets(10, 0, 0, 8));
        double panelWidth = (width - 80) * 0.2;
        Border panelBorder = new Border(new BorderStroke(
                Color.BLACK, BorderStrokeStyle.SOLID,
                new CornerRadii(5), BorderWidths.DEFAULT
        ));
        //font
        Font font = new Font("Comic Sans MS", 12);
        Font font1 = new Font("Trebuchet MS", 11);
        Font font2 = new Font("Impact", 10);

        // label
        Label title = new Label();

        title.setFont(font);
        title.setTextFill(Color.RED);
        title.setText("MINIPOLY");
        title.setScaleX(3);
        title.setScaleY(3);
        title.setTranslateX(70);

        // message pane
        messageLabel.setMinHeight(height * 0.2);
        messageLabel.setScaleX(1.1);
        messageLabel.setScaleY(1.1);
        messageLabel.setFont(font1);
        messageLabel.setTextFill(Color.DARKBLUE);
        TextFlow messagePanel = makeTextPanel(messageLabel, panelBorder, "☆⌒(*＾-゜)v THX!!\n"+"\nWelcome"
                + " to Minipoly! \n"+"\nClick a position to "
                + "inspect it.", new Insets(14, 17, 14, 17), panelWidth);

        posnInspectLabel.setFont(font);
        posnInspectLabel.setTextFill(Color.DARKRED);
        // inspector pane
        Pane inspectorPanel = makeTextPanel(posnInspectLabel, panelBorder,
                "\t  Inspect A Property\nPosition Number:\nProperty Name:\n"
                        + "Property Price:\nProperty Owner:\nImprovements:",
                new Insets(7, 7, 7, 7), panelWidth);

        // 显示钱
        GridPane moneyPane = makeInfoPane(panelWidth);
        playerOneMoneyLabel.setFont(font2);
        playerOneMoneyLabel.setTextFill(Color.DARKGRAY);
        playerTwoMoneyLabel.setFont(font2);
        playerTwoMoneyLabel.setTextFill(Color.DARKGRAY);
        moneyPane.add(makeInfoLabel("Player 1: ", playerOneMoneyLabel, 1.3), 0, 0);
        moneyPane.add(playerOneMoneyLabel, 1, 0);
        moneyPane.add(makeInfoLabel("Player 2: ", playerTwoMoneyLabel, 1.3), 0, 1);
        moneyPane.add(playerTwoMoneyLabel, 1, 1);

        // 显示玩家和骰子

        GridPane gameInfoPane = makeInfoPane(panelWidth);
        gameInfoPane.add(makeInfoLabel(" Current Player:", null, 1.3), 0, 0);
        gameInfoPane.add(currentPlayerPane, 1, 0);
        gameInfoPane.add(makeInfoLabel("Dice Roll: ", diceRollLabel, 1.3), 0, 1);
        gameInfoPane.add(diceRollLabel, 1, 1);


        // 创建 Label 控件
        Label diceRollLabel = new Label("4"); // 假设骰子点数为 4

// 创建 ImageView 控件数组
        Image[] diceImages = new Image[12];
        for (int i = 0; i < 12; i++) {
            diceImages[i] = new Image("file:../resource/" + (i + 1) + ".png");
        }

// 根据骰子点数选择对应的图片
        int diceRoll = Integer.parseInt(diceRollLabel.getText());
        ImageView diceImageView = new ImageView(diceImages[diceRoll - 1]);
        diceImageView.setFitWidth(30);
        diceImageView.setFitHeight(30);

// 创建一个包含数字和图片的 HBox
        HBox diceBox = new HBox();
        diceBox.getChildren().addAll(diceRollLabel, diceImageView);

// 将包含数字和图片的 HBox 添加到 GridPane 中
        gameInfoPane.add(makeInfoLabel("Dice Roll: ", null, 1.3), 0, 1);
        gameInfoPane.add(diceBox, 1, 1);

        // 显示赢家
        GridPane winnerPane = makeInfoPane(panelWidth);
        winnerPane.add(new Label(" "), 0, 1, 3, 1);
        winnerPane.add(winnerLabel, 1, 0);

        nextTurnBtn.setText("Next Turn");
        nextTurnBtn.setMinSize(panelWidth, 40);
        nextTurnBtn.setOnAction((ActionEvent e) -> {
            messageLabel.setText(controller.nextTurn());
            update(null, null);
        });
        nextTurnBtn.defaultButtonProperty().bind(nextTurnBtn.focusedProperty());

        interactBtn.setMinSize(panelWidth, 40);
        interactBtn.setOnAction((ActionEvent e) -> {
            messageLabel.setText(controller.interact());
            update(null, null);
        });
        interactBtn.defaultButtonProperty().bind(interactBtn.focusedProperty());

        // panel
        controlPanel.getChildren().addAll(
                title, messagePanel, inspectorPanel, moneyPane,
                makeSpace(panelWidth), gameInfoPane, makeSpace(panelWidth),
                winnerPane, makeSpace(panelWidth), nextTurnBtn, interactBtn
        );
        return controlPanel;
    }


    //contents 将包装的 Label
    //border 边框
    //text contents Label 的默认文本
    //padding 用于将此节点与其他节点分隔开的填充
    //textWidth 可以显示文本的最大宽度
    //return 用于显示游戏中执行的操作的文本结果
    private TextFlow makeTextPanel(Label contents, Border border, String text,
                                   Insets padding, double textWidth) {



        TextFlow panel = new TextFlow();
        panel.setBorder(border);
        contents.setText(text);
        contents.setPadding(padding);
        contents.setAlignment(Pos.TOP_LEFT);
        contents.setMaxWidth(textWidth);
        contents.setWrapText(true);
        panel.getChildren().add(contents);
        return panel;
    }

    //return 用于显示游戏信息的 GridPane
    private GridPane makeInfoPane(double width) {
        GridPane infoPane = new GridPane();
        infoPane.setPadding(new Insets(7, 0, 0, 17));
        infoPane.setMinWidth(width);
        infoPane.setHgap(40);
        infoPane.setVgap(10);
        return infoPane;
    }

    //text Label 的文本
    //resultLabel 可以与之关联的单独 Label
    //scale 缩放 Label 的大小
    //return 描述游戏信息的 Label

    private Label makeInfoLabel(String text, Label resultLabel, double scale) {

        Label infoLabel = new Label(text);
        infoLabel.setScaleX(scale);
        infoLabel.setScaleY(scale);
        if (resultLabel != null) {
            resultLabel.setScaleX(scale);
            resultLabel.setScaleY(scale);
        }
        return infoLabel;
    }

    //width pane宽度
    //return 填充控制面板中空白空间的 Pane

    private Pane makeSpace(double width) {
        Pane space = new Pane();
        space.setMaxWidth(width);
        return space;
    }


    @Override
    public void update(Observable o, Object arg) {
        // 更新pane
        playerOneMoneyLabel.setText("£" + String.valueOf(
                model.getPlayerOne().getMoney()
        ) + "0");
        playerTwoMoneyLabel.setText("£" + String.valueOf(
                model.getPlayerTwo().getMoney()
        ) + "0");
        diceRollLabel.setText(String.valueOf(model.getDiceRoll()));
        currentPlayerPane.getChildren().clear();
        currentPlayerPane.getChildren().add(
                makePlayerCounter(model.getCurrentPlayer().isPlayerOne())
        );

        for (HBox p : playerCounterPanes)
            updatePlayers(p);

        updateButton(controller.getCurrentPosnState());

        //地图上的位置
        for (Position p : model.getBoard()) {

            // 显示扩建数
            for (HBox b : houseIcons)
                if (controller.isPosn(b.getId(), p.getNumber()))
                    updateHouses(p, b);

            // 根据owner对位置进行颜色编码
            for (Pane b : propertiesPanes)
                if (controller.isPosn(b.getId(), p.getNumber()))
                    updatePosnBackgrounds(p, b);
        }

        // 结束游戏
        if (model.isGameOver()) {
            nextTurnBtn.setDisable(true);
            interactBtn.setDisable(true);

            messageLabel.setText(messageLabel.getText() + "\n\n\t   GAME OVER");
            String w = (model.getPlayerOne().getMoney() <= 0) ? "2" : "1";
            winnerLabel.setText("Player " + w + " Wins!");
        }

    }

    //更新玩家所在的地图位置。
    private void updatePlayers(HBox playersPane) {
        playersPane.getChildren().clear();
        String id = playersPane.getId();

        if (controller.isPlayerOnPosn(id, true))
            playersPane.getChildren().addAll(
                    makePlayerCounter(true),
                    new Label("  ")
            );

        if (controller.isPlayerOnPosn(id, false))
            playersPane.getChildren().add(
                    makePlayerCounter(false)
            );
    }

    //根据当前玩家位置的状态启用/禁用互动按钮。
    //（意思是如果玩家没有购买房产的资格则按钮显示灰色，无法点击）
    private void updateButton(posnState state) {
        assert (state != null) : "The state of the position"
                + " must be initialised.";
        interactBtn.setDisable(false);
        switch (state) {
            case BUYABLE:
                interactBtn.setText("Buy");
                break;

            case IMPROVABLE:
                interactBtn.setText("Improve");
                break;

            case NONE:
                interactBtn.setDisable(true);
                interactBtn.setText("Buy or Improve");
                break;
        }
    }

    //在地图上显示对房产的扩建
    // 对于每个扩建，都会显示一个房屋图标，如果在财产上建造了旅馆，则会显示单词“旅馆”。
    //p 可能已进行改进的游戏版上的位置
    //b 如果该财产上有改进，则创建房屋图标的 Pane
    private void updateHouses(Position p, HBox b) {
        assert (p.isProperty()) : "The position is not a property and therefore"
                + " cannot have improvements.";
        if (!p.isHotel()) {
            b.getChildren().clear();
            for (int i = 0; i < p.getImprovements(); i++)
                b.getChildren().add(makeHouseIcon());
        } else {
            b.getChildren().clear();
            b.getChildren().add(new Label("Hotel"));
            b.setPadding(new Insets(-2, 0, -1, 0));
        }
    }

    //根据所有者对property位置进行颜色编码
    // 如果该位置有所有者，则在该位置的背景中显示该玩家的颜色
    // 如果没有所有者，则背景保持透明
    //玩家点击位置时颜色稍微变化用以反馈
    private void updatePosnBackgrounds(Position p, Pane b) {
        Color bgColour, bgColourHover, bgColourClick;

        if (p.getOwner() != null) {
            if (!p.getOwner().isPlayerOne()) {
                bgColour = Color.LIGHTBLUE;
                bgColourHover = Color.SKYBLUE;
                bgColourClick = Color.DEEPSKYBLUE;
            } else {
                bgColour = Color.PINK;
                bgColourHover = Color.LIGHTPINK;
                bgColourClick = Color.TOMATO;
            }
        } else {
            bgColour = Color.TRANSPARENT;
            bgColourHover = Color.LIGHTGREY;
            bgColourClick = Color.DARKGREY;
        }

        Background bg = makePosnBackground(bgColour);
        b.setBackground(bg);
        b.setOnMouseEntered((MouseEvent e) -> {
            b.setBackground(makePosnBackground(bgColourHover));
        });
        b.setOnMousePressed((MouseEvent e) -> {
            b.setBackground(makePosnBackground(bgColourClick));
        });
        b.setOnMouseReleased((MouseEvent e) -> {
            b.setBackground(makePosnBackground(bgColourHover));
        });
        b.setOnMouseExited((MouseEvent e) -> {
            b.setBackground(bg);
        });
    }

    //c 背景颜色
    private Background makePosnBackground(Color c) {
        return new Background(new BackgroundFill(
                c, CornerRadii.EMPTY, Insets.EMPTY
        ));
    }

    //地图的边框为灰色
    private Border makeBorder() {
        return new Border(new BorderStroke(
                Color.GREY, BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY, BorderWidths.DEFAULT
        ));
    }

    //创建一个小图标，代表地图上的扩建。
    //返回 一个房屋形状的多边形
    private Polygon makeHouseIcon() {
        Polygon p = new Polygon();
        p.getPoints().addAll(new Double[]{
                1.0, 8.0, 1.0, 4.0,
                0.0, 4.0, 4.5, 0.0,
                9.0, 4.0, 8.0, 4.0,
                8.0, 8.0
        });

        p.setScaleX(1.1);
        p.setScaleY(1.3);
        p.setFill(Color.TRANSPARENT);
        p.setStroke(Color.BLACK);
        return p;
    }

    //创建一个小图标，代表玩家
    private StackPane makePlayerCounter(boolean isPlayerOne) {
        Circle circle = new Circle();
        circle.setRadius(12);
        //circle.setStroke(Color.WHITE);
        circle.setFill(isPlayerOne ? Color.CRIMSON : Color.CORNFLOWERBLUE);

        Label playerName = new Label();
        playerName.setText(isPlayerOne ? "P1" : "P2");
        playerName.setTextFill(Color.WHITE);

        StackPane playerCounter = new StackPane();
        playerCounter.getChildren().addAll(circle, playerName);
        return playerCounter;
    }

    public static void main(String[] args) {
        launch(args);
    }
}



