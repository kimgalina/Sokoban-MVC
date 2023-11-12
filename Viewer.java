import javax.swing.JFrame;
import java.awt.Image;
import java.awt.CardLayout;
import javax.swing.ImageIcon;

public class Viewer {

    private Controller controller;
    private Canvas canvas;
    private LevelChooser levelChooser;
    private MenuPanel menu;
    private JFrame frame;
    private Image backgroundImage;
    private CardLayout cardLayout;

    public Viewer() {
        controller = new Controller(this);
        Model model = controller.getModel();
        canvas = new Canvas(model);
        canvas.addKeyListener(controller);
        levelChooser = new LevelChooser(model);

        backgroundImage = new ImageIcon("images/background.jpg").getImage();
        menu = new MenuPanel(backgroundImage);

        cardLayout = new CardLayout();

        frame = new JFrame("Sokoban");
        frame.setSize(1200, 800);
        frame.setLocation(200, 15);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(cardLayout);

        frame.add(menu, "menu");
        frame.add(levelChooser, "levelChooser");
        frame.add(canvas, "canvas");

        frame.setVisible(true);
    }

    public void update() {
        canvas.repaint();
    }

    public void showCanvas() {
        update();
        cardLayout.show(frame.getContentPane(), "canvas");
        canvas.requestFocusInWindow();
    }
}
