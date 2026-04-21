package parkinson;

public class CompetitiveStrategy implements Strategy {
   @Override
   public boolean isItUseful(HasUtility utility) {
      return utility.getUtility() > 0;
   }

   @Override
   public boolean isItStillUseful(HasUtility utility) {
      return utility.getUtility() > 0;
   }
}