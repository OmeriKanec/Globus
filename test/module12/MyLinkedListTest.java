package module12;

import java.util.Objects;

public final class MyLinkedListTest {
    public static void main(String[] args) {
        emptyList();
        singleElement();
        removeHeadMiddleAndTail();
        nullAndDuplicateValues();
        largeList();
        System.out.println("5 MyLinkedList tests passed");
    }

    private static void emptyList() {
        MyLinkedList<String> list = new MyLinkedList<>();
        checkEquals(0, list.size(), "empty size");
        expectInvalidIndex(() -> list.get(0), "get from empty list");
        expectInvalidIndex(() -> list.remove(0), "remove from empty list");
    }

    private static void singleElement() {
        MyLinkedList<String> list = new MyLinkedList<>();
        list.add("first");

        checkEquals(1, list.size(), "single element size");
        checkEquals("first", list.get(0), "get single element");
        expectInvalidIndex(() -> list.get(-1), "get at negative index");
        expectInvalidIndex(() -> list.get(1), "get at size");
        expectInvalidIndex(() -> list.remove(-1), "remove at negative index");
        expectInvalidIndex(() -> list.remove(1), "remove at size");

        checkEquals("first", list.remove(0), "remove single element");
        checkEquals(0, list.size(), "size after removal");
        list.add("second");
        checkEquals("second", list.get(0), "append after emptying list");
    }

    private static void removeHeadMiddleAndTail() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        for (int value = 1; value <= 5; value++) {
            list.add(value);
        }

        checkEquals(1, list.remove(0), "remove head");
        checkEquals(3, list.remove(1), "remove middle");
        checkEquals(5, list.remove(2), "remove tail");
        list.add(6);

        checkEquals(3, list.size(), "size after removals and append");
        checkEquals(2, list.get(0), "remaining head");
        checkEquals(4, list.get(1), "remaining middle");
        checkEquals(6, list.get(2), "new tail");
    }

    private static void nullAndDuplicateValues() {
        MyLinkedList<String> list = new MyLinkedList<>();
        list.add("repeat");
        list.add(null);
        list.add("repeat");

        checkEquals(3, list.size(), "null and duplicates count");
        checkEquals(null, list.get(1), "get null");
        checkEquals(null, list.remove(1), "remove null");
        checkEquals("repeat", list.get(0), "first occurrence");
        checkEquals("repeat", list.get(1), "second occurrence");
    }

    private static void largeList() {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        int count = 10_000;
        for (int value = 0; value < count; value++) {
            list.add(value);
        }

        checkEquals(count, list.size(), "large list size");
        checkEquals(count - 1, list.get(count - 1), "get last element by index");
        for (int value = 0; value < count; value++) {
            checkEquals(value, list.remove(0), "insertion order at " + value);
        }
        checkEquals(0, list.size(), "size after removing all elements");
    }

    private static void expectInvalidIndex(Runnable operation, String description) {
        try {
            operation.run();
        } catch (IndexOutOfBoundsException expected) {
            return;
        }
        throw new AssertionError(description + ": expected IndexOutOfBoundsException");
    }

    private static void checkEquals(Object expected, Object actual, String description) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }
}
