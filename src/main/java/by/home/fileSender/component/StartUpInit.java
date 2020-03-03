package by.home.fileSender.component;


import by.home.fileSender.service.FileService;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class StartUpInit {


    private final FileService fileService;

    public StartUpInit(FileService fileService) {
        this.fileService = fileService;
    }

    @PostConstruct
    public void init() {
        fileService.observeFolder();
    }
}
