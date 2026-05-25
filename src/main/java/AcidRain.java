import io.qt.core.*;
import io.qt.widgets.*;
//import io.qt.gui.*;
import java.io.*;
import java.util.*;

public class AcidRain extends QWidget {

	private QStackedWidget stackedWidget;
	private QWidget menuWidget;
	private QWidget userWidget;
	private QWidget gameWidget;
	// private QWidget scoreWidget;

	private QGraphicsScene scene;
	private QGraphicsView view;
	private QLineEdit gameInputField;
	private QLineEdit userInputField;

	private QLabel scoreLabel;
	private QTimer gameTimer;
	private QTimer spawnTimer;

	private List<QGraphicsTextItem> fallingWords = new ArrayList<>();
	private int score = 0;
	private final int SCREEN_WIDTH = 500;
	private final int SCREEN_HEIGHT = 400;

	private String userName = "test";

	private int languageSet = 0;
	private String[] koreanWordList = { "안녕", "자바", "오잉", "리눅스", "빌드", "한글", "점심", "침대", "간식", "과제", "배개", "짜장면" };
	private String[] englishWordList = { "hello", "java", "bruh", "linux", "build", "clang", "maven", "what",
			"fedora" };
	private String[] symbolWordList = { "!", "@", "#", "$", "%", "^", "&", "*", "(", ")" };

	private Random random = new Random();

	public AcidRain() {
		stackedWidget = new QStackedWidget(this);
		initMenu();
		initUser();
		initGame();
		stackedWidget.addWidget(menuWidget);
		stackedWidget.addWidget(userWidget);
		stackedWidget.addWidget(gameWidget);
		// stackedWidget.addWidget(scoreWidget);

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
		userSetupButton.clicked.connect(this, "userLogic()");

		QPushButton startButton = new QPushButton("게임하기", menuWidget);
		startButton.setFixedSize(200, 45);
		// startButton.setStyleSheet("font-size: 16px; font-weight: bold;");

		startButton.clicked.connect(this, "gameLogic()");

		QPushButton recordsButton = new QPushButton("전체 랭킹 확인하기", menuWidget);
		recordsButton.setFixedSize(200, 45);
		// recordsButton.setStyleSheet("font-size: 14px;");
		recordsButton.clicked.connect(this, "displayHighScoreRecords()");

		QComboBox languageSelection = new QComboBox(menuWidget);
		languageSelection.addItem("한글 도전");
		languageSelection.addItem("영문 도전");
		languageSelection.addItem("기호 도전");
		languageSelection.addItem("한글 + 영문 도전");
		languageSelection.currentIndexChanged.connect((index) -> {
			this.languageSet = index;
		});

		menuLayout.addWidget(titleLabel);
		menuLayout.addWidget(subtitleLabel);
		menuLayout.addWidget(userSetupButton);
		menuLayout.addWidget(startButton);
		menuLayout.addWidget(recordsButton);
		menuLayout.addWidget(languageSelection);
	}

	private void initUser() {
		userWidget = new QWidget(this);

		// scene = new QGraphicsScene(userWidget);

		QLabel titleLabel = new QLabel("도전자 이름을 입력하십시오.");
		titleLabel.setAlignment(Qt.AlignmentFlag.AlignCenter);
		titleLabel.setStyleSheet("font: bold 16px;");

		userInputField = new QLineEdit(userWidget);
		userInputField.setPlaceholderText("여기에 입력하십시오.");
		userInputField.returnPressed.connect(this, "setUserData()");
		userInputField.setFixedSize(200, 30);

		QPushButton returnToMenu = new QPushButton("메뉴로 돌아가기", userWidget);
		returnToMenu.setFixedSize(200, 45);
		returnToMenu.clicked.connect(this, "returnToMenu()");

		QVBoxLayout userLayout = new QVBoxLayout(userWidget);
		userLayout.setAlignment(Qt.AlignmentFlag.AlignCenter);
		userLayout.addWidget(titleLabel);
		userLayout.addWidget(userInputField);
		userLayout.addWidget(returnToMenu);

	}

	@SuppressWarnings("unused")
	private void userLogic() {
		stackedWidget.setCurrentIndex(1);
	}

	@SuppressWarnings("unused")
	private void setUserData() {
		String userInput = this.userInputField.text().trim();

		this.userName = userInput;
		QMessageBox.information(this, "사용자 이름", "사용자 이름: " + this.userName);
	}

	private void initGame() {
		gameWidget = new QWidget(this);

		scene = new QGraphicsScene(gameWidget);
		scene.setSceneRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT - 100);

		view = new QGraphicsView(scene);
		// view.setFixedSize(SCREEN_WIDTH, SCREEN_HEIGHT - 80);
		view.setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
		view.setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);

		scoreLabel = new QLabel("점수: 0", gameWidget);

		gameInputField = new QLineEdit(gameWidget);
		gameInputField.setPlaceholderText("여기에 입력하십시오.");
		gameInputField.returnPressed.connect(this, "checkWord()");

		QVBoxLayout gameLayout = new QVBoxLayout(gameWidget);
		gameLayout.addWidget(view);
		gameLayout.addWidget(scoreLabel);
		gameLayout.addWidget(gameInputField);

	}

	@SuppressWarnings("unused")
	private void gameLogic() {

		score = 0;
		scoreLabel.setText("점수: 0");
		gameInputField.clear();

		for (QGraphicsTextItem word : fallingWords) {
			scene.removeItem(word);
		}
		fallingWords.clear();

		stackedWidget.setCurrentIndex(2);
		gameInputField.setFocus();

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
			randomWord = englishWordList[random.nextInt(koreanWordList.length)];
			break;
		case 2:
			randomWord = symbolWordList[random.nextInt(symbolWordList.length)];
			break;
		case 3:
			int swapRandomLanguage = random.nextInt(2);
			if (swapRandomLanguage == 1)
				randomWord = koreanWordList[random.nextInt(englishWordList.length)];
			else
				randomWord = englishWordList[random.nextInt(englishWordList.length)];
			break;
		} /*
			 * if (languageSet == 0) { randomWord =
			 * englishWordList[random.nextInt(englishWordList.length)]; }
			 */
		// String randomWord = englishWordList[random.nextInt(englishWordList.length)];
		QGraphicsTextItem textItem = scene.addText(randomWord);

		int xPos = random.nextInt(SCREEN_WIDTH - 200) + 100;
		textItem.setPos(xPos, -30);
		fallingWords.add(textItem);
	}

	@SuppressWarnings("unused")
	private void moveWord() {
		for (QGraphicsTextItem word : fallingWords) {
			word.setPos(word.x(), word.y() + 2);

			if (word.y() >= scene.height()) {
				gameTimer.stop();
				spawnTimer.stop();
				QMessageBox.information(this, "게임 종료 ", "최종 점수: " + score);
				saveScore(score);
				returnToMenu();
				break;
			}
		}

		scoreLabel.setText("점수: " + score);

	}

	private void returnToMenu() {
		stackedWidget.setCurrentIndex(0);
	}

	@SuppressWarnings("unused")
	private void checkWord() {
		String typedText = gameInputField.text().trim();
		gameInputField.clear();

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
		int currentHighScore = readFromFile();
		QMessageBox.information(this, "최고점수", "현재 최고점수는: " + currentHighScore);
	}
	/*
	 * @Override protected void closeEvent(QCloseEvent event) { if (gameTimer !=
	 * null && gameTimer.isActive()) { saveScore(score); } super.closeEvent(event);
	 * }
	 */

	private int readFromFile() {
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

	private void saveScore(int finalScore) {
		// int currentHighScore = readFromFile();
		/*
		 * if (finalScore > currentHighScore) { QMessageBox.information(this, "신기록",
		 * "신기록을 달성하였습니다.: " + finalScore); try (BufferedWriter writer = new
		 * BufferedWriter(new FileWriter("acidrain-qt.save"))) {
		 * writer.write(String.valueOf(finalScore)); } catch (IOException e) {
		 * e.printStackTrace(); } }
		 */
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("acidrain-qt.save", true))) {

			writer.write(userName + "; " + languageSet + "; " + String.valueOf(finalScore) + ";");
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
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