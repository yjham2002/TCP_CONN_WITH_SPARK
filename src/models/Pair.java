package models;

/**
 * Created by a on 2017-06-04.
 */
public class Pair<T> {
    private T head;
    private T tail;

    public Pair(T head, T tail){
        this.head = head;
        this.tail = tail;
    }

    public Pair(){}

    public T getHead() {
        return head;
    }

    public void setHead(T head) {
        this.head = head;
    }

    public T getTail() {
        return tail;
    }

    public void setTail(T tail) {
        this.tail = tail;
    }
}
