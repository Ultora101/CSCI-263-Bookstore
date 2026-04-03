package com.bookstore.model;

public class Main {
    public static void main(String[] args) {
        User john = new User();
        john.setUsername("john");
        john.setRole(User.Role.CUSTOMER);
        john.setStatus(User.Status.BLOCKED);
        System.out.println(john);

        User jimmy = new User();
        jimmy.setUsername("jimmy");
        jimmy.setRole(User.Role.ADMIN);
        jimmy.setStatus(User.Status.ACTIVE);
        System.out.println(jimmy);
    }
}
