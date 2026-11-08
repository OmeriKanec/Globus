package module12;

public final class MyLinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    //Сложность O(1), так как мы просто подставляем новый элемент в конец
    public void add(T value) {
        Node<T> node = new Node<>(value);
        if (tail == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
        size++;
    }

    //Сложность в худшем случае O(n), так как мы идём по массиву, пока не дойдём до индекса
    public T get(int index) {
        checkIndex(index);
        return nodeAt(index).value;
    }

    //Сложность в худшем случае O(n), так как мы идём по массиву, пока не дойдём до индекса, само удаление занимает максимум 3 операции
    public T remove(int index) {
        checkIndex(index);

        Node<T> previous = null;
        Node<T> removed = head;
        if (index == 0) {
            head = removed.next;
        } else {
            previous = nodeAt(index - 1);
            removed = previous.next;
            previous.next = removed.next;
        }

        if (removed == tail) {
            tail = previous;
        }
        size--;
        return removed.value;
    }

    //Сложность O(1), так как это просто возвращение значения переменной
    public int size() {
        return size;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + ", size " + size);
        }
    }

    private Node<T> nodeAt(int index) {
        Node<T> node = head;
        for (int i = 0; i < index; i++) {
            node = node.next;
        }
        return node;
    }

    private static final class Node<T> {
        private final T value;
        private Node<T> next;

        private Node(T value) {
            this.value = value;
        }
    }
}
