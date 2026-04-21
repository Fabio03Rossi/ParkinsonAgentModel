package parkinson;

public interface Strategy {
   boolean isItUseful(HasUtility utility);

   boolean isItStillUseful(HasUtility utility);
}