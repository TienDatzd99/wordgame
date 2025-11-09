package com.dat.wordgame.client.ui;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.net.URL;

public class SoundPlayer {
    private static AudioClip successSound;
    private static AudioClip wrongSound;
    private static MediaPlayer bgMusicPlayer;
    private static boolean enabled = true;
    private static boolean musicEnabled = true;

    static {
        try {
            // Load âm thanh từ resources
            ClassLoader classLoader = SoundPlayer.class.getClassLoader();
            
            // Thử load file success.wav hoặc success.mp3
            URL successUrl = classLoader.getResource("sounds/success.wav");
            if (successUrl == null) {
                successUrl = classLoader.getResource("sounds/success.mp3");
            }
            if (successUrl == null) {
                successUrl = classLoader.getResource("sounds/correct.wav");
            }
            
            // Thử load file wrong.wav hoặc wrong.mp3
            URL wrongUrl = classLoader.getResource("sounds/wrong.wav");
            if (wrongUrl == null) {
                wrongUrl = classLoader.getResource("sounds/wrong.mp3");
            }
            if (wrongUrl == null) {
                wrongUrl = classLoader.getResource("sounds/error.wav");
            }
            
            if (successUrl != null) {
                successSound = new AudioClip(successUrl.toString());
                System.out.println("[SoundPlayer] Success sound loaded from: " + successUrl);
            } else {
                System.out.println("[SoundPlayer] Success sound not found, creating beep sound");
                successSound = createBeepSound(800, 0.3);
            }
            
            if (wrongUrl != null) {
                wrongSound = new AudioClip(wrongUrl.toString());
                System.out.println("[SoundPlayer] Wrong sound loaded from: " + wrongUrl);
            } else {
                System.out.println("[SoundPlayer] Wrong sound not found, creating beep sound");
                wrongSound = createBeepSound(200, 0.5);
            }
            
            // Load background music
            URL bgMusicUrl = classLoader.getResource("sounds/lofi-chill-background-music-313055.mp3");
            if (bgMusicUrl != null) {
                Media bgMedia = new Media(bgMusicUrl.toString());
                bgMusicPlayer = new MediaPlayer(bgMedia);
                bgMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop vô hạn
                bgMusicPlayer.setVolume(0.15); // Âm lượng nhỏ (15%)
                System.out.println("[SoundPlayer] Background music loaded from: " + bgMusicUrl);
            } else {
                System.out.println("[SoundPlayer] Background music not found");
            }
            
            System.out.println("[SoundPlayer] Sounds initialized successfully");
        } catch (Exception e) {
            System.err.println("[SoundPlayer] Failed to initialize sounds: " + e.getMessage());
            e.printStackTrace();
            enabled = false;
        }
    }

    /**
     * Tạo âm thanh beep đơn giản với tần số và độ dài cho trước
     */
    private static AudioClip createBeepSound(double frequency, double duration) {
        // Tạo dữ liệu âm thanh WAV đơn giản
        int sampleRate = 44100;
        int numSamples = (int) (sampleRate * duration);
        byte[] audioData = new byte[numSamples * 2]; // 16-bit mono
        
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i * frequency / sampleRate;
            short sample = (short) (Math.sin(angle) * 32767 * 0.5); // 50% volume
            
            // Little-endian 16-bit
            audioData[i * 2] = (byte) (sample & 0xFF);
            audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        
        // Tạo WAV header
        byte[] wavData = createWavData(audioData, sampleRate);
        
        // Tạo data URI
        String base64 = java.util.Base64.getEncoder().encodeToString(wavData);
        String dataUri = "data:audio/wav;base64," + base64;
        
        return new AudioClip(dataUri);
    }

    /**
     * Tạo dữ liệu WAV hoàn chỉnh với header
     */
    private static byte[] createWavData(byte[] audioData, int sampleRate) {
        byte[] header = new byte[44];
        int dataSize = audioData.length;
        int fileSize = 36 + dataSize;
        
        // RIFF header
        header[0] = 'R'; header[1] = 'I'; header[2] = 'F'; header[3] = 'F';
        writeInt(header, 4, fileSize);
        header[8] = 'W'; header[9] = 'A'; header[10] = 'V'; header[11] = 'E';
        
        // fmt chunk
        header[12] = 'f'; header[13] = 'm'; header[14] = 't'; header[15] = ' ';
        writeInt(header, 16, 16); // fmt chunk size
        writeShort(header, 20, (short) 1); // PCM format
        writeShort(header, 22, (short) 1); // Mono
        writeInt(header, 24, sampleRate); // Sample rate
        writeInt(header, 28, sampleRate * 2); // Byte rate
        writeShort(header, 32, (short) 2); // Block align
        writeShort(header, 34, (short) 16); // Bits per sample
        
        // data chunk
        header[36] = 'd'; header[37] = 'a'; header[38] = 't'; header[39] = 'a';
        writeInt(header, 40, dataSize);
        
        // Combine header and audio data
        byte[] result = new byte[header.length + audioData.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(audioData, 0, result, header.length, audioData.length);
        
        return result;
    }

    private static void writeInt(byte[] data, int offset, int value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
        data[offset + 2] = (byte) ((value >> 16) & 0xFF);
        data[offset + 3] = (byte) ((value >> 24) & 0xFF);
    }

    private static void writeShort(byte[] data, int offset, short value) {
        data[offset] = (byte) (value & 0xFF);
        data[offset + 1] = (byte) ((value >> 8) & 0xFF);
    }

    public static void playSuccess() {
        if (enabled && successSound != null) {
            try {
                successSound.play();
            } catch (Exception e) {
                System.err.println("[SoundPlayer] Error playing success sound: " + e.getMessage());
            }
        }
    }

    public static void playWrong() {
        if (enabled && wrongSound != null) {
            try {
                wrongSound.play();
            } catch (Exception e) {
                System.err.println("[SoundPlayer] Error playing wrong sound: " + e.getMessage());
            }
        }
    }

    public static void setEnabled(boolean enable) {
        enabled = enable;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Bắt đầu phát nhạc nền
     */
    public static void playBackgroundMusic() {
        if (musicEnabled && bgMusicPlayer != null) {
            try {
                bgMusicPlayer.play();
                System.out.println("[SoundPlayer] Background music started");
            } catch (Exception e) {
                System.err.println("[SoundPlayer] Error playing background music: " + e.getMessage());
            }
        }
    }

    /**
     * Dừng nhạc nền
     */
    public static void stopBackgroundMusic() {
        if (bgMusicPlayer != null) {
            try {
                bgMusicPlayer.stop();
                System.out.println("[SoundPlayer] Background music stopped");
            } catch (Exception e) {
                System.err.println("[SoundPlayer] Error stopping background music: " + e.getMessage());
            }
        }
    }

    /**
     * Tạm dừng nhạc nền
     */
    public static void pauseBackgroundMusic() {
        if (bgMusicPlayer != null) {
            try {
                bgMusicPlayer.pause();
                System.out.println("[SoundPlayer] Background music paused");
            } catch (Exception e) {
                System.err.println("[SoundPlayer] Error pausing background music: " + e.getMessage());
            }
        }
    }

    /**
     * Bật/tắt nhạc nền
     */
    public static void setMusicEnabled(boolean enable) {
        musicEnabled = enable;
        if (!enable && bgMusicPlayer != null) {
            bgMusicPlayer.stop();
        }
    }

    public static boolean isMusicEnabled() {
        return musicEnabled;
    }
}
