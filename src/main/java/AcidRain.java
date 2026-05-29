import io.qt.core.*;
import io.qt.widgets.*;
//import io.qt.gui.*;
import java.io.*;
import java.util.*;
//import java.util.regex.Pattern;

public class AcidRain extends QWidget {

	private QStackedWidget stackedWidget;
	private QTableWidget rankTableWidget;
	private QWidget menuWidget;
	private QWidget userWidget;
	private QWidget gameWidget;
	private QWidget scoreWidget;

	private QGraphicsScene scene;
	private QGraphicsView view;
	private QLineEdit gameInputLineEdit;
	private QLineEdit nameLineEdit;

	private QLabel scoreLabel;
	private QTimer frameTimer;
	private QTimer spawnTimer;

	private List<QGraphicsTextItem> fallingWords = new ArrayList<>();
	private int score = 0;
	private int languageSet = 0;

	private final int SCREEN_WIDTH = 500;
	private final int SCREEN_HEIGHT = 400;

	private String[] koreanWordList = { "안녕", "자바", "오잉", "리눅스", "빌드", "한글", "점심", "침대", "간식", "과제", "배개", "짜장면" };
	private String[] englishWordList = { "hello", "java", "bruh", "linux", "build", "clang", "maven", "what",
			"fedora" };
	private String[] symbolWordList = { "!", "@", "#", "$", "%", "^", "&", "*", "(", ")" };
	private String[] gameModeList = { "한국어", "영어", "기호", "한/영" };
	private String userName;
	private String fileData;

	private Random random = new Random();

	public AcidRain() {
		stackedWidget = new QStackedWidget(this);

		initMenu();
		initUser();
		initGame();
		initScore();
		stackedWidget.addWidget(menuWidget);
		stackedWidget.addWidget(userWidget);
		stackedWidget.addWidget(gameWidget);
		stackedWidget.addWidget(scoreWidget);

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

		QPushButton userSetupPushButton = new QPushButton("도전자 정보 입력하기", menuWidget);
		userSetupPushButton.setFixedSize(200, 40);
		userSetupPushButton.clicked.connect(this, "userLogic()");

		QPushButton startGamePushButton = new QPushButton("게임하기", menuWidget);
		startGamePushButton.setFixedSize(200, 40);

		startGamePushButton.clicked.connect(this, "gameLogic()");

		QPushButton rankDisplayPushButton = new QPushButton("전체 랭킹 확인하기", menuWidget);
		rankDisplayPushButton.setFixedSize(200, 40);
		rankDisplayPushButton.clicked.connect(this, "scoreLogic()");

		QComboBox setLanguageComboBox = new QComboBox(menuWidget);
		setLanguageComboBox.setFixedSize(200, 30);

		setLanguageComboBox.addItem("한글 도전");
		setLanguageComboBox.addItem("영문 도전");
		setLanguageComboBox.addItem("기호 도전");
		setLanguageComboBox.addItem("한글 + 영문 도전");
		setLanguageComboBox.currentIndexChanged.connect((index) -> {
			this.languageSet = index;
		});

		menuLayout.addWidget(titleLabel);
		menuLayout.addWidget(subtitleLabel);
		menuLayout.addWidget(userSetupPushButton);
		menuLayout.addWidget(startGamePushButton);
		menuLayout.addWidget(rankDisplayPushButton);
		menuLayout.addWidget(setLanguageComboBox);
	}

	private void initUser() {
		userWidget = new QWidget(this);

		// scene = new QGraphicsScene(userWidget);

		QLabel titleLabel = new QLabel("도전자 이름을 입력하십시오.");
		titleLabel.setAlignment(Qt.AlignmentFlag.AlignCenter);
		titleLabel.setStyleSheet("font: bold 16px;");

		nameLineEdit = new QLineEdit(userWidget);
		nameLineEdit.setPlaceholderText("여기에 입력하십시오.");
		nameLineEdit.returnPressed.connect(this, "setUserData()");
		nameLineEdit.setFixedSize(200, 30);

		QPushButton returnToMenuPushButton = new QPushButton("메뉴로 돌아가기", userWidget);
		returnToMenuPushButton.setFixedSize(200, 45);
		returnToMenuPushButton.clicked.connect(this, "returnToMenu()");

		QVBoxLayout userLayout = new QVBoxLayout(userWidget);
		userLayout.setAlignment(Qt.AlignmentFlag.AlignCenter);
		userLayout.addWidget(titleLabel);
		userLayout.addWidget(nameLineEdit);
		userLayout.addWidget(returnToMenuPushButton);

	}

	@SuppressWarnings("unused")
	private void userLogic() {
		stackedWidget.setCurrentIndex(1);
	}

	@SuppressWarnings("unused")
	private void setUserData() {
		String userInput = this.nameLineEdit.text().trim();

		this.userName = userInput;
		QMessageBox.information(this, "도전자 이름", "새로운 사용자 이름은 " + this.userName + "입니다.");
		returnToMenu();
	}

	private void initGame() {
		gameWidget = new QWidget(this);

		scene = new QGraphicsScene(gameWidget);
		scene.setSceneRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT - 100);

		view = new QGraphicsView(scene);
		view.setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
		view.setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAlwaysOff);
		view.setFocusPolicy(Qt.FocusPolicy.NoFocus);

		scoreLabel = new QLabel("점수: 0", gameWidget);

		gameInputLineEdit = new QLineEdit(gameWidget);
		gameInputLineEdit.setPlaceholderText("여기에 입력하십시오.");
		gameInputLineEdit.returnPressed.connect(this, "checkWord()");

		QVBoxLayout gameLayout = new QVBoxLayout(gameWidget);
		gameLayout.addWidget(view);
		gameLayout.addWidget(scoreLabel);
		gameLayout.addWidget(gameInputLineEdit);

	}

	@SuppressWarnings("unused")
	private void gameLogic() {

		score = 0;
		scoreLabel.setText("점수: 0");
		gameInputLineEdit.clear();

		for (QGraphicsTextItem word : fallingWords) {
			scene.removeItem(word);
		}
		fallingWords.clear();

		stackedWidget.setCurrentIndex(2);
		gameInputLineEdit.setFocus();

		frameTimer = new QTimer(this);
		frameTimer.timeout.connect(this, "moveWord()");
		frameTimer.start(30);

		spawnTimer = new QTimer(this);
		spawnTimer.timeout.connect(this, "spawnWord()");
		spawnTimer.start(1000);
	}

	@SuppressWarnings("unused")
	private void spawnWord() {

		String randomWord = null;

		switch (languageSet) {
		case 0:
			randomWord = koreanWordList[random.nextInt(koreanWordList.length)];
			break;
		case 1:
			randomWord = englishWordList[random.nextInt(englishWordList.length)];
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
		}

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
				frameTimer.stop();
				spawnTimer.stop();
				QMessageBox.information(this, "게임 종료 ", "최종 점수는 " + score + "점입니다.");
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
		String typedText = gameInputLineEdit.text().trim();
		gameInputLineEdit.clear();

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

	private void initScore() {
		scoreWidget = new QWidget(this);

		QPushButton returnToMenuPushButton = new QPushButton("메뉴로 돌아가기", scoreWidget);
		returnToMenuPushButton.clicked.connect(this, "returnToMenu()");
		returnToMenuPushButton.setFixedSize(100, 30);

		rankTableWidget = new QTableWidget();
		rankTableWidget.setColumnCount(3);
		rankTableWidget.setHorizontalHeaderLabels(Arrays.asList("이름", "언어", "점수"));
		rankTableWidget.horizontalHeader().setSectionResizeMode(QHeaderView.ResizeMode.Stretch);
		rankTableWidget.setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers);
		rankTableWidget.setFocusPolicy(Qt.FocusPolicy.NoFocus);

		QVBoxLayout scoreLayout = new QVBoxLayout(scoreWidget);
		scoreLayout.setAlignment(Qt.AlignmentFlag.AlignLeft);
		scoreLayout.addWidget(returnToMenuPushButton);
		scoreLayout.addWidget(rankTableWidget);
	}

	@SuppressWarnings("unused")
	private void scoreLogic() {
		stackedWidget.setCurrentIndex(3);
		readFromFile();
	}

	// @SuppressWarnings("unused")
	private void readFromFile() {
		rankTableWidget.setRowCount(0);

		String userNameFromFile;
		int languageModeFromFile;
		String scoreFromFile;
		String unnamed = "null";

		try {
			Scanner fileScan = new Scanner(new File("acidrain-qt.save"));

			// fileScan.useDelimiter(Pattern.compile(";"));
			while (fileScan.hasNextLine()) {
				fileData = fileScan.nextLine().trim();
				int currentRow = rankTableWidget.rowCount();

				if (fileData.startsWith("username=")) {
					String line = fileData.substring("username=".length());
					String[] word = line.split(";");
					userNameFromFile = word[0].trim();
					languageModeFromFile = Integer.parseInt(word[1].trim());
					scoreFromFile = word[2].trim();

					if (unnamed.equals(userNameFromFile)) {
						userNameFromFile = "이름 없음";
					}

					rankTableWidget.insertRow(currentRow);
					rankTableWidget.setItem(currentRow, 0, new QTableWidgetItem(userNameFromFile));
					rankTableWidget.setItem(currentRow, 1, new QTableWidgetItem(gameModeList[languageModeFromFile]));
					rankTableWidget.setItem(currentRow, 2, new QTableWidgetItem(scoreFromFile + "점"));
					// QMessageBox.information(this, "읽은 자료", "이름: " + userNameFromFile + "모드: " +
					// languageModeFromFile + "점수: " + scoreFromFile);
				}
			}
			fileScan.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void saveScore(int finalScore) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter("acidrain-qt.save", true))) {

			writer.write("username= " + userName + "; " + languageSet + "; " + String.valueOf(finalScore) + ";");
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