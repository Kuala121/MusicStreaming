package com.example.demo;

import javazoom.jl.player.Player;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

@Service
public class MusicService {

    private Player mp3Player;
    private Clip wavClip;

    private String currentPlayingFile;
    private boolean playing = false;

    @Value("${music.folder.path}")
    private String musicFolderPath;

    private final Queue<String> playQueue = new LinkedList<>();

    @Value("${music.queue.max-size:20}")
    private int MAX_QUEUE_SIZE;

    public synchronized PlayResult play(String filename) {
        if (playing) {
        	if (!isValidMusicFile(filename)) {
                return new PlayResult(false, "파일이 존재하지 않아 큐에 추가할 수 없습니다.");
            }
            if (playQueue.size() >= MAX_QUEUE_SIZE) {
                return new PlayResult(false, "재생 대기열이 가득 찼습니다.");
            }
            filename = normalizeFilename(filename);
            if (playQueue.contains(filename)) {
                return new PlayResult(false, "이미 큐에 존재합니다: " + filename);
            }

            playQueue.add(filename);
            return new PlayResult(true, "현재 재생 중입니다. 큐에 추가됨: " + filename);
        }

        return startPlaying(filename);
    }

    private PlayResult startPlaying(String filename) {
        try {
        	if (!isValidMusicFile(filename)) {
                return new PlayResult(false, "파일이 존재하지 않습니다: " + filename);
            }
        	filename = normalizeFilename(filename);

            File file = new File(musicFolderPath + "/" + filename);
            if (!file.exists()) {
                return new PlayResult(false, "파일이 존재하지 않습니다: " + filename);
            }

            // mp3 재생
            if (filename.endsWith(".mp3")) {
                FileInputStream fis = new FileInputStream(file);
                mp3Player = new Player(fis);
                new Thread(() -> {
                    try {
                        mp3Player.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        onFinish();
                    }
                }).start();
            }

            // wav 재생
            else if (filename.endsWith(".wav")) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                wavClip = AudioSystem.getClip();
                wavClip.open(audioStream);
                wavClip.start();
                wavClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        onFinish();
                    }
                });
            }

            currentPlayingFile = filename;
            playing = true;
            return new PlayResult(true, "재생 시작: " + filename);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            return new PlayResult(false, "오디오 파일 처리 오류: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new PlayResult(false, "알 수 없는 오류 발생: " + e.getMessage());
        }
    }

    private synchronized void onFinish() {
        while (!playQueue.isEmpty()) {
            String next = playQueue.poll();
            if (isValidMusicFile(next)) {
                startPlaying(next);
                return;
            }
        }
        playing = false;
        currentPlayingFile = null;
        
        if (playQueue.isEmpty()) {
            playing = false;
            currentPlayingFile = null;
        }
    }
    
    public synchronized boolean removeFromQueue(String filename) {
        return playQueue.remove(filename);
    }

    private boolean isValidMusicFile(String filename) {
        if (!filename.endsWith(".mp3") && !filename.endsWith(".wav")) {
            filename += ".mp3";
        }
        File file = new File(musicFolderPath + "/" + filename);
        return file.exists();
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

    public Queue<String> getPlayQueue() {
        return new LinkedList<>(playQueue); // 복사본 반환 (직접 수정 방지)
    }

    public int getMaxQueueSize() {
        return MAX_QUEUE_SIZE;
    }
    
    private String normalizeFilename(String filename) {
        if (!filename.endsWith(".mp3") && !filename.endsWith(".wav")) {
            return filename + ".mp3"; // 기본 확장자
        }
        return filename;
    }
}
