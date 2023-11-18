import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.File;
import java.awt.Component;


public class Viewer {

    private Controller controller;
    private Canvas canvas;
    private CanvasForTwoPlayers myCanvas;
    private CanvasForTwoPlayers enemyCanvas;
    private JSplitPane splitPane;
    private SettingsPanel settings;
    private JFrame frame;
    private CardLayout cardLayout;
    private Model model;
    private EnemyModel enemyModel;

    public Viewer() {
        model = new Model(this);
        enemyModel = new EnemyModel(this);
        controller = new Controller(this, model);
        canvas = new Canvas(model, controller);
        canvas.addKeyListener(controller);

        myCanvas = new CanvasForTwoPlayers(model, controller);
        myCanvas.addKeyListener(controller);

        enemyCanvas = new CanvasForTwoPlayers(enemyModel, null);
        LevelChooser levelChooser = new LevelChooser(this, model);
        settings = new SettingsPanel(this, model);
        MenuPanel menu = new MenuPanel(this, model, enemyModel);

        cardLayout = new CardLayout();

        // for two players
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, myCanvas, enemyCanvas);
        splitPane.setDividerLocation(0.5);
        splitPane.setResizeWeight(0.5);


        frame = new JFrame("Sokoban");
        frame.setSize(1200, 800);
        frame.setLocation(200, 15);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(cardLayout);

        frame.add(menu, "menu");
        frame.add(levelChooser, "levelChooser");
        frame.add(settings, "settings");
        frame.add(canvas, "canvas");
        frame.add(splitPane, "splitPane");

        frame.setResizable(false);
        frame.setVisible(true);
    }

    public Viewer getViewer() {
        return this;
    }

    public CanvasForTwoPlayers getEnemyCanvas() {
        return enemyCanvas;
    }

    public void update() {
        canvas.repaint();
    }

    public void updateSkin() {
        canvas.setSkin();
        update();
    }

    public void updateEnemyCanvas() {
        enemyCanvas.repaint();
    }

    public void updateMyCanvas() {
        myCanvas.repaint();
    }


    public void updateSettings(Player player) {
        settings.setPlayer(player);
        settings.repaint();
    }

    public void showMenu() {
        cardLayout.show(frame.getContentPane(), "menu");
    }

    public void showCanvas() {
        update();
        cardLayout.show(frame.getContentPane(), "canvas");
        canvas.requestFocusInWindow();
    }

    public void showCanvas(String gameType) {
        System.out.println("in show canvas gameType" + gameType);
        if(gameType.equals("alone")) {
            showCanvas();
            return;
        }
        showTwoCanvas();

    }

    private boolean hasFrameCanvas() {
        Component[] components = frame.getContentPane().getComponents();
        for (Component component : components) {
            if (component == canvas) {
                return true;
            }
        }
        return false;
    }

    private void showTwoCanvas() {
        updateMyCanvas();
        updateEnemyCanvas();
        cardLayout.show(frame.getContentPane(), "splitPane");
        myCanvas.requestFocusInWindow();

    }
    public void showLevelChooser() {
        cardLayout.show(frame.getContentPane(), "levelChooser");
    }

    public void showSettings() {
        cardLayout.show(frame.getContentPane(), "settings");
    }

    public Font getCustomFont(int style, float size) {
        Font customFont = null;
        File file = new File("fonts/PixelFont.otf");
        try {
            customFont = Font.createFont(Font.TRUETYPE_FONT, file).deriveFont(style, size);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);
        } catch (IOException | FontFormatException e) {
            System.out.println(e);
        }
        return customFont;
    }
}
