package com.webcrawler.utils.graph;


public class Queue {
    private final int SIZE = 30;
    private int front;
    private int rear;
    private int[] queueArray;

    public Queue() {
        queueArray = new int[SIZE];
        front = 0;
        rear = -1;
    }

    public void insert(int value) {
        if (rear == SIZE - 1) {
            rear = -1;
        }
        queueArray[++rear] = value;
    }

    public int remove() {
        int temp = queueArray[front];
        if (front == SIZE - 1) {
            front = 0;
        }
        return temp;
    }

    public int peek() {
        return queueArray[front];
    }

    public boolean isFull() {
        return queueArray.length == SIZE;
    }

    public boolean isEmpty() {
        return (rear + 1 == front || front + SIZE - 1 == rear);
    }
}
