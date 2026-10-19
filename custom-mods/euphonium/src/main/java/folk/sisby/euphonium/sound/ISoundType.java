package folk.sisby.euphonium.sound;

public interface ISoundType<T extends ISoundInstance> {
   void addSounds(SoundHandler<T> var1);
}
