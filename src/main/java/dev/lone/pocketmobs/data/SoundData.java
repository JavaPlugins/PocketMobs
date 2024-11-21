package dev.lone.pocketmobs.data;

import dev.lone.pocketmobs.utils.Sound;

public class SoundData
{
    public Sound sound;
    public int volume;
    public int pitch;

    public SoundData(String name, int volume, int pitch)
    {
        this.sound = Sound.valueOf(name);
        this.volume = volume;
        this.pitch = pitch;
    }

    public Sound getSound()
    {
        return sound;
    }

    public void setSound(Sound sound)
    {
        this.sound = sound;
    }

    public int getVolume()
    {
        return volume;
    }

    public void setVolume(int volume)
    {
        this.volume = volume;
    }

    public int getPitch()
    {
        return pitch;
    }

    public void setPitch(int pitch)
    {
        this.pitch = pitch;
    }
}
