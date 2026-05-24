import io.qt.core.*;
import io.qt.widgets.*;
import io.qt.gui.*;
import java.io.*;
import java.util.*;

public class AcidRain extends QWidget {

	private QStackedWidget stackedWidget;
	private QWidget menuWidget;
	private QWidget gameWidget;

	private QGraphicsScene scene;
	private QGraphicsView view;
	private QLineEdit inputField;
	private QLabel scoreLabel;
	private QTimer gameTimer;
	private QTimer spawnTimer;

	private List<QGraphicsTextItem> fallingWords = new ArrayList<>();
	private int score = 0;
	private final int SCREEN_WIDTH = 500;
	private final int SCREEN_HEIGHT = 400;

	private String[] englishWordList = { "hello", "java", "bruh", "linux", "build", "clang", "maven", "what" };
	private String[] koreanWordList = {"안녕", "자바", "오잉", "리눅스", "빌드", "한글", "점심", "침대"};
	private int languageSet = 0;
	private Random random = new Random();

	public AcidRain() {
		stackedWidget = new QStackedWidget(this);
		initMenu();
		initGame();
		stackedWidget.addWidget(menuWidget);
		stackedWidget.addWidget(gameWidget);

		QVBoxLayout mainLayout = new QVBoxLayout(this);
		mainLayout.addWidget(stackedWidget);
		mainLayout.setContentsMargins(0, 0, 0, 0);

		stackedWidget.setCurrentIndex(0);

		setWindowTitle("산성비");
		setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT);
	}

	private void initMenu() {
		menuWidget = new QWidget(this);
		QVBoxLayout menuLayout = new QVBoxLayout(menuWidget);
		menuLayout.setAlignment(Qt.AlignmentFlag.AlignCenter);

		QLabel titleLabel = new QLabel("타자 프로그램(산성비)", menuWidget);
		titleLabel.setAlignment(Qt.AlignmentFlag.AlignCenter);
		titleLabel.setStyleSheet("font: bold 20px;");

		QLabel subtitleLabel = new QLabel("타자 프로그램에 오신 것을 환영합니다.");
		subtitleLabel.setAlignment(Qt.AlignmentFlag.AlignCenter);
		// subtitleLabel.setStyleSheet("margin-bottom: 100px;");

		QPushButton userSetupButton = new QPushButton("도전자 정보 입력하기", menuWidget);
		userSetupButton.setFixedSize(200, 45);
		userSetupButton.clicked.connect(this, "gameLogic()");

		QPushButton startButton = new QPushButton("게임하기", menuWidget);
		startButton.setFixedSize(200, 45);
		// startButton.setStyleSheet("font-size: 16px; font-weight: bold;");

		startButton.clicked.connect(this, "gameLogic()");

		QPushButton recordsButton = new QPushButton("전체 랭킹 확인하기", menuWidget);
		recordsButton.setFixedSize(200, 45);
		// recordsButton.setStyleSheet("font-size: 14px;");
		recordsButton.clicked.connect(this, "displayHighScoreRecords()");
		
		QComboBox languageSelection = new QComboBox(menuWidget);
		languageSelection.addItem("한국어");
		languageSelection.addItem("English");
		languageSelection.addItem("혼합");
		languageSelection.currentIndexChanged.connect((index) -> {this.languageSet = index;});
		
		

		menuLayout.addWidget(titleLabel);
		menuLayout.addWidget(subtitleLabel);
		menuLayout.addWidget(userSetupButton);
		menuLayout.addWidget(startButton);
		menuLayout.addWidget(recordsButton);
		menuLayout.addWidget(languageSelection);
	}

	private void initGame() {
		gameWidget = new QWidget(this);

		scene = new QGraphicsScene(gameWidget);
		scene.setSceneRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT - 100);
		

		view = new QGraphicsView(scene);
		//view.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT - 80);
		view.setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
		view.setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);

		scoreLabel = new QLabel("점수: 0", gameWidget);

		inputField = new QLineEdit(gameWidget);
		inputField.setPlaceholderText("여기에 입력하십시오.");
		inputField.returnPressed.connect(this, "checkWord()");

		QVBoxLayout gameLayout = new QVBoxLayout(gameWidget);
		gameLayout.addWidget(view);
		gameLayout.addWidget(scoreLabel);
		gameLayout.addWidget(inputField);
		
	}

	@SuppressWarnings("unused")
	private void gameLogic() {

		score = 0;
		scoreLabel.setText("점수: 0");
		inputField.clear();

		for (QGraphicsTextItem word : fallingWords) {
			scene.removeItem(word);
		}
		fallingWords.clear();

		stackedWidget.setCurrentIndex(1);
		inputField.setFocus();

		gameTimer = new QTimer(this);
		gameTimer.timeout.connect(this, "moveWord()");
		gameTimer.start(30);

		spawnTimer = new QTimer(this);
		spawnTimer.timeout.connect(this, "spawnWord()");
		spawnTimer.start(1000);
	}

	@SuppressWarnings("unused")
	private void spawnWord() {
		
		String randomWord = null;
		
		switch (languageSet) {
		case 0:
			randomWord = koreanWordList[random.nextInt(englishWordList.length)];
			break;
		case 1:
			randomWord = englishWordList[random.nextInt(englishWordList.length)];
			break;		
		} /*
		if (languageSet == 0) {
			randomWord = englishWordList[random.nextInt(englishWordList.length)];
		} */
		//String randomWord = englishWordList[random.nextInt(englishWordList.length)];
		QGraphicsTextItem textItem = scene.addText(randomWord);

		int xPos = random.nextInt(SCREEN_WIDTH - 200) + 100;
		textItem.setPos(xPos, -30);
		fallingWords.add(textItem);
	}

	@SuppressWarnings("unused")
	private void moveWord() {
		List<QGraphicsTextItem> toRemove = new ArrayList<>();

		for (QGraphicsTextItem word : fallingWords) {
			word.setPos(word.x(), word.y() + 2);

			if (word.y() >= scene.height()) {
				toRemove.add(word);
				//score = Math.max(0, score - 1);
				QMessageBox.information(this, "게임 종료 ", "사망하였습니다.");
				stackedWidget.setCurrentIndex(0);
				break;
			}
		}

		for (QGraphicsTextItem word : toRemove) {
			scene.removeItem(word);
			fallingWords.remove(word);
		}

		scoreLabel.setText("점수: " + score);

	}

	@SuppressWarnings("unused")
	private void checkWord() {
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

	@SuppressWarnings("unused")
	private void displayHighScoreRecords() {
		int currentHighScore = readHighScoreFromFile();
		QMessageBox.information(this, "최고점수", "현재 최고점수는: " + currentHighScore);
	}

	@Override
	protected void closeEvent(QCloseEvent event) {
		if (gameTimer != null && gameTimer.isActive()) {
			saveHighScore(score);
		}
		super.closeEvent(event);
	}

	private int readHighScoreFromFile() {
		File file = new File("acidrain-qt.save");
		if (file.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				String line = reader.readLine();
				if (line != null)
					return Integer.parseInt(line.trim());
			} catch (IOException | NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	private void saveHighScore(int finalScore) {
		int currentHighScore = readHighScoreFromFile();
		if (finalScore > currentHighScore) {
			QMessageBox.information(this, "신기록", "신기록을 달성하였습니다.: " + finalScore);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter("acidrain-qt.save"))) {
				writer.write(String.valueOf(finalScore));
			} catch (IOException e) {
				e.printStackTrace();
			}
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