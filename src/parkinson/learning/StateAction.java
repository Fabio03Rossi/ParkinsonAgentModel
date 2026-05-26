package parkinson.learning;

import java.util.List;

public class StateAction {

   private State state;
   private Action agentAction;
   private List<Action> opponentActions;

   public StateAction(State state, Action agentAction, List<Action> opponentActions) {
      this.state = state;
      this.agentAction = agentAction;
      this.opponentActions = opponentActions;
   }

   public StateAction(State state, Action agentAction) {
      this(state, agentAction, null);
   }

    // Getters and setters
   public State getState() {
      return state;
   }
   public Action getAgentAction() {
      return agentAction;
   }
   public List<Action> getOpponentActions() {
      return opponentActions;
   }

   @Override
   public int hashCode() {
      int result = state != null ? state.hashCode() : 0;
      result = 31 * result + (agentAction != null ? agentAction.hashCode() : 0);
      result = 31 * result + (opponentActions != null ? opponentActions.hashCode() : 0);
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null || getClass() != obj.getClass()) return false;

      StateAction that = (StateAction) obj;

      if (state != null ? !state.equals(that.state) : that.state != null) return false;
      if (agentAction != null ? !agentAction.equals(that.agentAction) : that.agentAction != null) return false;
      return opponentActions != null ? opponentActions.equals(that.opponentActions) : that.opponentActions == null;
   }

}
