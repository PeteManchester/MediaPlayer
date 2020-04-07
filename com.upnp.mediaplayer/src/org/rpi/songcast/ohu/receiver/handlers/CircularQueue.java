package org.rpi.songcast.ohu.receiver.handlers;

import java.util.LinkedList;

public class CircularQueue<E> extends LinkedList<E> {
	private int capacity = 10;

	public CircularQueue(int capacity) {
		this.capacity = capacity;
	}

	@Override
	public boolean add(E e) {
		if (size() >= capacity)
			removeFirst();
		return super.add(e);
	}

	@Override
	public String toString() {
		String s = super.toString();
		s = s.replace(",", System.lineSeparator());
		return s;
	}
}