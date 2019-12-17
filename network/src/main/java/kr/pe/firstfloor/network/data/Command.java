
package kr.pe.firstfloor.network.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public final class Command {
	private ArrayList<String> command = new ArrayList<>();

	public Command() {}
	public Command(int command) {
		this(command, false);
	}
	public Command(int command, boolean isSignal) {
		if (isSignal) {
			this.command.add(0, "SIG");
			this.command.add(1, Integer.toString(command));
		} else {
			this.command.add(0, Integer.toString(command));
		}
	}

	public void put(String parameter) { command.add(parameter); }
	public void put(int parameter) { command.add(Integer.toString(parameter)); }
	public void put(long parameter) { command.add(Long.toString(parameter)); }
	public void put(float parameter) { command.add(Float.toString(parameter)); }
	public void put(double parameter) { command.add(Double.toString(parameter)); }
	public void put(Data parameter) { command.add(Converter.toString(parameter)); }
	public void put(Collection parameter, Class E) { command.add(Converter.toString(parameter, E)); }
	public void put(Map parameter, Class K, Class V) { command.add(Converter.toString(parameter, K, V)); }

	public int size() { return command.size(); }

	public boolean isEmpty() { return this.command.get(0) != null; }
	public boolean is(int command) { return this.command.get(0).equals(Integer.toString(command)); }
	public boolean isSignal() { return command.get(0).equals("SIG"); }
	public int get() { if (isSignal()) return Integer.parseInt(this.command.get(1)); else return Integer.parseInt(this.command.get(0)); }
	public String getString(int index) { if (index > 0) return command.get(index); else return null; }
	public int getInt(int index) { if (index > 0) return Integer.parseInt(command.get(index)); else return 0; }
	public long getLong(int index) { if (index > 0) return Long.parseLong(command.get(index)); else return 0L; }
	public float getFloat(int index) { if (index > 0) return Float.parseFloat(command.get(index)); else return 0F; }
	public double getDouble(int index) { if (index > 0) return Double.parseDouble(command.get(index)); else return 0.0; }
	public Object getObject(int index) { if (index > 0) return Converter.toObject(command.get(index)); else return null; }
	public void setObject(Object object, int index) { if (index > 0) Converter.toObject(object, command.get(index)); }

	public String getString() { return getString(true); }
	public String getString(boolean searchUntilEnd) { return getString(1, searchUntilEnd); }
	public String getString(int parameterStart, int parameterEnd) {
		if (parameterStart > 0) {
			if (parameterEnd < 0) parameterEnd = size() + parameterEnd;

			String result = "";
			for (int idx = parameterStart; idx <= parameterEnd; idx++) {
				result = result + getString(idx) + " ";
			}

			return result.trim();
		} else {
			return "";
		}
	}
	public String getString(int parameterStart, boolean searchUntilEnd) {
		if (parameterStart > 0) {
			if (!searchUntilEnd) return getString(parameterStart);

			String result = "";
			for (int idx = parameterStart; getString(idx) != null; idx++) {
				result = result + getString(idx) + " ";
			}

			return result.trim();
		} else {
			return "";
		}
	}

	public ArrayList<String> getCommand() { return command; }
	public void setCommand(ArrayList<String> command) { this.command = command; }

	@Override
	public String toString() { return Converter.toString(this); }
	public static Command parse(String commandToParse) {
		Object object = Converter.toObject(commandToParse);

		if (object instanceof Command)
			return (Command) object;
		else
			return null;
	}
}