import javax.swing.*; // Swing-Paket für die GUI-Komponenten
import java.awt.*; // AWT-Paket für grafische Funktionen
import java.awt.event.*; // AWT-Event-Paket für Ereignisbehandlung
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*; // Sound-Paket für Audiofunktionen
import java.io.IOException;
import java.net.URL;

public class DodgerGame extends JPanel implements ActionListener, KeyListener
{
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;
    private static final int PLAYER_SIZE = 50;
    private static final int BADDIEMINSIZE = 10;
    private static final int BADDIEMAXSIZE = 40;
    private static final int BADDIEMINSPEED = 1;
    private static final int BADDIEMAXSPEED = 8;
    private static final int ADDNEWBADDIERATE = 6;
    private static final int PLAYERMOVERATE = 5;

    private Timer timer;
    private Rectangle player;
    private ArrayList<Rectangle> baddies;
    private ArrayList<Integer> baddieSpeeds;
    private int score;
    private int topScore;
    private int baddieAddCounter;
    private boolean moveLeft, moveRight, moveUp, moveDown;
    private boolean reverseCheat, slowCheat;
    private Image playerImage;
    private Image baddieImage;
    private Clip gameOverClip;
    private Clip backgroundMusicClip;

    public DodgerGame()
    {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)); // Setzt die bevorzugte Größe des Panels
        setBackground(Color.WHITE); // Setzt den Hintergrund auf weiß
        setFocusable(true); // Macht das Panel fokussierbar
        addKeyListener(this); // Fügt einen KeyListener hinzu
        timer = new Timer(1000 / 60, this); // Initialisiert den Timer für 60 FPS

        // Lade die Bilder für den Spieler und die Baddies
        playerImage = loadImage("player.png");
        baddieImage = loadImage("baddie.png");

        // Lade die Soundclips
        gameOverClip = loadSound("gameover.wav");
        backgroundMusicClip = loadSound("background.mid");
        if (backgroundMusicClip != null)
        {
            backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Hintergrundmusik in Schleife abspielen
        }

        // Initialisiere den Spieler und die Baddies
        player = new Rectangle(WINDOW_WIDTH / 2, WINDOW_HEIGHT - 50, PLAYER_SIZE, PLAYER_SIZE);
        baddies = new ArrayList<>();
        baddieSpeeds = new ArrayList<>();
        score = 0;
        topScore = 0;
        baddieAddCounter = 0;
        moveLeft = moveRight = moveUp = moveDown = false;
        reverseCheat = slowCheat = false;
    }

    public void startGame()
    {
        timer.start(); // Startet den Timer, um das Spiel zu beginnen
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g); // Ruft die Methode der Superklasse auf, um das Panel zu löschen

        // Zeichne den Spieler
        g.drawImage(playerImage, player.x, player.y, player.width, player.height, this);

        // Zeichne die Baddies
        for (Rectangle baddie : baddies)
        {
            g.drawImage(baddieImage, baddie.x, baddie.y, baddie.width, baddie.height, this);
        }

        // Punktestand und Highscore anzeigen
        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Top Score: " + topScore, 10, 40);

        // Game Over Bildschirm anzeigen
        if (!timer.isRunning())
        {
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("GAME OVER", WINDOW_WIDTH / 3, WINDOW_HEIGHT / 3);
            g.drawString("Press any key to play again", WINDOW_WIDTH / 3 - 100, WINDOW_HEIGHT / 3 + 50);
        }

        // Gewinnnachricht anzeigen, wenn der Spieler gewonnen hat
        if (player.y <= 0)
        {
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("YOU WIN!", WINDOW_WIDTH / 3, WINDOW_HEIGHT / 3);
            g.drawString("Press any key to play again", WINDOW_WIDTH / 3 - 100, WINDOW_HEIGHT / 3 + 50);
            timer.stop(); // Timer stoppen, um das Spiel anzuhalten
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        // Spielerbewegung verarbeiten
        if (moveLeft && player.x > 0) player.x -= PLAYERMOVERATE;
        if (moveRight && player.x + PLAYER_SIZE < WINDOW_WIDTH) player.x += PLAYERMOVERATE;
        if (moveUp && player.y > 0) player.y -= PLAYERMOVERATE;
        if (moveDown && player.y + PLAYER_SIZE < WINDOW_HEIGHT) player.y += PLAYERMOVERATE;

        // Neue Baddies hinzufügen, wenn keine Cheats aktiv sind
        if (!reverseCheat && !slowCheat)
        {
            baddieAddCounter++;
            if (baddieAddCounter == ADDNEWBADDIERATE)
            {
                baddieAddCounter = 0;
                addBaddie();
            }
        }

        // Baddies bewegen und auf Kollision prüfen
        moveBaddies();
        checkCollisions();
        score++; // Erhöht den Punktestand
        repaint(); // Zeichnet das Panel neu
    }

    private void addBaddie()
    {
        Random rand = new Random(); // Zufallszahlengenerator-Objekt
        int size = rand.nextInt(BADDIEMAXSIZE - BADDIEMINSIZE + 1) + BADDIEMINSIZE; // Zufällige Größe für den Baddie
        int speed = rand.nextInt(BADDIEMAXSPEED - BADDIEMINSPEED + 1) + BADDIEMINSPEED; // Zufällige Geschwindigkeit für den Baddie
        int x = rand.nextInt(WINDOW_WIDTH - size); // Zufällige x-Position für den Baddie
        baddies.add(new Rectangle(x, -size, size, size)); // Fügt den neuen Baddie zur Liste hinzu
        baddieSpeeds.add(speed); // Fügt die Geschwindigkeit des neuen Baddie zur Liste hinzu
    }

    private void moveBaddies()
    {
        for (int i = 0; i < baddies.size(); i++)
        {
            Rectangle baddie = baddies.get(i); // Holt Baddie an der aktuellen Position
            if (reverseCheat) baddie.y -= 5; // Bewegt Baddie nach oben, wenn der Reverse-Cheat aktiviert ist
            else if (slowCheat) baddie.y += 1; // Bewegt Baddie langsamer, wenn der Slow-Cheat aktiviert ist
            else baddie.y += baddieSpeeds.get(i); // Bewegt Baddie basierend auf seiner Geschwindigkeit

            // Entfernt Baddie, wenn er das Fenster verlässt
            if (baddie.y > WINDOW_HEIGHT)
            {
                baddies.remove(i);
                baddieSpeeds.remove(i);
                i--;
            }
        }
    }

    private void checkCollisions()
    {
        for (Rectangle baddie : baddies)
        {
            if (player.intersects(baddie))
            { // Prüft, ob der Spieler einen Baddie berührt
                if (score > topScore) topScore = score; // Aktualisiert den Höchstpunktestand
                if (gameOverClip != null)
                {
                    gameOverClip.setFramePosition(0); // Setzt den Game Over Sound auf den Anfang
                    gameOverClip.start(); // Spielt den Game Over Sound
                }
                if (backgroundMusicClip != null)
                {
                    backgroundMusicClip.stop(); // Stoppt die Hintergrundmusik
                }
                timer.stop(); // Stoppt den Timer, um das Spiel zu beenden
            }
        }
    }

    public void keyPressed(KeyEvent e)
    {
        if (!timer.isRunning())
        {
            resetGame(); // Setzt das Spiel zurück, wenn es nicht läuft
            if (backgroundMusicClip != null)
            {
                backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Startet die Hintergrundmusik neu
            }
            timer.start(); // Startet den Timer neu, um das Spiel zu beginnen
        }

        int key = e.getKeyCode(); // Holt den gedrückten Tastencode
        if (key == KeyEvent.VK_LEFT) moveLeft = true; // Setzt die Bewegung nach links
        if (key == KeyEvent.VK_RIGHT) moveRight = true; // Setzt die Bewegung nach rechts
        if (key == KeyEvent.VK_UP) moveUp = true; // Setzt die Bewegung nach oben
        if (key == KeyEvent.VK_DOWN) moveDown = true; // Setzt die Bewegung nach unten
        if (key == KeyEvent.VK_Z) reverseCheat = true; // Aktiviert den Reverse-Cheat
        if (key == KeyEvent.VK_X) slowCheat = true; // Aktiviert den Slow-Cheat
        if (key == KeyEvent.VK_ESCAPE) System.exit(0); // Beendet das Spiel
    }

    public void keyReleased(KeyEvent e)
    {
        int key = e.getKeyCode(); // Holt den losgelassenen Tastencode
        if (key == KeyEvent.VK_LEFT) moveLeft = false; // Deaktiviert die Bewegung nach links
        if (key == KeyEvent.VK_RIGHT) moveRight = false; // Deaktiviert die Bewegung nach rechts
        if (key == KeyEvent.VK_UP) moveUp = false; // Deaktiviert die Bewegung nach oben
        if (key == KeyEvent.VK_DOWN) moveDown = false; // Deaktiviert die Bewegung nach unten
        if (key == KeyEvent.VK_Z) reverseCheat = false; // Deaktiviert den Reverse-Cheat
        if (key == KeyEvent.VK_X) slowCheat = false; // Deaktiviert den Slow-Cheat
    }

    public void keyTyped(KeyEvent e)
    {
        // Nicht verwendet, aber erforderlich für KeyListener-Schnittstelle
    }

    private void resetGame()
    {
        player.setLocation(WINDOW_WIDTH / 2, WINDOW_HEIGHT - 50); // Setzt die Position des Spielers zurück
        baddies.clear(); // Entfernt alle Baddies
        baddieSpeeds.clear(); // Entfernt alle Baddie-Geschwindigkeiten
        score = 0; // Setzt den Punktestand zurück
        baddieAddCounter = 0; // Setzt den Zähler für das Hinzufügen von Baddies zurück
        moveLeft = moveRight = moveUp = moveDown = false; // Setzt die Bewegungsrichtungen zurück
        reverseCheat = slowCheat = false; // Deaktiviert die Cheats
    }

    private Image loadImage(String fileName)
    {
        URL url = getClass().getResource(fileName); // Holt die URL der Bilddatei
        if (url == null)
        {
            System.err.println("Couldn't find file: " + fileName); // Gibt eine Fehlermeldung aus, wenn die Datei nicht gefunden wurde
            return null;
        }
        return new ImageIcon(url).getImage(); // Gibt das Bild zurück
    }

    private Clip loadSound(String fileName)
    {
        try
        {
            URL url = getClass().getResource(fileName); // Holt die URL der Sounddatei
            if (url == null)
            {
                System.err.println("Couldn't find file: " + fileName); // Gibt eine Fehlermeldung aus, wenn die Datei nicht gefunden wurde
                return null;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url); // Erstellt einen AudioInputStream
            Clip clip = AudioSystem.getClip(); // Erstellt ein Clip-Objekt
            clip.open(audioIn); // Öffnet den AudioStream im Clip
            return clip; // Gibt das Clip-Objekt zurück
        }
        catch (UnsupportedAudioFileException | IOException | LineUnavailableException e)
        {
            e.printStackTrace(); // Gibt den Fehlerstacktrace aus
            return null;
        }
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Dodger"); // Erstellt ein neues JFrame für das Spiel
        DodgerGame game = new DodgerGame(); // Erstellt ein neues Spiel
        frame.add(game); // Fügt das Spiel zum Frame hinzu
        frame.pack(); // Passt das Frame an die bevorzugte Größe des Spiels an
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Setzt die Standard-Schließoperation
        frame.setVisible(true); // Macht das Frame sichtbar
        game.startGame(); // Startet das Spiel
    }
}
