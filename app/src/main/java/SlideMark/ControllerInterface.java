package SlideMark;

public interface ControllerInterface {
    /**
     * @param sender A reference to the object sending the request
     * @param message The request's intent
     * @return A ReturnObject encapsulating the appropriate data type for
     * return to the sender, if needed.
     */
    public ReturnObject<?> request(ControllerInterface sender, String message);
}
