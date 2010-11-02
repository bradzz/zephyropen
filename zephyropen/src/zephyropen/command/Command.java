package zephyropen.command;

import java.util.Enumeration;
import java.util.Hashtable;

import zephyropen.api.API;
import zephyropen.api.ZephyrOpen;
import zephyropen.api.PrototypeFactory;
import zephyropen.socket.OutputChannel;

/**
 * Encapsulates a command sent from the server to the device.
 * <p/>
 * <b>Syntax:</b> <code>&lt;specifier&gt; [&lt;modifier&gt;, ... ]</code>
 * <p/>
 * Both the specifier and the optional modifiers are called command elements.
 * <blockquoute>
 * 
 * <pre>
 * For example:
 *    load filename
 *      - load: is the command specifier
 *      - filename: is the command modifier
 * </pre>
 * 
 * </blockquoute>
 * <p>
 * Both <code>load</code> and <code>filename</code> are considered command
 * elements.
 * <p>
 * Created: 2002.09.08
 * 
 * @author <a href="mailto:brad.zdanivsky@gmail.com">Brad Zdanivsky</a>
 * @author Peter Brandt-Erichsen
 * 
 */
public class Command {

	private static final int LENGTH = 1024;

	private static ZephyrOpen constants = ZephyrOpen.getReference();

	//private static OutputChannel outputChannel = null;

	/** holds the outer most tags, not part of the command */
	private String type = null;

	/** the command holds each element in the command */
	private Hashtable<String, String> command = null;


	/**
	 * Construct a Command of tag, value pairs nested in a 'type' tag 
	 * 
	 * @param type is the <'type'><xxx>...</xxx></'type'> on the outer most tags 
	 */
	public Command(String str) {

		if (str == null)
			return;

		if (str.equals(""))
			return;

		type = str;
		command = new Hashtable<String, String>();
	}

	/** Create a command with the default tag name */
	public Command() {

		type = ZephyrOpen.zephyropen;
		command = new Hashtable<String, String>();
		
		// add a time stamp on creation
		// command.add(ZephyrOpen.TIME_MS, String.valueOf(System.currentTimeMillis()));
	}

	/** @return the element, specified by the key, from the command */
	public String get(String key) {
		return command.get(key);
	}

	/**
	 * Get the type field
	 * 
	 * @return the command type field
	 */
	public String getType() {
		return type;
	}

	/** Set the type field */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Deletes the element, specified by the key, from the command.
	 */
	public void delete(String key) {
		command.remove(key);
	}

	/**
	 * Adds the specified key/element pair to the command.
	 * <p/>
	 * If the specified key is null, or an empty string, this method will fail,
	 * and do nothing.
	 * <p/>
	 * If the specified key is already contained in the command, the element
	 * will be updated
	 * 
	 * @param key
	 *            uniquely identifies the element
	 * @param element
	 *            is the value to add
	 */
	public void add(String key, String element) {

		/** sanity checks */
		if (key == null) {
			return;
		}
		if (key.equals("")) {
			return;
		}
		if (element == null) {
			return;
		}
		if (element.equals("")) {
			return;
		}

		/** put the key/element pair into the command */
		try {

			command.put(key, element);

		} catch (Exception e) {
			constants.info("null in command?", this);
			e.printStackTrace();
		}
	}

	/** @return true if the command contains no elements, else false. */
	public boolean isEmpty() {
		return command.isEmpty();
	}

	/** @return how many elements are in the command. */
	public int size() {
		return command.size();
	}

	/**
	 * Flushes the command of all elements.
	 */
	public void flush() {
		command.clear();
	}

	/**
	 * Returns the contents of this command as an xml string without any
	 * whitespace
	 * 
	 * @return the contents of this command as a formatted xml pairs.
	 */
	public String toXML() {

		/** allocate a string buffer */
		StringBuffer buffer = new StringBuffer(LENGTH);

		/** nested in 'type' tag */
		buffer.append("<" + type + ">");

		/** assemble the name/value pairs */
		for (Enumeration<String> e = command.keys(); e.hasMoreElements();) {
			String element = e.nextElement();
			buffer.append("<" + element + ">");
			buffer.append(get(element));
			buffer.append("</" + element + ">");
		}

		/** outer nested tag */
		buffer.append("</" + type + ">");

		/** check for string buffer overflow */
		if (constants.getBoolean(ZephyrOpen.frameworkDebug))
			if (buffer.length() >= LENGTH) {
				constants.error("Command.toString(): specified size [" + LENGTH
						+ "], " + "actual buffer size [" + buffer.length()
						+ "]", this);
			}

		/** return the xml fragment */
		return buffer.toString();
	}

	/**
	 * Returns the contents of this command as an xml string without any
	 * whitespace
	 * 
	 * @return the contents of this command as a formatted xml pairs.
	 */
	@Override
	public String toString() {
		return toXML();
	}

	/**
	 * Get the name value pairs directly
	 * 
	 * @return a string formated for a new hashTable
	 */
	public String list() {
		return command.toString();
	}

	/**
	 * Get a list of values in ordered list, separated by commas
	 * 
	 * @param api
	 *            that this command would be dispatched to. Used to find the XML
	 *            prototype
	 * @return the comma separated list as a string
	 */
	public String list(API api) {

		if (api == null)
			return null;

		// allocate a string buffer
		StringBuffer buffer = new StringBuffer(LENGTH);

		// add time stamp
		buffer.append(System.currentTimeMillis());

		// get this API's command prototype to test against this command
		String[] commandPrototype = PrototypeFactory.create(type);
		
		// index into the prototype
		int index = 0;
		for (; index < commandPrototype.length; index++) {

			// write the values in prototype order
			buffer.append(", ");
			buffer.append(command.get(commandPrototype[index]));

		}
		return buffer.toString();
	}

	/**
	 * Get a list of values in ordered list, separated by commas
	 * 
	 * 
	 * @return the comma separated list as a string
	 */
	public String list(String[] commandPrototype) {

		// allocate a string buffer
		StringBuffer buffer = new StringBuffer(LENGTH);

		// index into the prototype
		int index = 0;
		for (; index < commandPrototype.length; index++) {

			// write the values in prototype order
			buffer.append(command.get(commandPrototype[index]));
			buffer.append(", ");
		}
		return buffer.toString();
	}

	/**
	 * Checks the specified command for errors.
	 * <p>
	 * This method returns true if the following conditions evaluate to true:
	 * <p>
	 * <ol>
	 * <li>the specified command is null
	 * <li>the command prototypes for this registered api is not found in the
	 * given command
	 * </ol>
	 * <p>
	 * Returns false otherwise.
	 * 
	 * @param api
	 *            is the API to test this command against
	 * @return true if the specified command is not well formed
	 */
	public boolean isMalformedCommand(API api) {
		return isMalformedCommand(PrototypeFactory.create(type));
	}

	/**
	 * Checks the specified command for errors.
	 * 
	 * @return true if the specified command is malformed
	 */
	public boolean isMalformedCommand(String[] commandPrototype) {

		// TODO: empty prototype is not an error, yet.. next version maybe ?
		if (commandPrototype == null) {
			constants.error("Command.isMalformedCommand(), null prototype : " + command.toString(), this);
			return false;
		}

		/** index into the prototype */
		int index = 0;

		/** ensure each prototype element is in the command */
		for (; index < commandPrototype.length; index++) {

			if (command.get(commandPrototype[index]) == null) {

				if (constants.getBoolean(ZephyrOpen.frameworkDebug)) {

					/** give comprehensive error message */
					constants.error(command.toString(), this);
					constants.error("command is missing the <"
							+ commandPrototype[index] + "> tag", this);
				}

				/** is an error to have one or more missing tags */
				return true;
			}
		}

		/** the specified command is well-formed */
		return false;
	}

	/** Send this command to the output channel */
	public void send() {
		OutputChannel outputChannel = constants.getOutputChannel();
		if (outputChannel != null)
			outputChannel.write(this);
	}
}
