import io.qt.core.*;
import io.qt.widgets.*;
import io.qt.gui.*;
import java.io.*;
import java.util.*;


public class AcidRain extends QWidget {
	
	private QGraphicsScene scene;
    private QGraphicsView view;
    private QLineEdit inputField;
    private QLabel scoreLabel;
    private QTimer gameTimer;
    
    private List<QGraphicsTextItem> fallingWords = new ArrayList<>();
    private int score = 0;
    private final int SCREEN_WIDTH = 500;
    private final int SCREEN_HEIGHT = 400;
    
    private String[] englishWordList = {"hello", "java", "bruh", "linux", "build", "clang", "maven", "what"};
    private Random random = new Random();

    public AcidRain() {
    	initGame();
    	setup();
    }
    
    private void initGame() {
    	scene = new QGraphicsScene(this);
        scene.setSceneRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT - 80);
        
        view = new QGraphicsView(scene);
        view.setHorizontalScrollBarPolicy(io.qt.core.Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
        view.setVerticalScrollBarPolicy(io.qt.core.Qt.ScrollBarPolicy.ScrollBarAlwaysOff);

        scoreLabel = new QLabel("점수: 0", this);
        inputField = new QLineEdit(this);
        inputField.setPlaceholderText("여기에 입력하십시오.");

        QVBoxLayout mainLayout = new QVBoxLayout(this);
        mainLayout.addWidget(view);
        mainLayout.addWidget(scoreLabel);
        mainLayout.addWidget(inputField);

        inputField.returnPressed.connect(this, "checkTypedWord()");

        setWindowTitle("산성비");
        setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
    }
    
    private void setup() {
    	gameTimer = new QTimer(this);
        gameTimer.timeout.connect(this, "updateGameLoop()");
        gameTimer.start(30);

        QTimer spawnTimer = new QTimer(this);
        spawnTimer.timeout.connect(this, "spawnWord()");
        spawnTimer.start(1000);
    }
    
    private void spawnWord() {
        String randomWord = englishWordList[random.nextInt(englishWordList.length)];
        QGraphicsTextItem textItem = scene.addText(randomWord);
        
        int xPos = random.nextInt(SCREEN_WIDTH - 100);
        textItem.setPos(xPos, 0);
        
        fallingWords.add(textItem);
    }
    
    private void updateGameLoop() {
        List<QGraphicsTextItem> reachEnd = new ArrayList<>();

        for (QGraphicsTextItem word : fallingWords) {
            word.setPos(word.x(), word.y() + 2);

            if (word.y() >= scene.height()) {
                reachEnd.add(word);
                score = Math.max(0, score - 1);
            }
        }

        for (QGraphicsTextItem word : reachEnd) {
            scene.removeItem(word);
            fallingWords.remove(word);
        }
        
        scoreLabel.setText("점수: " + score);
    }
	
    
    private void checkTypedWord() {
        String typedText = inputField.text().trim();
        inputField.clear();

        QGraphicsTextItem matchedWord = null;
        for (QGraphicsTextItem word : fallingWords) {
            if (word.toPlainText().equals(typedText)) {
                matchedWord = word;
                break;
            }
        }

        if (matchedWord != null) {
            scene.removeItem(matchedWord);
            fallingWords.remove(matchedWord);
            score += 1;
            scoreLabel.setText("점수: " + score);
        }
    }
    
    @Override
    protected void closeEvent(QCloseEvent event) {
        saveHighScore(score);
        super.closeEvent(event);
    }
    
    private void saveHighScore(int finalScore) {
        File file = new File("acidrain-qt.save");
        int currentHighScore = 0;

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                if (line != null) currentHighScore = Integer.parseInt(line.trim());
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }

        if (finalScore > currentHighScore) {
            QMessageBox.information(this, "New High Score!", "Congratulations! You set a new record: " + finalScore);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(String.valueOf(finalScore));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            QMessageBox.information(this, "게임 종료", "최종 점수: " + finalScore + "\n최고 점수: " + currentHighScore);
        }
    }
    
	public static void main(String[] args) {
        QApplication.initialize(args);
        String nativeStyle = QApplication.style().objectName();
        QApplication.setStyle(QStyleFactory.create(nativeStyle));

        AcidRain gameInstance = new AcidRain();
        gameInstance.show();

        QApplication.exec();
    }
	
}