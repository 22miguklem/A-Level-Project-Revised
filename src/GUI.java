import javafx.animation.PathTransition;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GUI {
    static final int dimension = 8;
    private static final StackPane startTile = new StackPane(), endTile = new StackPane();
    private static final ArrayList<Node> circles = new ArrayList<>();
    private static final StackPane square = new StackPane();
    private static final ArrayList<ImageView> capturedPieceImages = new ArrayList<>();
    public static ImageView[][] images = new ImageView[8][8];
    static int boardSize = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.9);
    static int sqSize = boardSize / dimension;
    public static Text evaluationText = new Text(sqSize, sqSize, "0.0");
    private static int whitePiecesCaptured = 0;
    private static int blackPiecesCaptured = 0;
    Piece p = new Piece();
    Board b = new Board();
    boolean errorFound = false;
    MakeMove makeMove = new MakeMove();
    MoveGenerator moveGenerator = new MoveGenerator();

    private static void delImage(ImageView image) {
        image.setImage(null);
    }

    public void drawBoard(GridPane root) {
        String colour;
        // This is the start of the nested for loop
        for (int row = 0; row < dimension; row++) {
            for (int col = 0; col < dimension; col++) {
                // initialises each "pane"/tile
                StackPane boardTile = new StackPane();
                // This makes every other colour alter, e.g. first tile is light, next tile is dark
                if ((row + col) % 2 == 0)
                    colour = "rgb(248,220,180)";
                else
                    colour = "rgb(184,140,100)";
                // sets the colour of the tile
                boardTile.setStyle("-fx-background-color: " + colour + ";");
                boardTile.setViewOrder(3);
                // adds the tile to the window, and sets where the pane needs to go
                root.add(boardTile, col, row);
            }
        }
        for (int i = 0; i < dimension; i++) {
            // Set Constraints
            root.getColumnConstraints().add(new ColumnConstraints(sqSize));
            root.getRowConstraints().add(new RowConstraints(sqSize));
        }
    }

    public void drawPieces(String fen, GridPane root) {
        int[] board = b.loadFromFen(fen);
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 16; file++) {
                if ((b.getSquare(rank, file) & 0x88) != 0)
                    continue;
                int piece = board[b.getSquare(rank, file)];
                if (piece != p.none) {
                    images[rank][file] = placeImage(rank, file, piece, root);
                    images[rank][file].setViewOrder(1);
                }
            }
        }
    }

    private ImageView placeImage(int rank, int file, int piece, GridPane root) {
        Image image = loadImage("src/assets/" + piece + ".png");
        ImageView imageView = new ImageView(image);
        imageView.setX(file * sqSize);
        imageView.setY(rank * sqSize);
        imageView.setFitHeight(sqSize);
        imageView.setFitWidth(sqSize);
        root.add(imageView, file, rank);
        return imageView;
    }

    private Image loadImage(String FileName) {
        // Image is initialised
        Image img = null;
        try {
            // this gets the image from the directory entered
            img = new Image(new FileInputStream(FileName));
        } catch (FileNotFoundException e) {
            // Checks if an error has been found before, so that multiple alerts aren't shown
            if (!errorFound) {
                errorFound = true;
                // Initialises window
                Stage errorWindow = new Stage();
                errorWindow.initModality(Modality.APPLICATION_MODAL);

                Label label = new Label();
                label.setText("Error, image of piece cannot be found.\nRe-download the application");
                Button closeButton = new Button("Ok");
                closeButton.setOnAction(event -> errorWindow.close());

                VBox layout = new VBox(10);
                layout.getChildren().addAll(label, closeButton);
                layout.setAlignment(Pos.CENTER);

                Scene scene = new Scene(layout);
                errorWindow.setScene(scene);
                errorWindow.setTitle("Error");
                errorWindow.initStyle(StageStyle.UTILITY);
                errorWindow.show();
            }
        }
        return img;
    }

    public void moveImages(Move move) {
        int startRank = b.getRank(move.getStartSq()), startFile = b.getFile(move.getStartSq()),
                endRank = b.getRank(move.getEndSq()), endFile = b.getFile(move.getEndSq());
        if (move.isUndoMove() && move.isCaptureMove())
            removeCapturedPiece();
        else
            drawCapturedPiece(move.getPieceCaptured());
        // For undo moves, add piece that was captured
        if (images[startRank][startFile] == null)
            images[startRank][startFile] = placeImage(startRank, startFile, move.getPieceMoved(), Main.root);
        showLastMove(move);
        moveAnimation(move);
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        ImageView image = null;
        if (images[endRank][endFile] != null)
            image = images[endRank][endFile];
        images[endRank][endFile] = images[startRank][startFile];
        images[startRank][startFile] = null;
        // Removes images for en passant
        if (move.isEnPassantMove()) {
            if (Board.whiteToMove) {
                image = images[endRank - 1][endFile];
                images[endRank - 1][endFile] = null;
            } else {
                image = images[endRank + 1][endFile];
                images[endRank + 1][endFile] = null;
            }
        }
        if (move.isCastle())
            if (move.isUndoMove()) {
                if (startFile - endFile == -2) {
                    images[endRank][0] = images[endRank][3];
                    images[endRank][3] = null;
                } else {
                    images[endRank][7] = images[endRank][5];
                    images[endRank][5] = null;
                }
            } else {
                if (startFile - endFile == 2) {
                    images[endRank][3] = images[endRank][0];
                    images[endRank][0] = null;
                } else {
                    images[endRank][5] = images[endRank][7];
                    images[endRank][7] = null;
                }
            }
        if (move.isPromotion()) {
            images[endRank][endFile].setImage(null);
            if (move.isUndoMove())
                images[endRank][endFile] = placeImage(endRank, endFile, move.getPieceMoved() > 0 ? 1 : -1, Main.root);
            else
                images[endRank][endFile] = placeImage(endRank, endFile, move.getPieceMoved() > 0 ? 5 : -5, Main.root);
        }
        ImageView finalImage = image;
        executorService.schedule(() -> {
            assert finalImage != null;
            delImage(finalImage);
        }, 150, TimeUnit.MILLISECONDS);
        // For undo moves add captured piece back
        if (move.getPieceCaptured() != 0 && move.isUndoMove())
            images[startRank][startFile] = placeImage(startRank, startFile, move.getPieceCaptured(), Main.root);
    }

    private void moveAnimation(Move move) {
        Line path = new Line();
        int startRank = b.getRank(move.getStartSq()), startFile = b.getFile(move.getStartSq()),
                endRank = b.getRank(move.getEndSq()), endFile = b.getFile(move.getEndSq());
        path.setStartX(startFile * sqSize + (sqSize >> 1));
        path.setStartY(startRank * sqSize + (sqSize >> 1));
        path.setEndX(endFile * sqSize + (sqSize >> 1));
        path.setEndY(endRank * sqSize + (sqSize >> 1));

        PathTransition transition = new PathTransition();
        ImageView image = images[startRank][startFile];
        image.toFront();
        transition.setPath(path);
        transition.setNode(image);
        transition.setDuration(Duration.seconds(0.2));
        transition.play();
        if (move.isCastle()) {
            ImageView rookImage;
            Line rookPath = new Line();
            rookPath.setStartY(startRank * sqSize + (sqSize >> 1));
            rookPath.setEndY(endRank * sqSize + (sqSize >> 1));
            if (move.isUndoMove()) {
                if (startFile - endFile == -2) {
                    rookPath.setStartX(3 * sqSize + (sqSize >> 1));
                    rookPath.setEndX(sqSize >> 1);
                    rookImage = images[startRank][3];
                } else {
                    rookPath.setStartX(5 * sqSize + (sqSize >> 1));
                    rookPath.setEndX(7 * sqSize + (sqSize >> 1));
                    rookImage = images[startRank][5];
                }
            } else {
                if (startFile - endFile == 2) { // Left castle
                    rookPath.setStartX(sqSize >> 1);
                    rookPath.setEndX(3 * sqSize + (sqSize >> 1));
                    rookImage = images[startRank][0];
                } else {
                    rookPath.setStartX(7 * sqSize + (sqSize >> 1));
                    rookPath.setEndX(5 * sqSize + (sqSize >> 1));
                    rookImage = images[startRank][7];
                }
            }
            PathTransition rookTransition = new PathTransition();
            rookTransition.setPath(rookPath);
            rookTransition.setNode(rookImage);
            rookTransition.setDuration(Duration.seconds(0.2));
            rookTransition.play();
        }
    }

    private void showLastMove(Move move) {
        String startSqColour, endSqColour;
        Main.root.getChildren().remove(startTile);
        Main.root.getChildren().remove(endTile);
        int startRank = b.getRank(move.getStartSq()), startFile = b.getFile(move.getStartSq()),
                endRank = b.getRank(move.getEndSq()), endFile = b.getFile(move.getEndSq());

        startSqColour = (startRank + startFile) % 2 == 0 ? "rgb(248,236,92)" : "rgb(224,196,52)";
        endSqColour = (endRank + endFile) % 2 == 0 ? "rgb(248,236,92)" : "rgb(224,196,52)";

        startTile.setStyle("-fx-background-color: " + startSqColour + ";");
        endTile.setStyle("-fx-background-color: " + endSqColour + ";");
        startTile.setViewOrder(2);
        endTile.setViewOrder(2);

        Main.root.add(startTile, startFile, startRank);
        Main.root.add(endTile, endFile, endRank);
    }

    public void drawSquare(int rank, int file) {
        Main.root.getChildren().remove(square);
        String colour = (rank + file) % 2 == 0 ? "rgb(248,236,92)" : "rgb(224,196,52)";
        square.setStyle("-fx-background-color: " + colour + ";");
        square.setViewOrder(2);
        Main.root.add(square, file, rank);
    }

    public void drawCaptureCircle(int endSquare) {
        int rank = b.getRank(endSquare), file = b.getFile(endSquare);
        double strokeWidth = (double) sqSize / 12;
        Arc ring = new Arc(0, 0, (double) sqSize / 2 - strokeWidth, (double) sqSize / 2 - strokeWidth, 0, 360);
        ring.setStrokeWidth(strokeWidth);
        ring.setStrokeType(StrokeType.OUTSIDE);
        ring.setOpacity(0.2);
        ring.setStrokeLineCap(StrokeLineCap.BUTT);
        ring.setStroke(Color.BLACK);
        ring.setFill(null);
        GridPane.setHalignment(ring, HPos.CENTER);
        GridPane.setValignment(ring, VPos.CENTER);
        Main.root.add(ring, file, rank);
        circles.add(ring);
    }

    public void removeSquare() {
        Main.root.getChildren().remove(square);
    }

    public void drawLegalMoves(int square, ArrayList<Move> moves) {
        for (Move move : moves) {
            if (move.getStartSq() == square) {
                if (move.getBoard()[move.getEndSq()] == 0)
                    drawEmptyCircle(move.getEndSq());
                else
                    drawCaptureCircle(move.getEndSq());
            }
        }
    }

    private void drawEmptyCircle(int endSquare) {
        int rank = b.getRank(endSquare), file = b.getFile(endSquare);
        Circle circle = new Circle(0, 0, (double) sqSize / 7);
        circle.setOpacity(0.2);
        GridPane.setHalignment(circle, HPos.CENTER);
        GridPane.setValignment(circle, VPos.CENTER);
        Main.root.add(circle, file, rank);
        circles.add(circle);
    }

    public void destroyCircles() {
        for (Node circle : circles)
            Main.root.getChildren().remove(circle);
        circles.clear();
    }

    private void createPanel(int row) {
        double sqSize = GUI.sqSize;
        // Create the panel
        Rectangle panel = new Rectangle(0, 0, sqSize * 3, sqSize);
        panel.setFill(Color.rgb(57, 62, 70));
        panel.setArcWidth(sqSize / 6);
        panel.setArcHeight(sqSize / 6);
        panel.setStroke(Color.rgb(34, 40, 49));
        Main.root.add(panel, 8, row);
    }

    public void drawDifficultyPanel() {
        double sqSize = GUI.sqSize;
        createPanel(0);
        // Create the text
        Text text = new Text(sqSize, sqSize, "Difficulty - Hard\n     ELO 1500");
        text.setFont(Font.font("Segoe UI", FontWeight.NORMAL, sqSize / 3));
        GridPane.setHalignment(text, HPos.CENTER);
        GridPane.setValignment(text, VPos.CENTER);
        text.setFill(Color.rgb(219, 216, 214));
        Main.root.add(text, 8, 0);
    }

    public void drawEvaluation() {
        createPanel(1);
        double sqSize = GUI.sqSize;
        evaluationText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, sqSize / 3));
        GridPane.setHalignment(evaluationText, HPos.CENTER);
        GridPane.setValignment(evaluationText, VPos.CENTER);
        evaluationText.setFill(Color.rgb(219, 216, 214));
        Main.root.add(evaluationText, 8, 1);
    }

    public void updateEvaluation(double evaluation) {
        if (evaluation == -1)
            evaluationText.setText("Book Move");
        else if (evaluation == Double.POSITIVE_INFINITY || evaluation == Double.NEGATIVE_INFINITY)
            evaluationText.setText("Forced Checkmate Found");
        else
            evaluationText.setText(-evaluation / 10 + "");
    }

    public void undoButton() {
        double sqSize = GUI.sqSize;
        Button button = new Button("Undo");
        button.setPrefWidth(sqSize);
        button.setPrefHeight(sqSize);
        Font font = Font.font("Segoe UI", FontWeight.NORMAL, sqSize / 5);
        button.setFont(font);
        button.setTextFill(Color.rgb(219, 216, 214));
        button.setStyle("-fx-background-color: rgb(57, 62, 70)");
        button.setOnAction(actionEvent -> undoClicked());
        Main.root.add(button, 8, 6);
    }

    private void undoClicked() {
        if (!AI.thinking && MakeMove.moveLog.size() > 1) {
            Main.root.getChildren().remove(startTile);
            Main.root.getChildren().remove(endTile);
            Move AIMove = makeMove.undoMove().setUndoMove();
            int startSq = AIMove.getStartSq(), endSq = AIMove.getEndSq();
            AIMove.setStartSq(endSq);
            AIMove.setEndSq(startSq);
            moveImages(AIMove);
            Move playerMove = makeMove.undoMove().setUndoMove();
            startSq = playerMove.getStartSq();
            endSq = playerMove.getEndSq();
            playerMove.setStartSq(endSq);
            playerMove.setEndSq(startSq);
            moveImages(playerMove);
            MouseHandler.moves = moveGenerator.generateLegalMoves();
            MouseHandler.squareSelected = -1;
            AI.chessNotationMoveLog.remove(AI.chessNotationMoveLog.size() - 1);
            AI.chessNotationMoveLog.remove(AI.chessNotationMoveLog.size() - 1);
            if (!Board.fenHistory.isEmpty())
                Board.fenHistory.remove(Board.fenHistory.size() - 1);
            evaluationText.setText("Evaluation reset");
        }
    }

    public void drawCapturedPiecesPanel() {
        double sqSize = GUI.sqSize;
        Rectangle whiteContainer = new Rectangle(0, 0, sqSize * 3, sqSize / 2);
        Rectangle blackContainer = new Rectangle(0, 0, sqSize * 3, sqSize / 2);
        whiteContainer.setFill(Color.rgb(57, 62, 70));
        blackContainer.setFill(Color.rgb(57, 62, 70));
        GridPane.setValignment(whiteContainer, VPos.TOP);
        GridPane.setValignment(blackContainer, VPos.BOTTOM);
        whiteContainer.setStroke(Color.rgb(34, 40, 49));
        blackContainer.setStroke(Color.rgb(34, 40, 49));
        Main.root.add(whiteContainer, 8, 7);
        Main.root.add(blackContainer, 8, 7);
    }

    private void drawCapturedPiece(int capturedPiece) {
        double sqSize = GUI.sqSize;
        if (capturedPiece == 0)
            return;
        Image image = loadImage("src/assets/" + capturedPiece + ".png");
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(sqSize / 2);
        imageView.setFitWidth(sqSize / 2);
        if (capturedPiece > 0) {
            GridPane.setValignment(imageView, VPos.TOP);
            imageView.setTranslateX(whitePiecesCaptured * sqSize / 5.5);
            whitePiecesCaptured++;
        } else {
            GridPane.setValignment(imageView, VPos.BOTTOM);
            imageView.setTranslateX(blackPiecesCaptured * sqSize / 5.5);
            blackPiecesCaptured++;
        }
        capturedPieceImages.add(imageView);
        Main.root.add(imageView, 8, 7);
    }

    private void removeCapturedPiece() {
        if (!capturedPieceImages.isEmpty()) {
            ImageView image = capturedPieceImages.get(capturedPieceImages.size() - 1);
            capturedPieceImages.remove(capturedPieceImages.size() - 1);
            Main.root.getChildren().remove(image);
            if (Board.whiteToMove)
                blackPiecesCaptured--;
            else
                whitePiecesCaptured--;
        }
    }
}
