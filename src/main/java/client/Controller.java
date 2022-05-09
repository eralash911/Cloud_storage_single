package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    private Path baseDir;
    public ListView <String>clientFiles;
    public ListView <String>serverFiles;
    private DataInputStream is;
    private DataOutputStream os;


    private void read(){
        try {
            while (true){
                String command = is.readUTF();
                if (command.equals("#List#")){
                    int filesCount = is.readInt();
                    Platform.runLater(()->serverFiles.getItems().clear());
                    for (int i = 0; i < filesCount; i++) {
                        String name = is.readUTF();
                        Platform.runLater(()->
                            serverFiles.getItems().add(name));

                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<FileInfo> getClientFiles() throws IOException {
       return Files.list(baseDir)
                .map(FileInfo::new)
                .collect(Collectors.toList());
    }

    private List<String>getFilesNames() throws IOException {
        return Files.list(baseDir)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
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
            Socket socket = new Socket("localhost", 9090);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void download(ActionEvent actionEvent) {
    }

    public void upload(ActionEvent actionEvent) throws IOException {
       String file = clientFiles.getSelectionModel().getSelectedItems().toString();
        Path filePath = baseDir.resolve(file);
        os.writeUTF("#upload#");
        os.writeUTF(file);
        os.writeLong(Files.size(filePath));
        os.write(Files.readAllBytes(filePath));
        os.flush();
    }
}
