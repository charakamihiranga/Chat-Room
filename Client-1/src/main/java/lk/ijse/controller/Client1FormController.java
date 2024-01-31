package lk.ijse.controller;

import com.vdurmont.emoji.EmojiManager;
import com.vdurmont.emoji.EmojiParser;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Setter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;


public class Client1FormController {


    @FXML
    private AnchorPane anchorpane;
    @FXML
    private ImageView btnAttach;

    @FXML
    private Button btnBack;

    @FXML
    private Button btnClose;

    @FXML
    private ImageView btnEmoji;

    @FXML
    private Button btnMinimize;

    @FXML
    private Label lblUsername;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField txtMessage;

    @FXML
    private VBox vBox;
    @FXML
    private ImageView imgView;

    @Setter
    private String username;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private static String message;
    private File fileToSend;
    private String imgSender;

    public void initialize() {

        FadeTransition fadeIn = new FadeTransition(Duration.millis(2000), anchorpane);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();

        new Thread(() -> {
            try {
                socket = new Socket("localhost", 5000);
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataInputStream = new DataInputStream(socket.getInputStream());

                dataOutputStream.writeUTF(username);
                dataOutputStream.flush();

                while (socket.isConnected()) {
                    try {
                        String msg = dataInputStream.readUTF();
                        Platform.runLater(() -> receiveMessage(msg));
                    } catch (EOFException e) {
                        // Handle the end of the stream (socket closure) gracefully
                        System.out.println("Server has closed the connection.");
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }


    private void receiveMessage(String message) {
        String received = message;

        if (received.length() > 200){
            setReceived(received);
        }else {
            setReceivedText(message);
        }
    }

    private void setReceived(String received) {
        try{
            System.out.println("Received code :"+received);

            Image image = convertStringToImage(received);
            String sender = imgSender;

            vBox.setSpacing(10);

            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setPadding(new Insets(5, 10, 5, 10));

            VBox messageBox = new VBox();
            messageBox.setAlignment(Pos.TOP_LEFT);
            messageBox.setSpacing(5); // Adjust spacing as needed

            Text senderText = new Text(sender);
            senderText.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-font-family: 'Sans Serif';");

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(300);
            imageView.setPreserveRatio(true);


            LocalDateTime currentTime = LocalDateTime.now();
            String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
            Text timeText = new Text(formattedTime);
            timeText.setStyle("-fx-font-size: 11");
            timeText.setFill(Color.GRAY);

            messageBox.getChildren().addAll(senderText, imageView, timeText);

            VBox.setMargin(timeText, new Insets(0, 0, 0, 10));

            messageBox.setBackground(new Background(new BackgroundFill(Color.web("#CBD2FF"), new CornerRadii(10), null)));
            messageBox.setPadding(new Insets(10, 10, 10, 10));

            hBox.getChildren().addAll(messageBox);
            vBox.getChildren().add(hBox);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Image convertStringToImage(String received) {

        String [] imgMessage = splitImage(received);

        imgSender = imgMessage[0];
        String img = imgMessage[1];
        byte[] imageBytes = Base64.getDecoder().decode(img);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);

        return new Image(bis);
    }


    private String[] splitImage(String received) {

        // Split the text into two parts based on the hyphen "-"
        String[] parts = received.split("-");

        return parts;
    }


    void sendMessage(String message) {
        try{
            if (!message.isEmpty()){
                if(message != null){

                    vBox.setSpacing(10);

                    HBox hBox = new HBox();
                    hBox.setAlignment(Pos.CENTER_RIGHT);
                    hBox.setPadding(new Insets(5, 10, 5, 10));

                    Text text = new Text(message);
                    text.setStyle("-fx-font-size: 16; -fx-font-family: 'Sans Serif';");

                    LocalDateTime currentTime = LocalDateTime.now();
                    String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a")); // 'a' for AM/PM indicator
                    Text timeText = new Text("  "+formattedTime);
                    timeText.setStyle("-fx-font-size: 11");
                    timeText.setFill(Color.GRAY);

                    TextFlow messageTextFlow = new TextFlow(text);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    messageTextFlow.setBackground(new Background(new BackgroundFill(Color.web("#D9D9D9"), new CornerRadii(10), null)));
                    messageTextFlow.setPadding(new Insets(10, 10, 10, 10));

                    messageTextFlow.getChildren().addAll(spacer, timeText);
                    text.setFill(Color.BLACK);

                    HBox innerHBox = new HBox(messageTextFlow);
                    innerHBox.setAlignment(Pos.BOTTOM_RIGHT);

                    hBox.getChildren().addAll(innerHBox);
                    vBox.getChildren().add(hBox);

                }
            }
            txtMessage.clear();
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    void setReceivedText(String message){
        if (message != null){

            String [] msgDetail = splitReceivedMsg(message);

            String sender = msgDetail[0];
            String receivedMessage = msgDetail[1];

            String textWithUnicodeEmoji = EmojiParser.parseToUnicode(receivedMessage);

            vBox.setSpacing(10);

            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setPadding(new Insets(5, 10, 5, 10));

            VBox messageBox = new VBox();
            messageBox.setAlignment(Pos.TOP_LEFT);

            Text senderText = new Text(sender);
            senderText.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-font-family: 'Sans Serif';");

            Text messageText = new Text(textWithUnicodeEmoji);
            messageText.setStyle("-fx-font-size: 16; -fx-font-family: 'Sans Serif';");

            LocalDateTime currentTime = LocalDateTime.now();
            String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
            Text timeText = new Text("  " + formattedTime);
            timeText.setStyle("-fx-font-size: 11");
            timeText.setFill(Color.GRAY);

            TextFlow senderFlow = new TextFlow(senderText);
            TextFlow messageFlow = new TextFlow(messageText, timeText);

            messageBox.getChildren().addAll(senderFlow, messageFlow);

            messageBox.setBackground(new Background(new BackgroundFill(Color.web("#CBD2FF"), new CornerRadii(10), null)));
            messageBox.setPadding(new Insets(10, 10, 10, 10));

            hBox.getChildren().addAll(messageBox);
            vBox.getChildren().add(hBox);

        }
    }

    private String[] splitReceivedMsg(String message) {

        System.out.println(EmojiManager.getForAlias("smile").getUnicode());

        String[] parts = message.split("-");

        return parts;
    }

    public void txtMessageOnAction(ActionEvent actionEvent) {

        String msg = txtMessage.getText();
        if(!msg.isEmpty()) {
            sendMessage(txtMessage.getText());
        }
    }


    @FXML
    void btnAttachOnAction(MouseEvent mouseEvent) {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Send");

        try {
            fileToSend = fileChooser.showOpenDialog(null);

            if (fileToSend != null) {
                String fileName = fileToSend.getName();

                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ||
                        fileName.endsWith(".png") || fileName.endsWith(".gif") ||
                        fileName.endsWith(".bmp")) {

                    sendImage(fileToSend);
                }
            }else{
                System.out.println("select file first!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void sendImage(File fileToSend) {

        try {

            Image image = new Image(fileToSend.getPath());



            String imageAsString = convertImageToString(image);
            System.out.println("Image :"+imageAsString);

            dataOutputStream.writeUTF(imageAsString);
            dataOutputStream.flush();
            setSentImage(image);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setSentImage(Image image) {

        vBox.setSpacing(10);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(5, 10, 5, 10));

        VBox messageBox = new VBox();
        messageBox.setAlignment(Pos.TOP_RIGHT);
        messageBox.setSpacing(5); // Adjust spacing as needed


// Create an ImageView with the loaded image
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(300); // Adjust the width as needed
        imageView.setPreserveRatio(true); // Maintain aspect ratio

// Text for displaying time
        LocalDateTime currentTime = LocalDateTime.now();
        String formattedTime = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
        Text timeText = new Text(formattedTime);
        timeText.setStyle("-fx-font-size: 11");
        timeText.setFill(Color.GRAY);

// Add sender text and image to the messageBox
        messageBox.getChildren().addAll(imageView, timeText);

// Set the alignment of timeText to center left
        VBox.setMargin(timeText, new Insets(0, 0, 0, 10));

        messageBox.setBackground(new Background(new BackgroundFill(Color.web("#CBD2FF"), new CornerRadii(10), null)));
        messageBox.setPadding(new Insets(10, 10, 10, 10));

        hBox.getChildren().addAll(messageBox);
        vBox.getChildren().add(hBox);
    }

    private String convertImageToString(Image image) {

        try {

            double maxWidth = 600;
            double maxHeight = 400;
            double width = image.getWidth();
            double height = image.getHeight();

            double scaleFactor = (width > maxWidth || height > maxHeight) ? Math.min(maxWidth / width, maxHeight / height) : 1.0;
            width *= scaleFactor;
            height *= scaleFactor;

            BufferedImage resizedImage = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(SwingFXUtils.fromFXImage(image, null), 0, 0, (int) width, (int) height, null);
            g.dispose();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, "jpg", outputStream);


            byte[] imageBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    @FXML
    private void btnCloseOnAction(ActionEvent actionEvent) throws IOException {

        System.exit(0);

    }

    @FXML
    private void btnMinimizeOnAction(ActionEvent actionEvent) {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        stage.setIconified(true);
    }


    @FXML
    private void btnBackOnAction(ActionEvent actionEvent) {
        System.out.println("Back");
    }

    public void playMouseEnterAnimation(MouseEvent mouseEvent) {
        if (mouseEvent.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) mouseEvent.getSource();


            ScaleTransition scaleT = new ScaleTransition(Duration.millis(200), icon);
            scaleT.setToX(1.2);
            scaleT.setToY(1.2);
            scaleT.play();

            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#454FD6")); // Set color to #454FD6
            glow.setWidth(20);
            glow.setHeight(20);
            glow.setRadius(20);
            icon.setEffect(glow);
        }
    }

    public void playMouseExitAnimation(MouseEvent mouseEvent) {
        if (mouseEvent.getSource() instanceof ImageView) {
            ImageView icon = (ImageView) mouseEvent.getSource();

            ScaleTransition scaleT = new ScaleTransition(Duration.millis(200), icon);
            scaleT.setToX(1);
            scaleT.setToY(1);
            scaleT.play();

            icon.setEffect(null);
        }
    }

    @FXML
    private void btnTakePhotoOnAction(ActionEvent actionEvent) {
       //write codes to open the camera

    }

//    @FXML
//    private void btnEmojiOnAction(MouseEvent mouseEvent) {
//
//
//
//
//    }

    @FXML
    private void btnEmojiOnAction(MouseEvent mouseEvent) {


        AnchorPane emojiPane = createEmojiPane();

        Scene scene = new Scene(emojiPane, 400, 200);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Emoji Selector");
        stage.show();
    }

    private AnchorPane createEmojiPane() {
        AnchorPane emojiPane = new AnchorPane();
        emojiPane.setPadding(new Insets(10));

        Button emoji1 = createEmojiButton("😀", 10, 10 );
        Button emoji2 = createEmojiButton("😂", 70, 10 );
        Button emoji3 = createEmojiButton("😍", 130, 10);

        emojiPane.getChildren().addAll(emoji1, emoji2, emoji3);
        return emojiPane;
    }

    private Button createEmojiButton(String emoji, double layoutX, double layoutY) {
        Button button = new Button(emoji);
        button.setOnAction(event -> {
            txtMessage.appendText(emoji);
            ((Stage) button.getScene().getWindow()).close(); // Close the emoji pane after selecting emoji
        });
        button.setLayoutX(layoutX);
        button.setLayoutY(layoutY);
        return button;
    }



}
