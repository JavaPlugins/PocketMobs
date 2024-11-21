package dev.lone.pocketmobs.data;

public class BallEffect
{
    public final ParticleData particleData;
    public final SoundData soundData;

    public BallEffect(ParticleData particleData, SoundData soundData)
    {
        this.particleData = particleData;
        this.soundData = soundData;
    }

    public static class BallEffectBuilder
    {
        private ParticleData particleData;
        private SoundData soundData;

        public BallEffectBuilder setParticleConfig(ParticleData particleData)
        {
            this.particleData = particleData;
            return this;
        }

        public BallEffectBuilder setSoundConfig(SoundData soundData)
        {
            this.soundData = soundData;
            return this;
        }

        public BallEffect build()
        {
            return new BallEffect(particleData, soundData);
        }
    }

}
