package folk.sisby.antique_atlas.gui.core;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ScreenState<T> {
   protected final BiConsumer<ScreenState.State<T>, ScreenState.State<T>> onChangedState;
   protected volatile ScreenState.State<T> currentState;

   public ScreenState(BiConsumer<ScreenState.State<T>, ScreenState.State<T>> onChangedState) {
      this.onChangedState = onChangedState;
   }

   public ScreenState() {
      this.onChangedState = null;
   }

   public ScreenState.State<T> current() {
      return this.currentState;
   }

   public boolean is(ScreenState.State<T> state) {
      return this.current() == state;
   }

   public void switchTo(ScreenState.State<T> state, T screen) {
      if (this.currentState != null) {
         this.currentState.onExitState(screen);
      }

      if (this.onChangedState != null) {
         this.onChangedState.accept(this.currentState, state);
      }

      this.currentState = state;
      if (state != null) {
         state.onEnterState(screen);
      }
   }

   public interface State<T> {
      void onEnterState(T var1);

      void onExitState(T var1);
   }

   public record ToggleState<T>(Function<T, ToggleButtonComponent> toggleButton, Consumer<T> onEnterState, Consumer<T> onExitState)
      implements ScreenState.State<T> {
      public ToggleState(Function<T, ToggleButtonComponent> toggleButton) {
         this(toggleButton, null, null);
      }

      public ToggleState() {
         this(null, null, null);
      }

      @Override
      public void onEnterState(T screen) {
         if (this.toggleButton != null) {
            this.toggleButton.apply(screen).setSelected(true);
         }

         if (this.onEnterState != null) {
            this.onEnterState.accept(screen);
         }
      }

      @Override
      public void onExitState(T screen) {
         if (this.toggleButton != null) {
            this.toggleButton.apply(screen).setSelected(false);
         }

         if (this.onExitState != null) {
            this.onExitState.accept(screen);
         }
      }
   }
}
