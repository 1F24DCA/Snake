
package kr.pe.firstfloor.network.data;

import java.lang.reflect.*;
import java.util.regex.*;
import java.util.*;

import kr.pe.firstfloor.util.StackTrace;


// TODO: untested module! need to test, and stack tracing...
@SuppressWarnings("unchecked")
public class Converter {
	private static final Class DEFAULT_COLLECTION = ArrayList.class;
	private static final Class DEFAULT_MAP = HashMap.class;

	private static final String[] REGISTERED_CLASS = {
		// "java.lang.Object", // Already registered
		// "kr.pe.firstfloor.network.data.Command", // Already registered
		"java.util.List",
		"java.util.ArrayList",
		"java.util.LinkedList",
		"java.util.Set",
		"java.util.HashSet",
		"java.util.TreeSet",
		"java.util.Map",
		"java.util.HashMap",
		"kr.pe.firstfloor.network.data.User",
		"kr.pe.firstfloor.snake.data.Room",
		"kr.pe.firstfloor.snake.data.Game"
	};

	private class RegExp {
		// T is true, and F is false.
		static final String BOOLEAN = "([TF])";
		// 'a', 'b', 'C', '0', 'F'... (need to add escape sequence, for example: '\n', '\u0010'...)
		static final String CHARACTER = "'(.)'";
		// 1, 2, 100, 1000000L, 212221221L, 0.3, 111.444444, 55.6781F, 0.87654F, 10F...
		static final String NUMBER = "(-?\\d+(?:\\.\\d+)?)";
		// "abCdE", "Frank's HOUSE", "I'm...\n\"IRON MAN\""...
		static final String CHAR_SEQUENCE = "\"((?:\"|[^\"]+?)*?)\"";
		// {0}, {1}, {2}, ..., {10}, {11}, ..., {n} (reserved for put instances, DO NOT USE IT)
		static final String REPLACEABLE = "\\{(\\d+)}";

		// captures primitive types and replaceable.
		static final String DEFAULT = "(?:T|F|'.'|-?\\d+(?:\\.\\d+)?[FL]?|\"(?:\"|[^\"]+?)*?\"|\\{\\d+}|(?:#\\d+)?\\([^()\\[\\]]*?\\))";

		static final String CLASS_NAME = "([\\w.]*(?:#\\d+)?)";

		// `command,parameter,parameter,...`...
		static final String COMMAND = "`([^`]+?)`";
		// Collection<E>[DEFAULT type element,DEFAULT type element,...], if Collection is empty, it's java.util.ArrayList. and if E is empty, it's java.lang.Object.
		static final String COLLECTION = CLASS_NAME+"<([\\w.]+)?>\\[([^()\\[\\]]*?)]";
		// Map<K,V>[DEFAULT type element:DEFAULT type element,...], if Map is empty, it's java.util.HashMap. and if K or V is empty, it's java.lang.Object.
		static final String MAP = CLASS_NAME+"<([\\w.]+)?,([\\w.]+)?>\\[([^()\\[\\]]*?)]";
		// Class<? extends Data>(field1=DEFAULT type value,field2=DEFAULT type value,...), if you create class implements Data interface, you MUST declare two public method: getter(getXxxXxx()) and setter(setXxxXxx())! (isXxxXxx()... is allowed for boolean type field)
		static final String CLASS = CLASS_NAME+"\\(([^()\\[\\]]*?)\\)";

		// captures instances (like Data class, Collection, Map...)
		static final String INSTANCES_NO_IDENTIFIER = "(?:#\\d+)(?:<[\\w.]*,?[\\w.]*>)?[\\[(][^()\\[\\]]*?[)\\]]";
		static final String INSTANCES = "(?:[\\w.]*(?:#\\d+)?<[\\w.]*,?[\\w.]*>[\\[(][^()\\[\\]]*?[)\\]]|`[^`]+?`)";
	}



	public static String toString(Command commandToConvert) {
		StringBuilder convertedString = new StringBuilder();

		try {
			ArrayList<String> listCommand = (ArrayList<String>) getFieldValue(commandToConvert, "command");
			for (String command : listCommand) {
				convertedString.append(toString(command, String.class));
			}
		} catch (Exception exception) {
			StackTrace.print(Converter.class, exception);
		}

		return "`"+commandToConvert+"`";
	}

	public static String toString(Data dataToConvert) { return toString(dataToConvert, dataToConvert.getClass(), true); }
	public static String toString(Data dataToConvert, Class dataClass) { return toString(dataToConvert, dataClass, true); }
	private static String toString(Data dataToConvert, boolean showType) { return toString(dataToConvert, dataToConvert.getClass(), showType); }
	private static String toString(Data dataToConvert, Class dataClass, boolean showType) {
		StringBuilder convertedString = new StringBuilder();

		ArrayList<Field> fields = new ArrayList<>();
		Class classes = dataClass;
		while (classes.getSuperclass() != null) {
			fields.addAll(Arrays.asList(classes.getDeclaredFields()));
			classes = classes.getSuperclass();
		}

		int index = 0;
		for (Field field : fields) {
			if (Modifier.isFinal(field.getModifiers())) continue;
			
			try {
				String fieldValue = toString(getFieldValue(dataToConvert, field.getName()), field.getGenericType());
				if (fieldValue != null) {
					if (!convertedString.toString().equals("")) convertedString.append(",");
					convertedString.append("#"+(index++));
					convertedString.append("=");
					convertedString.append(fieldValue);
				}
			} catch (Exception exception) {
				StackTrace.print(Converter.class, exception);
			}
		}

		return (showType ? toString(dataClass) : "")+"#"+Data.getId(dataToConvert)+"("+convertedString.toString()+")";
	}

	public static String toString(Collection collectionToConvert, Type E) { return toString(collectionToConvert, E, DEFAULT_COLLECTION.isAssignableFrom(collectionToConvert.getClass())); }
	private static String toString(Collection collectionToConvert, Type E, boolean showType) {
		StringBuilder convertedString = new StringBuilder();
		String elementType = "";

		if (E != null)
			elementType = E.toString().replaceAll("class "+RegExp.CLASS_NAME, "$1");

		//int index = 0;
		for (Object element : collectionToConvert.toArray()) {
			if (!convertedString.toString().equals("")) convertedString.append(",");

			if (elementType.equals(element.getClass().getName()))
				convertedString.append(toString(element, E, false));
			else
				convertedString.append(toString(element, E));
		}

		return (showType ? toString(collectionToConvert.getClass()) : "")+"<"+elementType+">"+"["+convertedString.toString()+"]";
	}

	public static String toString(Map mapToConvert, Type K, Type V) { return toString(mapToConvert, K, V, DEFAULT_MAP.isAssignableFrom(mapToConvert.getClass())); }
	private static String toString(Map mapToConvert, Type K, Type V, boolean showType) {
		StringBuilder convertedString = new StringBuilder();
		String keyType = "";
		String valueType = "";

		if (K != null)
			keyType = K.toString().replaceAll("class "+RegExp.CLASS_NAME, "$1");

		if (V != null)
			valueType = V.toString().replaceAll("class "+RegExp.CLASS_NAME, "$1");

		for (Object entry : mapToConvert.entrySet()) {
			Object key = ((Map.Entry) entry).getKey();
			Object value = ((Map.Entry) entry).getValue();

			if (!convertedString.toString().equals("")) convertedString.append(",");

			if (keyType.equals(key.getClass().getName()))
				convertedString.append(toString(key, K, false));
			else
				convertedString.append(toString(key, K));

			convertedString.append(":");

			if (valueType.equals(value.getClass().getName()))
				convertedString.append(toString(value, V, false));
			else
				convertedString.append(toString(value, V));
		}

		return (showType ? toString(mapToConvert.getClass()) : "")+"<"+keyType+","+valueType+">"+"["+convertedString+"]";
	}

	private static String toString(Object objectToConvert, Type type) { return toString(objectToConvert, type, true); }
	private static String toString(Object objectToConvert, Type type, boolean showType) {
		if (objectToConvert != null) {
			if (objectToConvert instanceof Data) {
				return toString((Data)objectToConvert, showType);
			} else if (objectToConvert instanceof Boolean) {
				return objectToConvert.equals(true)?"T":"F";
			} else if (objectToConvert instanceof Character) {
				return "'"+objectToConvert+"'";
			} else if (objectToConvert instanceof Number) {
				return objectToConvert.toString();
			} else if (objectToConvert instanceof CharSequence) {
				return "\""+((String)objectToConvert).replace("\"","\\\"")+"\"";
			} else if (objectToConvert instanceof Collection) {
				Type E = null;

				if (type instanceof ParameterizedType) {
					try {
						Type[] genericTypes = ((ParameterizedType)type).getActualTypeArguments();

						if (genericTypes.length == 1) {
							E = genericTypes[0];
						}
					} catch (Exception exception) {
						StackTrace.print(Converter.class, exception);
					}
				}

				return toString((Collection)objectToConvert, E);
			} else if (objectToConvert instanceof Map) {
				Type K = null;
				Type V = null;

				if (type instanceof ParameterizedType) {
					try {
						Type[] genericTypes = ((ParameterizedType)type).getActualTypeArguments();

						if (genericTypes.length == 2) {
							K = genericTypes[0];
							V = genericTypes[1];
						}
					} catch (Exception exception) {
						StackTrace.print(Converter.class, exception);
					}
				}

				return toString((Map)objectToConvert, K, V);
			}
		}

		return null;
	}
	


	public static Object toObject(String convertedString) { return toObject(null, convertedString); }
	public static Object toObject(Object objectToSet, String convertedString) {
		// convertedString.replace("\n", "");
		// convertedString.replaceAll("("+RegExp.CHAR_SEQUENCE+")|\\s", "$1");

		ArrayList<String> replaceable = new ArrayList();

		while (true) {
			Matcher matcher = Pattern.compile(RegExp.INSTANCES).matcher(convertedString);
            if (!matcher.find()) break;

			replaceable.add(matcher.group());
			convertedString = matcher.replaceFirst("{"+(replaceable.size()-1)+"}");
		}

		return toObject(objectToSet, convertedString, replaceable);
	}
	private static Object toObject(Object objectToSet, String convertedString, ArrayList<String> replaceable) {
		String root = replaceable.get(Integer.parseInt(convertedString.replaceAll(RegExp.REPLACEABLE, "$1")));

		Matcher matcher = Pattern.compile(RegExp.COMMAND).matcher(root);
		if (matcher.find() && objectToSet instanceof Data)
			return toObject(Command.class, matcher.group(1));

		matcher = Pattern.compile(RegExp.COLLECTION).matcher(root);
		if (matcher.find() && objectToSet instanceof Collection)
			if (objectToSet != null)
				return toObject((Collection)objectToSet, matcher.group(2), matcher.group(3), replaceable);
			else
				return toObject(matcher.group(1), matcher.group(2), matcher.group(3), replaceable);

		matcher = Pattern.compile(RegExp.MAP).matcher(root);
		if (matcher.find() && objectToSet instanceof Map)
			if (objectToSet != null)
				return toObject((Map)objectToSet, matcher.group(2), matcher.group(3), matcher.group(4), replaceable);
			else
				return toObject(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), replaceable);

		matcher = Pattern.compile(RegExp.CLASS).matcher(root);
		if (matcher.find() && objectToSet instanceof Data)
			if (objectToSet != null)
				return toObject((Data)objectToSet, matcher.group(2), replaceable);
			else
				return toObject(matcher.group(1), matcher.group(2), replaceable);

		return null;
	}

	public static Command toObject(Class<Command> isCommand, String convertedString) {
		if (isCommand == null) return null;

		Command command = new Command();
		ArrayList<String> listCommand = new ArrayList<>();

		Matcher matcher = Pattern.compile("("+RegExp.DEFAULT+")(?=(?:,"+RegExp.DEFAULT+")*$)").matcher(convertedString);
		while (matcher.find())
			listCommand.add(matcher.group(1));

		command.setCommand(listCommand);
		return command;
	}

	private static Data toObject(Data dataToSet, String convertedString, ArrayList<String> replaceable) {
		ArrayList<Field> fields = new ArrayList<>();
		Class classes = dataToSet.getClass();
		while (classes.getSuperclass() != null) {
			fields.addAll(Arrays.asList(classes.getDeclaredFields()));
			classes = classes.getSuperclass();
		}

		int index = 0;
		for (Field field : fields) {
			if (Modifier.isFinal(field.getModifiers())) continue;

			Class fieldClass = field.getType();
			String fieldIndex = "#"+(index++);
			String fieldName = field.getName();
			Object fieldValue = null;

			Matcher matcher = Pattern.compile(fieldIndex+"=("+RegExp.DEFAULT+")(?=,\\w+=.+|$)").matcher(convertedString);
			if (matcher.find()) {
				String found = matcher.group(1);
				if (found.matches("^"+RegExp.REPLACEABLE+"$"))
                    found = replaceable.get(Integer.parseInt(found.replaceAll(RegExp.REPLACEABLE, "$1")));

                if (found.matches("^" + RegExp.INSTANCES_NO_IDENTIFIER + "$"))
                    found = fieldClass.getName() + found;

				if (isDefaultType(fieldClass))
					fieldValue = toObject(null, fieldClass, found, replaceable);
				else
					fieldValue = toObject(getFieldValue(dataToSet, fieldName), fieldClass, found, replaceable);
			}

			if (fieldValue != null)
				setFieldValue(dataToSet, field.getName(), fieldValue);
		}

		return dataToSet;
	}
	private static Data toObject(String className, String convertedString, ArrayList<String> replaceable) {
	    Matcher matcher = Pattern.compile("([\\w.]*)?(?:#(\\d+))?").matcher(className);
	    if (matcher.find()) {
            try {
                className = matcher.group(1);
                int id = Integer.parseInt(matcher.group(2));

                Class dataClass = toClass(className);
                if (matcher.group(2) != null && Data.findById(id).getClass().equals(dataClass)) {
                    return toObject(Data.findById(id), convertedString, replaceable);
                } else {
                    if (dataClass != null && Data.class.isAssignableFrom(dataClass))
                        return toObject(Data.newInstance(dataClass, id), convertedString, replaceable);
                }
            } catch (Exception exception) {
                StackTrace.print(Converter.class, exception);
            }
        }

		return null;
	}

	private static Collection toObject(Collection collectionToSet, String E, String convertedString, ArrayList<String> replaceable) {
		if (E == null) E = "java.lang.Object";

		if (collectionToSet != null)
			collectionToSet.clear();

		Matcher matcher = Pattern.compile("("+RegExp.DEFAULT+")(?=(?:,"+RegExp.DEFAULT+")*$)").matcher(convertedString);
		while (matcher.find()) {
			String element = matcher.group(1);
            if (element.matches("^"+RegExp.REPLACEABLE+"$"))
                element = replaceable.get(Integer.parseInt(element.replaceAll(RegExp.REPLACEABLE, "$1")));

			if (element.matches("^" + RegExp.INSTANCES_NO_IDENTIFIER + "$"))
				element = E + element;

			collectionToSet.add(toObject(null, toClass(E), element, replaceable));
		}
		
		return collectionToSet;
	}
	private static Collection toObject(String collectionName, String E, String convertedString, ArrayList<String> replaceable) {
		Class collection = (collectionName != null) ? toClass(collectionName) : DEFAULT_COLLECTION;
		try {
			if (collection != null && Collection.class.isAssignableFrom(collection))
				return toObject((Collection) collection.newInstance(), E, convertedString, replaceable);
		} catch (Exception exception) {
			StackTrace.print(Converter.class, exception);
		}

		return null;
	}
	
	private static Map toObject(Map mapToSet, String K, String V, String convertedString, ArrayList<String> replaceable) {
		if (K == null) K = "java.lang.Object";
		if (V == null) V = "java.lang.Object";

		Matcher matcher = Pattern.compile("("+RegExp.DEFAULT+"):("+RegExp.DEFAULT+")(?=(?:,"+RegExp.DEFAULT+":"+RegExp.DEFAULT+")*$)").matcher(convertedString);
		while (matcher.find()) {
			String key = matcher.group(1);
            if (key.matches("^"+RegExp.REPLACEABLE+"$"))
                key = replaceable.get(Integer.parseInt(key.replaceAll(RegExp.REPLACEABLE, "$1")));
			if (key.matches("^" + RegExp.INSTANCES_NO_IDENTIFIER + "$"))
				key = K + key;

			String value = matcher.group(2);
            if (value.matches("^"+RegExp.REPLACEABLE+"$"))
                value = replaceable.get(Integer.parseInt(value.replaceAll(RegExp.REPLACEABLE, "$1")));
			if (value.matches("^" + RegExp.INSTANCES_NO_IDENTIFIER + "$"))
				value = V + value;

			mapToSet.put(toObject(null, toClass(K), key, replaceable), toObject(null, toClass(V), value, replaceable));
		}

		return mapToSet;
	}
	private static Map toObject(String mapName, String K, String V, String convertedString, ArrayList<String> replaceable) {
		Class map = (mapName != null) ? toClass(mapName) : DEFAULT_MAP;
		try {
			if (map != null && Map.class.isAssignableFrom(map))
				return toObject((Map) map.newInstance(), K, V, convertedString, replaceable);
		} catch (Exception exception) {
			StackTrace.print(Converter.class, exception);
		}

		return null;
	}

	private static Object toObject(Object objectToSet, Class objectClass, String found, ArrayList<String> replaceable) {
		if (objectToSet == null) {
			Matcher matcher = Pattern.compile(RegExp.BOOLEAN).matcher(found);
			if (matcher.find())
				objectToSet = matcher.group(1);

			matcher = Pattern.compile(RegExp.CHARACTER).matcher(found);
			if (matcher.find())
				objectToSet = matcher.group(1);

			matcher = Pattern.compile(RegExp.NUMBER).matcher(found);
			if (matcher.find())
				objectToSet = matcher.group(1);

			matcher = Pattern.compile(RegExp.CHAR_SEQUENCE).matcher(found);
			if (matcher.find())
				objectToSet = matcher.group(1);
		}

		if (objectToSet != null) {
			if (found.matches(RegExp.INSTANCES)) {
				objectToSet = toObject(objectToSet, found, replaceable);
			} else {
				if (boolean.class.isAssignableFrom(objectClass) || Boolean.class.isAssignableFrom(objectClass)) {
					objectToSet = objectToSet.equals("T");
				} else if (char.class.isAssignableFrom(objectClass) || Character.class.isAssignableFrom(objectClass)) {
					objectToSet = ((String) objectToSet).charAt(0);
				} else if (byte.class.isAssignableFrom(objectClass) || Byte.class.isAssignableFrom(objectClass)) {
					objectToSet = Byte.parseByte((String) objectToSet);
				} else if (short.class.isAssignableFrom(objectClass) || Short.class.isAssignableFrom(objectClass)) {
					objectToSet = Short.parseShort((String) objectToSet);
				} else if (int.class.isAssignableFrom(objectClass) || Integer.class.isAssignableFrom(objectClass)) {
					objectToSet = Integer.parseInt((String) objectToSet);
				} else if (long.class.isAssignableFrom(objectClass) || Long.class.isAssignableFrom(objectClass)) {
					objectToSet = Long.parseLong((String) objectToSet);
				} else if (float.class.isAssignableFrom(objectClass) || Float.class.isAssignableFrom(objectClass)) {
					objectToSet = Float.parseFloat((String) objectToSet);
				} else if (double.class.isAssignableFrom(objectClass) || Double.class.isAssignableFrom(objectClass)) {
					objectToSet = Double.parseDouble((String) objectToSet);
				} else if (CharSequence.class.isAssignableFrom(objectClass)) {
					objectToSet = objectClass.cast((objectToSet.toString()).replace("\\\"", "\""));
				}
			}
		}

		return objectToSet;
	}



	private static String toString(Class classToConvert) {
		for (String className : REGISTERED_CLASS)
			if (className.equals(classToConvert.getName()))
				return classToConvert.getName().replaceAll("[\\w.]+\\.(\\w+)", "$1");

		return classToConvert.getName();
	}
	private static Class toClass(String classString) {
		for (String className : REGISTERED_CLASS)
			if (className.contains(classString))
				classString = className;

		try {
			return Class.forName(classString);
		} catch (Exception exception) {
			StackTrace.print(Converter.class, exception);

			return null;
		}
	}

	private static boolean isDefaultType(Class classToCheck) {
		return classToCheck.isPrimitive()
				|| Boolean.class.isAssignableFrom(classToCheck)
				|| Character.class.isAssignableFrom(classToCheck)
				|| Number.class.isAssignableFrom(classToCheck)
				|| CharSequence.class.isAssignableFrom(classToCheck);
	}


	
	private static Object getFieldValue(Object dataObject, String fieldName) {
		String getter = "(?:is|get)"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1);

		ArrayList<Method> methods = new ArrayList<>();
		Class classes = dataObject.getClass();
		while (classes.getSuperclass() != null) {
			methods.addAll(Arrays.asList(classes.getDeclaredMethods()));
			classes = classes.getSuperclass();
		}

		try {
			for (Method method : methods)
				if (Modifier.isPublic(method.getModifiers()) && method.getName().matches(getter)) // isXxxXxx or getXxxXxx
					return method.invoke(dataObject);
		} catch (Exception exception) {
			StackTrace.print(Converter.class, exception);
		}
		
		return null;
	}
	
	private static void setFieldValue(Object dataObject, String fieldName, Object fieldValue) {
		String setter = "set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1);

		ArrayList<Method> methods = new ArrayList<>();

		Class classes = dataObject.getClass();
		while (classes.getSuperclass() != null) {
			methods.addAll(Arrays.asList(classes.getDeclaredMethods()));
			classes = classes.getSuperclass();
		}

		try {
			for (Method method : methods)
				if (Modifier.isPublic(method.getModifiers()) && method.getName().equals(setter))
					method.invoke(dataObject, fieldValue);
		} catch (Exception exception) {
			StackTrace.print(Converter.class, exception);
		}
	}
}