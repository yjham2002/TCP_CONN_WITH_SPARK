package databases.exception;

public class NothingToTakeException extends Exception {

    public NothingToTakeException(String message){
        super(message);
    }

    public NothingToTakeException(){
        this("There is no tuple to take a value from.");
    }

}
