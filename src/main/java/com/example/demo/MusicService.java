package com.example.demo;

import javazoom.jl.player.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class MusicService {

    private Player mp3Player;
    private Clip wavClip;

    private String currentPlayingFile;
    private boolean playing = false;

    @Value("${music.folder.path}")
    private String musicFolderPath;

    public synchronized PlayResult play(String filename) {
        try {
            if (playing) {
                return new PlayResult(false, "이미 음악이 재생 중입니다: " + currentPlayingFile);
            }

            if (!filename.endsWith(".mp3") && !filename.endsWith(".wav")) {
                filename += ".mp3";
            }

            File file = new File(musicFolderPath + "/" + filename);
            if (!file.exists()) {
                return new PlayResult(false, "파일이 존재하지 않습니다: " + filename);
            }

            // mp3 재생
            if (filename.endsWith(".mp3")) {
                FileInputStream fis = new FileInputStream(file);
                this.mp3Player = new Player(fis);
                playing = true;
                currentPlayingFile = filename;
                new Thread(() -> {
                    try {
                        mp3Player.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        stop(); // 끝나면 정리
                    }
                }).start();
            }

            // wav 재생
            else if (filename.endsWith(".wav")) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                wavClip = AudioSystem.getClip();
                wavClip.open(audioStream);
                playing = true;
                currentPlayingFile = filename;
                wavClip.start();
                wavClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        stop(); // 끝나면 정리
                    }
                });
            }

            return new PlayResult(true, "재생 시작: " + filename);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            return new PlayResult(false, "오디오 파일 처리 오류: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new PlayResult(false, "알 수 없는 오류 발생: " + e.getMessage());
        }
    }

    public synchronized boolean stop() {
        boolean stopped = false;

        if (mp3Player != null) {
            mp3Player.close();
            mp3Player = null;
            stopped = true;
        }

        if (wavClip != null && wavClip.isRunning()) {
            wavClip.stop();
            wavClip.close();
            wavClip = null;
            stopped = true;
        }

        if (stopped) {
            currentPlayingFile = null;
            playing = false;
        }

        return stopped;
    }

    public boolean isPlaying() {
        return playing;
    }

    public String getCurrentPlayingFile() {
        return currentPlayingFile;
    }
}
