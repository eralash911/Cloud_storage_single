package model;

import lombok.Data;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class FilesList implements AbstractMessage{
    private final List<String> files;

    public FilesList(Path path) throws IOException {
        files = Files.list(path)
                .map(path1 -> path1.getFileName().toString())
                .collect(Collectors.toList());
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILES_LIST;
    }
}
