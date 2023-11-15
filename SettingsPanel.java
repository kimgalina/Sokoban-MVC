import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import java.awt.Image;
import javax.swing.ImageIcon;
import java.awt.Graphics;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JSlider;

public class SettingsPanel extends JPanel {

    private Viewer viewer;
    private Player player;
    private Image backgroundImage;
    private Font font;
    private JLabel nickname;
    private JLabel coins;
    private SettingsController controller;
    private JButton defaultSkinButton;
    private JButton santaSkinButton;
    private JButton premiumSkinButton;

    public SettingsPanel(Viewer viewer, Model model) {
        this.viewer = viewer;
        controller = new SettingsController(this, viewer, model);
        player = model.getPlayer();
        backgroundImage = new ImageIcon("images/settings-background.png").getImage();
        font = viewer.getCustomFont(Font.PLAIN, 24f);
        init();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        nickname.setText("Player: " + player.getNickname());
        coins.setText(": " + player.getTotalCoins());
        g.drawImage(backgroundImage, 0, 0, null);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setEnableSantaSkinButton(boolean enable) {
        santaSkinButton.setEnabled(enable);
    }

    public void setEnableDefaultSkinButton(boolean enable) {
        defaultSkinButton.setEnabled(enable);
    }

    public void setEnablePremiumSkinButton(boolean enable) {
        premiumSkinButton.setEnabled(enable);
    }

    private void init() {
        setLayout(null);

        ImageIcon coinIcon = new ImageIcon("images/coin-image.png");
        JLabel coinImage = createLabelImage(coinIcon, 15, 10, 70, 70);

        Font labelFont = viewer.getCustomFont(Font.PLAIN, 34f);
        coins = createLabel("", 80, 25, 100, 30, labelFont);
        nickname = createLabel("", 170, 25, 200, 30, labelFont);

        JLabel label = createLabel("Settings", 490, 60, 500, 100, viewer.getCustomFont(Font.PLAIN, 72f));
        JLabel skinsLabel = createLabel("Skins:", 80, 130, 100, 30, labelFont);
        showSkinSettings();

        JLabel musicLabel = createLabel("Music:", 80, 450, 100, 30, labelFont);
        showMusicSettings();

        JLabel themeLabel = createLabel("Theme:", 80, 600, 100, 30, labelFont);
        showThemeSettings();

        JButton returnButton = createButton("Back", "Back", 40, 680, true);

        add(coinImage);
        add(nickname);
        add(coins);
        add(label);
        add(skinsLabel);
        add(musicLabel);
        add(themeLabel);
        add(returnButton);
    }

    private void showSkinSettings() {      //TODO: get skins from DB
        ImageIcon defaultIcon = new ImageIcon("images/default-skin.png");
        ImageIcon santaIcon = new ImageIcon("images/santa-skin.png");
        ImageIcon premiumIcon = new ImageIcon("images/premium-skin.png");

        JLabel defaultSkinImage = createLabelImage(defaultIcon, 350, 200, 100, 130);
        JLabel santaSkinImage = createLabelImage(santaIcon, 545, 165, 100, 150);
        JLabel premiumSkinImage = createLabelImage(premiumIcon, 740, 190, 100, 150);

        JLabel defaultSkinPrice = createLabel("Default", 370, 320, 100, 30, font);
        JLabel santaSkinPrice = createLabel("Free", 575, 320, 100, 30, font);
        JLabel premiumSkinPrice = createLabel("15 coins", 755, 320, 110, 30, font);

        defaultSkinButton = createButton("Choose", "Default Skin", 350, 375, false);
        santaSkinButton = createButton("Choose", "Santa Skin", 550, 375, true);
        premiumSkinButton = createButton("Buy", "Premium Skin", 740, 375, true);

        add(defaultSkinImage);
        add(santaSkinImage);
        add(premiumSkinImage);
        add(defaultSkinPrice);
        add(santaSkinPrice);
        add(premiumSkinPrice);
        add(defaultSkinButton);
        add(premiumSkinButton);
        add(santaSkinButton);
    }

    private void showMusicSettings() {
        JRadioButton defaultMusic = createJRadioButton("Default", "Default Music", 420, 460, true);
        JRadioButton christmasMusic = createJRadioButton("Jingle Bells", "Jingle Bells", 620, 460, false);

        ButtonGroup music = new ButtonGroup();
        music.add(defaultMusic);
        music.add(christmasMusic);

        JLabel volumeLabel = createLabel("Volume:", 450, 530, 100, 20, font);

        JSlider volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        volumeSlider.setBounds(540, 520, 200, 50);
        volumeSlider.setOpaque(false);
        volumeSlider.setMajorTickSpacing(10);
        volumeSlider.setMinorTickSpacing(1);
        volumeSlider.setPaintTicks(false);
        volumeSlider.setPaintLabels(false);

        add(defaultMusic);
        add(christmasMusic);
        add(volumeLabel);
        add(volumeSlider);
    }

    private void showThemeSettings() {
        JRadioButton defaultTheme = createJRadioButton("Default", "Default Theme", 420, 600, true);
        JRadioButton brightTheme = createJRadioButton("Bright", "Bright Theme", 620, 600, false);

        ButtonGroup theme = new ButtonGroup();
        theme.add(defaultTheme);
        theme.add(brightTheme);

        add(defaultTheme);
        add(brightTheme);
    }

    private JLabel createLabel(String name, int x, int y, int width, int height, Font font) {
        JLabel label = new JLabel(name);
        label.setBounds(x, y, width, height);
        label.setForeground(Color.WHITE);
        label.setFont(font);
        return label;
    }

    private JLabel createLabelImage(ImageIcon icon, int x, int y, int width, int height) {
        JLabel labelImage = new JLabel(icon);
        labelImage.setBounds(x, y, width, height);
        return labelImage;
    }

    private JButton createButton(String name, String command, int x, int y, boolean isEnabled) {
        JButton button = new JButton(name);
        button.setBounds(x, y, 100, 30);
        button.setFocusable(false);
        button.setFont(viewer.getCustomFont(Font.PLAIN, 20f));
        button.setEnabled(isEnabled);
        button.setActionCommand(command);
        button.addActionListener(controller);
        return button;
    }

    private JRadioButton createJRadioButton(String name, String command, int x, int y, boolean isSelected) {
        JRadioButton radioButton = new JRadioButton(name);
        radioButton.setBounds(x, y, 200, 30);
        radioButton.setSelected(isSelected);
        radioButton.setFont(font);
        radioButton.setForeground(Color.WHITE);
        radioButton.setFocusable(false);
        radioButton.setOpaque(false);
        radioButton.setActionCommand(command);
        radioButton.addActionListener(controller);
        return radioButton;
    }
}