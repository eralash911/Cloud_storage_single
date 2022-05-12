package client;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import model.AbstractMessage;
import model.FileMessage;
import model.FileRequest;
import model.FilesList;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.ResourceBundle;
import java.util.stream.Collectors;



public class Controller implements Initializable {

    private Path baseDir;
    public ListView <String>clientFiles;
    public ListView <String>serverFiles;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;


    private void read(){
        try {
            while (true){
                AbstractMessage msg = (AbstractMessage) is.readObject();
                switch (msg.getMessageType()){
                    case FILE:
                        FileMessage fileMsg = (FileMessage) msg;
                        Files.write(baseDir.resolve(fileMsg.getFileName()), fileMsg.getBytes());

                        Platform.runLater(()->fillClientView(getFilesNames()));

                        break;
                    case FILES_LIST:
                        FilesList files = (FilesList) msg;
                        Platform.runLater(() -> fillServerView(files.getFiles()));
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public void fillClientView(List<String> list){
        clientFiles.getItems().clear();
        clientFiles.getItems().addAll(list);

    }

    public void fillServerView(List<String> list){
        serverFiles.getItems().clear();
        serverFiles.getItems().addAll(list);
    }

    private List<FileInfo> getClientFiles() throws IOException {
       return Files.list(baseDir)
                .map(FileInfo::new)
                .collect(Collectors.toList());
    }

    private List<String>getFilesNames() {
        try {
            return Files.list(baseDir)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        }catch (Exception e){
            return new ArrayList<>();
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            baseDir = Paths.get(System.getProperty("user.home"));
//            clientFiles.setCellFactory(list -> new ListCell<FileInfo>(){
//                @Override
//                protected void updateItem(FileInfo item, boolean empty) {
//
//                    if (item != null && !empty) {
//                        String text = item.getFileName();
//                        if (item.isDirectory()) {
//                            text += " DIR";
//                        } else {
//                            text += item.getSize() + " bytes";
//                        }
//                        setText(text);
//                    }else {
//                        setText("");
//                    }
//                }
//            });
            clientFiles.getItems().addAll(getFilesNames());
            clientFiles.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2){
                    String file = clientFiles.getSelectionModel().getSelectedItem();
                    Path path = baseDir.resolve(file);
                    if (Files.isDirectory(path)){
                        baseDir = path;
                        fillClientView(getFilesNames());
                    }
                }
            });
            Socket socket = new Socket("localhost", 8780);
        os = new ObjectEncoderOutputStream(socket.getOutputStream());
        is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String file = serverFiles.getSelectionModel().getSelectedItems().toString();
        os.writeObject(new FileRequest(file));
    }

//    public void upload(ActionEvent actionEvent) throws IOException {
//       String file = clientFiles.getSelectionModel().getSelectedItems().toString();
//        Path filePath = baseDir.resolve(file);
//        os.writeObject(new FileMessage(filePath));
//
//    }
    public void upload(ActionEvent actionEvent) throws IOException {
        String file = clientFiles.getSelectionModel().getSelectedItem();
        Path filePath = baseDir.resolve(file);
//        System.out.println(filePath.toString());
        os.writeObject(new FileMessage(filePath));
    }
}
