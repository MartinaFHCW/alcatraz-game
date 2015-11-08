package alcatraz;


/**
 *
 * @author  Lorenz Froihofer
 * @version $Id$
 */
public class IServerException extends java.lang.Exception {
  
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/**
   * Creates a new instance of <code>CalculatorException</code> without detail message.
   */
  public IServerException() {
  }
  
  
  /**
   * Constructs an instance of <code>CalculatorException</code> with the specified detail message.
   * @param msg the detail message.
   */
  public IServerException(String msg) {
    super(msg);
  }
  
  /**
   * Creates a new instance of <code>CalculatorException</code>
   * with the specified detail message and cause.
   * @param msg the detail message (which is saved for later retrieval by the
   *            <code>getMessage()</code> method).
   * @param cause the cause (which is saved for later retrieval by the
   *              <code>getCause()</code> method).
   *              (A <code>null</code> value is permitted, and indicates that
   *              the cause is nonexistent or unknown.)
   */
  public IServerException(String msg, Throwable cause) {
    super(msg,cause);
  }
  
  /**
   * Creates a new instance of <code>CalculatorException</code>
   * with the specified cause.
   * @param cause the cause (which is saved for later retrieval by the
   *              <code>getCause()</code> method).
   *              (A <code>null</code> value is permitted, and indicates that
   *              the cause is nonexistent or unknown.)
   */
  public IServerException(Throwable cause) {
    super(cause);
  }
}