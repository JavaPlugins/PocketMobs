package dev.lone.pocketmobs.data;

import fr.mrmicky.fastparticles.ParticleType;

public class ParticleData
{
    ParticleType type;
    int amount;

    public ParticleData(String particleType, int amount) throws IllegalArgumentException
    {
        this.type = ParticleType.of(particleType);
        this.amount = amount;
    }

    public ParticleType getType()
    {
        return type;
    }

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(int amount)
    {
        this.amount = amount;
    }
}
