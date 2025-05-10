package com.example.demo;

import javazoom.jl.player.Player;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;

@Service
public class MusicService {

    private Player player;
    
    @Value("${music.folder.path}")
    private String musicFolderPath;
    
    public boolean play(String filename) {
        try {
            File file = new File(musicFolderPath + filename + ".mp3");
            if (!file.exists()) {
                System.out.println("파일 없음: " + file.getAbsolutePath());
                return false;
            }

            FileInputStream fis = new FileInputStream(file);
            Player player = new Player(fis);
            new Thread(() -> {
                try {
                    player.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    
    public boolean stop() {
        if (player != null) {
            player.close();  // JLayer에서 음악 정지
            player = null;
            return true;
        }
        return false;
    }
}
