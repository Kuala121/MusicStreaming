package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    @Autowired
    private MusicService musicService;

    @Value("${music.folder.path}")
    private String musicFolderPath;

    @GetMapping
    public List<String> listMusicFiles() {
        File folder = new File(musicFolderPath);
        File[] files = folder.listFiles((dir, name) ->
                name.endsWith(".mp3") || name.endsWith(".wav"));

        if (files == null) {
            return List.of();
        }

        return Arrays.stream(files)
                .map(File::getName)
                .collect(Collectors.toList());
    }

    @PostMapping("/play")
    public String playMusic(@RequestParam("filename") String filename) {
        boolean success = musicService.play(filename);
        return success ? "재생 시작" : "파일을 찾을 수 없습니다.";
    }

    @PostMapping("/stop")
    public String stopMusic() {
        boolean success = musicService.stop();
        return success ? "재생 중지" : "재생 중인 음악이 없습니다.";
    }
}
