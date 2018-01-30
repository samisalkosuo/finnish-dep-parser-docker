package fi.dep.utils;

//http://fabian-kostadinov.github.io/2014/11/25/implementing-a-fixed-length-fifo-queue-in-java/

public interface Queue<E> {

    public boolean add(E e);
    public E element();
    public boolean offer(E e);
    public E peek();
    public E poll();
    public E remove();
    
}