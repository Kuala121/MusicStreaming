package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
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
    public ResponseEntity<String> playMusic(@RequestParam("filename") String filename) {
        PlayResult result = musicService.play(filename);

        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getMessage());
        }

        String message = result.getMessage();
        if (message.contains("이미 음악이 재생 중")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message); // 409
        } else if (message.contains("파일이 존재하지 않습니다")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message); // 404
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message); // 500
        }
    }

    @PostMapping("/stop")
    public ResponseEntity<String> stopMusic() {
        boolean success = musicService.stop();
        return success ?
                ResponseEntity.ok("재생 중지") :
                ResponseEntity.status(HttpStatus.CONFLICT).body("재생 중인 음악이 없습니다.");
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        if (musicService.isPlaying()) {
            return ResponseEntity.ok("재생 중: " + musicService.getCurrentPlayingFile());
        } else {
            return ResponseEntity.ok("재생 중인 음악이 없습니다.");
        }
    }
    
    @GetMapping("/queue")
    public List<String> getQueue() {
        return new LinkedList<>(musicService.getPlayQueue());
    }
    
    @PostMapping("/queue/remove")
    public String removeFromQueue(@RequestParam("filename") String filename) {
        boolean removed = musicService.removeFromQueue(filename);
        return removed ? "큐에서 제거됨: " + filename : "큐에 해당 파일이 없습니다.";
    }
}