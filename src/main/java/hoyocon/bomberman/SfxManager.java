package hoyocon.bomberman;

import javafx.scene.media.AudioClip;

public class SfxManager {
    private static boolean sfxEnabled = true;
    private static AudioClip walkClip;

    // Đường dẫn cố định tới file SFX (đường dẫn tương đối trong resources)
    private static final String EXPLOSION_SOUND = "/assets/sounds/explosion.wav";
    private static final String PLACE_BOMB_SOUND = "/assets/sounds/place_bomb.wav";
    private static final String WALK_SOUND    = "/assets/sounds/jump.wav";
    private static final String BUFF_SOUND    = "/assets/sounds/powerup.wav";

    public static void initWalk() {
        walkClip = new AudioClip(SfxManager.class.getResource(WALK_SOUND).toString());
        walkClip.setCycleCount(AudioClip.INDEFINITE);
    }

    public static void playWalk() {
        if (sfxEnabled && walkClip != null && !walkClip.isPlaying()) {
            walkClip.play();
        }
    }

    public static void stopWalk() {
        if (walkClip != null && walkClip.isPlaying()) {
            walkClip.stop();
        }
    }

    public static void playBuff() {
        playSound(BUFF_SOUND);
    }

    public static void playExplosion() {
        playSound(EXPLOSION_SOUND);
    }

    public static void playPlaceBomb() {
        playSound(PLACE_BOMB_SOUND);
    }

    private static void playSound(String soundFile) {
        System.out.println("SfxManager: playSound called with " + soundFile + ", sfxEnabled=" + sfxEnabled);
        if (sfxEnabled) {
            AudioClip clip = new AudioClip(SfxManager.class.getResource(soundFile).toString());
            clip.play();
        }
    }

    public static void setSfxEnabled(boolean enabled) {
        System.out.println("SfxManager: setSfxEnabled = " + enabled);
        sfxEnabled = enabled;
    }

    public static boolean isSfxEnabled() {
        return sfxEnabled;
    }
}
