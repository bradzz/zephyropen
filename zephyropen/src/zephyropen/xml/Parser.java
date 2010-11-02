package zephyropen.xml;

import zephyropen.command.Command;

/**
 * Interface for modelling XML parsers.
 * <p>
 * Created: 2003.03.12
 * @author Brad Zdanivsky
 * @author Peter Brandt-Erichsen
 */
public interface Parser {

   /**
    * Parses the specified xml string and fills the specified command
    * with the name/value pairs of each element.
	* @param xml contains the xml-formatted string to parse
    * @return a command filled with the element values
    */
   public Command parse(String xml); 

}



