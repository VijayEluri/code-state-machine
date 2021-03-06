import ceylon.collection {
	...
}

"Simple base class for chaining instances of some type."
shared class Chain<T>(T* values)
		satisfies Iterable<T>
		given T satisfies Object {
	MutableList<T> items = LinkedList<T>(values);
	
	shared void add(T* additionalItem) {
		items.addAll(additionalItem);
	}
	
	shared void addChain(Chain<T> additionalItems) {
		items.addAll(additionalItems.items);
	}
	
	shared void remove(T item) {
		items.remove(item);
	}
	
	shared actual Iterator<T> iterator() => items.iterator();
	
	shared actual String string {
		if (items.size == 0) {
			return "always";
		}
		if (items.size == 1) {
			value item = items.get(0);
			assert (exists item);
			return item.string;
		}
		value buffer = StringBuilder().append("[");
		for (item in items) {
			buffer.append(item.string);
			buffer.append(", ");
		}
		buffer.deleteTerminal(2);
		return buffer.append("]").string;
	}
}

