package parkinson;

public class CollaborativeStrategy implements Strategy {
   @Override
   public boolean isItUseful(HasUtility utility) {
      return utility.getUtility() > 3;
   }

   @Override
   public boolean isItStillUseful(HasUtility utility) {
      return utility.getUtility() > 2;
   }
}